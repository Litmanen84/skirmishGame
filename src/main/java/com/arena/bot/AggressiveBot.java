package com.arena.bot;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.combat.PlayerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy prioritizing maximal direct damage output, attack buffs, and combo plays within play limits.
 */
public class AggressiveBot implements BotStrategy {

    @Override
    public List<Card> selectPlays(PlayerView self, PlayerView opponent) {
        List<Card> plays = new ArrayList<>();
        List<Card> availableHand = new ArrayList<>(self.handCards());
        int remainingMana = self.mana();
        int playsAllowed = 1;

        // Sort candidate cards: Combo / Extra play cards & Buffs first, then high damage attacks
        Comparator<Card> aggressiveOrder = Comparator.comparingInt(this::getAggressivePriority)
                .thenComparing(Comparator.comparingInt((Card c) -> c.value() + c.attackBuff()).reversed());

        while (playsAllowed > 0) {
            Card chosen = null;
            availableHand.sort(aggressiveOrder);

            for (Card card : availableHand) {
                if (card.cost() <= remainingMana) {
                    chosen = card;
                    break;
                }
            }

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

    private int getAggressivePriority(Card card) {
        // Priority 0: Extra play enablers (chains combo actions)
        if (card.extraPlays() > 0) {
            return 0;
        }
        // Priority 1: Attack Buffs & Resources
        if (card.attackBuff() > 0 && card.type() == CardType.UTILITY) {
            return 1;
        }
        if (card.type() == CardType.RESOURCE) {
            return 1;
        }
        // Priority 2: Direct Attacks
        if (card.type() == CardType.ATTACK) {
            return 2;
        }
        // Priority 3: Other defensive / utility cards
        return 3;
    }

    @Override
    public String getName() {
        return "AggressiveBot";
    }
}
