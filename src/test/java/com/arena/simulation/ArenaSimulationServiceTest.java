package com.arena.simulation;

import com.arena.bot.AggressiveBot;
import com.arena.bot.DefensiveBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArenaSimulationServiceTest {

    @Test
    @DisplayName("Should execute batch simulation and aggregate statistics correctly")
    void shouldRunBatchSimulation() {
        ArenaSimulationService service = new ArenaSimulationService();
        AggressiveBot aggressiveBot = new AggressiveBot();
        DefensiveBot defensiveBot = new DefensiveBot();

        MatchStatistics stats = service.runSimulation(aggressiveBot, defensiveBot, 20);

        assertThat(stats.totalMatches()).isEqualTo(20);
        assertThat(stats.p1Wins() + stats.p2Wins() + stats.ties()).isEqualTo(20);
        assertThat(stats.averageTurns()).isGreaterThan(0.0);
        assertThat(stats.averageDamageDealt()).isGreaterThan(0.0);
    }
}
