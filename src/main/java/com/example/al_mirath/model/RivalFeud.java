package com.example.al_mirath.model;

/**
 * How far a rivalry has curdled.
 *
 * <p>A rival who merely dislikes you is a nuisance. A rival who has been
 * humiliated in front of witnesses three times, and who now outranks you, is a
 * story. This is the difference between those two, and it decides which of the
 * rival's moves the game is willing to make against you.
 *
 * <p>Derived rather than stored: the relationship score and the memories the
 * rival is already carrying say everything needed, so a feud costs nothing to
 * persist and cannot drift out of step with the bond it describes.
 */
public enum RivalFeud {

    /** Nothing personal. Competition, at most. */
    NONE,

    /** A grudge exists, but it would not survive a good apology. */
    SLIGHTED,

    /** Settled into dislike. They will take an opportunity if handed one. */
    RESENTFUL,

    /** They are looking for opportunities rather than waiting for them. */
    VENGEFUL,

    /** Past repair, and it will outlive whichever of you dies first. */
    BLOOD_FEUD;

    /** Above this the rivalry is not personal enough to act on. */
    private static final int GRUDGE_BEGINS = -15;

    private static final int RESENTFUL_AT = -40;
    private static final int VENGEFUL_AT = -65;
    private static final int BLOOD_FEUD_AT = -85;

    /**
     * A pattern of humiliation cuts deeper than one bad afternoon, so this
     * many wounds moves the feud up a stage on its own.
     */
    private static final int WOUNDS_THAT_COMPOUND = 3;

    /**
     * Reads the feud currently standing between the player and this character.
     *
     * @return {@link #NONE} for anyone who holds no grudge worth acting on, or
     *         who is no longer alive to hold one
     */
    public static RivalFeud of(RecurringCharacter character) {
        if (character == null || !character.isAlive()) {
            return NONE;
        }

        int standing = character.getRelationship();

        if (standing > GRUDGE_BEGINS) {
            return NONE;
        }

        RivalFeud stage = fromStanding(standing);

        // Someone humiliated repeatedly stops weighing each slight separately
        // and starts treating you as the thing to be answered.
        if (character.woundCount() >= WOUNDS_THAT_COMPOUND) {
            stage = stage.next();
        }

        return stage;
    }

    private static RivalFeud fromStanding(int standing) {
        if (standing <= BLOOD_FEUD_AT) {
            return BLOOD_FEUD;
        }

        if (standing <= VENGEFUL_AT) {
            return VENGEFUL;
        }

        if (standing <= RESENTFUL_AT) {
            return RESENTFUL;
        }

        return SLIGHTED;
    }

    /** The next stage up, or this one if the feud is already as bad as it gets. */
    public RivalFeud next() {
        RivalFeud[] stages = values();

        return this == BLOOD_FEUD
                ? this
                : stages[ordinal() + 1];
    }

    /** True when this feud is at least as far gone as {@code other}. */
    public boolean isAtLeast(RivalFeud other) {
        return ordinal() >= other.ordinal();
    }

    /** How the People In Your Life screen names this feud. */
    public String displayName() {
        return switch (this) {
            case NONE -> "No quarrel";
            case SLIGHTED -> "Slighted";
            case RESENTFUL -> "Resentful";
            case VENGEFUL -> "Vengeful";
            case BLOOD_FEUD -> "Blood feud";
        };
    }
}
