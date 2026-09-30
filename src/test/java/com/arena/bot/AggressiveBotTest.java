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
    @DisplayName("Should prioritize highest-damage attack card within mana budget under standard 1-card limit")
    void shouldPrioritizeHighestDamageCards() {
        AggressiveBot bot = new AggressiveBot();
        Card strike = Card.strike();         // Cost 1, Value 3
        Card heavySlash = Card.heavySlash(); // Cost 3, Value 8
        Card fireball = Card.fireball();     // Cost 5, Value 14
        Card guard = Card.guard();           // Cost 1, Value 4

        PlayerView self = new PlayerView("P1", 30, 30, 4, 0, List.of(strike, guard, heavySlash, fireball));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Under 1-card limit with 4 mana, bot plays the strongest affordable single card: Heavy Slash (cost 3, value 8)
        assertThat(plays).containsExactly(heavySlash);
    }

    @Test
    @DisplayName("Should allow multiple card plays when combo card granting extra play is used")
    void shouldChainPlaysWhenExtraPlayCardUsed() {
        AggressiveBot bot = new AggressiveBot();
        Card quickSlash = Card.quickSlash(); // Cost 1, Value 3, +1 Extra Play
        Card heavySlash = Card.heavySlash(); // Cost 3, Value 8
        Card guard = Card.guard();           // Cost 1, Value 4

        PlayerView self = new PlayerView("P1", 30, 30, 4, 0, List.of(quickSlash, guard, heavySlash));
        PlayerView opp = new PlayerView("P2", 30, 30, 0, 0, List.of());

        List<Card> plays = bot.selectPlays(self, opp);

        // Quick Slash grants +1 extra play, allowing subsequent Heavy Slash within 4 mana
        assertThat(plays).containsExactly(quickSlash, heavySlash);
    }
}
