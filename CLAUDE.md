# Skirmish Arena

## Rules for this project
- This is a 100% vibecoding exercise — write nothing by hand, express every change as a prompt. Always show me the diff before I approve it.
- Plain Java, Maven, no frameworks. No UI, no web layer, no database.
- Run `mvn test` before considering any task done — don't tell me something works without having run it.

## Architecture
- Card categories: Attack, Defense, Resource, Utility (see DESIGN.md for our specific numbers)
- Turn phases: draw → mana → play → resolve → end
- Bots are pure strategy objects — no bot should touch UI/IO code

## Conventions
- Package by feature, not by layer (`com.arena.combat`, `com.arena.cards`, `com.arena.bot`, `com.arena.simulation`)
- Every public method needs a one-line Javadoc explaining *why*, not *what*
