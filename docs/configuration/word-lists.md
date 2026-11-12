---
title: Word lists
description: words.yml, trivia.yml, and material names.
sidebar:
  order: 3
---

## words.yml

```yaml
words:
  - Jungle Sapling
  - Ender Pearl
```

`complete`, `type`, `reverse`, and `unscramble` draw from this list. Blank lines are skipped. The same phrase twice, ignoring capitals, is kept once, in the form it first appeared.

On startup, and again on reload, RexChatGames also adds names from `org.bukkit.Material` on that server. `JUNGLE_SAPLING` becomes `Jungle Sapling` if that material exists. Air and names that start with `LEGACY_` are skipped. This is the server's own enum, not a downloaded name dump.

Those material names are not written back into `words.yml`.

`Jungle Sapling` is scrambled as two words. The letters of `Jungle` stay in the first word, and the letters of `Sapling` stay in the second. The space between them stays a space.

## trivia.yml

```yaml
questions:
  - question: "Which mob drops a blaze rod?"
    answers:
      - "blaze"
```

`question` is plain text. `answers` is the list of lines that win. A single `answer` string is also read. A question with no answers is skipped.

The player has to send one of those lines by itself. `a blaze` does not match `blaze` unless you add it.

## Files that already exist

`words.yml` and `trivia.yml` are copied out of the jar only when the file is not there. Reload reads them again. It does not overwrite them, and `config.yml` auto-update does not touch them.

Delete a file and reload if you want the jar's copy back. That replaces your edits, so keep a backup if the file has your own lines.
