package com.example.al_mirath.model;

/**
 * Someone who could carry the house after the player dies.
 *
 * <p>Blood first — a living child or grandchild — and failing that a student
 * or companion who was close enough to be handed the name. The distinction
 * matters past the label: a child inherits the money and the position, a
 * student inherits the learning and very little else.
 */
public record Succession(
        String id,
        String name,
        String relation,
        int age,
        String trait,
        LifePath path,
        boolean blood
) {

    /** Old enough to be handed a house rather than raised into one. */
    public static final int OLD_ENOUGH = 12;

    /** How the option reads on the button that offers it. */
    public String offer() {
        return name + ", " + relation;
    }

    /** The sentence under the offer. */
    public String describe() {
        String living =
                path == null || path == LifePath.UNDECIDED
                        ? name + " has not yet chosen what to be."
                        : path.describe(name);

        return "Aged " + age + ", " + trait + ". " + living;
    }
}
