---
title: Permissions
description: RexChatGames permission nodes.
sidebar:
  order: 2
---

| Permission | Default | Description |
|---|---|---|
| `rexchatgames.play` | true | Have a chat line checked as an answer |
| `rexchatgames.toggle` | true | Run `/rexchatgames toggle` |
| `rexchatgames.start` | op | Start or stop a round |
| `rexchatgames.reload` | op | Reload the plugin files |
| `rexchatgames.admin` | op | Start, stop, toggle, and reload |

Players without `rexchatgames.play` can still read the round. Their chat is not cancelled and cannot win.

A player in `worlds.disabled` is treated the same way, even if they have `rexchatgames.play`.

`top`, `status`, and `help` have no permission node.
