package com.arena;

import com.arena.bot.AggressiveBot;
import com.arena.bot.BotStrategy;
import com.arena.bot.DefensiveBot;
import com.arena.combat.MatchResult;
import com.arena.simulation.ArenaSimulationService;
import com.arena.simulation.MatchLogger;
import com.arena.simulation.MatchStatistics;
import com.arena.simulation.VictoryHistoryTracker;
import com.arena.simulation.VictoryRecord;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point for running Skirmish Arena CLI simulations with interactive configuration and victory history tracking.
 */
public class Main {

    /**
     * Executes the simulation runner based on command line arguments or interactive console input.
     *
     * @param args simulation flags such as --matches, --p1, --p2, --verbose, --history
     */
    public static void main(String[] args) {
        int matches = 1;
        boolean matchesExplicitlySet = false;
        String p1Type = "Aggressive";
        String p2Type = "Defensive";
        boolean verbose = false;
        boolean historyOnly = false;

        for (int i = 0; i < args.length; i++) {
            if ("--matches".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    matches = Integer.parseInt(args[++i]);
                    matchesExplicitlySet = true;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid matches count, defaulting to 1");
                }
            } else if ("--p1".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                p1Type = args[++i];
            } else if ("--p2".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                p2Type = args[++i];
            } else if ("--verbose".equalsIgnoreCase(args[i])) {
                verbose = true;
            } else if ("--history".equalsIgnoreCase(args[i])) {
                historyOnly = true;
            }
        }

        VictoryHistoryTracker historyTracker = new VictoryHistoryTracker();
        MatchLogger logger = new MatchLogger();

        if (historyOnly) {
            logger.printVictoryHistory(historyTracker.loadAllRecords());
            return;
        }

        // If matches were not passed as CLI arguments, show existing history and prompt interactively
        if (!matchesExplicitlySet) {
            List<VictoryRecord> previousRecords = historyTracker.loadAllRecords();
            if (!previousRecords.isEmpty()) {
                logger.printVictoryHistory(previousRecords);
            }
            matches = promptForMatchCount();
        }

        BotStrategy p1Strategy = resolveStrategy(p1Type);
        BotStrategy p2Strategy = resolveStrategy(p2Type);

        ArenaSimulationService simulationService = new ArenaSimulationService();

        if (matches <= 1) {
            MatchResult singleMatch = simulationService.runSingleMatch(p1Strategy, p2Strategy);
            logger.printMatchLog(singleMatch);

            // Record single match victory
            int totalDamage = singleMatch.events().stream()
                    .filter(e -> e.description().contains("deals"))
                    .mapToInt(e -> 0) // or calculate from player stats
                    .sum();
            VictoryRecord record = VictoryRecord.ofSingleMatch(
                    p1Strategy.getName(),
                    p2Strategy.getName(),
                    singleMatch.winnerId(),
                    singleMatch.totalTurns(),
                    totalDamage
            );
            historyTracker.recordVictory(record);
        } else {
            if (verbose) {
                MatchResult sampleMatch = simulationService.runSingleMatch(p1Strategy, p2Strategy);
                logger.printMatchLog(sampleMatch);
            }
            MatchStatistics stats = simulationService.runSimulation(p1Strategy, p2Strategy, matches);
            logger.printBatchStatistics(stats, p1Strategy.getName(), p2Strategy.getName());

            // Record batch simulation victory outcome
            VictoryRecord record = VictoryRecord.of(p1Strategy.getName(), p2Strategy.getName(), stats);
            historyTracker.recordVictory(record);
        }

        // Print updated victory history
        System.out.println();
        logger.printVictoryHistory(historyTracker.loadAllRecords());
    }

    private static int promptForMatchCount() {
        System.out.println("================================================================================");
        System.out.println("                     ⚔️  WELCOME TO SKIRMISH ARENA  ⚔️                         ");
        System.out.println("================================================================================");
        System.out.print("👉 How many matches would you like to simulate? [default: 1]: ");
        try {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    int parsed = Integer.parseInt(input);
                    if (parsed > 0) {
                        return parsed;
                    }
                    System.out.println("Match count must be greater than 0. Using default of 1 match.");
                }
            }
        } catch (Exception e) {
            // In non-interactive environments (e.g. piped stdin), gracefully fall back
        }
        return 1;
    }

    private static BotStrategy resolveStrategy(String type) {
        if ("Defensive".equalsIgnoreCase(type) || "DefensiveBot".equalsIgnoreCase(type)) {
            return new DefensiveBot();
        }
        return new AggressiveBot();
    }
}
