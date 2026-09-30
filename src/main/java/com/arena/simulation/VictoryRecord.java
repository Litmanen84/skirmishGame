package com.arena.simulation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record capturing the outcome of a simulation session for historical tracking.
 *
 * @param timestamp formatted timestamp of the simulation execution
 * @param p1Name name of player 1 strategy
 * @param p2Name name of player 2 strategy
 * @param totalMatches number of matches simulated in the session
 * @param p1Wins number of victories achieved by player 1
 * @param p2Wins number of victories achieved by player 2
 * @param ties number of drawn matches
 * @param averageTurns average turn duration across matches
 * @param averageDamage average total damage dealt per match
 */
public record VictoryRecord(
        String timestamp,
        String p1Name,
        String p2Name,
        int totalMatches,
        int p1Wins,
        int p2Wins,
        int ties,
        double averageTurns,
        double averageDamage
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Factory method creating a record from current simulation statistics.
     */
    public static VictoryRecord of(String p1Name, String p2Name, MatchStatistics stats) {
        String ts = LocalDateTime.now().format(FORMATTER);
        return new VictoryRecord(
                ts,
                p1Name,
                p2Name,
                stats.totalMatches(),
                stats.p1Wins(),
                stats.p2Wins(),
                stats.ties(),
                stats.averageTurns(),
                stats.averageDamageDealt()
        );
    }

    /**
     * Factory method creating a record from a single match outcome.
     */
    public static VictoryRecord ofSingleMatch(String p1Name, String p2Name, String winnerId, int turns, int damage) {
        String ts = LocalDateTime.now().format(FORMATTER);
        int p1Wins = winnerId != null && winnerId.contains(p1Name) ? 1 : 0;
        int p2Wins = winnerId != null && winnerId.contains(p2Name) ? 1 : 0;
        int ties = (p1Wins == 0 && p2Wins == 0) ? 1 : 0;
        return new VictoryRecord(ts, p1Name, p2Name, 1, p1Wins, p2Wins, ties, turns, damage);
    }
}
