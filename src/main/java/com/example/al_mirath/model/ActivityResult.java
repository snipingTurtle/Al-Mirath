package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;

/**
 * Immutable outcome of performing an {@link Activity}.
 *
 * <p>Captures everything the UI needs to display the result in the life log
 * and everything the engine needs to apply the mechanical effects.</p>
 *
 * @param activityName  Display name of the activity attempted
 * @param success       Whether the attempt succeeded
 * @param narration     Flavour text describing what happened
 * @param statChanges   Stat deltas that were applied (stat name → delta)
 * @param factionChanges Faction deltas that were applied
 * @param flagsAdded    World-state flags set by this outcome
 * @param wealthChange  Net change to the character's gold reserves
 */
public record ActivityResult(
        String activityName,
        boolean success,
        String narration,
        Map<String, Integer> statChanges,
        Map<String, Integer> factionChanges,
        List<String> flagsAdded,
        double wealthChange
) {

    /** Compact constructor with defensive copying. */
    public ActivityResult {
        statChanges = statChanges != null ? Map.copyOf(statChanges) : Map.of();
        factionChanges = factionChanges != null ? Map.copyOf(factionChanges) : Map.of();
        flagsAdded = flagsAdded != null ? List.copyOf(flagsAdded) : List.of();
    }

    // ── Factories ───────────────────────────────────────────────────────

    public static ActivityResult success(String name, String narration,
                                         Map<String, Integer> stats,
                                         Map<String, Integer> factions,
                                         List<String> flags,
                                         double wealthChange) {
        return new ActivityResult(name, true, narration,
                stats, factions, flags, wealthChange);
    }

    public static ActivityResult failure(String name, String narration,
                                         Map<String, Integer> stats,
                                         Map<String, Integer> factions,
                                         List<String> flags,
                                         double wealthChange) {
        return new ActivityResult(name, false, narration,
                stats, factions, flags, wealthChange);
    }
}
