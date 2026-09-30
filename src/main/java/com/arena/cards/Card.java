package com.arena.cards;

/**
 * Represents an immutable combat action card playable by a bot.
 *
 * @param name human-readable name of the card
 * @param cost mana cost required to play
 * @param type classification defining the primary mechanical role
 * @param value numeric magnitude for damage, shield, or heal
 */
public record Card(String name, int cost, CardType type, int value) {

    /**
     * Creates a standard low-cost direct attack card.
     */
    public static Card strike() {
        return new Card("Strike", 1, CardType.ATTACK, 3);
    }

    /**
     * Creates a medium-cost high-damage attack card.
     */
    public static Card heavySlash() {
        return new Card("Heavy Slash", 3, CardType.ATTACK, 8);
    }

    /**
     * Creates an expensive high-impact finishing attack card.
     */
    public static Card fireball() {
        return new Card("Fireball", 5, CardType.ATTACK, 14);
    }

    /**
     * Creates a low-cost defensive shield card.
     */
    public static Card guard() {
        return new Card("Guard", 1, CardType.DEFENSE, 4);
    }

    /**
     * Creates a high-value defensive wall card.
     */
    public static Card ironWall() {
        return new Card("Iron Wall", 3, CardType.DEFENSE, 10);
    }

    /**
     * Creates a supportive health restoration card.
     */
    public static Card heal() {
        return new Card("Heal", 2, CardType.UTILITY, 5);
    }

    /**
     * Creates a free burst resource card.
     */
    public static Card manaCrystal() {
        return new Card("Mana Crystal", 0, CardType.RESOURCE, 1);
    }
}
