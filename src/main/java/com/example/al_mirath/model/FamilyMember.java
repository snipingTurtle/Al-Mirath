package com.example.al_mirath.model;

import java.util.Objects;

/**
 * One person in the player's household.
 *
 * <p>Deliberately not a {@link RecurringCharacter}. The cast are people the
 * player met and can lose touch with; these are people the player is related
 * to whether either of them likes it. They carry a kinship rather than a
 * relationship type, they take a {@link LifePath} instead of climbing a role
 * ladder, and they arrive by birth rather than by being introduced.
 */
public final class FamilyMember {

    /** The age a child is treated as having chosen their life. */
    public static final int COMES_OF_AGE = 18;

    private final String id;
    private final String name;
    private final Kinship kinship;
    private final String trait;

    /** Which child a grandchild belongs to; empty for everyone else. */
    private final String parentId;

    private int age;
    private int affection;
    private boolean alive;
    private LifePath lifePath;
    private String deathReason;

    public FamilyMember(
            String id,
            String name,
            Kinship kinship,
            String trait,
            int age,
            int affection,
            boolean alive,
            LifePath lifePath,
            String parentId
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.kinship = Objects.requireNonNull(kinship, "kinship");
        this.trait = trait == null ? "" : trait;
        this.parentId = parentId == null ? "" : parentId;

        this.age = Math.max(0, age);
        this.affection = clamp(affection);
        this.alive = alive;
        this.lifePath = lifePath == null ? LifePath.UNDECIDED : lifePath;
        this.deathReason = "";
    }

    private static int clamp(int value) {
        return Math.max(-100, Math.min(100, value));
    }

    public void ageBy(int years) {
        age += Math.max(0, years);
    }

    public void changeAffection(int amount) {
        affection = clamp(affection + amount);
    }

    public void markDead(String reason) {
        alive = false;
        deathReason = reason == null ? "" : reason;
    }

    /**
     * Settles what this child became, if they are old enough and have not
     * already chosen.
     *
     * @return the new path, or null when nothing changed. Callers use the
     *         non-null case to announce it.
     */
    public LifePath comeOfAge(PlayerCharacter parent, java.util.Random random) {
        if (!kinship.takesALifePath()
                || lifePath != LifePath.UNDECIDED
                || age < COMES_OF_AGE) {

            return null;
        }

        lifePath = LifePath.decideFor(parent, random);

        return lifePath;
    }

    public void setLifePath(LifePath lifePath) {
        this.lifePath = lifePath == null ? LifePath.UNDECIDED : lifePath;
    }

    /** How close this person is, in words rather than a number. */
    public String affectionLabel() {
        if (affection >= 70) {
            return "Devoted";
        }

        if (affection >= 35) {
            return "Close";
        }

        if (affection >= 5) {
            return "Warm";
        }

        if (affection > -25) {
            return "Distant";
        }

        if (affection > -60) {
            return "Estranged";
        }

        return "Severed";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Kinship getKinship() {
        return kinship;
    }

    public String getTrait() {
        return trait;
    }

    public String getParentId() {
        return parentId;
    }

    public int getAge() {
        return age;
    }

    public int getAffection() {
        return affection;
    }

    public boolean isAlive() {
        return alive;
    }

    public LifePath getLifePath() {
        return lifePath;
    }

    public String getDeathReason() {
        return deathReason;
    }
}
