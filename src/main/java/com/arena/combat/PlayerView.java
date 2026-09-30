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
 */
public record PlayerView(String playerId, int hp, int maxHp, int mana, int shield, List<Card> handCards) {
}
