package com.arena.combat;

import com.arena.bot.BotStrategy;
import com.arena.cards.Card;
import com.arena.cards.CardType;
import com.arena.cards.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchEngineTest {

    @Test
    @DisplayName("PlayerState initializes with expected HP and empty hand view")
    void shouldInitializePlayerState() {
        Card strike = new Card("Strike", 1, CardType.ATTACK, 3);
        Deck deck = new Deck(List.of(strike));
        PlayerState playerState = new PlayerState("P1", 30, deck);

        PlayerView view = playerState.toView();

        assertThat(view.playerId()).isEqualTo("P1");
        assertThat(view.hp()).isEqualTo(30);
        assertThat(view.maxHp()).isEqualTo(30);
        assertThat(view.mana()).isEqualTo(0);
        assertThat(view.shield()).isEqualTo(0);
        assertThat(view.handCards()).isEmpty();
    }

    @Test
    @DisplayName("Should execute match and declare winner when opponent HP reaches 0")
    void shouldDeclareWinnerOnKnockout() {
        // Player 1 has Fireballs (Cost 5, 14 Dmg) and Strikes (Cost 1, 3 Dmg)
        List<Card> p1Cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            p1Cards.add(Card.fireball());
        }
        PlayerState p1 = new PlayerState("P1", 30, new Deck(p1Cards));
        PlayerState p2 = new PlayerState("P2", 20, new Deck(List.of(Card.strike())));

        BotStrategy attackAll = (self, opp) -> {
            List<Card> plays = new ArrayList<>();
            int availableMana = self.mana();
            for (Card c : self.handCards()) {
                if (c.cost() <= availableMana) {
                    plays.add(c);
                    availableMana -= c.cost();
                }
            }
            return plays;
        };

        BotStrategy passive = (self, opp) -> List.of();

        MatchEngine engine = new MatchEngine(p1, attackAll, p2, passive);
        MatchResult result = engine.playMatch();

        assertThat(result.isTie()).isFalse();
        assertThat(result.winnerId()).isEqualTo("P1");
        assertThat(p2.getCurrentHp()).isEqualTo(0);
        assertThat(result.events()).isNotEmpty();
    }

    @Test
    @DisplayName("Should resolve tie-break when 50 turns elapse")
    void shouldResolveTieBreakAtTurn50() {
        PlayerState p1 = new PlayerState("P1", 30, new Deck(List.of()));
        PlayerState p2 = new PlayerState("P2", 25, new Deck(List.of()));

        BotStrategy passive = (self, opp) -> List.of();

        MatchEngine engine = new MatchEngine(p1, passive, p2, passive);
        MatchResult result = engine.playMatch();

        assertThat(result.totalTurns()).isEqualTo(50);
        assertThat(result.isTie()).isFalse();
        assertThat(result.winnerId()).isEqualTo("P1"); // P1 has 30 HP vs 25 HP
    }
}
