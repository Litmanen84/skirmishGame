# Skirmish Arena — Vibecoding Day

## Concept

- **Attack** — deals damage to the opponent.
- **Defense** — blocks or reduces incoming damage for a number of turns.
- **Resource** — grows the caster's mana pool.
- **Utility** — draws cards, heals, or buffs a future card.

## Turn structure

Draw phase → mana phase (mana grows by 1/turn, capped at 10) → play phase (play as many cards as affordable mana allows) → resolve phase (attacks and defenses take effect) → end phase.

A match ends when a player reaches 0 HP, or after 50 turns (in which case you define your own tie-break rule. Document what you chose and why).

## Bots

Ship at least two bot strategies, e.g. *Aggressive* (always plays the highest-damage playable card) and *Defensive* (prioritizes defense/healing once HP drops below 15). Any two bots must be able to face off against each other.

## What "done" looks like

1. A `main` that runs N simulated matches (N configurable) between two chosen bot/deck pairings.
2. A full turn-by-turn, human-readable log for at least one sample match.
3. Aggregate stats across all N matches: win rate per side, average match length, average damage dealt per match.
4. A short `DESIGN.md` (a few paragraphs, not a report) noting the decisions you made where the brief was vague. Tie-break rule, exact card numbers, anything else you had to just decide.

## Ground rules for the day

- 100% vibecoding. No hand-typed or hand-edited lines of code. Every change goes in as a prompt to your coding agent. You can read the diff, revert it, and re-prompt, but you don't type code yourselves.
- Use whatever coding agent/tool your group prefers and you have access to. We will, however, provide API keys for Claude Code for each group.
- Groups of 4; pair or mob programming required. You can split into two pairs on different subsystems, or rotate who's driving the prompt with all four working together.
- Commit to git regularly. Your commit history is part of what gets looked at afterwards.
- Ambitious is fine, you are not expected to finish. How far you get, and how well what you *do* have holds up, both matter.

## Steering the agent

Prompting alone gets you started, but it doesn't scale across a whole day of building. Two files let you give your agent standing instructions instead of re-explaining yourself every prompt.

**`CLAUDE.md`** — instructions the agent reads at the start of every turn, automatically. Put project facts and rules here: architecture decisions, conventions, "always run tests before saying you're done." Create it at the root of your repo. Keep it short. Under ~30 lines is plenty for a one-day project. It is *not* a contract: the agent tries to follow it, but nothing forces it to. Treat it as steering, not guardrails, and expect to catch it slipping.

A reasonable starting point for this project:

```markdown
# Skirmish Arena

## Rules for this project
- This is a 100% vibecoding exercise — write nothing by hand, express every
  change as a prompt. Always show me the diff before I approve it.
- Plain Java, Maven, no frameworks. No UI, no web layer, no database.
- Run `mvn test` before considering any task done — don't tell me something
  works without having run it.

## Architecture
- Card categories: Attack, Defense, Resource, Utility (see DESIGN.md for our
  specific numbers)
- Turn phases: draw → mana → play → resolve → end
- Bots are pure strategy objects — no bot should touch UI/IO code

## Conventions
- Package by feature, not by layer (`engine.combat`, `engine.cards`, not
  `models` / `services`)
- Every public method needs a one-line Javadoc explaining *why*, not *what*
```

**Skills** (`.claude/skills/<name>/SKILL.md`) are a procedure the agent loads only when it's relevant, instead of every turn. Use these for something you'd otherwise paste into chat repeatedly. Once you have a runnable engine, a skill like this pays off fast:

```markdown
---
description: Runs N simulated matches between two bot/deck pairings and prints win rate, average match length, and average damage. Use when asked to test or benchmark the engine.
---

Run the simulation with:
!`mvn -q exec:java -Dexec.mainClass=com.arena.Main -Dexec.args="--matches 1000 --p1 Aggressive --p2 Defensive"`

Summarize the output: win rate per side, average match length, and average
damage per match. Flag anything that looks off (e.g. a win rate near 0% or
100%, which usually means a bug rather than a balanced matchup).
```

Two things worth doing deliberately today:

- Write "always run tests before saying you're done" into `CLAUDE.md`, then watch for the moment the agent claims success anyway without running them. That gap — between what you told it and what it actually does — is the whole point of the exercise.
- Once you have a working skeleton, ask your agent to run `/init` (Claude Code) — it reads your codebase and drafts a starting `CLAUDE.md` for you, which you then edit down rather than write from scratch.
