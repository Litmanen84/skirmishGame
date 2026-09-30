package com.arena.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VictoryHistoryTrackerTest {

    @Test
    @DisplayName("Should persist and reload victory records from storage")
    void shouldPersistAndReloadRecords(@TempDir Path tempDir) {
        Path historyFile = tempDir.resolve("test_history.csv");
        VictoryHistoryTracker tracker = new VictoryHistoryTracker(historyFile);

        assertThat(tracker.loadAllRecords()).isEmpty();

        MatchStatistics stats = new MatchStatistics(100, 60, 35, 5, 8.5, 92.0);
        VictoryRecord batchRecord = VictoryRecord.of("AggressiveBot", "DefensiveBot", stats);
        tracker.recordVictory(batchRecord);

        VictoryRecord singleRecord = VictoryRecord.ofSingleMatch(
                "AggressiveBot",
                "DefensiveBot",
                "Player 1 (AggressiveBot)",
                6,
                64
        );
        tracker.recordVictory(singleRecord);

        List<VictoryRecord> loaded = tracker.loadAllRecords();
        assertThat(loaded).hasSize(2);

        VictoryRecord first = loaded.get(0);
        assertThat(first.p1Name()).isEqualTo("AggressiveBot");
        assertThat(first.p2Name()).isEqualTo("DefensiveBot");
        assertThat(first.totalMatches()).isEqualTo(100);
        assertThat(first.p1Wins()).isEqualTo(60);
        assertThat(first.p2Wins()).isEqualTo(35);
        assertThat(first.ties()).isEqualTo(5);

        VictoryRecord second = loaded.get(1);
        assertThat(second.totalMatches()).isEqualTo(1);
        assertThat(second.p1Wins()).isEqualTo(1);
        assertThat(second.p2Wins()).isEqualTo(0);
    }
}
