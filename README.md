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

## Playing it

You do not need Java, Maven, or an IDE to play — but somebody has to build a
package for your platform first, on that platform. There are no prebuilt
downloads yet.

### If you have a package

| Platform | What you get | What you do |
|---|---|---|
| Windows | `AlMirath-1.0.exe` installer | Run it, then launch **Al-Mirath** from the Start menu |
| macOS | `AlMirath-1.0.dmg` | Open it, drag the app to Applications, launch it |
| Linux | `almirath_1.0_amd64.deb` | `sudo apt install ./almirath_1.0_amd64.deb`, then launch it from your applications menu |

The package carries its own Java runtime, so nothing else has to be installed
and nothing else on the machine is touched. It is around 250 MB installed,
almost all of it artwork.

There is also a portable form that needs no installer: the `AlMirath` folder
produced by the `app-image` build below. Copy it anywhere and run
`AlMirath/bin/AlMirath` (`AlMirath\AlMirath.exe` on Windows).

### Building a package

Needs **JDK 21 or newer** (`jpackage` ships with it). Build on the platform you
are building *for* — a Linux build will not run on Windows.

```bash
./mvnw package -DskipTests          # produces target/almirath.jar
mkdir -p target/app && cp target/almirath.jar target/app/
```

Then, on **Linux**:

```bash
jpackage --type deb --name almirath --app-version 1.0 \
  --input target/app --main-jar almirath.jar \
  --main-class com.example.al_mirath.Launcher \
  --dest target/dist --linux-shortcut
```

on **Windows** (`--type msi` also works):

```
jpackage --type exe --name AlMirath --app-version 1.0 ^
  --input target/app --main-jar almirath.jar ^
  --main-class com.example.al_mirath.Launcher ^
  --dest target/dist --win-menu --win-shortcut
```

on **macOS**:

```bash
jpackage --type dmg --name AlMirath --app-version 1.0 \
  --input target/app --main-jar almirath.jar \
  --main-class com.example.al_mirath.Launcher \
  --dest target/dist
```

Replace `--type` with `app-image` on any platform to get the portable folder
instead of an installer. On Linux, `--type deb` needs `dpkg` and `fakeroot`;
`--type rpm` needs `rpmbuild`.

### Running it without a package

If Java is already installed, the jar runs on its own:

```bash
./mvnw package -DskipTests
java -jar target/almirath.jar
```

That jar contains the game, its dependencies and the JavaFX libraries for the
platform it was built on, so it needs only a Java 21 runtime — no Maven, no
JavaFX install, no IDE.

## Working on it

```bash
./mvnw javafx:run    # run from source
./mvnw test          # the engine and interface test suite
./mvnw package       # build target/almirath.jar
```

Use `mvnw` / `mvnw.cmd` rather than a system Maven; it fetches the right version
itself. Any IDE that imports a Maven project will do — the project carries
Eclipse and IntelliJ metadata, and neither is required.

The game creates its own database on first launch. There is nothing to install
or configure.

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

src/test/java/    Engine, layout and packaging tests
```

The engine holds no JavaFX types, so the rules are testable headlessly; the
controllers hold no game rules.

## Testing

```bash
./mvnw test
```

The suite covers the rules headlessly — rewind restoring every field, saves
round-tripping, succession handing on a world rather than rebuilding one, the
city bending the odds and the outcomes — and then covers the things that only
break once a person is looking at them: that no text is cut off at small window
sizes, that pressing New Game reaches the naming screen, that closing a final
chronicle offers the succession, and that the artwork is still found when the
game is a packaged jar rather than a folder of classes.

That last group exists because each of those shipped broken at least once while
every rules test passed.
