package com.arena.simulation;

import com.arena.bot.BotStrategy;
import com.arena.cards.CardLibrary;
import com.arena.cards.Deck;
import com.arena.combat.MatchEngine;
import com.arena.combat.MatchResult;
import com.arena.combat.PlayerState;

/**
 * Coordinates execution of single or batch matches between strategy bots.
 */
public class ArenaSimulationService {
    private static final int DEFAULT_HP = 30;

    /**
     * Executes a single simulation match between two strategies.
     *
     * @param p1Strategy player 1 strategy
     * @param p2Strategy player 2 strategy
     * @return outcome record with complete event log
     */
    public MatchResult runSingleMatch(BotStrategy p1Strategy, BotStrategy p2Strategy) {
        Deck deck1 = CardLibrary.createStandardDeck();
        deck1.shuffle();
        Deck deck2 = CardLibrary.createStandardDeck();
        deck2.shuffle();

        PlayerState p1 = new PlayerState("Player 1 (" + p1Strategy.getName() + ")", DEFAULT_HP, deck1);
        PlayerState p2 = new PlayerState("Player 2 (" + p2Strategy.getName() + ")", DEFAULT_HP, deck2);

        MatchEngine engine = new MatchEngine(p1, p1Strategy, p2, p2Strategy);
        return engine.playMatch();
    }

    /**
     * Executes batch simulation between two strategies.
     *
     * @param p1Strategy player 1 strategy
     * @param p2Strategy player 2 strategy
     * @param matchCount number of iterations to run
     * @return aggregated metrics
     */
    public MatchStatistics runSimulation(BotStrategy p1Strategy, BotStrategy p2Strategy, int matchCount) {
        int p1Wins = 0;
        int p2Wins = 0;
        int ties = 0;
        long totalTurns = 0;
        long totalDamage = 0;

        String p1Identifier = "Player 1 (" + p1Strategy.getName() + ")";
        String p2Identifier = "Player 2 (" + p2Strategy.getName() + ")";

        for (int i = 0; i < matchCount; i++) {
            Deck deck1 = CardLibrary.createStandardDeck();
            deck1.shuffle();
            Deck deck2 = CardLibrary.createStandardDeck();
            deck2.shuffle();

            PlayerState p1 = new PlayerState(p1Identifier, DEFAULT_HP, deck1);
            PlayerState p2 = new PlayerState(p2Identifier, DEFAULT_HP, deck2);

            MatchEngine engine = new MatchEngine(p1, p1Strategy, p2, p2Strategy);
            MatchResult result = engine.playMatch();

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

        double avgTurns = matchCount > 0 ? (double) totalTurns / matchCount : 0.0;
        double avgDamage = matchCount > 0 ? (double) totalDamage / matchCount : 0.0;

        return new MatchStatistics(matchCount, p1Wins, p2Wins, ties, avgTurns, avgDamage);
    }
}
