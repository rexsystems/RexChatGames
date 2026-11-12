---
title: RexChatGames
description: Chat games for Paper, Purpur, and Folia.
---

RexChatGames runs a chat round on a timer, or when you start one. Seven types are built in. The first correct answer wins, and the reward runs on that player's scheduler.

Messages use MiniMessage. Legacy `&` codes, `&#RRGGBB`, and `#RRGGBB` also work.

A normal chat line stays in chat. The plugin cancels a line only while a round is active and the line is an answer attempt for that round.

## Files

```
plugins/RexChatGames/
  config.yml
  words.yml
  trivia.yml
  stats.db
```

A reload that changes `config.yml` writes `config.yml.bak.<timestamp>` first, then adds any keys that were missing. Values you already set stay as they are. `words.yml` and `trivia.yml` are created only when they are missing.

## Next

- [Installation](/rexchatgames/getting-started/installation/)
- [Games](/rexchatgames/configuration/games/)
- [Commands](/rexchatgames/reference/commands/)
