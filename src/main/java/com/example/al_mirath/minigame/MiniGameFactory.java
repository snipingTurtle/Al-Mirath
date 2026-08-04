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

        return switch (pickKind(player)) {
            case SCRIBE -> new ScribeMiniGame(difficulty);
            case MERCHANT -> new MerchantMiniGame(difficulty);
            case COURIER -> new CourierMiniGame(difficulty);
            case ORATOR -> new OratorMiniGame(difficulty);
            case PHYSICIAN -> new PhysicianMiniGame(difficulty);
        };
    }

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
            case "education" -> RANDOM.nextBoolean()
                    ? new ScribeMiniGame(scaled)
                    : new PhysicianMiniGame(scaled);

            case "wealth" -> new MerchantMiniGame(scaled);

            case "health" -> new CourierMiniGame(scaled);

            case "reputation", "morality" -> new OratorMiniGame(scaled);

            case "politicalPower" -> RANDOM.nextBoolean()
                    ? new OratorMiniGame(scaled)
                    : new CourierMiniGame(scaled);

            default -> randomGame(scaled);
        };
    }

    private static MiniGame randomGame(int difficulty) {
        return switch (randomKind()) {
            case SCRIBE -> new ScribeMiniGame(difficulty);
            case MERCHANT -> new MerchantMiniGame(difficulty);
            case COURIER -> new CourierMiniGame(difficulty);
            case ORATOR -> new OratorMiniGame(difficulty);
            case PHYSICIAN -> new PhysicianMiniGame(difficulty);
        };
    }

    private enum Kind {
        SCRIBE, MERCHANT, COURIER, ORATOR, PHYSICIAN
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
