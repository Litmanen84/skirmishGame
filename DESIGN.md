# Skirmish Arena — Design & Game Rules

## Core Constants & Rules

- **Starting HP**: 30 HP per combatant.
- **Starting Shields**: Both players start with distinct random shields constrained by `|shield1 - shield2| <= 3` (e.g. if one has 4 shield, the other receives between 1 and 7 shield, excluding 4). Starting shields protect players during round 1 and decay at the start of round 2.
- **Starting Mana**: 0 Mana at turn start, increments by 1 at the beginning of each turn's Mana Phase (capped at 10 Mana). Each card costs an amount of mana strictly scaled to its strength.
- **Hand & Deck**:
  - Starting hand size: 10 cards drawn on match start to maximize tactical variety and randomization.
  - Hand size capacity: Strict maximum cap of 10 cards in hand; cards are replenished up to 10 during the Draw Phase (a bot never holds more than 10 cards).
  - Draw rate: 1 card per turn during Draw Phase (if hand size < 10).
  - Deck size: 26-card balanced and randomized decks.
  - Card recycling: Once a card is played from hand, it goes to the discard pile and cannot be replayed until the draw deck is exhausted. Upon draw exhaustion, all discarded cards are reshuffled back into the draw deck.
- **Play Phase Limits**: Standard limit of 1 card played per round, unless a card with bonus action allowance (`extraPlays > 0`) is played (e.g. *Quick Slash*, *Twin Strike*, *Adrenaline*, *Flurry of Strikes*).
- **Match Logging & Spectator Feedback**: Every played card outputs a spectator summary specification line detailing its mana cost, card type, direct damage/shield/heal value, multi-round buffs, and extra action enablers.
- **Max Turns**: 50 turns per match.

## Bot Decision Strategies

- **AggressiveBot**:
  - *Damage Maximization*: Whenever affordable attack cards are in hand, it evaluates all available attacks within the current mana budget and deterministically plays the card dealing the highest direct damage (accounting for active attack buffs and bonus play enablers).
  - *Fallback Selection*: When no direct attack is playable within current mana, it plays the most beneficial card allowed by its mana pool (resources to accelerate mana, attack buffs to empower upcoming attacks, shields for mitigation, or utility heals).
- **DefensiveBot**:
  - *Survival Priority*: When HP drops to 15 or below, it prioritizes defensive shields and healing cards before considering attacks.
  - *Normal Posture*: When HP > 15, it plays the highest-value attacks and buffs within available mana.

## Card Categories & Pool

- **Attack**: Deals direct damage to the opponent. Blocked first by active shields; excess damage reduces HP.
  - *Strike*: Cost 1, Deals 3 damage.
  - *Quick Slash*: Cost 1, Deals 3 damage & grants +1 extra card play this round.
  - *Twin Strike*: Cost 2, Deals 5 damage & grants +1 extra card play this round.
  - *Cleave*: Cost 2, Deals 6 damage.
  - *Shield Slam*: Cost 2, Deals 4 damage & grants +3 Defense buff for 2 rounds.
  - *Heavy Slash*: Cost 3, Deals 8 damage.
  - *Bloodlust*: Cost 3, Deals 6 damage & grants +3 Attack buff for 2 rounds.
  - *Flurry of Strikes*: Cost 3, Deals 7 damage & grants +1 extra card play this round.
  - *Lightning Bolt*: Cost 3, Deals 9 damage.
  - *Pyroblast*: Cost 4, Deals 11 damage.
  - *Dragon's Roar*: Cost 4, Deals 8 damage & grants +3 Attack & +3 Defense buffs for 2 rounds.
  - *Fireball*: Cost 5, Deals 14 damage.
  - *Meteor Strike*: Cost 6, Deals 18 damage.
- **Defense**: Grants defensive shield absorption mitigating incoming damage.
  - *Deflect*: Cost 1, Grants 3 shield.
  - *Guard*: Cost 1, Grants 4 shield.
  - *Iron Skin*: Cost 2, Grants 4 shield & +4 Defense buff for 3 rounds.
  - *Shield Wall*: Cost 2, Grants 7 shield.
  - *Stone Form*: Cost 3, Grants 6 shield & +5 Defense buff for 2 rounds.
  - *Iron Wall*: Cost 3, Grants 10 shield.
  - *Aegis*: Cost 4, Grants 14 shield.
- **Resource**: Accelerates temporary mana pool.
  - *Mana Crystal*: Cost 0, Grants +1 mana.
  - *Mana Surge*: Cost 1, Grants +2 mana.
  - *Energy Potion*: Cost 0, Grants +2 mana.
- **Utility / Buffs & Healing**: Restores health points or activates multi-round enhancements.
  - *Sharpen Blade*: Cost 1, Grants +2 Attack buff for 3 rounds.
  - *Adrenaline*: Cost 1, Grants +2 Attack buff for 1 round & grants +1 extra card play this round.
  - *Bandage*: Cost 1, Restores 3 HP.
  - *Battle Cry*: Cost 2, Grants +3 Attack buff for 3 rounds.
  - *Barrier Ward*: Cost 2, Restores 4 HP & grants +3 Defense buff for 3 rounds.
  - *Heal*: Cost 2, Restores 5 HP.
  - *Berserk Fury*: Cost 2, Grants +5 Attack buff for 2 rounds.
  - *Paladin's Blessing*: Cost 3, Restores 5 HP & grants +2 Attack & +2 Defense buffs for 2 rounds.
  - *Holy Light*: Cost 3, Restores 8 HP.
  - *Elixir of Life*: Cost 4, Restores 12 HP.

## Multi-Round Buff Mechanics

- **Attack Buff (+X Attack for N rounds)**: Amplifies the damage of all Attack cards cast by the player by +X while active.
- **Defense Buff (+X Defense for N rounds)**: Automatically generates +X Shield points at the beginning of the player's turn for N rounds.
- **Duration & Expiration**: Buff durations decrement at the conclusion of the player's turn; once the round counter reaches 0, the active enhancement cleanly expires.

## Turn Lifecycle

Each turn progresses strictly through 5 phases:
1. **Draw Phase**: Active player draws 1 card from their deck.
2. **Mana Phase**: Active player's mana pool increases by 1 (up to the max cap of 10), and available mana is refreshed to maximum.
3. **Play Phase**: Active bot selects and plays valid cards within affordable mana limits.
4. **Resolve Phase**: Played effects are resolved (damage against shields/HP, heals applied, mana modifications).
5. **End Phase**: Lingering shield buffs decay, expired effects are cleared, and unplayed cards remain in hand.

## Termination & Tie-Break Policy

1. A match terminates immediately if any player reaches 0 HP.
2. If neither player reaches 0 HP after 50 complete turns:
   - **Primary Tie-Breaker**: Player with the higher remaining HP wins.
   - **Secondary Tie-Breaker**: Player with the higher cumulative damage dealt throughout the match wins.
   - **Draw**: If remaining HP and total damage dealt are identical, the match is recorded as a Tie.

## Simulation CLI & Victory History Tracking

- **Interactive Match Selection**: When executed without explicit CLI parameters, the simulator greets the user, displays past historical records, and prompts for the desired match count.
- **Victory History Persistence**: Each simulation run (single or batch) appends session statistics (timestamp, bot matchup, match count, victory counts, draws, average turns, and average damage) to `arena_victory_history.csv`.
- **Cumulative Record of Victories**: After each execution, an updated terminal table presents cumulative lifetime match counts, overall win totals, and percentage distributions across bot strategies.
