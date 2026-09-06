package com.example.al_mirath.model;

import java.util.Objects;

/**
 * A title forged for a life from the shape it took — the arena the life was
 * spent in, and the one detail that made this life's version of that name its
 * own.
 *
 * <p>The game used to keep a fixed shelf of honorifics and hand you whichever
 * one your stats unlocked, word for word, every run. A forged title carries a
 * prestige (which one history leads with) and a domain (so the forge keeps at
 * most one title per arena), but equality is on the words alone: the same
 * epithet earned twice is the same title.
 */
public final class EarnedTitle {

    private final String text;
    private final int prestige;
    private final String domain;

    public EarnedTitle(String text, int prestige, String domain) {
        this.text = text;
        this.prestige = prestige;
        this.domain = domain;
    }

    /** What people call you. */
    public String text() {
        return text;
    }

    /** How far up the record it sits; the highest one crowns the life. */
    public int prestige() {
        return prestige;
    }

    /** The arena it was earned in, so the forge keeps only one per arena. */
    public String domain() {
        return domain;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EarnedTitle earned && text.equals(earned.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
