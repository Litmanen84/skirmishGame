package com.arena.simulation;

import com.arena.combat.CombatEvent;
import com.arena.combat.MatchResult;

/**
 * Formats match telemetry and turn event logs for human-readable console inspection.
 */
public class MatchLogger {

    /**
     * Prints detailed chronological combat log for a single completed match.
     *
     * @param result match outcome containing event history
     */
    public void printMatchLog(MatchResult result) {
        System.out.println("================================================================================");
        System.out.println("                      ⚔️  SKIRMISH ARENA COMBAT LOG  ⚔️                         ");
        System.out.println("================================================================================");
        int currentTurn = -1;
        for (CombatEvent event : result.events()) {
            if (event.turn() != currentTurn && event.turn() > 0) {
                currentTurn = event.turn();
                System.out.println("-------------------------------- Turn " + String.format("%02d", currentTurn) + " --------------------------------");
            }
            if ("SYSTEM".equals(event.playerId())) {
                System.out.println("  [SYSTEM] " + event.description());
            } else {
                System.out.println("  [" + event.playerId() + "] " + event.description());
            }
        }
        System.out.println("================================================================================");
        if (result.isTie()) {
            System.out.println(" ⚖️  MATCH RESULT: DRAW (Tie-Break) after " + result.totalTurns() + " turns.");
        } else {
            System.out.println(" 🏆 MATCH RESULT: Winner is " + result.winnerId() + " in " + result.totalTurns() + " turns!");
        }
        System.out.println("================================================================================");
    }

    /**
     * Prints aggregated statistical summary for batch simulations.
     *
     * @param stats aggregated metrics
     * @param p1Name name of player 1 strategy
     * @param p2Name name of player 2 strategy
     */
    public void printBatchStatistics(MatchStatistics stats, String p1Name, String p2Name) {
        double p1Pct = stats.totalMatches() > 0 ? (stats.p1Wins() * 100.0 / stats.totalMatches()) : 0.0;
        double p2Pct = stats.totalMatches() > 0 ? (stats.p2Wins() * 100.0 / stats.totalMatches()) : 0.0;
        double tiePct = stats.totalMatches() > 0 ? (stats.ties() * 100.0 / stats.totalMatches()) : 0.0;

        System.out.println("================================================================================");
        System.out.println("                     📊  BATCH SIMULATION SUMMARY  📊                           ");
        System.out.println("================================================================================");
        System.out.printf(" Total Simulated Matches : %,d%n", stats.totalMatches());
        System.out.printf(" P1 [%-15s] Wins: %,d (%.1f%%)%n", p1Name, stats.p1Wins(), p1Pct);
        System.out.printf(" P2 [%-15s] Wins: %,d (%.1f%%)%n", p2Name, stats.p2Wins(), p2Pct);
        System.out.printf(" Draws / Ties            : %,d (%.1f%%)%n", stats.ties(), tiePct);
        System.out.printf(" Average Match Length    : %.2f turns%n", stats.averageTurns());
        System.out.printf(" Average Total Damage    : %.2f dmg%n", stats.averageDamageDealt());
        System.out.println("================================================================================");
    }

    /**
     * Prints formatted historical record of past match sessions and cumulative stats.
     *
     * @param history unmodifiable list of past victory records
     */
    public void printVictoryHistory(java.util.List<VictoryRecord> history) {
        System.out.println("================================================================================");
        System.out.println("                   📜  HISTORICAL RECORD OF VICTORIES  📜                       ");
        System.out.println("================================================================================");
        if (history == null || history.isEmpty()) {
            System.out.println("  No past simulation records found. Run a simulation to start tracking!");
            System.out.println("================================================================================");
            return;
        }

        int totalLifetimeMatches = 0;
        int totalP1LifetimeWins = 0;
        int totalP2LifetimeWins = 0;
        int totalLifetimeTies = 0;

        System.out.printf(" %-19s | %-12s vs %-12s | %-7s | %-15s%n", "Timestamp", "Player 1", "Player 2", "Matches", "Outcome");
        System.out.println("--------------------------------------------------------------------------------");

        for (VictoryRecord record : history) {
            totalLifetimeMatches += record.totalMatches();
            totalP1LifetimeWins += record.p1Wins();
            totalP2LifetimeWins += record.p2Wins();
            totalLifetimeTies += record.ties();

            String outcome;
            if (record.totalMatches() == 1) {
                if (record.p1Wins() > 0) {
                    outcome = record.p1Name() + " WON";
                } else if (record.p2Wins() > 0) {
                    outcome = record.p2Name() + " WON";
                } else {
                    outcome = "DRAW";
                }
            } else {
                outcome = String.format("%d-%d (T:%d)", record.p1Wins(), record.p2Wins(), record.ties());
            }

            System.out.printf(
                    " %-19s | %-12s vs %-12s | %-7d | %-15s%n",
                    record.timestamp(),
                    record.p1Name(),
                    record.p2Name(),
                    record.totalMatches(),
                    outcome
            );
        }

        System.out.println("--------------------------------------------------------------------------------");
        double p1LifetimePct = totalLifetimeMatches > 0 ? (totalP1LifetimeWins * 100.0 / totalLifetimeMatches) : 0.0;
        double p2LifetimePct = totalLifetimeMatches > 0 ? (totalP2LifetimeWins * 100.0 / totalLifetimeMatches) : 0.0;
        double tieLifetimePct = totalLifetimeMatches > 0 ? (totalLifetimeTies * 100.0 / totalLifetimeMatches) : 0.0;

        System.out.printf(" 🏆 Cumulative Matches: %,d | P1 Wins: %,d (%.1f%%) | P2 Wins: %,d (%.1f%%) | Ties: %,d (%.1f%%)%n",
                totalLifetimeMatches, totalP1LifetimeWins, p1LifetimePct, totalP2LifetimeWins, p2LifetimePct, totalLifetimeTies, tieLifetimePct);
        System.out.println("================================================================================");
    }
}
