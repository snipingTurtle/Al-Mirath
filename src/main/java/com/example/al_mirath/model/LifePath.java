package com.example.al_mirath.model;

import java.util.Random;

/**
 * What a child of the household made of themselves.
 *
 * <p>This is the point of having a family at all: the path is not rolled in a
 * vacuum, it is drawn from what the player had become by the time the child
 * was old enough to choose. A house of scholars produces scholars. A parent
 * who spent a life on politics produces someone comfortable at court — or, if
 * that life was spent badly, someone comfortable outside the law.
 */
public enum LifePath {

    /** Too young to have taken one. */
    UNDECIDED,

    SCHOLAR,
    MERCHANT,
    SOLDIER,
    COURTIER,
    CRIMINAL,

    /** Rare, and only from a house that was already close to power. */
    RULER;

    /** Below this, a parent's example is not worth following. */
    private static final int NOTABLE = 55;

    /** A house this corrupt teaches its children the wrong lesson. */
    private static final int CORRUPTING_MORALITY = 30;

    private static final int RULER_POWER = 80;
    private static final int RULER_REPUTATION = 70;

    /**
     * Picks the path a child takes, weighted by the household they grew up in.
     *
     * @param random broken ties, so two children of the same house need not
     *               end up identical
     */
    public static LifePath decideFor(PlayerCharacter parent, Random random) {
        if (parent == null) {
            return SOLDIER;
        }

        // The throne is not a career one drifts into.
        if (parent.getPoliticalPower() >= RULER_POWER
                && parent.getReputation() >= RULER_REPUTATION
                && random.nextInt(100) < 35) {

            return RULER;
        }

        // A house that taught its children that rules are for other people.
        if (parent.getMorality() <= CORRUPTING_MORALITY
                && random.nextInt(100) < 45) {

            return CRIMINAL;
        }

        LifePath[] candidates = {SCHOLAR, MERCHANT, COURTIER, SOLDIER};

        int[] weights = {
                weightOf(parent.getEducation()),
                weightOf(parent.getWealth()),
                weightOf(parent.getPoliticalPower()),
                weightOf(parent.getHealth())
        };

        int total = 0;

        for (int weight : weights) {
            total += weight;
        }

        int roll = random.nextInt(total);

        for (int i = 0; i < candidates.length; i++) {
            if (roll < weights[i]) {
                return candidates[i];
            }

            roll -= weights[i];
        }

        return SOLDIER;
    }

    /**
     * A stat the parent never built still leaves a small chance — children are
     * not only ever their parents — but a stat they mastered dominates.
     */
    private static int weightOf(int stat) {
        return stat >= NOTABLE
                ? 2 + (stat - NOTABLE)
                : 2;
    }

    public String displayName() {
        return switch (this) {
            case UNDECIDED -> "Still a child";
            case SCHOLAR -> "Scholar";
            case MERCHANT -> "Merchant";
            case SOLDIER -> "Soldier";
            case COURTIER -> "Courtier";
            case CRIMINAL -> "Outlaw";
            case RULER -> "Ruler";
        };
    }

    /** One line of what this path has meant for them so far. */
    public String describe(String name) {
        return switch (this) {
            case UNDECIDED -> name + " is not old enough to have chosen anything.";
            case SCHOLAR -> name + " reads, argues, and is beginning to be cited.";
            case MERCHANT -> name + " trades, and is better at it than you were.";
            case SOLDIER -> name + " serves under arms, far enough away to worry you.";
            case COURTIER -> name + " has learned which rooms matter, and stands in them.";
            case CRIMINAL -> name + " keeps company you have decided not to ask about.";
            case RULER -> name + " governs, and the household's name is spoken carefully now.";
        };
    }
}
