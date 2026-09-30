package com.arena.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an immutable combat action card playable by a bot.
 * Supports direct effects (damage, shield, heal, mana), multi-round buffs, and extra action allowances.
 *
 * @param name human-readable name of the card
 * @param cost mana cost required to play
 * @param type classification defining the primary mechanical role
 * @param value numeric magnitude for immediate damage, shield, heal, or mana
 * @param attackBuff temporary attack damage bonus applied to subsequent attacks
 * @param defenseBuff temporary passive shield granted at each turn
 * @param buffDuration number of rounds/turns the temporary buffs persist
 * @param extraPlays number of additional card plays granted in the current round
 */
public record Card(
        String name,
        int cost,
        CardType type,
        int value,
        int attackBuff,
        int defenseBuff,
        int buffDuration,
        int extraPlays
) {

    /**
     * Backward-compatible constructor for standard direct action cards without duration buffs or extra plays.
     */
    public Card(String name, int cost, CardType type, int value) {
        this(name, cost, type, value, 0, 0, 0, 0);
    }

    /**
     * Backward-compatible constructor for cards with duration buffs without extra plays.
     */
    public Card(String name, int cost, CardType type, int value, int attackBuff, int defenseBuff, int buffDuration) {
        this(name, cost, type, value, attackBuff, defenseBuff, buffDuration, 0);
    }

    /**
     * Indicates whether this card applies any multi-turn temporary buff.
     *
     * @return true if an attack or defense buff with positive duration is present
     */
    public boolean hasBuff() {
        return buffDuration > 0 && (attackBuff > 0 || defenseBuff > 0);
    }

    /**
     * Indicates whether this card allows playing another card in the same round.
     *
     * @return true if extra plays are granted
     */
    public boolean grantsExtraPlay() {
        return extraPlays > 0;
    }

    /**
     * Formats a comprehensive specification summary of the card attributes for spectators.
     *
     * @return formatted card specification string
     */
    public String formatSpecs() {
        List<String> specs = new ArrayList<>();
        specs.add("Cost: " + cost + " Mana");
        specs.add("Type: " + type);

        if (type == CardType.ATTACK && value > 0) {
            specs.add("Dmg: " + value);
        } else if (type == CardType.DEFENSE && value > 0) {
            specs.add("Shield: +" + value);
        } else if (type == CardType.UTILITY && value > 0) {
            specs.add("Heal: +" + value);
        } else if (type == CardType.RESOURCE && value > 0) {
            specs.add("Mana: +" + value);
        }

        if (attackBuff > 0) {
            specs.add("AtkBuff: +" + attackBuff + " (" + buffDuration + " rds)");
        }
        if (defenseBuff > 0) {
            specs.add("DefBuff: +" + defenseBuff + " (" + buffDuration + " rds)");
        }
        if (extraPlays > 0) {
            specs.add("Special: +" + extraPlays + " Extra Play");
        }

        return String.join(" | ", specs);
    }

    // === ATTACK CARDS ===

    /**
     * Creates a standard low-cost direct attack card (Cost 1, Dmg 3).
     */
    public static Card strike() {
        return new Card("Strike", 1, CardType.ATTACK, 3);
    }

    /**
     * Creates a fast attack card that allows playing an additional card in the round (Cost 1, Dmg 3, +1 Extra Play).
     */
    public static Card quickSlash() {
        return new Card("Quick Slash", 1, CardType.ATTACK, 3, 0, 0, 0, 1);
    }

    /**
     * Creates a moderate-cost sweeping attack card (Cost 2, Dmg 6).
     */
    public static Card cleave() {
        return new Card("Cleave", 2, CardType.ATTACK, 6);
    }

    /**
     * Creates a combo strike dealing solid damage and granting an extra card play (Cost 2, Dmg 5, +1 Extra Play).
     */
    public static Card twinStrike() {
        return new Card("Twin Strike", 2, CardType.ATTACK, 5, 0, 0, 0, 1);
    }

    /**
     * Creates a tactical attack granting temporary defense buff (Cost 2, Dmg 4, DefBuff +3 for 2 rounds).
     */
    public static Card shieldSlam() {
        return new Card("Shield Slam", 2, CardType.ATTACK, 4, 0, 3, 2, 0);
    }

    /**
     * Creates a medium-cost high-damage attack card (Cost 3, Dmg 8).
     */
    public static Card heavySlash() {
        return new Card("Heavy Slash", 3, CardType.ATTACK, 8);
    }

    /**
     * Creates an enraging strike that deals damage and boosts attack power for 2 rounds (Cost 3, Dmg 6, AtkBuff +3 for 2 rounds).
     */
    public static Card bloodlust() {
        return new Card("Bloodlust", 3, CardType.ATTACK, 6, 3, 0, 2, 0);
    }

    /**
     * Creates a rapid barrage dealing damage and granting an extra card play (Cost 3, Dmg 7, +1 Extra Play).
     */
    public static Card flurryOfStrikes() {
        return new Card("Flurry of Strikes", 3, CardType.ATTACK, 7, 0, 0, 0, 1);
    }

    /**
     * Creates a medium-cost piercing lightning attack (Cost 3, Dmg 9).
     */
    public static Card lightningBolt() {
        return new Card("Lightning Bolt", 3, CardType.ATTACK, 9);
    }

    /**
     * Creates a heavy elemental blast attack (Cost 4, Dmg 11).
     */
    public static Card pyroblast() {
        return new Card("Pyroblast", 4, CardType.ATTACK, 11);
    }

    /**
     * Creates an empowering dragon roar dealing heavy damage and granting dual buffs (Cost 4, Dmg 8, AtkBuff +3, DefBuff +3 for 2 rounds).
     */
    public static Card dragonsRoar() {
        return new Card("Dragon's Roar", 4, CardType.ATTACK, 8, 3, 3, 2, 0);
    }

    /**
     * Creates an expensive high-impact finishing attack card (Cost 5, Dmg 14).
     */
    public static Card fireball() {
        return new Card("Fireball", 5, CardType.ATTACK, 14);
    }

    /**
     * Creates a devastating endgame meteor strike attack (Cost 6, Dmg 18).
     */
    public static Card meteorStrike() {
        return new Card("Meteor Strike", 6, CardType.ATTACK, 18);
    }

    // === DEFENSE CARDS ===

    /**
     * Creates a quick parrying defense card (Cost 1, Shield 3).
     */
    public static Card deflect() {
        return new Card("Deflect", 1, CardType.DEFENSE, 3);
    }

    /**
     * Creates a low-cost defensive shield card (Cost 1, Shield 4).
     */
    public static Card guard() {
        return new Card("Guard", 1, CardType.DEFENSE, 4);
    }

    /**
     * Creates an armored fortification granting immediate shield and passive defense for 3 rounds (Cost 2, Shield 4, DefBuff +4 for 3 rounds).
     */
    public static Card ironSkin() {
        return new Card("Iron Skin", 2, CardType.DEFENSE, 4, 0, 4, 3, 0);
    }

    /**
     * Creates a solid defensive shield wall (Cost 2, Shield 7).
     */
    public static Card shieldWall() {
        return new Card("Shield Wall", 2, CardType.DEFENSE, 7);
    }

    /**
     * Creates a stance granting strong armor over 2 rounds (Cost 3, Shield 6, DefBuff +5 for 2 rounds).
     */
    public static Card stoneForm() {
        return new Card("Stone Form", 3, CardType.DEFENSE, 6, 0, 5, 2, 0);
    }

    /**
     * Creates a high-value defensive wall card (Cost 3, Shield 10).
     */
    public static Card ironWall() {
        return new Card("Iron Wall", 3, CardType.DEFENSE, 10);
    }

    /**
     * Creates a fortress-grade defensive barrier (Cost 4, Shield 14).
     */
    public static Card aegis() {
        return new Card("Aegis", 4, CardType.DEFENSE, 14);
    }

    // === UTILITY / BUFF & HEALING CARDS ===

    /**
     * Creates a fast weapon sharpening utility boosting attack for 3 rounds (Cost 1, AtkBuff +2 for 3 rounds).
     */
    public static Card sharpenBlade() {
        return new Card("Sharpen Blade", 1, CardType.UTILITY, 0, 2, 0, 3, 0);
    }

    /**
     * Creates an adrenaline boost granting attack power and allowing an extra card play (Cost 1, AtkBuff +2 for 1 round, +1 Extra Play).
     */
    public static Card adrenaline() {
        return new Card("Adrenaline", 1, CardType.UTILITY, 0, 2, 0, 1, 1);
    }

    /**
     * Creates a light emergency healing bandage (Cost 1, Heal 3).
     */
    public static Card bandage() {
        return new Card("Bandage", 1, CardType.UTILITY, 3);
    }

    /**
     * Creates a motivating battle cry boosting attack power for 3 rounds (Cost 2, AtkBuff +3 for 3 rounds).
     */
    public static Card battleCry() {
        return new Card("Battle Cry", 2, CardType.UTILITY, 0, 3, 0, 3, 0);
    }

    /**
     * Creates a defensive barrier ward providing healing and defense buff (Cost 2, Heal 4, DefBuff +3 for 3 rounds).
     */
    public static Card barrierWard() {
        return new Card("Barrier Ward", 2, CardType.UTILITY, 4, 0, 3, 3, 0);
    }

    /**
     * Creates a supportive health restoration card (Cost 2, Heal 5).
     */
    public static Card heal() {
        return new Card("Heal", 2, CardType.UTILITY, 5);
    }

    /**
     * Creates a berserk fury buff significantly boosting attack (Cost 2, AtkBuff +5 for 2 rounds).
     */
    public static Card berserkFury() {
        return new Card("Berserk Fury", 2, CardType.UTILITY, 0, 5, 0, 2, 0);
    }

    /**
     * Creates a blessed paladin aura providing heal, attack buff, and defense buff (Cost 3, Heal 5, AtkBuff +2, DefBuff +2 for 2 rounds).
     */
    public static Card paladinsBlessing() {
        return new Card("Paladin's Blessing", 3, CardType.UTILITY, 5, 2, 2, 2, 0);
    }

    /**
     * Creates a powerful divine healing spell (Cost 3, Heal 8).
     */
    public static Card holyLight() {
        return new Card("Holy Light", 3, CardType.UTILITY, 8);
    }

    /**
     * Creates a potent restoration elixir (Cost 4, Heal 12).
     */
    public static Card elixirOfLife() {
        return new Card("Elixir of Life", 4, CardType.UTILITY, 12);
    }

    // === RESOURCE CARDS ===

    /**
     * Creates a free burst resource card granting +1 mana (Cost 0, Mana +1).
     */
    public static Card manaCrystal() {
        return new Card("Mana Crystal", 0, CardType.RESOURCE, 1);
    }

    /**
     * Creates an infusion spell converting 1 mana into +2 mana burst (Cost 1, Mana +2).
     */
    public static Card manaSurge() {
        return new Card("Mana Surge", 1, CardType.RESOURCE, 2);
    }

    /**
     * Creates an instantaneous energy potion granting +2 mana (Cost 0, Mana +2).
     */
    public static Card energyPotion() {
        return new Card("Energy Potion", 0, CardType.RESOURCE, 2);
    }
}
