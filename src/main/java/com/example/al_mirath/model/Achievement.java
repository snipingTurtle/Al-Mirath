package com.example.al_mirath.model;

/**
 * A single unlockable milestone.
 *
 * @param id      stable key stored in the database; never change it once shipped
 * @param name    display name
 * @param hint    what the player must do, shown even while locked
 * @param hidden  true for spoiler achievements, whose hint stays masked until unlocked
 */
public record Achievement(String id, String name, String hint, boolean hidden) {

    public static Achievement of(String id, String name, String hint) {
        return new Achievement(id, name, hint, false);
    }

    public static Achievement hidden(String id, String name, String hint) {
        return new Achievement(id, name, hint, true);
    }
}
