package com.arena.bot;

import com.arena.cards.Card;
import com.arena.combat.PlayerView;
import java.util.List;

/**
 * Defines decision contract for bot strategies to select plays based on immutable state views.
 */
@FunctionalInterface
public interface BotStrategy {
    /**
     * Evaluates current game state and selects cards to play within mana constraints.
     *
     * @param self immutable view of acting player
     * @param opponent immutable view of opponent player
     * @return list of cards selected for play
     */
    List<Card> selectPlays(PlayerView self, PlayerView opponent);

    /**
     * Returns the display name of the strategy.
     *
     * @return bot strategy name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
