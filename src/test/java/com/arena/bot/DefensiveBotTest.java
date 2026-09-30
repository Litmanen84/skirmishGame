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
    @DisplayName("Should prioritize defense and healing when HP is at or below 15")
    void shouldPrioritizeDefenseWhenHpLow() {
        DefensiveBot bot = new DefensiveBot();
        Card strike = Card.strike();   // Cost 1, Value 3 (Attack)
        Card guard = Card.guard();     // Cost 1, Value 4 (Defense)
        Card heal = Card.heal();       // Cost 2, Value 5 (Utility)

        // HP <= 15
        PlayerView self = new PlayerView("P1", 12, 30, 3, 0, List.of(strike, guard, heal));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Defensive bot should play Heal (Cost 2) and Guard (Cost 1) before Strike
        assertThat(plays).containsExactly(heal, guard);
    }

    @Test
    @DisplayName("Should attack when HP is above 15")
    void shouldAttackWhenHpIsHigh() {
        DefensiveBot bot = new DefensiveBot();
        Card strike = Card.strike();         // Cost 1, Value 3
        Card heavySlash = Card.heavySlash(); // Cost 3, Value 8
        Card guard = Card.guard();           // Cost 1, Value 4

        // HP > 15
        PlayerView self = new PlayerView("P1", 25, 30, 4, 0, List.of(strike, guard, heavySlash));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Attacks take priority when healthy
        assertThat(plays).containsExactly(heavySlash, strike);
    }
}
