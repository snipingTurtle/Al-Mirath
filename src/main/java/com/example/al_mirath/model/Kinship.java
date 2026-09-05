package com.example.al_mirath.model;

/** How someone in the household is related to the player. */
public enum Kinship {

    FATHER,
    MOTHER,
    SIBLING,
    SPOUSE,
    CHILD,
    GRANDCHILD;

    /** How the Household panel labels this relation. */
    public String displayName() {
        return switch (this) {
            case FATHER -> "Father";
            case MOTHER -> "Mother";
            case SIBLING -> "Sibling";
            case SPOUSE -> "Spouse";
            case CHILD -> "Child";
            case GRANDCHILD -> "Grandchild";
        };
    }

    /** True for the generation the player was born into. */
    public boolean isElder() {
        return this == FATHER || this == MOTHER;
    }

    /** True for the generations that outlive the player and carry the name. */
    public boolean isDescendant() {
        return this == CHILD || this == GRANDCHILD;
    }

    /**
     * True for anyone who grew up in the player's household and so has a life
     * of their own still ahead of them. Parents arrived with theirs already
     * settled, and a spouse arrives with theirs.
     */
    public boolean takesALifePath() {
        return this == SIBLING || isDescendant();
    }
}
