# RexChatGames

Seven chat games for Paper, Purpur, and Folia. A round starts on a timer or from `/rexchatgames start`. The first correct answer is the only one that gets paid.

<p>
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-62B47A?style=for-the-badge" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
</p>

## Features

- Complete, guess-number, math, trivia, type, reverse, and unscramble
- Unscramble keeps spaces as word boundaries: `Jungle Sapling` scrambles each word alone
- Math uses whole numbers and never divides by zero
- Word list in `words.yml`, plus material names read from the server at startup
- Trivia in `trivia.yml`, created only when that file is missing
- Per-game rewards as console commands and items, paid on the winner's player scheduler
- If the winner leaves before that runs, nobody else is paid
- `/rexchatgames toggle` hides messages and sounds for that player only
- Chat is cancelled only while a round is active and the line is an answer attempt
- Wins, fastest times, and a short history in SQLite
- MiniMessage, legacy `&` codes, `&#RRGGBB`, and `#RRGGBB`
- PlaceholderAPI, including `%rexchatgames_wins%`

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- PlaceholderAPI is optional

## Install

1. Put the jar in `plugins/`
2. Restart
3. Edit `plugins/RexChatGames/config.yml`
4. Run `/rexchatgames reload`

`words.yml` and `trivia.yml` stay as they are if they already exist.

## Commands

| Command | Permission |
|---|---|
| `/rexchatgames help` | everyone |
| `/rexchatgames start [type]` | `rexchatgames.start` |
| `/rexchatgames stop` | `rexchatgames.start` |
| `/rexchatgames toggle` | `rexchatgames.toggle` |
| `/rexchatgames top [type]` | everyone |
| `/rexchatgames status` | everyone |
| `/rexchatgames reload` | `rexchatgames.reload` |

Aliases: `/rcg`, `/chatgames`

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexchatgames/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: [https://rexsystems.me/docs/rexchatgames](https://rexsystems.me/docs/rexchatgames)
