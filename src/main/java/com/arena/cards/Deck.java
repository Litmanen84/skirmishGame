package com.arena.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the draw pile, reshuffling, and exhaustion behavior during a match.
 */
public class Deck {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Constructs a deck with an initial collection of cards.
     *
     * @param initialCards initial card list
     */
    public Deck(List<Card> initialCards) {
        if (initialCards != null) {
            this.cards.addAll(initialCards);
        }
    }

    /**
     * Draws the top card from the deck if available.
     *
     * @return optional containing the drawn card, or empty if deck is exhausted
     */
    public Optional<Card> draw() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cards.remove(0));
    }

    /**
     * Adds a collection of cards into this deck.
     *
     * @param newCards cards to add
     */
    public void addCards(List<Card> newCards) {
        if (newCards != null) {
            this.cards.addAll(newCards);
        }
    }

    /**
     * Shuffles the deck cards in-place to ensure non-deterministic match variety.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Returns whether the deck is empty.
     *
     * @return true if no cards remain
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns the current number of cards remaining in the draw pile.
     *
     * @return remaining card count
     */
    public int size() {
        return cards.size();
    }
}
