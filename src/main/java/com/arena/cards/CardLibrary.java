package com.arena.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory providing preconfigured balanced card collections and standard decks.
 */
public final class CardLibrary {

    private CardLibrary() {
    }

    /**
     * Constructs a standard 20-card deck with balanced attack, defense, and utility cards.
     */
    public static Deck createStandardDeck() {
        List<Card> cards = new ArrayList<>();
        // 6 Strikes (Cost 1, 3 Dmg)
        for (int i = 0; i < 6; i++) {
            cards.add(Card.strike());
        }
        // 4 Heavy Slashes (Cost 3, 8 Dmg)
        for (int i = 0; i < 4; i++) {
            cards.add(Card.heavySlash());
        }
        // 2 Fireballs (Cost 5, 14 Dmg)
        for (int i = 0; i < 2; i++) {
            cards.add(Card.fireball());
        }
        // 4 Guards (Cost 1, 4 Shield)
        for (int i = 0; i < 4; i++) {
            cards.add(Card.guard());
        }
        // 2 Iron Walls (Cost 3, 10 Shield)
        for (int i = 0; i < 2; i++) {
            cards.add(Card.ironWall());
        }
        // 2 Heals (Cost 2, 5 Heal)
        for (int i = 0; i < 2; i++) {
            cards.add(Card.heal());
        }
        return new Deck(cards);
    }
}
