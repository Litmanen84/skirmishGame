package com.arena.simulation;

/**
 * Aggregates statistical metrics across a batch simulation of N matches.
 *
 * @param totalMatches total match iterations executed
 * @param p1Wins matches won by player 1
 * @param p2Wins matches won by player 2
 * @param ties matches concluding without a decisive winner
 * @param averageTurns mean turns elapsed per match
 * @param averageDamageDealt mean total damage dealt per match
 */
public record MatchStatistics(
        int totalMatches,
        int p1Wins,
        int p2Wins,
        int ties,
        double averageTurns,
        double averageDamageDealt
) {
}
