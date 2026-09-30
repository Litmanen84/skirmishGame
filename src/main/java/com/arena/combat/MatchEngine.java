package com.arena.combat;

import com.arena.bot.BotStrategy;
import com.arena.cards.Card;
import com.arena.cards.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates turn progression, combat resolution, and match termination.
 */
public class MatchEngine {
    private static final int MAX_TURNS = 50;
    private static final int STARTING_HAND_SIZE = 4;

    private final PlayerState player1;
    private final PlayerState player2;
    private final BotStrategy strategy1;
    private final BotStrategy strategy2;
    private final List<CombatEvent> events = new ArrayList<>();

    /**
     * Configures a match between two players and their respective bot strategies.
     *
     * @param player1 player 1 state
     * @param strategy1 player 1 decision strategy
     * @param player2 player 2 state
     * @param strategy2 player 2 decision strategy
     */
    public MatchEngine(PlayerState player1, BotStrategy strategy1, PlayerState player2, BotStrategy strategy2) {
        this.player1 = player1;
        this.strategy1 = strategy1;
        this.player2 = player2;
        this.strategy2 = strategy2;
    }

    /**
     * Executes complete match simulation until victory condition or turn limit tie-break.
     *
     * @return outcome record with winner, duration, and event telemetry
     */
    public MatchResult playMatch() {
        // Initial draw phase for both players
        for (int i = 0; i < STARTING_HAND_SIZE; i++) {
            player1.drawCard();
            player2.drawCard();
        }
        events.add(new CombatEvent(0, "SYSTEM", "Match started. Both players drew " + STARTING_HAND_SIZE + " opening cards."));

        int currentTurn = 1;
        while (currentTurn <= MAX_TURNS) {
            // Player 1 turn
            boolean matchEnded = executePlayerTurn(currentTurn, player1, strategy1, player2);
            if (matchEnded) {
                String winner = player1.isAlive() ? player1.getId() : player2.getId();
                events.add(new CombatEvent(currentTurn, "SYSTEM", "Match concluded! Winner: " + winner));
                return new MatchResult(winner, currentTurn, false, List.copyOf(events));
            }

            // Player 2 turn
            matchEnded = executePlayerTurn(currentTurn, player2, strategy2, player1);
            if (matchEnded) {
                String winner = player2.isAlive() ? player2.getId() : player1.getId();
                events.add(new CombatEvent(currentTurn, "SYSTEM", "Match concluded! Winner: " + winner));
                return new MatchResult(winner, currentTurn, false, List.copyOf(events));
            }

            currentTurn++;
        }

        // Reached 50 turns without knockout: evaluate tie-break
        return resolveTieBreak(MAX_TURNS);
    }

    private boolean executePlayerTurn(int turn, PlayerState active, BotStrategy strategy, PlayerState opponent) {
        // Clear lingering shields from the previous round at start of new turn
        active.decayShield();

        // 1. Draw phase
        Optional<Card> drawn = active.drawCard();
        String drawnText = drawn.map(c -> " (" + c.name() + ")").orElse(" (Deck empty)");
        events.add(new CombatEvent(turn, active.getId(), active.getId() + " draws a card" + drawnText));

        // 2. Mana phase
        active.startTurnMana();
        events.add(new CombatEvent(turn, active.getId(), active.getId() + " mana refreshed to " + active.getCurrentMana()));

        // 3. Play & Resolve phase
        List<Card> plays = strategy.selectPlays(active.toView(), opponent.toView());
        if (plays != null) {
            for (Card card : plays) {
                if (active.canPlay(card)) {
                    active.playCard(card);
                    resolveCardEffect(turn, active, opponent, card);
                    if (!opponent.isAlive()) {
                        return true;
                    }
                }
            }
        }

        // 4. End phase
        events.add(new CombatEvent(turn, active.getId(), active.getId() + " ends turn [HP: " + active.getCurrentHp() + "/" + active.getMaxHp() + ", Mana: " + active.getCurrentMana() + ", Shield: " + active.getActiveShield() + "]"));

        return false;
    }

    private void resolveCardEffect(int turn, PlayerState active, PlayerState opponent, Card card) {
        if (card.type() == CardType.ATTACK) {
            int shieldBefore = opponent.getActiveShield();
            int hpDamage = opponent.takeDamage(card.value());
            int blocked = shieldBefore - opponent.getActiveShield();
            active.recordDamageDealt(card.value());
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " for " + card.value() + " dmg -> " +
                            opponent.getId() + " [Blocked: " + blocked + ", Taken: " + hpDamage + ", Remaining HP: " + opponent.getCurrentHp() + "/" + opponent.getMaxHp() + "]"));
        } else if (card.type() == CardType.DEFENSE) {
            active.gainShield(card.value());
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " -> +" + card.value() + " Shield [Total Shield: " + active.getActiveShield() + "]"));
        } else if (card.type() == CardType.UTILITY) {
            int healed = active.heal(card.value());
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " -> Restored " + healed + " HP [HP: " + active.getCurrentHp() + "/" + active.getMaxHp() + "]"));
        } else if (card.type() == CardType.RESOURCE) {
            active.gainMana(card.value());
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " -> +" + card.value() + " Mana [Current: " + active.getCurrentMana() + "]"));
        }
    }

    private MatchResult resolveTieBreak(int finalTurn) {
        events.add(new CombatEvent(finalTurn, "SYSTEM", "Turn limit reached (" + finalTurn + "). Resolving tie-break..."));
        // Primary tie-break: highest HP
        if (player1.getCurrentHp() > player2.getCurrentHp()) {
            events.add(new CombatEvent(finalTurn, "SYSTEM", "Winner by HP tie-break: " + player1.getId() + " (" + player1.getCurrentHp() + " vs " + player2.getCurrentHp() + ")"));
            return new MatchResult(player1.getId(), finalTurn, false, List.copyOf(events));
        } else if (player2.getCurrentHp() > player1.getCurrentHp()) {
            events.add(new CombatEvent(finalTurn, "SYSTEM", "Winner by HP tie-break: " + player2.getId() + " (" + player2.getCurrentHp() + " vs " + player1.getCurrentHp() + ")"));
            return new MatchResult(player2.getId(), finalTurn, false, List.copyOf(events));
        }

        // Secondary tie-break: highest total damage dealt
        if (player1.getTotalDamageDealt() > player2.getTotalDamageDealt()) {
            events.add(new CombatEvent(finalTurn, "SYSTEM", "Winner by damage dealt tie-break: " + player1.getId() + " (" + player1.getTotalDamageDealt() + " vs " + player2.getTotalDamageDealt() + ")"));
            return new MatchResult(player1.getId(), finalTurn, false, List.copyOf(events));
        } else if (player2.getTotalDamageDealt() > player1.getTotalDamageDealt()) {
            events.add(new CombatEvent(finalTurn, "SYSTEM", "Winner by damage dealt tie-break: " + player2.getId() + " (" + player2.getTotalDamageDealt() + " vs " + player1.getTotalDamageDealt() + ")"));
            return new MatchResult(player2.getId(), finalTurn, false, List.copyOf(events));
        }

        // Complete tie
        events.add(new CombatEvent(finalTurn, "SYSTEM", "Match concluded as a Draw."));
        return new MatchResult(null, finalTurn, true, List.copyOf(events));
    }
}
