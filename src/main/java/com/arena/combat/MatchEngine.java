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
    private static final int STARTING_HAND_SIZE = 10;

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
        // Initial draw phase for both players (opening hand of 10 cards)
        for (int i = 0; i < STARTING_HAND_SIZE; i++) {
            player1.drawCard();
            player2.drawCard();
        }
        events.add(new CombatEvent(0, "SYSTEM", String.format(
                "Match started. Opening hand: %d cards. Initial HP: %d vs %d | Starting Shields: %s [%d], %s [%d].",
                STARTING_HAND_SIZE,
                player1.getCurrentHp(),
                player2.getCurrentHp(),
                player1.getId(),
                player1.getActiveShield(),
                player2.getId(),
                player2.getActiveShield()
        )));

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
        // Clear lingering shields from the previous round at start of subsequent turns
        if (turn > 1) {
            active.decayShield();
        }

        // Apply turn-start passive buffs (e.g. defense buff generating shield)
        int defBuffShield = active.applyTurnStartDefenseBuff();
        if (defBuffShield > 0) {
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " defense buff triggers -> +" + defBuffShield + " Shield (" + active.getDefenseBuffDuration() + " rounds remaining)"));
        }

        // 1. Draw phase
        Optional<Card> drawn = active.drawCard();
        if (active.wasLastDrawReshuffled()) {
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " deck exhausted! Reshuffled " + active.getLastReshuffleCount() + " cards from discard pile into draw deck."));
        }
        if (drawn.isPresent()) {
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " draws a card (" + drawn.get().name() + ") [Hand: " + active.getHand().size() + "/10 cards, Deck: " + active.getDeckSize() + " left]"));
        } else if (active.getHand().size() >= PlayerState.MAX_HAND_SIZE) {
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " holds full hand [Hand: " + active.getHand().size() + "/10 cards, Deck: " + active.getDeckSize() + " left]"));
        } else {
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " draws no card (Deck empty) [Hand: " + active.getHand().size() + "/10 cards, Deck: 0 left]"));
        }

        // 2. Mana phase
        active.startTurnMana();
        events.add(new CombatEvent(turn, active.getId(),
                active.getId() + " mana refreshed to " + active.getCurrentMana() + " (Capacity: " + active.getManaCapacity() + ")"));

        // 3. Play & Resolve phase (Standard 1 card per round unless extra plays granted)
        int allowedPlays = 1;
        List<Card> plays = strategy.selectPlays(active.toView(), opponent.toView());
        if (plays != null) {
            for (Card card : plays) {
                if (allowedPlays > 0 && active.canPlay(card)) {
                    allowedPlays--;
                    allowedPlays += card.extraPlays();
                    active.playCard(card);

                    events.add(new CombatEvent(turn, active.getId(),
                            active.getId() + " plays " + card.name() + " [" + card.formatSpecs() + "] (Remaining Mana: " + active.getCurrentMana() + ")"));

                    resolveCardEffect(turn, active, opponent, card);

                    if (card.grantsExtraPlay()) {
                        events.add(new CombatEvent(turn, active.getId(),
                                active.getId() + " gains +" + card.extraPlays() + " bonus card play from " + card.name() + " (Plays remaining this round: " + allowedPlays + ")"));
                    }

                    if (!opponent.isAlive()) {
                        return true;
                    }
                }
            }
        }

        // 4. End phase
        active.endTurnBuffs();
        String buffSummary = formatBuffSummary(active);
        String buffSuffix = buffSummary.isEmpty() ? "" : ", Buffs: " + buffSummary;
        events.add(new CombatEvent(turn, active.getId(),
                active.getId() + " ends turn [HP: " + active.getCurrentHp() + "/" + active.getMaxHp() +
                        ", Mana: " + active.getCurrentMana() + ", Shield: " + active.getActiveShield() + buffSuffix + "]"));

        return false;
    }

    private void resolveCardEffect(int turn, PlayerState active, PlayerState opponent, Card card) {
        if (card.type() == CardType.ATTACK) {
            int attackBonus = active.getAttackBuff();
            int totalDamage = card.value() + attackBonus;
            int shieldBefore = opponent.getActiveShield();
            int hpDamage = opponent.takeDamage(totalDamage);
            int blocked = shieldBefore - opponent.getActiveShield();
            active.recordDamageDealt(totalDamage);

            String attackDetails = attackBonus > 0
                    ? card.value() + " (+" + attackBonus + " Atk Buff = " + totalDamage + " dmg)"
                    : card.value() + " dmg";

            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " for " + attackDetails + " -> " +
                            opponent.getId() + " [Blocked: " + blocked + ", Taken: " + hpDamage + ", Remaining HP: " + opponent.getCurrentHp() + "/" + opponent.getMaxHp() + "]"));
        } else if (card.type() == CardType.DEFENSE) {
            if (card.value() > 0) {
                active.gainShield(card.value());
                events.add(new CombatEvent(turn, active.getId(),
                        active.getId() + " casts " + card.name() + " -> +" + card.value() + " Shield [Total Shield: " + active.getActiveShield() + "]"));
            }
        } else if (card.type() == CardType.UTILITY) {
            if (card.value() > 0) {
                int healed = active.heal(card.value());
                events.add(new CombatEvent(turn, active.getId(),
                        active.getId() + " casts " + card.name() + " -> Restored " + healed + " HP [HP: " + active.getCurrentHp() + "/" + active.getMaxHp() + "]"));
            } else {
                events.add(new CombatEvent(turn, active.getId(),
                        active.getId() + " casts " + card.name()));
            }
        } else if (card.type() == CardType.RESOURCE) {
            active.gainMana(card.value());
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " casts " + card.name() + " -> +" + card.value() + " Mana [Current: " + active.getCurrentMana() + "]"));
        }

        // Apply multi-turn temporary buff if card possesses one
        if (card.hasBuff()) {
            active.applyBuffs(card.attackBuff(), card.defenseBuff(), card.buffDuration());
            List<String> buffEffects = new ArrayList<>();
            if (card.attackBuff() > 0) {
                buffEffects.add("+" + card.attackBuff() + " Attack");
            }
            if (card.defenseBuff() > 0) {
                buffEffects.add("+" + card.defenseBuff() + " Defense");
            }
            events.add(new CombatEvent(turn, active.getId(),
                    active.getId() + " activates buff -> " + String.join(", ", buffEffects) +
                            " for " + card.buffDuration() + " rounds"));
        }
    }

    private String formatBuffSummary(PlayerState player) {
        List<String> buffs = new ArrayList<>();
        if (player.getAttackBuff() > 0 && player.getAttackBuffDuration() > 0) {
            buffs.add("+" + player.getAttackBuff() + " Atk (" + player.getAttackBuffDuration() + "r)");
        }
        if (player.getDefenseBuff() > 0 && player.getDefenseBuffDuration() > 0) {
            buffs.add("+" + player.getDefenseBuff() + " Def (" + player.getDefenseBuffDuration() + "r)");
        }
        return String.join(", ", buffs);
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
