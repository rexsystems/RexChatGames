---
title: Installation
description: Put RexChatGames on a Paper, Purpur, or Folia server.
sidebar:
  order: 1
---

1. Download the jar.
2. Put it in the server `plugins/` folder.
3. Start the server once so `plugins/RexChatGames/` is written.
4. Edit `config.yml`. Add your own lines to `words.yml` and `trivia.yml` if you want.
5. Run `/rexchatgames reload`, or restart.

PlaceholderAPI is not required. If it is installed when RexChatGames enables, the `%rexchatgames_...%` placeholders register on their own.

The first automatic round waits one full `settings.interval-seconds` after enable, so the server can finish starting.
