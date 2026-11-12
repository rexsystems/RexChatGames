---
title: Placeholders
description: PlaceholderAPI tokens from RexChatGames.
sidebar:
  order: 3
---

These register when PlaceholderAPI is installed at enable. Reload does not unregister them.

| Placeholder | Description |
|---|---|
| `%rexchatgames_wins%` | Total wins for that player |
| `%rexchatgames_wins_<type>%` | Wins for one type |
| `%rexchatgames_fastest_<type>%` | That player's best time for the type, such as `1.20s` |
| `%rexchatgames_record_<type>%` | Fastest time on the server for the type |

Examples: `%rexchatgames_wins_math%`, `%rexchatgames_wins_guess-number%`, `%rexchatgames_record_unscramble%`.

An unknown type is left unparsed. A known type with no row is `0` for wins and empty for a time.

Times are kept in `stats.db`. They survive a restart. A player with no wins yet has `%rexchatgames_wins%` of `0`.

Inside `config.yml`, reward commands use `{player}` and `{uuid}`. Chat templates use `{player}`, `{prompt}`, `{answer}`, `{time}`, `{type}`, and `{type-label}`. Those braces are not PlaceholderAPI.
