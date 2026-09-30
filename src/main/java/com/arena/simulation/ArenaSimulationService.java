package com.arena.simulation;

import com.arena.bot.BotStrategy;
import com.arena.cards.CardLibrary;
import com.arena.cards.Deck;
import com.arena.combat.MatchEngine;
import com.arena.combat.MatchResult;
import com.arena.combat.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Coordinates execution of single or batch matches between strategy bots.
 */
public class ArenaSimulationService {
    private static final int DEFAULT_HP = 30;
    private final Random random;

    /**
     * Initializes simulation service with a default pseudo-random generator.
     */
    public ArenaSimulationService() {
        this(new Random());
    }

    /**
     * Initializes simulation service with a specific random source for deterministic testing.
     *
     * @param random random generator
     */
    public ArenaSimulationService(Random random) {
        this.random = random != null ? random : new Random();
    }

    /**
     * Executes a single simulation match between two strategies.
     *
     * @param p1Strategy player 1 strategy
     * @param p2Strategy player 2 strategy
     * @return outcome record with complete event log
     */
    public MatchResult runSingleMatch(BotStrategy p1Strategy, BotStrategy p2Strategy) {
        return runSimulation(p1Strategy, p2Strategy, 1).firstMatch();
    }

    /**
     * Executes batch simulation between two strategies, generating randomized decks and constrained starting shields.
     *
     * @param p1Strategy player 1 strategy
     * @param p2Strategy player 2 strategy
     * @param matchCount number of iterations to run
     * @return simulation result holding the first match log and aggregate statistics
     */
    public SimulationResult runSimulation(BotStrategy p1Strategy, BotStrategy p2Strategy, int matchCount) {
        int effectiveMatches = Math.max(1, matchCount);
        int p1Wins = 0;
        int p2Wins = 0;
        int ties = 0;
        long totalTurns = 0;
        long totalDamage = 0;
        MatchResult firstMatch = null;

        String p1Identifier = "Player 1 (" + p1Strategy.getName() + ")";
        String p2Identifier = "Player 2 (" + p2Strategy.getName() + ")";

        for (int i = 0; i < effectiveMatches; i++) {
            Deck deck1 = CardLibrary.createRandomizedDeck(random);
            Deck deck2 = CardLibrary.createRandomizedDeck(random);
            StartingShields shields = generateStartingShields(random);

            PlayerState p1 = new PlayerState(p1Identifier, DEFAULT_HP, shields.p1Shield(), deck1);
            PlayerState p2 = new PlayerState(p2Identifier, DEFAULT_HP, shields.p2Shield(), deck2);

            MatchEngine engine = new MatchEngine(p1, p1Strategy, p2, p2Strategy);
            MatchResult result = engine.playMatch();

            if (i == 0) {
                firstMatch = result;
            }

            totalTurns += result.totalTurns();
            totalDamage += (p1.getTotalDamageDealt() + p2.getTotalDamageDealt());

            if (result.isTie()) {
                ties++;
            } else if (p1Identifier.equals(result.winnerId())) {
                p1Wins++;
            } else if (p2Identifier.equals(result.winnerId())) {
                p2Wins++;
            }
        }

        double avgTurns = (double) totalTurns / effectiveMatches;
        double avgDamage = (double) totalDamage / effectiveMatches;

        MatchStatistics stats = new MatchStatistics(effectiveMatches, p1Wins, p2Wins, ties, avgTurns, avgDamage);
        return new SimulationResult(firstMatch, stats);
    }

    /**
     * Generates different random starting shields for two players such that neither shield is more than 3 units away from the other.
     *
     * @param rng random number generator
     * @return pair of valid starting shields
     */
    public static StartingShields generateStartingShields(Random rng) {
        // Base shield range: 1 to 8
        int s1 = 1 + rng.nextInt(8);
        int minS2 = Math.max(1, s1 - 3);
        int maxS2 = s1 + 3;

        List<Integer> candidates = new ArrayList<>();
        for (int v = minS2; v <= maxS2; v++) {
            if (v != s1) {
                candidates.add(v);
            }
        }

        int s2 = candidates.get(rng.nextInt(candidates.size()));
        return new StartingShields(s1, s2);
    }

    /**
     * Immutable value record holding starting shield values for Player 1 and Player 2.
     */
    public record StartingShields(int p1Shield, int p2Shield) {
    }
}
