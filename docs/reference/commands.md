---
title: Commands
description: RexChatGames commands and who can run them.
sidebar:
  order: 1
---

| Command | Description | Permission |
|---|---|---|
| `/rexchatgames` | Show help | everyone |
| `/rexchatgames help` | Show help | everyone |
| `/rexchatgames start [type]` | Start a round. No type uses the weights | `rexchatgames.start` |
| `/rexchatgames stop` | Stop the round with no reward and no answer | `rexchatgames.start` |
| `/rexchatgames toggle` | Hide or show game messages and sounds for you | `rexchatgames.toggle` |
| `/rexchatgames top [type]` | Top 10 wins. A type also prints the fastest time | everyone |
| `/rexchatgames status` | Last time each type started | everyone |
| `/rexchatgames reload` | Reload configuration, words, and trivia | `rexchatgames.reload` |

Aliases: `/rcg`, `/chatgames`

`rexchatgames.admin` can run start, stop, toggle, and reload as well.

Types for `start` and `top`: `complete`, `guess-number`, `math`, `trivia`, `type`, `reverse`, `unscramble`. Tab completion lists them.

`/rexchatgames toggle` is stored in `stats.db`, so it survives a restart. Replies to your own guess still show. Scheduled announcements, the title, the action bar, and sounds do not.

Help text comes from `messages.help-header` and `messages.help`.
