package com.arena.combat;

/**
 * Encapsulates an immutable record of a specific combat action or phase transition for logging and metrics.
 *
 * @param turn current match turn
 * @param playerId identifier of active player
 * @param description formatted summary of the event
 */
public record CombatEvent(int turn, String playerId, String description) {
}
