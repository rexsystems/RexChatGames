---
title: Games
description: The seven chat games, their prompts, and rewards.
sidebar:
  order: 2
---

Each block under `games` has `enabled`, `weight`, `prompt`, `hover`, and `rewards`. Automatic rounds pick among enabled games with a weight above 0. `/rexchatgames start <type>` ignores weight and still requires `enabled: true`.

Type ids:

| Id | What the player does |
|---|---|
| `complete` | Type the full phrase. Some letters are replaced with `_`. |
| `guess-number` | Type a whole number from `min` to `max`, both ends included. |
| `math` | Type the result of the sum, difference, product, or quotient. |
| `trivia` | Type one accepted answer from `trivia.yml`, and nothing else. |
| `type` | Type the phrase exactly. |
| `reverse` | The phrase is shown backwards. Type it forwards. |
| `unscramble` | Letters are shuffled inside each word. Spaces stay put. |

## complete

`hide-percent` is 0 to 100. The mask hides about that share of the letters and always leaves at least one letter visible. Spaces and other characters stay as they are.

Prompt token: `{masked}`.

## guess-number

`min` and `max` are inclusive. If you swap them, the plugin still uses the lower number as the start of the range.

A chat line counts as a guess only when the whole line is an integer, such as `42` or `-3`. `I pick 42` stays in public chat and is not a guess.

Prompt tokens: `{min}`, `{max}`.

## math

`operations` accepts `add`, `subtract`, `multiply`, and `divide`. An empty list means addition only.

Division picks a divisor that is not zero. The question is built so the result is a whole number. If the configured range is only `0`, the divisor is `1` rather than `0`.

Products, sums, and differences that would overflow an integer are reduced so the printed sum still matches the answer.

Prompt token: `{expression}`.

## trivia, type, reverse, unscramble

Text answers use `settings.case-sensitive`. With case sensitivity on, the right letters and the wrong capitals are not a win. `settings.warn-on-capitalization` tells that player, and the round stays open.

A text line is an answer attempt only when it matches an accepted answer, ignoring capitals. Anything else stays in chat.

Prompt tokens: `{question}`, `{word}`, `{reversed}`, `{scrambled}`.

## Rewards

```yaml
rewards:
  commands:
    - "minecraft:give {player} diamond 1"
  items:
    - material: DIAMOND
      amount: 1
      name: "<gold>Round winner"
```

`{player}` is the account name. `{uuid}` is also replaced in commands. The command runs as console, without a leading slash.

Item `name` is MiniMessage. Amounts are clamped between 1 and 576. Stacks that do not fit drop at the winner's feet.

The reward runs on the winner's player scheduler. The first correct answer is the only claim. If that player is gone before the task runs, the round ends and nobody else is paid.

A timeout reveals `messages.timeout` and pays nobody. `/rexchatgames stop` ends the round without revealing the answer and without paying.
