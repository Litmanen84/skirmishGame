package com.arena.bot;

import com.arena.cards.Card;
import com.arena.combat.PlayerView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefensiveBotTest {

    @Test
    @DisplayName("Should provide correct bot strategy name")
    void shouldProvideStrategyName() {
        DefensiveBot bot = new DefensiveBot();
        assertThat(bot.getName()).isEqualTo("DefensiveBot");
    }

    @Test
    @DisplayName("Should prioritize defense and healing when HP is at or below 15 under 1-card limit")
    void shouldPrioritizeDefenseWhenHpLow() {
        DefensiveBot bot = new DefensiveBot();
        Card strike = Card.strike();   // Cost 1, Value 3 (Attack)
        Card guard = Card.guard();     // Cost 1, Value 4 (Defense)
        Card heal = Card.heal();       // Cost 2, Value 5 (Utility)

        // HP <= 15
        PlayerView self = new PlayerView("P1", 12, 30, 3, 0, List.of(strike, guard, heal));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Defensive bot plays the highest-value restorative card: Heal (Cost 2, Value 5)
        assertThat(plays).containsExactly(heal);
    }

    @Test
    @DisplayName("Should attack when HP is above 15 under 1-card limit")
    void shouldAttackWhenHpIsHigh() {
        DefensiveBot bot = new DefensiveBot();
        Card strike = Card.strike();         // Cost 1, Value 3
        Card heavySlash = Card.heavySlash(); // Cost 3, Value 8
        Card guard = Card.guard();           // Cost 1, Value 4

        // HP > 15
        PlayerView self = new PlayerView("P1", 25, 30, 4, 0, List.of(strike, guard, heavySlash));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Highest damage attack card takes priority when healthy
        assertThat(plays).containsExactly(heavySlash);
    }

    @Test
    @DisplayName("Should chain extra play cards before resolving subsequent actions")
    void shouldChainExtraPlayCardsWhenDefending() {
        DefensiveBot bot = new DefensiveBot();
        Card quickSlash = Card.quickSlash(); // Cost 1, Value 3, +1 Extra Play
        Card heal = Card.heal();             // Cost 2, Value 5

        PlayerView self = new PlayerView("P1", 10, 30, 3, 0, List.of(quickSlash, heal));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Quick Slash grants extra play, allowing both Quick Slash and Heal to be played with 3 mana
        assertThat(plays).containsExactly(quickSlash, heal);
    }
}
