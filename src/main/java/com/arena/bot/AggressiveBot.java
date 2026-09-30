package com.arena.bot;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.combat.PlayerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy prioritizing maximal direct attack damage output within mana budget,
 * playing the strongest damage card when possible or the best playable card allowed by the mana pool.
 */
public class AggressiveBot implements BotStrategy {

    @Override
    public List<Card> selectPlays(PlayerView self, PlayerView opponent) {
        List<Card> plays = new ArrayList<>();
        List<Card> availableHand = new ArrayList<>(self.handCards());
        int remainingMana = self.mana();
        int playsAllowed = 1;

        while (playsAllowed > 0) {
            Card chosen = chooseBestPlayableCard(availableHand, remainingMana, self.attackBuff());
            if (chosen == null) {
                break;
            }

            availableHand.remove(chosen);
            plays.add(chosen);
            remainingMana -= chosen.cost();
            playsAllowed--;
            playsAllowed += chosen.extraPlays();

            if (chosen.type() == CardType.RESOURCE) {
                remainingMana += chosen.value();
            }
        }

        return plays;
    }

    private Card chooseBestPlayableCard(List<Card> hand, int mana, int activeAtkBuff) {
        List<Card> affordableCards = hand.stream()
                .filter(c -> c.cost() <= mana)
                .toList();

        if (affordableCards.isEmpty()) {
            return null;
        }

        // 1. Combo / Extra play enablers (chains combo actions to maximize aggregate damage in the round)
        List<Card> affordableComboCards = affordableCards.stream()
                .filter(c -> c.extraPlays() > 0)
                .sorted(Comparator.comparingInt((Card c) -> c.value() + activeAtkBuff + c.attackBuff()).reversed()
                        .thenComparing(Comparator.comparingInt(Card::cost)))
                .toList();

        if (!affordableComboCards.isEmpty()) {
            return affordableComboCards.get(0);
        }

        // 2. When possible, always choose the strongest attack card to deal maximum direct damage
        List<Card> affordableAttacks = affordableCards.stream()
                .filter(c -> c.type() == CardType.ATTACK)
                .sorted(Comparator.comparingInt((Card c) -> c.value() + activeAtkBuff + c.attackBuff()).reversed()
                        .thenComparing(Comparator.comparingInt(Card::cost)))
                .toList();

        if (!affordableAttacks.isEmpty()) {
            return affordableAttacks.get(0);
        }

        // 3. Otherwise, play the most beneficial card allowed by the mana pool
        return affordableCards.stream()
                .sorted(Comparator.comparingInt(this::getFallbackPriority)
                        .thenComparing(Comparator.comparingInt((Card c) -> c.value() + c.attackBuff() + c.defenseBuff()).reversed())
                        .thenComparing(Comparator.comparingInt(Card::cost)))
                .findFirst()
                .orElse(null);
    }

    private int getFallbackPriority(Card card) {
        // Priority 0: Extra play enablers (e.g. utility with bonus plays like Adrenaline)
        if (card.extraPlays() > 0) {
            return 0;
        }
        // Priority 1: Resource cards (provide mana to potentially enable attack plays)
        if (card.type() == CardType.RESOURCE) {
            return 1;
        }
        // Priority 2: Attack buffs (to power up future attacks)
        if (card.attackBuff() > 0 && card.type() == CardType.UTILITY) {
            return 2;
        }
        // Priority 3: Defense / shields
        if (card.type() == CardType.DEFENSE) {
            return 3;
        }
        // Priority 4: Healing and other utilities
        return 4;
    }

    @Override
    public String getName() {
        return "AggressiveBot";
    }
}
