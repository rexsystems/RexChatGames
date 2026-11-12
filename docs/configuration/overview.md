---
title: Configuration
description: Timers, worlds, sounds, and the start message in config.yml.
sidebar:
  order: 1
---

`config.yml` holds timers, feedback, and each game. `config-version` is internal. Leave it alone.

`words.yml` and `trivia.yml` are not merged and not replaced when they already exist. See [Word lists](/rexchatgames/configuration/word-lists/).

## settings

| Key | What it does |
|---|---|
| `interval-seconds` | Gap between automatic rounds. Values under 10 are raised to 10. |
| `timeout-seconds` | How long a round stays open. Values under 15 are raised to 15. |
| `min-players` | Players with `rexchatgames.play` outside disabled worlds. Manual start uses the same check. |
| `case-sensitive` | When false, `Creeper` and `creeper` both count. |
| `warn-on-capitalization` | When `case-sensitive` is true, a right word with the wrong capitals gets a warning instead of a win. |

Players who used `/rexchatgames toggle` still count toward `min-players`. They do not see the round.

## worlds

`worlds.disabled` is a deny list. Players in those worlds do not see rounds, and their chat is not treated as an answer. An empty list means every world.

## sounds, title, actionbar

`sounds.start` plays when a round opens. `sounds.win` plays when someone wins. Both go only to players who can see the round.

Sound names are Bukkit names, such as `BLOCK_NOTE_BLOCK_PLING`. An unknown name is logged and that sound is skipped.

Title times are ticks. 20 ticks is one second. Each time is clamped between 0 and 200.

The action bar uses `actionbar.text` for the same moment as the title. `{prompt}` is the question.

## Start message

`messages.start` is the chat line. The default wraps it in a MiniMessage hover:

```
<hover:show_text:"{hover}">...</hover>
```

`{hover}` comes from that game's `hover` field. It is plain text, so write the hint there and keep the color in `messages.start`.

Other tokens on broadcast lines: `{prefix}`, `{type}`, `{type-label}`, `{prompt}`, `{answer}`, `{player}`, `{time}`, `{min}`, `{max}`.

`{player}`, `{prompt}`, `{answer}`, and `{hover}` are inserted as plain text. Color belongs in the template.
