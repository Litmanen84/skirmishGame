package com.arena.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Manages persistent storage and retrieval of simulation victory records across runs.
 */
public class VictoryHistoryTracker {

    private static final String CSV_HEADER = "timestamp,p1Name,p2Name,totalMatches,p1Wins,p2Wins,ties,averageTurns,averageDamage";
    private final Path storagePath;

    /**
     * Initializes tracker targeting default file storage in current working directory.
     */
    public VictoryHistoryTracker() {
        this(Path.of("arena_victory_history.csv"));
    }

    /**
     * Initializes tracker targeting a specific storage path for testing and isolation.
     *
     * @param storagePath destination file path
     */
    public VictoryHistoryTracker(Path storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Appends a newly completed victory record into persistent storage.
     *
     * @param record simulation outcome record
     */
    public synchronized void recordVictory(VictoryRecord record) {
        if (record == null) {
            return;
        }
        try {
            boolean needsHeader = !Files.exists(storagePath) || Files.size(storagePath) == 0;
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(
                    storagePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )) {
                if (needsHeader) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }
                String line = String.format(
                        Locale.US,
                        "%s,%s,%s,%d,%d,%d,%d,%.2f,%.2f",
                        record.timestamp(),
                        escapeCsv(record.p1Name()),
                        escapeCsv(record.p2Name()),
                        record.totalMatches(),
                        record.p1Wins(),
                        record.p2Wins(),
                        record.ties(),
                        record.averageTurns(),
                        record.averageDamage()
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to persist victory record: " + e.getMessage());
        }
    }

    /**
     * Loads all historical victory records from storage.
     *
     * @return unmodifiable list of historical victory records in chronological order
     */
    public synchronized List<VictoryRecord> loadAllRecords() {
        if (!Files.exists(storagePath)) {
            return List.of();
        }
        List<VictoryRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (firstLine && line.startsWith("timestamp")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    try {
                        String timestamp = parts[0];
                        String p1Name = parts[1];
                        String p2Name = parts[2];
                        int totalMatches = Integer.parseInt(parts[3]);
                        int p1Wins = Integer.parseInt(parts[4]);
                        int p2Wins = Integer.parseInt(parts[5]);
                        int ties = Integer.parseInt(parts[6]);
                        double avgTurns = Double.parseDouble(parts[7]);
                        double avgDamage = Double.parseDouble(parts[8]);

                        records.add(new VictoryRecord(
                                timestamp, p1Name, p2Name, totalMatches,
                                p1Wins, p2Wins, ties, avgTurns, avgDamage
                        ));
                    } catch (NumberFormatException ignored) {
                        // Skip malformed records safely
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to read victory records: " + e.getMessage());
        }
        return Collections.unmodifiableList(records);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", ";");
    }
}
