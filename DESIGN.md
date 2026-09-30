# Skirmish Arena — Design & Game Rules

## Core Constants & Rules

- **Starting HP**: 30 HP per player.
- **Starting Mana**: 0 Mana at turn start, increments by 1 at the beginning of each turn's Mana Phase (capped at 10 Mana).
- **Hand & Deck**:
  - Starting hand size: 4 cards.
  - Draw rate: 1 card per turn during Draw Phase.
  - Deck size: 20 cards standard deck. When a deck is depleted during a draw, the discard pile is reshuffled into the draw deck.
- **Max Turns**: 50 turns per match.

## Card Categories & Numbers

- **Attack**: Deals direct damage to the opponent. Blocked first by any active defense shield; excess damage reduces player HP.
  - *Strike*: Cost 1, Deals 3 damage.
  - *Heavy Slash*: Cost 3, Deals 8 damage.
  - *Fireball*: Cost 5, Deals 14 damage.
- **Defense**: Grants a shield that mitigates incoming damage. Lasts for 1 turn cycle.
  - *Guard*: Cost 1, Grants 4 shield.
  - *Iron Wall*: Cost 3, Grants 10 shield.
- **Resource**: Accelerates mana progression.
  - *Mana Crystal*: Cost 0, Grants +1 temporary mana this turn.
  - *Harvest*: Cost 2, Permanently increases mana growth rate or refunds 3 mana.
- **Utility**: Card draw and healing support.
  - *Heal*: Cost 2, Restores 5 HP.
  - *Meditate*: Cost 2, Draws 2 cards.

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
