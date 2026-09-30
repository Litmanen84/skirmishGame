package com.arena.cards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckTest {

    @Test
    @DisplayName("Should initialize deck with correct card count")
    void shouldInitializeDeckWithCorrectCardCount() {
        Card strike = new Card("Strike", 1, CardType.ATTACK, 3);
        Deck deck = new Deck(List.of(strike, strike, strike));

        assertThat(deck.size()).isEqualTo(3);
    }
}
