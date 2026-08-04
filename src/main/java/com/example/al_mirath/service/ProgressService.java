package com.example.al_mirath.service;

import com.example.al_mirath.dao.ProgressDAO;
import com.example.al_mirath.model.Achievement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * In-memory front for cross-run progress, backed by {@link ProgressDAO}.
 *
 * <p>Unlocks and counters are cached on first use so the UI can read them every
 * frame without touching the database. Writes go to both the cache and disk.
 * A listener can be registered to raise the unlock toast.
 */
public final class ProgressService {

    // Statistic keys.
    public static final String STAT_LIVES_COMPLETED = "lives_completed";
    public static final String STAT_CHOICES_MADE = "choices_made";
    public static final String STAT_REWINDS_WON = "rewinds_won";
    public static final String STAT_REWINDS_LOST = "rewinds_lost";
    public static final String STAT_TITLES_EARNED = "titles_earned";
    public static final String STAT_OLDEST_AGE = "oldest_age";
    public static final String STAT_HIGHEST_SCORE = "highest_score";

    private static final ProgressDAO DAO = new ProgressDAO();

    private static Set<String> unlocked;
    private static Map<String, Integer> statistics;

    private static Consumer<Achievement> unlockListener;

    private ProgressService() {
    }

    /** Registers the callback used to surface a newly earned achievement. */
    public static void setUnlockListener(Consumer<Achievement> listener) {
        unlockListener = listener;
    }

    public static synchronized Set<String> unlockedIds() {
        if (unlocked == null) {
            unlocked = new LinkedHashSet<>(DAO.loadUnlockedAchievements());
        }

        return unlocked;
    }

    public static boolean isUnlocked(String id) {
        return unlockedIds().contains(id);
    }

    /**
     * Unlocks an achievement if it is not already held.
     * Notifies the listener only on a genuine first unlock.
     */
    public static synchronized void unlock(String id) {
        if (AchievementLibrary.byId(id) == null) {
            System.out.println("Unknown achievement id: " + id);
            return;
        }

        if (unlockedIds().contains(id)) {
            return;
        }

        unlockedIds().add(id);
        DAO.unlockAchievement(id);

        if (unlockListener != null) {
            unlockListener.accept(AchievementLibrary.byId(id));
        }
    }

    /** Unlocks only when the condition holds, to keep call sites terse. */
    public static void unlockIf(boolean condition, String id) {
        if (condition) {
            unlock(id);
        }
    }

    public static synchronized Map<String, Integer> statistics() {
        if (statistics == null) {
            statistics = new LinkedHashMap<>(DAO.loadStatistics());
        }

        return statistics;
    }

    public static int statistic(String key) {
        return statistics().getOrDefault(key, 0);
    }

    public static synchronized void increment(String key, int amount) {
        if (amount == 0) {
            return;
        }

        statistics().merge(key, amount, Integer::sum);
        DAO.incrementStatistic(key, amount);
    }

    public static synchronized void recordMaximum(String key, int value) {
        int current = statistics().getOrDefault(key, 0);

        if (value <= current) {
            return;
        }

        statistics().put(key, value);
        DAO.recordMaximum(key, value);
    }

    /** Tracks which eras have been completed, for the Across the Ages unlock. */
    public static synchronized void recordEraCompleted(String era) {
        if (era == null || era.isBlank()) {
            return;
        }

        String key = "era_" + era.toLowerCase().replace(' ', '_');

        if (statistics().getOrDefault(key, 0) > 0) {
            return;
        }

        statistics().put(key, 1);
        DAO.incrementStatistic(key, 1);
    }

    public static synchronized int erasCompleted() {
        int count = 0;

        for (Map.Entry<String, Integer> entry : statistics().entrySet()) {
            if (entry.getKey().startsWith("era_") && entry.getValue() > 0) {
                count++;
            }
        }

        return count;
    }

    public static int unlockedCount() {
        return unlockedIds().size();
    }

    public static List<Achievement> unlockedAchievements() {
        List<Achievement> result = new ArrayList<>();

        for (Achievement achievement : AchievementLibrary.all()) {
            if (isUnlocked(achievement.id())) {
                result.add(achievement);
            }
        }

        return result;
    }

    /** Wipes achievements and statistics. Used by the settings screen. */
    public static synchronized void resetAll() {
        DAO.resetProgress();
        unlocked = new LinkedHashSet<>();
        statistics = new LinkedHashMap<>();
    }
}
