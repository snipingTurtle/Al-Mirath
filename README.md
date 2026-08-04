# Al-Mirath: Heirs of the Golden Age

A narrative life simulator set across four eras of Islamic history — Umayyad,
Abbasid, Mamluk, and Ottoman. You are born into a randomly generated station in
life and carry a single character from childhood to their final chronicle. Every
decision moves eight personal stats and eight faction standings, sets story
flags that later events read back, and steers you toward one of many endings.

Built with JavaFX 21 and an embedded SQLite database.

---

## Threads of Fate

Regret is a mechanic, not an accident.

Each life begins holding one **Thread of Fate**, and earns another for surviving
each life stage, up to three. When a decision goes badly, you may spend a Thread
to challenge fate itself — but the Thread is consumed whether you win or lose,
so attempting a rewind is a genuine gamble rather than a free retry.

Spending a Thread opens one of three skill challenges:

| Challenge | Skill | Rules |
|---|---|---|
| **The Scribe's Hand** | Memory | Glyphs light in sequence; repeat them. Each round adds one. A single mistake ends the attempt. |
| **The Merchant's Scales** | Arithmetic | Select coins summing to the demanded debt exactly, against a clock. Overpaying resets the pile. |
| **The Night Courier** | Timing | Stop a sweeping marker inside a lit gap. Every success narrows the gap and quickens the sweep. |

Which challenge appears is weighted by who your character has become — a scholar
is usually tested on memory, a merchant on arithmetic — and the difficulty
scales with how far into the life you are, so undoing a decision made as an
elder statesman is meaningfully harder than undoing a childhood mistake.

Win, and the run rewinds to the exact moment before the choice: stats, faction
standings, world flags, age, legacy titles, stage progress, and even life itself
if the choice killed you. The same event is presented again, fully playable.

Lose, and the decision stands.

## Other systems

- **27 achievements**, including hidden ones whose names stay masked until
  earned, with unlock notifications during play.
- **Lifetime statistics** tracked across every run — lives completed, choices
  made, titles earned, oldest age reached, best score.
- **Scoring** for completed lives, weighting longevity, earned titles, and broad
  faction standing, with penalties for stress and for rewinds used.
- **Legacy Records** archiving every completed life.
- **Keyboard play**: `1` `2` `3` to choose, `Space` to advance or skip the text
  reveal, `R` to challenge fate, `Esc` for the menu.

## Running it

Requires JDK 21 or newer.

```bash
mvn javafx:run
```

The game creates its own database on first launch. There is nothing to install
or configure.

```bash
mvn test        # run the engine test suite
mvn package     # build a jar
```

## Where your data lives

Saves, achievements, statistics, and settings are stored per-user, outside the
installation directory, so an update or reinstall never destroys progress:

| Platform | Location |
|---|---|
| Windows | `%APPDATA%\AlMirath\` |
| macOS | `~/Library/Application Support/AlMirath/` |
| Linux | `~/.local/share/AlMirath/` (respects `XDG_DATA_HOME`) |

## Project layout

```
src/main/java/com/example/al_mirath/
├── core/         AppPaths, GameSettings — platform paths and preferences
├── controller/   JavaFX screen controllers
├── dao/          JDBC persistence and schema creation
├── minigame/     Threads of Fate challenges
├── model/        Plain data: character, factions, events, choices, snapshots
├── service/      Game rules, event content, achievements, progress
└── ui/           Reusable components: trial overlay, notification toasts

src/test/java/    Engine tests covering rewind and save round-tripping
```

The engine holds no JavaFX types, so the rules are testable headlessly; the
controllers hold no game rules.

## Testing

The rewind system is the one place where a bug corrupts a run silently instead
of throwing, so it is covered directly: snapshots restoring every field, events
replaying after a rewind, the token economy refusing to go negative, defensive
copying of snapshot state, and saves written before Threads existed still
loading.

```bash
mvn test
```
