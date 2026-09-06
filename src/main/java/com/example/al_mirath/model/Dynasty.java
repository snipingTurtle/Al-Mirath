package com.example.al_mirath.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The house a run belongs to, and everyone who has already carried it.
 *
 * <p>A life used to end and take its world with it. A dynasty is the thing
 * that does not end: the house keeps its name, its generation count, and the
 * roll of the people who held it. The heir inherits a world that remembers
 * what their forebears did in it, which is the whole point of continuing
 * rather than restarting.
 */
public class Dynasty {

    /** One life that has already been lived under this name. */
    public record Forebear(
            String name,
            String knownAs,
            int ageAtDeath,
            String ending,
            int generation
    ) {

        /** The line this forebear gets in the chronicle. */
        public String line() {
            String styled = knownAs == null || knownAs.isBlank() ? "" : ", " + knownAs;

            return name + styled + " — died at " + ageAtDeath + ". " + ending;
        }
    }

    private final String houseName;
    private final List<Forebear> line = new ArrayList<>();

    private int generation = 1;

    public Dynasty(String houseName) {
        this.houseName = houseName;
    }

    /** Names the house after whoever founded it. */
    public static Dynasty foundedBy(PlayerCharacter founder) {
        return new Dynasty(
                founder == null ? "an unnamed house" : "the House of " + founder.getName()
        );
    }

    public String houseName() {
        return houseName;
    }

    /** 1 for the founder, 2 for their heir, and so on. */
    public int generation() {
        return generation;
    }

    public List<Forebear> line() {
        return List.copyOf(line);
    }

    /**
     * Closes one life and opens the next.
     *
     * @param knownAs the forged title the life was led with, if it earned one
     */
    public void succeed(PlayerCharacter forebear, String knownAs, String ending) {
        if (forebear != null) {
            line.add(new Forebear(
                    forebear.getName(),
                    knownAs == null ? "" : knownAs,
                    forebear.getAge(),
                    ending == null || ending.isBlank() ? "Their life ended." : ending,
                    generation
            ));
        }

        generation++;
    }

    /** How the house is styled now: "the House of Yusuf, third generation". */
    public String styling() {
        return houseName + ", " + ordinal(generation) + " generation";
    }

    private static String ordinal(int value) {
        return switch (value) {
            case 1 -> "first";
            case 2 -> "second";
            case 3 -> "third";
            case 4 -> "fourth";
            case 5 -> "fifth";
            case 6 -> "sixth";
            case 7 -> "seventh";
            case 8 -> "eighth";
            case 9 -> "ninth";
            case 10 -> "tenth";
            default -> value + "th";
        };
    }

    /** Everyone who has held the name, oldest first. */
    public String chronicle() {
        if (line.isEmpty()) {
            return "You are the first of your name.";
        }

        StringBuilder text = new StringBuilder();

        for (Forebear forebear : line) {
            if (!text.isEmpty()) {
                text.append("\n\n");
            }

            text.append(forebear.line());
        }

        return text.toString();
    }

    // ---- persistence -----------------------------------------------------

    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        root.put("houseName", houseName);
        root.put("generation", generation);

        JSONArray forebears = new JSONArray();

        for (Forebear forebear : line) {
            JSONObject entry = new JSONObject();

            entry.put("name", forebear.name());
            entry.put("knownAs", forebear.knownAs());
            entry.put("ageAtDeath", forebear.ageAtDeath());
            entry.put("ending", forebear.ending());
            entry.put("generation", forebear.generation());

            forebears.put(entry);
        }

        root.put("line", forebears);

        return root;
    }

    public static Dynasty fromJson(JSONObject root) {
        if (root == null) {
            return null;
        }

        Dynasty dynasty = new Dynasty(root.optString("houseName", "an unnamed house"));
        dynasty.generation = Math.max(1, root.optInt("generation", 1));

        JSONArray forebears = root.optJSONArray("line");

        if (forebears != null) {
            for (int i = 0; i < forebears.length(); i++) {
                JSONObject entry = forebears.getJSONObject(i);

                dynasty.line.add(new Forebear(
                        entry.optString("name", "Someone"),
                        entry.optString("knownAs", ""),
                        entry.optInt("ageAtDeath", 0),
                        entry.optString("ending", "Their life ended."),
                        entry.optInt("generation", 1)
                ));
            }
        }

        return dynasty;
    }
}
