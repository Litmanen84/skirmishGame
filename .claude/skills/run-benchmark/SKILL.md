---
description: Runs N simulated matches between two bot/deck pairings and prints win rate, average match length, and average damage. Use when asked to test or benchmark the engine.
---

Run the simulation with:
!`mvn -q exec:java -Dexec.mainClass=com.arena.Main -Dexec.args="--matches 1000 --p1 Aggressive --p2 Defensive"`

Summarize the output: win rate per side, average match length, and average
damage per match. Flag anything that looks off (e.g. a win rate near 0% or
100%, which usually means a bug rather than a balanced matchup).
