package com.arena.simulation;

import com.arena.combat.MatchResult;

/**
 * Encapsulates the complete result of a simulation session including the detailed first match and batch aggregate metrics.
 *
 * @param firstMatch complete event-logged result of the first simulated match
 * @param statistics aggregate statistical metrics across all matches in the batch
 */
public record SimulationResult(MatchResult firstMatch, MatchStatistics statistics) {
}
