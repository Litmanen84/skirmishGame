package com.arena.combat;

import java.util.List;

/**
 * Holds final match outcome, winner designation, and full event telemetry.
 *
 * @param winnerId id of winning player, or null if tie
 * @param totalTurns number of turns played
 * @param isTie whether match concluded in a tie
 * @param events chronological history of combat events
 */
public record MatchResult(String winnerId, int totalTurns, boolean isTie, List<CombatEvent> events) {
}
