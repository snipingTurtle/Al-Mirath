package com.example.al_mirath.service;

import com.example.al_mirath.model.Achievement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The full achievement catalogue, in display order.
 *
 * <p>IDs are permanent: they are the primary key in the database, so renaming
 * one would silently orphan every player's existing unlock.
 */
public final class AchievementLibrary {

    private static final Map<String, Achievement> ACHIEVEMENTS = new LinkedHashMap<>();

    // --- First steps ---
    public static final String FIRST_LIFE = "first_life";
    public static final String FIRST_CHOICE = "first_choice";
    public static final String FIVE_LIVES = "five_lives";
    public static final String TWENTY_LIVES = "twenty_lives";

    // --- Threads of Fate ---
    public static final String FIRST_REWIND = "first_rewind";
    public static final String SCRIBE_MASTER = "scribe_master";
    public static final String MERCHANT_MASTER = "merchant_master";
    public static final String COURIER_MASTER = "courier_master";
    public static final String THREAD_UNBROKEN = "thread_unbroken";
    public static final String NO_REGRETS = "no_regrets";

    // --- Stations in life ---
    public static final String TOOK_THRONE = "took_throne";
    public static final String BECAME_REGENT = "became_regent";
    public static final String RESPECTED_SCHOLAR = "respected_scholar";
    public static final String INFLUENTIAL_MERCHANT = "influential_merchant";
    public static final String MILITARY_COMMANDER = "military_commander";
    public static final String SHADOW_BROKER = "shadow_broker";

    // --- Moral extremes ---
    public static final String SAINT = "saint";
    public static final String TYRANT = "tyrant";
    public static final String INCORRUPTIBLE = "incorruptible";

    // --- Endings ---
    public static final String OLD_AGE = "old_age";
    public static final String DIED_YOUNG = "died_young";
    public static final String FORGOTTEN = "forgotten";
    public static final String OWNED_BY_SHADOWS = "owned_by_shadows";

    // --- Collection ---
    public static final String FIVE_TITLES = "five_titles";
    public static final String TEN_TITLES = "ten_titles";
    public static final String ALL_ERAS = "all_eras";

    static {
        add(Achievement.of(FIRST_CHOICE, "The First Step",
                "Make your first decision."));
        add(Achievement.of(FIRST_LIFE, "A Life Concluded",
                "Complete a full life from birth to ending."));
        add(Achievement.of(FIVE_LIVES, "The Chronicle Grows",
                "Complete five lives."));
        add(Achievement.of(TWENTY_LIVES, "Keeper of the Archive",
                "Complete twenty lives."));

        add(Achievement.of(FIRST_REWIND, "Destiny Rewritten",
                "Win a Threads of Fate challenge and undo a decision."));
        add(Achievement.of(SCRIBE_MASTER, "The Steady Hand",
                "Win the Scribe's Hand challenge."));
        add(Achievement.of(MERCHANT_MASTER, "Balanced to the Dirham",
                "Win the Merchant's Scales challenge."));
        add(Achievement.of(COURIER_MASTER, "Unseen in the Dark",
                "Win the Night Courier challenge."));
        add(Achievement.of(THREAD_UNBROKEN, "Threefold Fate",
                "Win three Threads of Fate challenges in a single life."));
        add(Achievement.of(NO_REGRETS, "No Regrets",
                "Complete a life without ever spending a Thread of Fate."));

        add(Achievement.of(TOOK_THRONE, "The Crown",
                "Seize the throne."));
        add(Achievement.of(BECAME_REGENT, "Power Behind the Throne",
                "Rule as regent instead of taking the crown."));
        add(Achievement.of(RESPECTED_SCHOLAR, "Light of the Madrasa",
                "Reach the standing of a Respected Scholar."));
        add(Achievement.of(INFLUENTIAL_MERCHANT, "Master of the Bazaar",
                "Reach the standing of an Influential Merchant."));
        add(Achievement.of(MILITARY_COMMANDER, "Sword of the Realm",
                "Reach the standing of a Military Commander."));
        add(Achievement.of(SHADOW_BROKER, "Whisper Lord",
                "Reach the standing of a Shadow Broker."));

        add(Achievement.of(SAINT, "The Righteous Path",
                "End a life with Morality of 90 or more."));
        add(Achievement.of(TYRANT, "Feared by All",
                "End a life with Morality of 10 or less."));
        add(Achievement.of(INCORRUPTIBLE, "Clean Hands",
                "Complete a life without ever using bribery or the shadow network."));

        add(Achievement.of(OLD_AGE, "The Long Road",
                "Live to the age of 85."));
        add(Achievement.hidden(DIED_YOUNG, "Cut Short",
                "Die before the age of 25."));
        add(Achievement.hidden(FORGOTTEN, "Unwritten",
                "End a life having earned no legacy title at all."));
        add(Achievement.hidden(OWNED_BY_SHADOWS, "The Debt Collected",
                "End a life owned by the shadow network."));

        add(Achievement.of(FIVE_TITLES, "A Name Remembered",
                "Earn five legacy titles across all your lives."));
        add(Achievement.of(TEN_TITLES, "A Name That Echoes",
                "Earn ten legacy titles across all your lives."));
        add(Achievement.of(ALL_ERAS, "Across the Ages",
                "Complete a life in each of the four eras."));
    }

    private AchievementLibrary() {
    }

    private static void add(Achievement achievement) {
        ACHIEVEMENTS.put(achievement.id(), achievement);
    }

    public static List<Achievement> all() {
        return List.copyOf(ACHIEVEMENTS.values());
    }

    public static Achievement byId(String id) {
        return ACHIEVEMENTS.get(id);
    }

    public static int total() {
        return ACHIEVEMENTS.size();
    }
}
