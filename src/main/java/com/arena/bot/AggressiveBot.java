package com.arena.bot;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.combat.PlayerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy prioritizing maximal direct damage output on each turn.
 */
public class AggressiveBot implements BotStrategy {

    @Override
    public List<Card> selectPlays(PlayerView self, PlayerView opponent) {
        List<Card> plays = new ArrayList<>();
        int remainingMana = self.mana();

        // Sort hand: Attacks with highest damage first, then other playable cards
        List<Card> sortedHand = self.handCards().stream()
                .sorted(Comparator.comparing((Card c) -> c.type() == CardType.ATTACK ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(Card::value).reversed()))
                .toList();

        for (Card card : sortedHand) {
            if (card.cost() <= remainingMana) {
                plays.add(card);
                remainingMana -= card.cost();
                if (card.type() == CardType.RESOURCE) {
                    remainingMana += card.value();
                }
            }
        }

        return plays;
    }

    @Override
    public String getName() {
        return "AggressiveBot";
    }
}
