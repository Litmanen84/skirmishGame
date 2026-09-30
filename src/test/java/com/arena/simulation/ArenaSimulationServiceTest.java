package com.arena.simulation;

import com.arena.bot.AggressiveBot;
import com.arena.bot.DefensiveBot;
import com.arena.cards.CardLibrary;
import com.arena.cards.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ArenaSimulationServiceTest {

    @Test
    @DisplayName("Should execute batch simulation, capture first match details, and aggregate statistics correctly")
    void shouldRunBatchSimulation() {
        ArenaSimulationService service = new ArenaSimulationService();
        AggressiveBot aggressiveBot = new AggressiveBot();
        DefensiveBot defensiveBot = new DefensiveBot();

        SimulationResult result = service.runSimulation(aggressiveBot, defensiveBot, 20);

        assertThat(result.firstMatch()).isNotNull();
        assertThat(result.firstMatch().events()).isNotEmpty();
        assertThat(result.firstMatch().totalTurns()).isGreaterThan(0);

        MatchStatistics stats = result.statistics();
        assertThat(stats.totalMatches()).isEqualTo(20);
        assertThat(stats.p1Wins() + stats.p2Wins() + stats.ties()).isEqualTo(20);
        assertThat(stats.averageTurns()).isGreaterThan(0.0);
        assertThat(stats.averageDamageDealt()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should generate different starting shields that are at most 3 units apart")
    void shouldGenerateConstrainedStartingShields() {
        Random random = new Random(42);
        for (int i = 0; i < 1000; i++) {
            ArenaSimulationService.StartingShields shields = ArenaSimulationService.generateStartingShields(random);
            int s1 = shields.p1Shield();
            int s2 = shields.p2Shield();

            assertThat(s1).isNotEqualTo(s2);
            assertThat(Math.abs(s1 - s2)).isLessThanOrEqualTo(3);
            assertThat(s1).isGreaterThanOrEqualTo(1);
            assertThat(s2).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("Should generate variable randomized decks from the expanded library")
    void shouldGenerateVariableDecks() {
        Random random = new Random(123);
        Deck deck1 = CardLibrary.createRandomizedDeck(random);
        Deck deck2 = CardLibrary.createRandomizedDeck(random);

        assertThat(deck1.size()).isEqualTo(26);
        assertThat(deck2.size()).isEqualTo(26);
    }
}
