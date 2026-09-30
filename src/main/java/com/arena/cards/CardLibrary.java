package com.arena.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Factory providing preconfigured balanced card collections, randomized decks, and card pools.
 */
public final class CardLibrary {

    private static final List<Supplier<Card>> ATTACK_POOL = List.of(
            Card::strike,
            Card::quickSlash,
            Card::twinStrike,
            Card::cleave,
            Card::shieldSlam,
            Card::heavySlash,
            Card::bloodlust,
            Card::flurryOfStrikes,
            Card::lightningBolt,
            Card::pyroblast,
            Card::dragonsRoar,
            Card::fireball,
            Card::meteorStrike
    );

    private static final List<Supplier<Card>> DEFENSE_POOL = List.of(
            Card::deflect,
            Card::guard,
            Card::ironSkin,
            Card::shieldWall,
            Card::stoneForm,
            Card::ironWall,
            Card::aegis
    );

    private static final List<Supplier<Card>> UTILITY_POOL = List.of(
            Card::sharpenBlade,
            Card::adrenaline,
            Card::bandage,
            Card::battleCry,
            Card::barrierWard,
            Card::heal,
            Card::berserkFury,
            Card::paladinsBlessing,
            Card::holyLight,
            Card::elixirOfLife
    );

    private static final List<Supplier<Card>> RESOURCE_POOL = List.of(
            Card::manaCrystal,
            Card::manaSurge,
            Card::energyPotion
    );

    private CardLibrary() {
    }

    /**
     * Constructs a standard 26-card deck with balanced attack, defense, utility, and resource cards.
     */
    public static Deck createStandardDeck() {
        List<Card> cards = new ArrayList<>();
        // Attacks
        cards.add(Card.strike());
        cards.add(Card.strike());
        cards.add(Card.quickSlash());
        cards.add(Card.quickSlash());
        cards.add(Card.twinStrike());
        cards.add(Card.cleave());
        cards.add(Card.cleave());
        cards.add(Card.shieldSlam());
        cards.add(Card.heavySlash());
        cards.add(Card.heavySlash());
        cards.add(Card.flurryOfStrikes());
        cards.add(Card.lightningBolt());
        cards.add(Card.pyroblast());
        cards.add(Card.fireball());
        cards.add(Card.meteorStrike());

        // Defenses
        cards.add(Card.deflect());
        cards.add(Card.guard());
        cards.add(Card.guard());
        cards.add(Card.ironSkin());
        cards.add(Card.shieldWall());
        cards.add(Card.ironWall());
        cards.add(Card.aegis());

        // Utility (Heal & Buffs)
        cards.add(Card.adrenaline());
        cards.add(Card.bandage());
        cards.add(Card.battleCry());
        cards.add(Card.heal());
        cards.add(Card.holyLight());

        // Resources (Mana)
        cards.add(Card.manaCrystal());
        cards.add(Card.manaSurge());

        return new Deck(cards);
    }

    /**
     * Constructs a highly randomized 26-card deck drawn non-deterministically from the rich card pool.
     *
     * @return randomized deck
     */
    public static Deck createRandomizedDeck() {
        return createRandomizedDeck(new Random());
    }

    /**
     * Constructs a randomized 26-card deck drawn with the provided random source.
     *
     * @param random random generator
     * @return randomized deck
     */
    public static Deck createRandomizedDeck(Random random) {
        List<Card> cards = new ArrayList<>();

        // 14 Attacks randomly selected from attack pool
        for (int i = 0; i < 14; i++) {
            Supplier<Card> supplier = ATTACK_POOL.get(random.nextInt(ATTACK_POOL.size()));
            cards.add(supplier.get());
        }

        // 6 Defenses randomly selected from defense pool
        for (int i = 0; i < 6; i++) {
            Supplier<Card> supplier = DEFENSE_POOL.get(random.nextInt(DEFENSE_POOL.size()));
            cards.add(supplier.get());
        }

        // 4 Utilities randomly selected from utility pool
        for (int i = 0; i < 4; i++) {
            Supplier<Card> supplier = UTILITY_POOL.get(random.nextInt(UTILITY_POOL.size()));
            cards.add(supplier.get());
        }

        // 2 Resources randomly selected from resource pool
        for (int i = 0; i < 2; i++) {
            Supplier<Card> supplier = RESOURCE_POOL.get(random.nextInt(RESOURCE_POOL.size()));
            cards.add(supplier.get());
        }

        Deck deck = new Deck(cards);
        deck.shuffle();
        return deck;
    }
}
