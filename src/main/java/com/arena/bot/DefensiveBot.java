package com.arena.bot;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.combat.PlayerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy prioritizing shields and healing when health drops below critical thresholds within turn play limits.
 */
public class DefensiveBot implements BotStrategy {
    private static final int DEFENSIVE_HP_THRESHOLD = 15;

    @Override
    public List<Card> selectPlays(PlayerView self, PlayerView opponent) {
        List<Card> plays = new ArrayList<>();
        List<Card> availableHand = new ArrayList<>(self.handCards());
        int remainingMana = self.mana();
        boolean lowHp = self.hp() <= DEFENSIVE_HP_THRESHOLD;
        int playsAllowed = 1;

        Comparator<Card> defensivePriority = (c1, c2) -> {
            int p1 = getPriority(c1, lowHp);
            int p2 = getPriority(c2, lowHp);
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            return Integer.compare(c2.value() + c2.defenseBuff() + c2.attackBuff(), c1.value() + c1.defenseBuff() + c1.attackBuff());
        };

        while (playsAllowed > 0) {
            Card chosen = null;
            availableHand.sort(defensivePriority);

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

    private int getPriority(Card card, boolean lowHp) {
        if (lowHp) {
            // Priority 0: Extra play enablers (to chain into defenses/heals)
            if (card.extraPlays() > 0) {
                return 0;
            }
            // Priority 1: Defense / Defense Buffs / Healing Utility
            if (card.type() == CardType.DEFENSE || (card.type() == CardType.UTILITY && (card.value() > 0 || card.defenseBuff() > 0))) {
                return 1;
            }
            // Priority 2: Resources
            if (card.type() == CardType.RESOURCE) {
                return 2;
            }
            return 3;
        } else {
            // Priority 0: Extra play enablers (to chain into attacks/buffs)
            if (card.extraPlays() > 0) {
                return 0;
            }
            // Priority 1: Attack buffs & Attacks
            if (card.attackBuff() > 0 && card.type() == CardType.UTILITY) {
                return 1;
            }
            if (card.type() == CardType.ATTACK) {
                return 1;
            }
            // Priority 2: Resources
            if (card.type() == CardType.RESOURCE) {
                return 2;
            }
            return 3;
        }
    }

    @Override
    public String getName() {
        return "DefensiveBot";
    }
}
