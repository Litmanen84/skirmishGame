package com.arena.combat;

import com.arena.cards.Card;
import java.util.List;

/**
 * Provides an immutable read-only perspective of a player's state for bot decision making.
 *
 * @param playerId player identifier
 * @param hp current remaining health points
 * @param maxHp maximum health capacity
 * @param mana currently available mana
 * @param shield active damage mitigation points
 * @param handCards immutable list of cards in hand
 * @param attackBuff active attack bonus power
 * @param defenseBuff active defense shield bonus
 * @param attackBuffDuration remaining turns of attack buff
 * @param defenseBuffDuration remaining turns of defense buff
 */
public record PlayerView(
        String playerId,
        int hp,
        int maxHp,
        int mana,
        int shield,
        List<Card> handCards,
        int attackBuff,
        int defenseBuff,
        int attackBuffDuration,
        int defenseBuffDuration
) {

    /**
     * Backward-compatible constructor for basic player views without detailed buff stats.
     */
    public PlayerView(String playerId, int hp, int maxHp, int mana, int shield, List<Card> handCards) {
        this(playerId, hp, maxHp, mana, shield, handCards, 0, 0, 0, 0);
    }
}
