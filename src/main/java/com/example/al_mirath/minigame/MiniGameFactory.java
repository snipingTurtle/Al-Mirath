package com.example.al_mirath.minigame;

import com.example.al_mirath.model.PlayerCharacter;

import java.util.Random;

/**
 * Chooses which challenge a rewind attempt presents.
 *
 * <p>The pick is weighted by who the character has become, so a scholar is
 * usually tested on memory and a merchant on arithmetic. It stays random enough
 * that the player cannot drill a single game forever, and difficulty scales
 * with the life stage reached so a late-life rewind is genuinely harder to earn.
 */
public final class MiniGameFactory {

    private static final Random RANDOM = new Random();

    private MiniGameFactory() {
    }

    public static MiniGame createFor(PlayerCharacter player, int stageIndex) {
        int difficulty = difficultyFor(stageIndex);

        return build(pickKind(player), difficulty);
    }

    /**
     * Builds a named challenge, which is how an activity asks for the one it
     * was written around.
     *
     * <p>A rewind may present any test the character's life makes plausible,
     * but training at the butts has to be the bow and nothing else — the
     * activity's whole claim is that the player earned <em>that</em> skill.
     *
     * @param type       one of the names in {@link #TYPES}; anything
     *                   unrecognised falls back to a random challenge rather
     *                   than failing, so a content typo costs flavour and not
     *                   a crash
     * @param difficulty 1 (gentle) to 5 (brutal)
     */
    public static MiniGame createByType(String type, int difficulty) {
        int scaled = Math.max(1, Math.min(5, difficulty));

        if (type == null) {
            return randomGame(scaled);
        }

        return switch (type.toLowerCase()) {
            case "scribe" -> new ScribeMiniGame(scaled);
            case "merchant" -> new MerchantMiniGame(scaled);
            case "courier" -> new CourierMiniGame(scaled);
            case "orator" -> new OratorMiniGame(scaled);
            case "physician" -> new PhysicianMiniGame(scaled);
            case "archery" -> new ArcheryMiniGame(scaled);
            case "haggle" -> new HagglingMiniGame(scaled);
            case "caravan" -> new CaravanMiniGame(scaled);
            case "geometer" -> new GeometerMiniGame(scaled);
            case "prosody" -> new ProsodyMiniGame(scaled);
            case "lighthand" -> new LightHandMiniGame(scaled);
            default -> randomGame(scaled);
        };
    }

    /** Every challenge an activity may name. Used by content checks. */
    public static final java.util.List<String> TYPES = java.util.List.of(
            "scribe", "merchant", "courier", "orator", "physician",
            "archery", "haggle", "caravan", "geometer", "prosody", "lighthand"
    );

    /**
     * Builds the challenge for a Trial of Skill — a stat check the player has
     * chosen to earn rather than leave to the engine's roll.
     *
     * <p>The game is matched to the stat under test so the challenge measures
     * something recognisably related to it: memory and compounding for the
     * mind, timing and nerve for the body, reading a room for standing.
     *
     * @param checkStat  the stat the choice tests
     * @param difficulty the choice's difficulty, 0-100 as authored in content
     */
    public static MiniGame createForStatCheck(String checkStat, int difficulty) {
        // Content difficulties run roughly 25-65; map that onto the 1-5 the
        // mini-games expect, so a hard check really is a harder challenge.
        int scaled = Math.max(1, Math.min(5, (difficulty / 15) + 1));

        String stat = checkStat == null ? "" : checkStat;

        return switch (stat) {
            case "education" -> switch (RANDOM.nextInt(3)) {
                case 0 -> new ScribeMiniGame(scaled);
                case 1 -> new PhysicianMiniGame(scaled);
                default -> new GeometerMiniGame(scaled);
            };

            case "wealth" -> RANDOM.nextBoolean()
                    ? new MerchantMiniGame(scaled)
                    : new HagglingMiniGame(scaled);

            case "health" -> RANDOM.nextBoolean()
                    ? new CourierMiniGame(scaled)
                    : new ArcheryMiniGame(scaled);

            case "reputation", "morality" -> RANDOM.nextBoolean()
                    ? new OratorMiniGame(scaled)
                    : new ProsodyMiniGame(scaled);

            case "politicalPower" -> switch (RANDOM.nextInt(3)) {
                case 0 -> new OratorMiniGame(scaled);
                case 1 -> new CourierMiniGame(scaled);
                default -> new LightHandMiniGame(scaled);
            };

            default -> randomGame(scaled);
        };
    }

    private static MiniGame randomGame(int difficulty) {
        return build(randomKind(), difficulty);
    }

    /** The single place a kind becomes a game, so adding one cannot be half-done. */
    private static MiniGame build(Kind kind, int difficulty) {
        return switch (kind) {
            case SCRIBE -> new ScribeMiniGame(difficulty);
            case MERCHANT -> new MerchantMiniGame(difficulty);
            case COURIER -> new CourierMiniGame(difficulty);
            case ORATOR -> new OratorMiniGame(difficulty);
            case PHYSICIAN -> new PhysicianMiniGame(difficulty);
            case ARCHERY -> new ArcheryMiniGame(difficulty);
            case HAGGLE -> new HagglingMiniGame(difficulty);
            case CARAVAN -> new CaravanMiniGame(difficulty);
            case GEOMETER -> new GeometerMiniGame(difficulty);
            case PROSODY -> new ProsodyMiniGame(difficulty);
            case LIGHTHAND -> new LightHandMiniGame(difficulty);
        };
    }

    private enum Kind {
        SCRIBE, MERCHANT, COURIER, ORATOR, PHYSICIAN,
        ARCHERY, HAGGLE, CARAVAN, GEOMETER, PROSODY, LIGHTHAND
    }

    /**
     * Life stages run 0..4 (Childhood to Legacy). Undoing a decision late in a
     * life should cost more skill than undoing a childhood mistake.
     */
    private static int difficultyFor(int stageIndex) {
        return Math.max(1, Math.min(5, stageIndex + 1));
    }

    private static Kind pickKind(PlayerCharacter player) {
        if (player == null) {
            return randomKind();
        }

        String status = lower(player.getCurrentStatus());
        String origin = lower(player.getOrigin());

        // A weighted lean, not a hard rule: there is always a chance of another test.
        boolean leanScribe = containsAny(status, "scholar", "educated")
                || containsAny(origin, "scholar", "madrasa", "scribe", "clerk");

        boolean leanOrator = containsAny(status, "court", "ruler", "regent", "leader", "hero")
                || containsAny(origin, "noble", "prince", "governor");

        boolean leanMerchant = containsAny(status, "merchant", "householder")
                || containsAny(origin, "merchant", "guild");

        boolean leanCourier = containsAny(status, "shadow", "enemy of the court", "desperate")
                || containsAny(origin, "orphan", "servant");

        int roll = RANDOM.nextInt(100);

        if (leanScribe && roll < 60) {
            return Kind.SCRIBE;
        }

        if (leanMerchant && roll < 60) {
            return Kind.MERCHANT;
        }

        if (leanCourier && roll < 60) {
            return Kind.COURIER;
        }

        if (leanOrator && roll < 60) {
            return Kind.ORATOR;
        }

        return randomKind();
    }

    private static Kind randomKind() {
        Kind[] values = Kind.values();
        return values[RANDOM.nextInt(values.length)];
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}
