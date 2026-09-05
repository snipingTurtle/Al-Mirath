package com.example.al_mirath.model;

/**
 * What actually killed the player, and how to say it.
 *
 * <p>The engine used to assign a fixed sentence at each risk branch, which
 * produced lines like "Years of pressure weakened your chance of survival"
 * over the body of a seven-year-old. A cause is not a sentence: the same
 * exhaustion reads differently in a child, in someone who never reached
 * twenty-five, and in an elder with no reserve left. The stat decides the
 * cause; the age decides the telling.
 */
public enum DeathCause {

    /** The body gave out. */
    FRAILTY,

    /** Carried more than could be carried, for too long. */
    EXHAUSTION,

    /** Made enemies faster than protectors, and one of them collected. */
    ENMITY,

    /** Simply old. */
    OLD_AGE,

    /** A physical gamble that did not come off. */
    INJURY,

    /** A reach for power that exposed a throat. */
    POWER_MOVE,

    COURT_SUSPICION,
    INFORMED_ON,
    SHADOW_DEBT,
    BURIED_LIE;

    private static final int CHILD = 13;
    private static final int YOUTH = 25;
    private static final int ELDER = 60;

    /** The sentence shown on the death panel. */
    public String describe(int age) {
        return switch (this) {
            case FRAILTY -> frailty(age);
            case EXHAUSTION -> exhaustion(age);
            case ENMITY -> enmity(age);
            case OLD_AGE -> "Old age closed the final chapter of your life.";
            case INJURY -> "A failed physical challenge left lasting damage.";
            case POWER_MOVE -> "A failed power move exposed you to dangerous enemies.";
            case COURT_SUSPICION -> "Court suspicion surrounded your final days.";
            case INFORMED_ON -> "The report against you destroyed your protection.";
            case SHADOW_DEBT -> "The shadow network protected you for too long to let you walk away freely.";
            case BURIED_LIE -> "The lie you built began to collapse around you.";
        };
    }

    private String frailty(int age) {
        if (age < CHILD) {
            return "You were a sickly child in a century with no medicine for it. "
                    + "A winter fever found you already weak, and there was nothing "
                    + "in you left to fight it with.";
        }

        if (age < YOUTH) {
            return "Your body had been failing for years before anyone treated it "
                    + "as serious. By the time it was, you were too far gone to "
                    + "be brought back.";
        }

        if (age < ELDER) {
            return "Your body could not carry what your life kept asking of it, "
                    + "and it stopped trying somewhere short of old age.";
        }

        return "Your body finally failed after years of hardship.";
    }

    private String exhaustion(int age) {
        if (age < CHILD) {
            return "You were a child asked to hold what grown men could not. "
                    + "You stopped eating first, then stopped speaking, and the "
                    + "household only understood how much you had been carrying "
                    + "after there was nothing left to carry it.";
        }

        if (age < YOUTH) {
            return "You were not yet twenty-five and already worn through. The "
                    + "sickness that took you would not have troubled someone who "
                    + "had been sleeping.";
        }

        if (age < ELDER) {
            return "Years of pressure weakened your chance of survival.";
        }

        return "The pressure never lifted, and at your age the body keeps no "
                + "reserve against it.";
    }

    private String enmity(int age) {
        if (age < CHILD) {
            return "Even as a child you were known for cruelty, and the household "
                    + "that suffered it stopped watching over you. When the fever "
                    + "came, nobody sat up with you through it.";
        }

        if (age < YOUTH) {
            return "You had made more enemies than protectors, and you were still "
                    + "too young to have anyone powerful enough to stand between "
                    + "you and them.";
        }

        if (age < ELDER) {
            return "Someone you ruined had been waiting a long while for the "
                    + "chance. Nobody at court looked very hard into how you died.";
        }

        return "You grew old without one friend left willing to stand between you "
                + "and the people you had wronged.";
    }
}
