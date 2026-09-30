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

    @Test
    @DisplayName("Should apply attack buff bonus to subsequent attacks and generate combat events")
    void shouldAmplifyDamageWithAttackBuff() {
        // P1 has Battle Cry (Cost 2, AtkBuff +3 for 3 rounds) and Strike (Cost 1, Dmg 3)
        List<Card> p1Cards = List.of(Card.battleCry(), Card.strike(), Card.strike(), Card.strike());
        PlayerState p1 = new PlayerState("P1", 30, new Deck(p1Cards));
        PlayerState p2 = new PlayerState("P2", 30, new Deck(List.of()));

        BotStrategy buffAndAttack = (self, opp) -> {
            List<Card> plays = new ArrayList<>();
            int mana = self.mana();
            for (Card c : self.handCards()) {
                if (c.cost() <= mana) {
                    plays.add(c);
                    mana -= c.cost();
                }
            }
            return plays;
        };
        BotStrategy passive = (self, opp) -> List.of();

        MatchEngine engine = new MatchEngine(p1, buffAndAttack, p2, passive);
        MatchResult result = engine.playMatch();

        // Check that events record buff activation and enhanced attack damage
        boolean foundBuffEvent = result.events().stream()
                .anyMatch(e -> e.description().contains("activates buff -> +3 Attack"));
        assertThat(foundBuffEvent).isTrue();

        boolean foundEnhancedAttack = result.events().stream()
                .anyMatch(e -> e.description().contains("casts Strike for 3 (+3 Atk Buff = 6 dmg)"));
        assertThat(foundEnhancedAttack).isTrue();
    }

    @Test
    @DisplayName("Should trigger defense buff at turn start and mitigate damage")
    void shouldTriggerDefenseBuffAtTurnStart() {
        // P1 has Iron Skin (Cost 2, Shield 4, DefBuff +4 for 3 rounds)
        List<Card> p1Cards = List.of(Card.ironSkin(), Card.guard(), Card.guard(), Card.guard());
        PlayerState p1 = new PlayerState("P1", 30, new Deck(p1Cards));
        PlayerState p2 = new PlayerState("P2", 30, new Deck(List.of(Card.strike())));

        BotStrategy playAll = (self, opp) -> {
            List<Card> plays = new ArrayList<>();
            int mana = self.mana();
            for (Card c : self.handCards()) {
                if (c.cost() <= mana) {
                    plays.add(c);
                    mana -= c.cost();
                }
            }
            return plays;
        };

        MatchEngine engine = new MatchEngine(p1, playAll, p2, playAll);
        MatchResult result = engine.playMatch();

        boolean foundDefBuffTrigger = result.events().stream()
                .anyMatch(e -> e.description().contains("defense buff triggers -> +4 Shield"));
        assertThat(foundDefBuffTrigger).isTrue();
    }
}
