package com.example.al_mirath.model;

/**
 * One line in the chronicle of a life — the year it happened, what happened,
 * and how it felt.
 *
 * <p>The log is the spine of a year-by-year life: stats say what a character
 * is, but only the log says what they <em>did</em>. Every system that changes
 * something the player would notice writes a line here, so ageing up produces
 * a readable year rather than a silent shuffle of numbers.
 *
 * @param age  the character's age when it happened
 * @param text the line as the player reads it
 * @param tone how the line should be coloured
 */
public record LifeLogEntry(int age, String text, Tone tone) {

    /**
     * What kind of line this is. Drives colour and weight in the log, so a
     * player can skim a decade and see the shape of it without reading.
     */
    public enum Tone {

        /** Something went well. */
        GOOD,

        /** Something went badly. */
        BAD,

        /** Neither — a salary paid, a year spent quietly. */
        NEUTRAL,

        /** A birthday, a coming of age, a death: the beats of a life. */
        MILESTONE,

        /** A narrative scene the player was asked to decide. */
        SCENE,

        /** News from the wider world, which happened with or without you. */
        WORLD
    }

    public LifeLogEntry {
        text = text == null ? "" : text.strip();
        tone = tone == null ? Tone.NEUTRAL : tone;
        age = Math.max(0, age);
    }

    public static LifeLogEntry good(int age, String text) {
        return new LifeLogEntry(age, text, Tone.GOOD);
    }

    public static LifeLogEntry bad(int age, String text) {
        return new LifeLogEntry(age, text, Tone.BAD);
    }

    public static LifeLogEntry plain(int age, String text) {
        return new LifeLogEntry(age, text, Tone.NEUTRAL);
    }

    public static LifeLogEntry milestone(int age, String text) {
        return new LifeLogEntry(age, text, Tone.MILESTONE);
    }

    public static LifeLogEntry scene(int age, String text) {
        return new LifeLogEntry(age, text, Tone.SCENE);
    }

    public static LifeLogEntry world(int age, String text) {
        return new LifeLogEntry(age, text, Tone.WORLD);
    }

    /** "Age 14 — You copied a manuscript for the madrasa." */
    public String display() {
        return "Age " + age + " — " + text;
    }

    /** The style class the log uses to colour this line. */
    public String styleClass() {
        return switch (tone) {
            case GOOD -> "log-good";
            case BAD -> "log-bad";
            case MILESTONE -> "log-milestone";
            case SCENE -> "log-scene";
            case WORLD -> "log-world";
            case NEUTRAL -> "log-plain";
        };
    }
}
