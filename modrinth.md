# RexChatGames

Chat games for Paper, Purpur, and Folia. Seven round types run on a timer, or you start one with `/rexchatgames start`. The first correct answer wins. Later answers are not paid.

**Java 21. Paper, Purpur, or Folia 1.21+.** PlaceholderAPI is optional.

## Features

- Complete, guess-number, math, trivia, type, reverse, and unscramble
- Unscramble scrambles each word on its own, so spaces stay where they are
- Math stays on whole numbers and does not divide by zero
- Words from `words.yml`, plus names taken from the server's own material list
- Trivia questions in `trivia.yml`. An existing file is left alone
- Rewards are commands and items, given on the winner's player scheduler
- A winner who leaves before the reward is given ends the round with no payout
- `/rexchatgames toggle` hides messages and sounds for that player only
- A chat line is removed only when a round is running and the line is an answer for that round
- SQLite stores wins per type, the fastest time, and a short log
- `/rexchatgames top` and `/rexchatgames status`
- MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB` in player-facing text
- `%rexchatgames_wins%` when PlaceholderAPI is installed

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rexchatgames help` | Show help | everyone |
| `/rexchatgames start [type]` | Start a round now | `rexchatgames.start` |
| `/rexchatgames stop` | Stop the current round | `rexchatgames.start` |
| `/rexchatgames toggle` | Hide or show game messages and sounds | `rexchatgames.toggle` |
| `/rexchatgames top [type]` | Top wins. A type also shows the fastest time | everyone |
| `/rexchatgames status` | Last time each game started | everyone |
| `/rexchatgames reload` | Reload `config.yml`, `words.yml`, and `trivia.yml` | `rexchatgames.reload` |

Aliases: `/rcg`, `/chatgames`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `rexchatgames.play` | true | Answer a running round |
| `rexchatgames.toggle` | true | Hide or show game messages and sounds |
| `rexchatgames.start` | op | Start or stop a round |
| `rexchatgames.reload` | op | Reload the configuration |
| `rexchatgames.admin` | op | Use the admin commands |

## Placeholders

| Placeholder | Description |
|---|---|
| `%rexchatgames_wins%` | That player's total wins |
| `%rexchatgames_wins_<type>%` | Wins for one type, such as `%rexchatgames_wins_math%` |
| `%rexchatgames_fastest_<type>%` | That player's best time for the type |
| `%rexchatgames_record_<type>%` | Server record for the type |

Type ids: `complete`, `guess-number`, `math`, `trivia`, `type`, `reverse`, `unscramble`.

Documentation: https://docs.rexsystems.me/rexchatgames/
