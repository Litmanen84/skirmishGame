package com.arena.combat;

import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.cards.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerStateTest {

    @Test
    @DisplayName("Should initialize player state with standard values")
    void shouldInitializePlayerState() {
        PlayerState player = new PlayerState("P1", 30, new Deck(List.of()));
        assertThat(player.getId()).isEqualTo("P1");
        assertThat(player.getCurrentHp()).isEqualTo(30);
        assertThat(player.getMaxHp()).isEqualTo(30);
        assertThat(player.getCurrentMana()).isEqualTo(0);
        assertThat(player.getActiveShield()).isEqualTo(0);
        assertThat(player.isAlive()).isTrue();
    }

    @Test
    @DisplayName("Should increment and cap mana on turn start")
    void shouldGrowAndCapMana() {
        PlayerState player = new PlayerState("P1", 30, new Deck(List.of()));
        for (int i = 1; i <= 12; i++) {
            player.startTurnMana();
            int expected = Math.min(i, 10);
            assertThat(player.getCurrentMana()).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("Should absorb incoming damage with shield before reducing HP")
    void shouldMitigateDamageWithShield() {
        PlayerState player = new PlayerState("P1", 30, new Deck(List.of()));
        player.gainShield(5);

        int damageToHp = player.takeDamage(8);
        assertThat(damageToHp).isEqualTo(3);
        assertThat(player.getCurrentHp()).isEqualTo(27);
        assertThat(player.getActiveShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reshuffle discard pile when deck is exhausted during draw")
    void shouldReshuffleDiscardPileWhenDeckEmpty() {
        Card strike = Card.strike();
        List<Card> initialCards = new ArrayList<>(List.of(strike));
        PlayerState player = new PlayerState("P1", 30, new Deck(initialCards));

        // Draw the only card
        assertThat(player.drawCard()).contains(strike);
        player.startTurnMana();
        player.playCard(strike);

        // Discard pile now has the card, deck is empty
        assertThat(player.drawCard()).contains(strike);
        assertThat(player.getHand()).hasSize(1);
    }
}
