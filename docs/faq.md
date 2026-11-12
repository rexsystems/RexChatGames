---
title: FAQ
description: Rounds, cancelled chat, rewards, and files.
sidebar:
  order: 1
---

## A normal sentence disappeared from chat

The plugin cancels chat only when a round is active and the line is an answer attempt.

For complete, trivia, type, reverse, and unscramble, that means the line matches an answer, ignoring capitals. Other lines stay.

For guess-number and math, the whole line has to be an integer. `12` is an attempt, including a wrong number. `see you at 12` is not, and it stays in chat.

Players without `rexchatgames.play`, and players in `worlds.disabled`, are never cancelled.

## The second correct answer got nothing

The first claim wins. The next correct line is told that someone else was first, and it is not paid. Two answers in the same moment still produce one winner.

## The winner left and nobody got the prize

Rewards run on the winner's player scheduler. If that player is gone before the task runs, the round closes. The plugin does not pick a new winner for that round.

## I toggled games off and still won

`/rexchatgames toggle` hides announcements, the title, the action bar, and sounds. It does not take away `rexchatgames.play`. A correct answer still pays. Your own wrong-answer reply still shows, so a cancelled guess is not silent.

## Unscramble mixed two words together

It should not. `Jungle Sapling` is scrambled as `Jungle` and `Sapling` separately. The space stays between them. If a phrase in `words.yml` uses a character other than a normal space, that character is not a boundary.

## Can a math round divide by zero?

No. A divisor is chosen from the range, skipping `0`. The dividend is a multiple of that divisor, so the answer is a whole number. A range of only `0` uses `1` as the divisor.

## words.yml did not change after reload

That is intentional. Reload reads the file you have. It does not copy the jar over it, and config auto-update does not write it. The same rule applies to `trivia.yml`. New material names appear in memory only.

## A round never starts

Check `settings.min-players`, `worlds.disabled`, and that at least one game is `enabled` with a `weight` above 0. Word games need a non-empty word list. Trivia needs one question with an answer. The automatic timer waits a full interval after enable or reload.

## `%rexchatgames_wins%` is blank

PlaceholderAPI has to be on the server when RexChatGames enables. The token is per player. A player who has never won shows `0`, not a blank, once the expansion is registered.

## Status says never

`/rexchatgames status` stores the moment a type started, in `stats.db`. A type that has not started since the database was created says never. Wins are separate and show under `/rexchatgames top`.
