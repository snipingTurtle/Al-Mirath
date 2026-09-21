# Al-Mirath: Heirs of the Golden Age

A life simulator set across four eras of Islamic history — Umayyad, Abbasid,
Mamluk, and Ottoman. You are born into a randomly generated station in life and
live it a year at a time: press **Age Up**, read what the year did to you, and
spend the next one studying, trading, training at the butts, praying, buying a
shop, taking a post at court, or picking pockets in the souk. When you die, the
house passes to whoever can carry it, and you play them.

Built with JavaFX 21 and an embedded SQLite database.

---

## The year is the clock

Everything runs off one button. Pressing **Age Up** lives a year: you are paid,
your holdings earn and decay, your household ages, the city has its own year,
something small happens to you, and the world checks whether you are still
alive. All of it arrives as lines in the chronicle down the middle of the
screen — good in green, bad in red, the beats of a life in gold.

Nothing else moves time. A decision costs what it costs, not six years.

Between one year and the next you can do as much as you like, but each thing
only once — a year has only so many afternoons in it.

| Menu | What it is for |
|---|---|
| **Activities** | Forty-odd things to do with a year, across Faith, Knowledge, Trade, Arms, Court, Household, Leisure and the Shadow. |
| **Trade** | Nine careers with five rungs each. Take a post, ask to be raised, retire, or walk out. |
| **Holdings** | Houses, shops, farmland, workshops, a caravansary, a bathhouse, a walled garden. They pay every year, wear out, and outlive you. |
| **People** | Your household and the people who keep turning up. Give them time, or give them something worth having. |
| **Travel** | Move to another city. It changes who is hiring, what the market charges, and whether this year has a plague in it. |

Every row that is shut says why it is shut. "Not until you are 18" is a plan;
a missing row is nothing.

## History happens whether you like it or not

A life is not merely "in the Abbasid Era". It is dated: you are born in a real
year, and the panel says so — `Age 12 · 1252 CE · Baghdad`. Every year you
live through, the game checks what actually happened that year, in the city
you are actually standing in.

None of it is invented. Every entry in the timeline is dated from a published
source and carries that source with it: the Metropolitan Museum of Art's
Heilbrunn Timeline of Art History, Encyclopaedia Britannica, the World History
Encyclopedia, and the US National Library of Medicine for the plague years.
Where a source gives a year and not a month, the game says a year.

How close you are decides what you get. Something that happened to the empire
is news, and lands at a third of its weight. Something that happened to *your*
city is a scene you have to answer, and may not survive.

| Year | Where | What |
|---|---|---|
| 680 | Everywhere | Karbala. What you say about it in public is a choice with a price either way. |
| 717 | Everywhere | The army goes to Constantinople for a year and fails. Go, pay the fine, or sell it grain. |
| 750 | Damascus, Aleppo, Jerusalem | The Umayyads fall at the Zab, and men who served them are being looked for by name. |
| 762 | Baghdad, Basra, Damascus | A new capital is laid out on the Tigris. Go while the rents are nothing, or wait and see. |
| 869 | Basra | The Zanj rise in the salt flats. Leave the south, hold what you have, or trade with them. |
| 1257 | Baghdad, Basra | Hulegu is coming. Leave, send the family out, or trust the walls. |
| 1258 | Baghdad | Baghdad falls. The caliph and three hundred officials are put to death ten days after surrendering. |
| 1348 | Cairo, Damascus, Aleppo, Jerusalem | The plague year. Damascus loses something like half of itself. |
| 1400 | Aleppo | Timur destroys the Mamluk army in under an hour, and sacks the city for four days. |
| 1453 | Istanbul | Constantinople falls after fifty-five days. The half-empty city is being handed to anyone who will live in it. |

**It can kill you.** Being in Baghdad in 1258 or Cairo in 1348 is dangerous in
a way that nothing you chose caused. What you *do* about it matters: a
household already out of the city, or behind a door shut in time, is far more
likely to see the next year than one that stayed. The warning usually comes a
year early, and the warning is playable.

Dates also decide the map. Baghdad is not a birthplace in 755 — it is laid out
in 762 — and Istanbul is Istanbul from 1453. An heir does not restart the
calendar: they take up the house in the year their forebear died in, and the
house is not sacked twice.

## Mini-games

Half the activities are not resolved by a die roll. They hand you a board.

| Trial | What it tests | Rules |
|---|---|---|
| **The Scribe's Hand** | Memory | Glyphs light in sequence; repeat them. Each round adds one. One mistake ends it. |
| **The Merchant's Scales** | Arithmetic | Select coins summing to the demanded debt exactly, against a clock. |
| **The Night Courier** | Timing | Stop a sweeping marker inside a lit gap. Each success narrows the gap. |
| **The Orator's Floor** | Reading a room | Say the thing the room will take, as its mood shifts under you. |
| **The Physician's Table** | Judgement | Read the case and choose the herb, not the one that sounds right. |
| **The Butts** | Aim | Your hand drifts on two crossed rhythms. Loose when it crosses the gold. Every arrow after the first drifts wider. |
| **The Bargain** | Deduction | Find a seller's hidden floor without insulting him off it. Every refusal tells you something and costs his patience. |
| **The Caravan Road** | Planning | Cross the waste on the water you can carry. The cheapest next leg is usually the wrong one. |
| **The Geometer's Tile** | Pattern | A quarter of a symmetric panel is missing. Exactly one of four tiles restores it. The rule is never stated; it is on the wall. |
| **The Poet's Meter** | Ear | Finish the couplet. Three of the four words are what a man who reads poetry and does not write it would choose. |
| **The Light Hand** | Nerve | Take purses while the guard looks away. He gives a warning as he turns back, and every purse you take shortens the next window. |

The same eleven boards do three jobs: they settle an activity, they are the
interview when you apply for a post, and they are how you challenge fate.

## Threads of Fate

Regret is a mechanic, not an accident.

Each life begins holding one **Thread of Fate**, and earns another for surviving
each life stage, up to three. When a decision goes badly, you may spend a Thread
to challenge fate itself — but the Thread is consumed whether you win or lose,
so attempting a rewind is a genuine gamble rather than a free retry.

Which challenge appears is weighted by who your character has become — a scholar
is usually tested on memory, a merchant on arithmetic — and the difficulty
scales with how far into the life you are, so undoing a decision made as an
elder statesman is meaningfully harder than undoing a childhood mistake.

Win, and the run rewinds to the exact moment before the choice: stats, faction
standings, world flags, age, legacy titles, stage progress, and even life itself
if the choice killed you. The same scene is presented again, fully playable.

Lose, and the decision stands.

## Scenes

Between the ordinary years, authored scenes arrive and ask to be answered. They
are the set pieces — a rival in a doorway, a secret worth selling, a governor's
petition — and they carry the story flags that later years read back. They come
roughly every two or three years, more often the longer it has been since the
last, and they are modal: a decision you can click past is not a decision.

## What the world does without you

- **Eight faction standings** — court, nobles, military, scholars, merchants,
  the common people, your family council, and the shadow network.
- **Cities that live on their own**, with prosperity, trade, scholarship, crime,
  war and disease that drift year by year, bend the odds on what you attempt,
  price the property market, and can kill you.
- **A household** that ages, marries, has children, takes up paths of its own,
  and buries its dead.
- **A recurring cast** who grow into roles, remember what you did to them, and
  outlive you into the next generation.
- **Renown**: what you are actually known for, which spreads over years and
  reaches the scenes you are shown.
- **Delayed consequences** planted by a choice and coming due decades later.
- **Real history**, dated from published sources, arriving on its own years in
  the cities it actually reached.

## When you die

The chronicle closes, the life is scored and archived, and the house is offered
to whoever can carry it. The heir inherits your **property** and rather more
than half your **purse** — a death is expensive — along with the world, the
cities as you left them, half your standing with every faction, and a
disposition toward the people your house has history with. What they do not
inherit is your body, your learning, your titles or your story: those belonged
to whoever earned them.

## Other systems

- **27 achievements**, including hidden ones whose names stay masked until
  earned, with unlock notifications during play.
- **Lifetime statistics** tracked across every run — lives completed, choices
  made, titles earned, oldest age reached, best score.
- **Scoring** for completed lives, weighting longevity, earned titles, and broad
  faction standing, with penalties for stress and for rewinds used.
- **Legacy Records** archiving every completed life.
- **Keyboard play**: `Space` to age up, `1` `2` `3` to answer a scene, `A` for
  activities, `P` for the people in your life, `R` to challenge fate, `Esc` to
  close a menu or leave for the main menu.

## Playing it

You do not need Java, Maven, or an IDE. Download the build for your platform
from the [latest release](https://github.com/snipingTurtle/Al-Mirath/releases/latest),
install it, and open it like anything else.

| Platform | Download | What you do |
|---|---|---|
| Windows | `AlMirath-windows-x86_64.msi` | Run it, then launch **Al-Mirath** from the Start menu |
| macOS (Apple silicon) | `AlMirath-macos-arm64.dmg` | Open it, drag the app to Applications |
| macOS (Intel) | `AlMirath-macos-x86_64.dmg` | The same |
| Linux | `AlMirath-linux-x86_64.deb` | `sudo apt install ./AlMirath-linux-x86_64.deb`, then launch it from your applications menu |

Each one carries its own Java runtime, so nothing else has to be installed and
nothing else on the machine is touched. They are around 250 MB installed,
almost all of it artwork.

Every platform also has a `-portable` archive that needs no installer at all:
unpack it anywhere and run `AlMirath/bin/AlMirath`, or `AlMirath\AlMirath.exe`
on Windows. Useful on a machine you cannot install software on.

The macOS builds are not signed by Apple, so the first launch is refused with
"cannot be opened because the developer cannot be verified". Right-click the
app and choose **Open**, which offers to run it anyway.

### Publishing a release

`.github/workflows/release.yml` builds all four, because jpackage can only
build for the machine it runs on — a Windows installer has to be made on
Windows. Tagging is all it takes:

```bash
git tag v1.0.0
git push origin v1.0.0
```

That runs the test suite, then builds on Linux, Windows, and both Intel and
Apple silicon macOS runners in parallel. Each attaches its own installer and
portable archive to a GitHub release named after the tag as soon as it has
them, so a platform that is slow, broken, or waiting on a scarce runner delays
its own download and nobody else's. The version in the package comes from the
tag, so `v1.2.3` produces a 1.2.3 build.

To get builds without cutting a release, run the workflow by hand from the
**Actions** tab; the packages are attached to that run as artifacts and kept
for a fortnight.

If one platform fails to build, the release still gets the ones that did — a
Windows installer that will not package is a reason to fix Windows, not a
reason for nobody to be able to download the game. The failed job stays red on
the run, and a platform whose installer failed still attaches the portable
archive it built before that.

### Building a package yourself

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
instead of an installer.

A few things the platforms want that the others do not: on Linux, `--type deb`
needs `dpkg` and `fakeroot` and `--type rpm` needs `rpmbuild`; on Windows,
either installer type needs [WiX 3](https://github.com/wixtoolset/wix3/releases)
on the `PATH`. `--linux-deb-maintainer you@example.com` fills in the blank the
Debian package otherwise leaves in its metadata.

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

Part of the suite drives real JavaFX screens, so it needs a display. On a
headless machine, run it under a virtual one — `xvfb-run -a ./mvnw test` — which
is what the workflow does on CI.

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
├── minigame/     The eleven trials, and the factory that picks between them
├── model/        Plain data: character, career, property, activities, the log
├── service/      Game rules, the yearly tick, the activity and career content
└── ui/           Reusable components: the list menu, trial overlay, toasts

src/test/java/    Engine, screen, trial and packaging tests
```

The engine holds no JavaFX types, so the rules are testable headlessly; the
controllers hold no game rules.

## Testing

```bash
./mvnw test
```

The suite covers the rules headlessly — ageing up moving the year on by exactly
one and a decision moving it by none, rewind restoring every field, saves
round-tripping a career and a chronicle, succession handing on a world and an
estate rather than rebuilding one, the city bending the odds and the outcomes.

It also checks the activity catalogue as content, because a stat name with a
typo in it silently does nothing and a mini-game name with a typo in it
silently becomes a different game. `ActivityLibraryTest` walks every entry and
refuses both.

`LivelihoodTest` asserts the thing the whole activity menu rests on: that a
character who prays, walks in the garden, takes the baths, wrestles and sits
with their household ends up measurably better off than one who does none of
it. If the menus ever stop mattering, that is where it shows.

Then there are the things that only break once a person is looking at them:
that Age Up is wired to the clock, that every button on the action bar opens
something, that the chronicle is scrolled to the year you just lived rather
than to your birth, that every one of the eleven trials builds a board it can
be played on, that no text is cut off at small window sizes, that closing a
final chronicle offers the succession, and that the artwork is still found when
the game is a packaged jar rather than a folder of classes.

That last group exists because each of those shipped broken at least once while
every rules test passed.
