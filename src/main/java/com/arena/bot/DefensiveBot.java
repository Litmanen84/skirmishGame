package com.arena.bot;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.combat.PlayerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy prioritizing shields and healing when health drops below critical thresholds.
 */
public class DefensiveBot implements BotStrategy {
    private static final int DEFENSIVE_HP_THRESHOLD = 15;

    @Override
    public List<Card> selectPlays(PlayerView self, PlayerView opponent) {
        List<Card> plays = new ArrayList<>();
        int remainingMana = self.mana();
        boolean lowHp = self.hp() <= DEFENSIVE_HP_THRESHOLD;

        Comparator<Card> defensivePriority = (c1, c2) -> {
            int p1 = getPriority(c1, lowHp);
            int p2 = getPriority(c2, lowHp);
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            return Integer.compare(c2.value(), c1.value());
        };

        List<Card> sortedHand = self.handCards().stream()
                .sorted(defensivePriority)
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

    private int getPriority(Card card, boolean lowHp) {
        if (lowHp) {
            // Priority 0: Defense / Utility (heal), Priority 1: Resource, Priority 2: Attack
            if (card.type() == CardType.DEFENSE || card.type() == CardType.UTILITY) {
                return 0;
            }
            if (card.type() == CardType.RESOURCE) {
                return 1;
            }
            return 2;
        } else {
            // Standard: Attack first, then Resource, then Defense/Utility
            if (card.type() == CardType.ATTACK) {
                return 0;
            }
            if (card.type() == CardType.RESOURCE) {
                return 1;
            }
            return 2;
        }
    }

    @Override
    public String getName() {
        return "DefensiveBot";
    }
}
