package com.arena.combat;

import com.arena.cards.Card;
import com.arena.cards.Deck;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Encapsulates mutable combat state for an individual player throughout a match lifecycle.
 */
public class PlayerState {
    private final String id;
    private final int maxHp;
    private int currentHp;
    private int manaCapacity;
    private int currentMana;
    private int activeShield;
    private int totalDamageDealt;
    private int attackBuff;
    private int attackBuffDuration;
    private int defenseBuff;
    private int defenseBuffDuration;
    private boolean lastDrawReshuffled;
    private int lastReshuffleCount;
    private final Deck deck;
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();

    /**
     * Initializes player state with initial health and deck.
     *
     * @param id player identifier
     * @param maxHp maximum and starting health points
     * @param deck player's draw deck
     */
    public PlayerState(String id, int maxHp, Deck deck) {
        this(id, maxHp, 0, deck);
    }

    /**
     * Initializes player state with initial health, starting shield, and deck.
     *
     * @param id player identifier
     * @param maxHp maximum and starting health points
     * @param startingShield initial defensive shield points
     * @param deck player's draw deck
     */
    public PlayerState(String id, int maxHp, int startingShield, Deck deck) {
        this.id = id;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.manaCapacity = 0;
        this.currentMana = 0;
        this.activeShield = Math.max(0, startingShield);
        this.totalDamageDealt = 0;
        this.attackBuff = 0;
        this.attackBuffDuration = 0;
        this.defenseBuff = 0;
        this.defenseBuffDuration = 0;
        this.deck = deck != null ? deck : new Deck(List.of());
    }

    /**
     * Executes start-of-turn mana growth and replenishment up to the maximum limit.
     */
    public void startTurnMana() {
        if (manaCapacity < 10) {
            manaCapacity++;
        }
        currentMana = manaCapacity;
    }

    /**
     * Draws a card into the player's hand, recycling the discard pile if the deck is empty.
     *
     * @return optional containing the drawn card, or empty if no cards are available
     */
    public Optional<Card> drawCard() {
        lastDrawReshuffled = false;
        lastReshuffleCount = 0;
        Optional<Card> drawn = deck.draw();
        if (drawn.isEmpty() && !discardPile.isEmpty()) {
            lastDrawReshuffled = true;
            lastReshuffleCount = discardPile.size();
            deck.addCards(discardPile);
            discardPile.clear();
            deck.shuffle();
            drawn = deck.draw();
        }
        drawn.ifPresent(hand::add);
        return drawn;
    }

    /**
     * Checks if the player can afford to play the specified card.
     *
     * @param card card candidate
     * @return true if affordable and present in hand
     */
    public boolean canPlay(Card card) {
        return card != null && hand.contains(card) && currentMana >= card.cost();
    }

    /**
     * Plays a card from hand, deducting its mana cost and routing it to the discard pile.
     *
     * @param card card to play
     * @return true if card was successfully played
     */
    public boolean playCard(Card card) {
        if (!canPlay(card)) {
            return false;
        }
        hand.remove(card);
        currentMana -= card.cost();
        discardPile.add(card);
        return true;
    }

    /**
     * Applies defensive shield mitigation against incoming raw attack damage.
     *
     * @param rawDamage incoming damage value
     * @return effective damage dealt to health points
     */
    public int takeDamage(int rawDamage) {
        int damageToShield = Math.min(activeShield, rawDamage);
        activeShield -= damageToShield;
        int remainingDamage = rawDamage - damageToShield;
        int damageToHp = Math.min(currentHp, remainingDamage);
        currentHp -= damageToHp;
        return damageToHp;
    }

    /**
     * Grants temporary defensive shield absorption points.
     *
     * @param amount shield value to add
     */
    public void gainShield(int amount) {
        if (amount > 0) {
            activeShield += amount;
        }
    }

    /**
     * Clears expired temporary defense shields at the end of a turn cycle.
     */
    public void decayShield() {
        activeShield = 0;
    }

    /**
     * Restores health points without exceeding maximum capacity.
     *
     * @param amount health points to restore
     * @return actual amount of health restored
     */
    public int heal(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int effectiveHeal = Math.min(amount, maxHp - currentHp);
        currentHp += effectiveHeal;
        return effectiveHeal;
    }

    /**
     * Temporarily boosts available mana for additional tactical plays.
     *
     * @param amount mana bonus
     */
    public void gainMana(int amount) {
        if (amount > 0) {
            currentMana += amount;
        }
    }

    /**
     * Records cumulative damage dealt by this player for statistical metrics and tie-breaking.
     *
     * @param damage amount of damage dealt
     */
    public void recordDamageDealt(int damage) {
        if (damage > 0) {
            totalDamageDealt += damage;
        }
    }

    /**
     * Indicates whether the player is still active and has remaining health.
     *
     * @return true if health is above zero
     */
    public boolean isAlive() {
        return currentHp > 0;
    }

    /**
     * Applies temporary attack and/or defense buffs that last for a specified number of rounds.
     *
     * @param attackBonus attack bonus points
     * @param defenseBonus defense bonus shield points per turn
     * @param duration number of rounds/turns the buffs will persist
     */
    public void applyBuffs(int attackBonus, int defenseBonus, int duration) {
        if (duration <= 0) {
            return;
        }
        if (attackBonus > 0) {
            this.attackBuff = attackBonus;
            this.attackBuffDuration = Math.max(this.attackBuffDuration, duration);
        }
        if (defenseBonus > 0) {
            this.defenseBuff = defenseBonus;
            this.defenseBuffDuration = Math.max(this.defenseBuffDuration, duration);
        }
    }

    /**
     * Processes turn-start passive buffs (e.g. granting active defense shield from defense buff).
     *
     * @return shield value gained from defense buff, or 0 if none active
     */
    public int applyTurnStartDefenseBuff() {
        if (defenseBuff > 0 && defenseBuffDuration > 0) {
            gainShield(defenseBuff);
            return defenseBuff;
        }
        return 0;
    }

    /**
     * Decrements active buff durations at the end of a turn, expiring any buffs reaching zero.
     */
    public void endTurnBuffs() {
        if (attackBuffDuration > 0) {
            attackBuffDuration--;
            if (attackBuffDuration == 0) {
                attackBuff = 0;
            }
        }
        if (defenseBuffDuration > 0) {
            defenseBuffDuration--;
            if (defenseBuffDuration == 0) {
                defenseBuff = 0;
            }
        }
    }

    /**
     * Indicates whether any attack or defense buff is currently active.
     *
     * @return true if an active buff with remaining duration exists
     */
    public boolean hasActiveBuffs() {
        return (attackBuff > 0 && attackBuffDuration > 0) || (defenseBuff > 0 && defenseBuffDuration > 0);
    }

    /**
     * Creates an immutable read-only view of current state for safe bot consumption.
     *
     * @return immutable PlayerView snapshot
     */
    public PlayerView toView() {
        return new PlayerView(
                id,
                currentHp,
                maxHp,
                currentMana,
                activeShield,
                List.copyOf(hand),
                attackBuff,
                defenseBuff,
                attackBuffDuration,
                defenseBuffDuration
        );
    }

    /**
     * Returns the player identifier.
     *
     * @return player id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the current HP.
     *
     * @return current health points
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Returns the maximum HP.
     *
     * @return maximum health points
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Returns the maximum mana capacity reachable this turn.
     *
     * @return maximum mana capacity
     */
    public int getManaCapacity() {
        return manaCapacity;
    }

    /**
     * Returns currently available mana.
     *
     * @return available mana
     */
    public int getCurrentMana() {
        return currentMana;
    }

    /**
     * Returns the active shield points.
     *
     * @return active shield
     */
    public int getActiveShield() {
        return activeShield;
    }

    /**
     * Returns total cumulative damage dealt.
     *
     * @return total damage dealt
     */
    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }

    /**
     * Returns the active attack buff bonus.
     *
     * @return attack buff bonus
     */
    public int getAttackBuff() {
        return attackBuff;
    }

    /**
     * Returns the remaining turns for the active attack buff.
     *
     * @return attack buff remaining duration
     */
    public int getAttackBuffDuration() {
        return attackBuffDuration;
    }

    /**
     * Returns the active defense buff bonus.
     *
     * @return defense buff bonus
     */
    public int getDefenseBuff() {
        return defenseBuff;
    }

    /**
     * Returns the remaining turns for the active defense buff.
     *
     * @return defense buff remaining duration
     */
    public int getDefenseBuffDuration() {
        return defenseBuffDuration;
    }

    /**
     * Returns whether the most recent draw caused the discard pile to be reshuffled.
     *
     * @return true if discard pile was recycled during the last draw
     */
    public boolean wasLastDrawReshuffled() {
        return lastDrawReshuffled;
    }

    /**
     * Returns the count of cards recycled from discard pile during the last draw.
     *
     * @return count of cards reshuffled
     */
    public int getLastReshuffleCount() {
        return lastReshuffleCount;
    }

    /**
     * Returns the current number of cards remaining in the draw deck.
     *
     * @return remaining cards in deck
     */
    public int getDeckSize() {
        return deck.size();
    }

    /**
     * Returns an unmodifiable snapshot of cards currently in the discard pile.
     *
     * @return unmodifiable list of cards in discard pile
     */
    public List<Card> getDiscardPile() {
        return List.copyOf(discardPile);
    }

    /**
     * Returns the cards currently in hand.
     *
     * @return unmodifiable list of cards in hand
     */
    public List<Card> getHand() {
        return List.copyOf(hand);
    }
}
