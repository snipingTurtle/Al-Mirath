package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;

/**
 * Something the player is known for.
 *
 * <p>Reputation was a number that quietly gated choices. This is the other
 * half of it: a specific deed, the people who saw it happen, and the name they
 * put to you afterwards. Two players with identical reputation scores can be
 * the Hand That Feeds and the one who emptied the granary, and a merchant
 * should not greet them the same way.
 */
public enum Renown {

    // ---- the street -----------------------------------------------------

    HAND_THAT_FEEDS(
            "The Hand That Feeds",
            true,
            "commonPeople",
            8,
            List.of(
                    "fed_people_during_riot",
                    "built_a_common_granary",
                    "rationed_the_grain",
                    "named_the_hungry"
            )
    ),

    GRAIN_HOARDER(
            "The One Who Sold the Grain",
            false,
            "commonPeople",
            9,
            List.of(
                    "profited_from_famine",
                    "joined_the_hoarders",
                    "emptied_the_granary",
                    "sided_with_order_against_hungry"
            )
    ),

    FRIEND_OF_THE_VILLAGE(
            "Friend of the Village",
            true,
            "commonPeople",
            5,
            List.of(
                    "protected_commoners",
                    "quietly_helped_the_village",
                    "sheltered_the_fallen",
                    "returned_to_the_village"
            )
    ),

    WHO_WALKED_PAST(
            "The One Who Walked Past",
            false,
            "commonPeople",
            6,
            List.of("walked_past_the_child", "refused_the_village")
    ),

    // ---- the court ------------------------------------------------------

    SAVED_THE_THRONE(
            "Shield of the Throne",
            true,
            "court",
            10,
            List.of("saved_the_throne", "took_throne", "became_regent")
    ),

    COURT_INFORMANT(
            "The Court's Ear",
            false,
            "court",
            8,
            List.of(
                    "became_an_informant",
                    "exposed_as_informant",
                    "became_double_agent",
                    "sold_palace_secret"
            )
    ),

    // ---- the scholars ---------------------------------------------------

    HONEST_CHRONICLER(
            "Keeper of the Honest Record",
            true,
            "scholars",
            7,
            List.of(
                    "endowed_an_honest_chronicle",
                    "wrote_an_honest_account",
                    "faithful_copyist",
                    "known_for_fairness"
            )
    ),

    CORRUPTER_OF_TEXTS(
            "The One Who Altered the Text",
            false,
            "scholars",
            8,
            List.of(
                    "corrupted_a_text",
                    "biased_scholar_judgment",
                    "family_record_forged"
            )
    ),

    // ---- the merchants --------------------------------------------------

    GOLDEN_HAND(
            "The Golden Hand",
            true,
            "merchants",
            6,
            List.of(
                    "first_caravan_paid",
                    "built_a_credit_network",
                    "endowed_a_caravanserai",
                    "paid_debt_honestly"
            )
    ),

    WHO_REPORTED_THE_TRADE(
            "The One Who Reported the Trade",
            false,
            "merchants",
            6,
            List.of("reported_merchants", "profited_from_merchants")
    ),

    // ---- the army -------------------------------------------------------

    HELD_THE_LINE(
            "The One Who Held",
            true,
            "military",
            9,
            List.of(
                    "held_the_ribat",
                    "held_the_corridor",
                    "disciplined_soldiers",
                    "rebuilt_the_walls"
            )
    ),

    RULED_BY_FEAR(
            "The Butcher",
            false,
            "military",
            9,
            List.of(
                    "ruled_soldiers_by_fear",
                    "made_an_example_of",
                    "raided_across_the_line"
            )
    ),

    // ---- the shadows ----------------------------------------------------

    KNIFE_IN_THE_DARK(
            "The Knife in the Dark",
            false,
            "shadowNetwork",
            10,
            List.of(
                    "framed_an_innocent_man",
                    "framed_innocent",
                    "used_shadow_contacts",
                    "entered_city_through_shadows"
            )
    ),

    // ---- the nobility ---------------------------------------------------

    RESTORED_THE_NAME(
            "Restorer of the Name",
            true,
            "nobles",
            7,
            List.of(
                    "family_name_restored",
                    "exiled_branch_restored",
                    "legacy_of_restoration"
            )
    ),

    OATHBREAKER(
            "The Oathbreaker",
            false,
            "nobles",
            9,
            List.of(
                    "betrayed_the_in_laws",
                    "dishonoured_a_letter",
                    "denounced_the_old_house",
                    "betrayed_comrade"
            )
    ),

    // ---- the household --------------------------------------------------

    PILLAR_OF_THE_HOUSE(
            "Pillar of the House",
            true,
            "familyCouncil",
            5,
            List.of(
                    "taught_family_unity",
                    "family_peacemaker",
                    "stood_by_father",
                    "devoted_parent"
            )
    ),

    TURNED_ON_THEIR_OWN(
            "The One Who Turned on Their Own",
            false,
            "familyCouncil",
            8,
            List.of(
                    "brother_removed",
                    "brother_exiled",
                    "family_record_destroyed"
            )
    );

    private final String epithet;
    private final boolean admired;
    private final String witness;
    private final int weight;
    private final List<String> deeds;

    Renown(
            String epithet,
            boolean admired,
            String witness,
            int weight,
            List<String> deeds
    ) {
        this.epithet = epithet;
        this.admired = admired;
        this.witness = witness;
        this.weight = weight;
        this.deeds = deeds;
    }

    /** Every deed that earns any renown, mapped to the renown it earns. */
    public static Map<String, Renown> byDeed() {
        Map<String, Renown> index = new java.util.HashMap<>();

        for (Renown renown : values()) {
            for (String deed : renown.deeds) {
                index.put(deed, renown);
            }
        }

        return Map.copyOf(index);
    }

    /** What people call you for it. */
    public String epithet() {
        return epithet;
    }

    /** True when the name is one people say to your face. */
    public boolean isAdmired() {
        return admired;
    }

    /** The faction that was there, and so knows it first. */
    public String witness() {
        return witness;
    }

    /** How notable the deed is, deciding which name sticks hardest. */
    public int weight() {
        return weight;
    }

    public List<String> deeds() {
        return deeds;
    }

    /**
     * How someone from a particular circle opens with you.
     *
     * <p>The same name lands differently depending on who is saying it: a
     * merchant prices it, a soldier decides whether to follow you, the court
     * decides where to seat you. One line, but it is the line the whole system
     * exists to produce.
     */
    public String greeting(String audience) {
        String name = "\"" + epithet + ".\"";

        return switch (audience == null ? "" : audience) {
            case "commonPeople" -> admired
                    ? "Someone in the crowd says it before you can give your own name — "
                            + name + " Room is made for you."
                    : "You hear it said behind you as you pass — " + name
                            + " The space around you widens.";

            case "court" -> admired
                    ? "You are announced as " + name
                            + " The room rearranges itself slightly in your favour."
                    : "You are announced correctly, and then announced again, quietly, as "
                            + name + " You are seated accordingly.";

            case "scholars" -> admired
                    ? "You are introduced with a citation rather than a title: " + name
                            + " It is meant as the higher compliment."
                    : "Your name is given, then followed by " + name
                            + " Nobody disputes it, which is the problem.";

            case "merchants" -> admired
                    ? "The price you are quoted is not the price the man before you was quoted. "
                            + name + " buys a little goodwill here."
                    : "The price you are quoted is worse than the one before you got. "
                            + name + " costs money in this quarter.";

            case "military" -> admired
                    ? "The guard straightens without being told to. " + name
                            + " has reached the barracks ahead of you."
                    : "The guard does not straighten. " + name
                            + " has reached the barracks ahead of you.";

            case "shadowNetwork" -> admired
                    ? "They know the name and use it early, to show you they knew it early: "
                            + name
                    : "They greet you with " + name
                            + " They mean it as a recommendation.";

            case "familyCouncil" -> admired
                    ? "They have heard what you are called now — " + name
                            + " — and they are careful about how pleased they look."
                    : "They have heard what you are called now — " + name
                            + " — and nobody in the household says it aloud.";

            default -> admired
                    ? name + " They say it as a greeting."
                    : name + " They say it, and it is not a greeting.";
        };
    }
}
