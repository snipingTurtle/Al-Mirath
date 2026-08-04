package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;

/**
 * A scripted announcement about the wider world, delivered as an "ancient
 * decree" popup rather than tied to a player choice.
 *
 * <p>These exist to make the setting feel alive even when a life stays quiet:
 * a ruler dies, a rebellion breaks out, a plague spreads, none of it caused by
 * the player. They also carry real mechanical weight — a plague costs health,
 * a rebellion shakes the court's standing — so the wider world genuinely
 * pushes back on the life being lived inside it, not just narrates around it.
 */
public record WorldEvent(
        String title,
        String description,
        List<String> allowedEras,
        Map<String, Integer> statEffects,
        Map<String, Integer> factionEffects
) {

    public WorldEvent {
        statEffects = statEffects == null ? Map.of() : statEffects;
        factionEffects = factionEffects == null ? Map.of() : factionEffects;
    }

    /** Convenience constructor for a pure flavor event with no mechanical effect. */
    public WorldEvent(String title, String description, List<String> allowedEras) {
        this(title, description, allowedEras, Map.of(), Map.of());
    }

    /** True for an event that can appear in any era. */
    public boolean isEraAllowed(String era) {
        return allowedEras.isEmpty() || allowedEras.contains(era);
    }

    public boolean hasEffects() {
        return !statEffects.isEmpty() || !factionEffects.isEmpty();
    }
}
