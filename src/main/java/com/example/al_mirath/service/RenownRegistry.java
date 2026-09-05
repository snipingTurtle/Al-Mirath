package com.example.al_mirath.service;

import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.Renown;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * What the player is known for, and how far it has travelled.
 *
 * <p>The suggestion this implements is not "give the player titles" — the game
 * already had those. It is that reputation should <em>spread</em>. A deed
 * starts known only to the people who were standing there. Whether the rest of
 * the world ever hears about it depends on how notable the deed was, how
 * well-known the player already is, and how many years have passed since.
 *
 * <p>Which is why the same act can be common knowledge in one run and a thing
 * nobody outside the room remembers in another.
 */
public final class RenownRegistry {

    /** A deed is this far along the moment it happens. */
    private static final int REACH_AT_THE_TIME = 15;

    /** Reach at which a story has escaped the people who witnessed it. */
    private static final int REACH_BEYOND_WITNESSES = 50;

    /**
     * The shadow network trades in things that have not spread yet, so they
     * hear a story well before the street does.
     */
    private static final int REACH_FOR_THE_SHADOWS = 25;

    private static final String SHADOW_NETWORK = "shadowNetwork";

    /** A famous name carries a story further each year than an unknown one. */
    private static final int REPUTATION_PER_EXTRA_YEAR_OF_TRAVEL = 25;

    private static final Map<String, Renown> BY_DEED = Renown.byDeed();

    private final Map<Renown, Integer> reach = new EnumMap<>(Renown.class);

    /**
     * Notes a deed, if it is one anybody would repeat.
     *
     * @return true when this was a deed that earns a name
     */
    public boolean record(String flag) {
        Renown earned = BY_DEED.get(flag);

        if (earned == null) {
            return false;
        }

        // Doing the same kind of thing twice does not restart the story, it
        // gives it a push.
        reach.merge(
                earned,
                REACH_AT_THE_TIME,
                (existing, added) -> clamp(existing + added / 2)
        );

        return true;
    }

    /** Carries every story a little further. */
    public void spread(int years, PlayerCharacter player) {
        if (years <= 0 || reach.isEmpty()) {
            return;
        }

        int fame = player == null ? 0 : player.getReputation();

        for (Map.Entry<Renown, Integer> story : reach.entrySet()) {
            int perYear =
                    1
                            + story.getKey().weight() / 3
                            + fame / REPUTATION_PER_EXTRA_YEAR_OF_TRAVEL;

            story.setValue(clamp(story.getValue() + perYear * years));
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // ---- who knows what --------------------------------------------------

    /** How far this story has travelled, 0 when it never happened. */
    public int reachOf(Renown renown) {
        return reach.getOrDefault(renown, 0);
    }

    /** True when someone of this faction would have heard the story. */
    public boolean isKnownTo(Renown renown, String faction) {
        int travelled = reachOf(renown);

        if (travelled <= 0) {
            return false;
        }

        if (renown.witness().equals(faction)) {
            return true;
        }

        int needed =
                SHADOW_NETWORK.equals(faction)
                        ? REACH_FOR_THE_SHADOWS
                        : REACH_BEYOND_WITNESSES;

        return travelled >= needed;
    }

    /** Everything this faction has heard, most notable first. */
    public List<Renown> knownTo(String faction) {
        List<Renown> known = new ArrayList<>();

        for (Renown renown : reach.keySet()) {
            if (isKnownTo(renown, faction)) {
                known.add(renown);
            }
        }

        known.sort((a, b) -> notabilityOf(b) - notabilityOf(a));

        return known;
    }

    /**
     * The single thing this faction most associates with the player, or null
     * when they have heard nothing.
     */
    public Renown bestKnownTo(String faction) {
        List<Renown> known = knownTo(faction);

        return known.isEmpty() ? null : known.get(0);
    }

    /**
     * The name the player carries generally: the most notable story that has
     * escaped the people who witnessed it.
     */
    public Renown publicName() {
        Renown strongest = null;

        for (Renown renown : reach.keySet()) {
            if (reachOf(renown) < REACH_BEYOND_WITNESSES) {
                continue;
            }

            if (strongest == null
                    || notabilityOf(renown) > notabilityOf(strongest)) {

                strongest = renown;
            }
        }

        return strongest;
    }

    /** A story is notable for what it was and for how far it has got. */
    private int notabilityOf(Renown renown) {
        return renown.weight() * 10 + reachOf(renown);
    }

    /**
     * How a member of this faction opens with the player.
     *
     * @return null when they have heard nothing, so callers can leave the
     *         scene as it was rather than inventing a greeting
     */
    public String greetingFrom(String faction) {
        Renown known = bestKnownTo(faction);

        return known == null ? null : known.greeting(faction);
    }

    /** The Renown panel's text. */
    public String renownSummary() {
        if (reach.isEmpty()) {
            return "";
        }

        List<Renown> stories = new ArrayList<>(reach.keySet());
        stories.sort((a, b) -> notabilityOf(b) - notabilityOf(a));

        StringBuilder result = new StringBuilder();

        for (Renown renown : stories) {
            if (!result.isEmpty()) {
                result.append("\n\n");
            }

            result.append(renown.epithet())
                    .append("\n")
                    .append(spreadLabel(reachOf(renown)));
        }

        return result.toString();
    }

    private String spreadLabel(int travelled) {
        if (travelled >= 90) {
            return "Everyone has heard it";
        }

        if (travelled >= REACH_BEYOND_WITNESSES) {
            return "Widely repeated";
        }

        if (travelled >= REACH_FOR_THE_SHADOWS) {
            return "Passing between those who trade in it";
        }

        return "Known only to those who were there";
    }

    // ---- persistence -----------------------------------------------------

    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        for (Map.Entry<Renown, Integer> story : reach.entrySet()) {
            root.put(story.getKey().name(), story.getValue());
        }

        return root;
    }

    public static RenownRegistry fromJson(JSONObject root) {
        RenownRegistry registry = new RenownRegistry();

        if (root == null) {
            return registry;
        }

        for (String key : root.keySet()) {
            try {
                registry.reach.put(
                        Renown.valueOf(key),
                        clamp(root.getInt(key))
                );
            } catch (IllegalArgumentException unknownRenown) {
                // A save from a build that knew a name this one does not.
            }
        }

        return registry;
    }

    public void replaceWith(RenownRegistry restored) {
        reach.clear();

        if (restored != null) {
            reach.putAll(restored.reach);
        }
    }
}
