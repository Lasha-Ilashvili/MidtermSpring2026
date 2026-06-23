# Supported UNO Rules

This document maps the final-project rule reference to the implemented CLI
variant.

## Implemented Rules

| Reference rule | Support |
|---|---|
| Correct deck composition | Implemented as a 108-card deck: four colors, one 0 per color, two 1-9 cards per color, two Skip/Reverse/Draw Two cards per color, four Wild cards, and four Wild Draw Four cards. |
| Legal play validation | Implemented. A card may match active color, number, action type, or be exactly `W` or `W4`. Invalid wild-like codes such as `WX` are rejected. |
| Skip | Implemented. The next player loses a turn. |
| Reverse | Implemented. Direction changes with three or more players. With two players, Reverse behaves like Skip. |
| Draw Two | Implemented. The next player draws two and loses a turn. |
| Wild | Implemented. The player chooses the next active color. |
| Wild Draw Four | Implemented. The player chooses the next active color; the next player draws four and loses a turn. |
| Draw/pass behavior | Implemented. A player may draw. If the drawn card is legal, bots play it automatically and humans may choose whether to play it. Otherwise the player passes. |
| UNO call and missed-UNO penalty | Implemented. Humans append `uno` to a play, such as `3 uno` or `R5 uno`. Bots call UNO automatically. A human who reaches one card without calling UNO immediately draws two penalty cards. |
| Round scoring | Implemented. Number cards score face value, Skip/Reverse/Draw Two score 20, and Wild/Wild Draw Four score 50. |
| Multi-round target score | Implemented with `--target-score N`. Without a target, `--games N` plays exactly N rounds. With a target, `--games N` is the maximum round cap; if omitted, the cap is 100 rounds. |

## Documented Simplifications

* Wild Draw Four challenge rules are not implemented.
* Draw-card stacking is not implemented.
* Jump-in and 7-0 house rules are not implemented.
* CLI play is text-only.
* Only one human player is supported in the current CLI.
* If the starting discard is a wild card, the game redraws. Colored action
  cards may start the round, but their action effect is not applied at setup.
* The missed-UNO penalty is automatic and immediate after a human play leaves
  exactly one card.
