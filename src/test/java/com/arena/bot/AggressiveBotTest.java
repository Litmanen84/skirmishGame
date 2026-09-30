package com.arena.bot;

import com.arena.cards.Card;
import com.arena.combat.PlayerView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggressiveBotTest {

    @Test
    @DisplayName("Should provide correct bot strategy name")
    void shouldProvideStrategyName() {
        AggressiveBot bot = new AggressiveBot();
        assertThat(bot.getName()).isEqualTo("AggressiveBot");
    }

    @Test
    @DisplayName("Should prioritize highest-damage attack cards within mana budget")
    void shouldPrioritizeHighestDamageCards() {
        AggressiveBot bot = new AggressiveBot();
        Card strike = Card.strike();         // Cost 1, Value 3
        Card heavySlash = Card.heavySlash(); // Cost 3, Value 8
        Card fireball = Card.fireball();     // Cost 5, Value 14
        Card guard = Card.guard();           // Cost 1, Value 4

        PlayerView self = new PlayerView("P1", 30, 30, 4, 0, List.of(strike, guard, heavySlash, fireball));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // With 4 mana, bot should play Heavy Slash (cost 3, value 8) then Strike (cost 1, value 3)
        assertThat(plays).containsExactly(heavySlash, strike);
    }
}
