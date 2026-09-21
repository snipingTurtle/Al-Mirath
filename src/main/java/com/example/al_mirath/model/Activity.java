package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Something the player decides to do with a year, rather than something the
 * year does to them.
 *
 * <p>Narrative events arrive and ask to be answered. Activities are the other
 * half of a life: the player opens a menu and chooses to study, to trade, to
 * train at the butts, to pray. Their outcome is either certain (sitting with
 * your household is never a failure) or earned by playing a
 * {@link com.example.al_mirath.minigame.MiniGame} — which is what stops an
 * activity menu from being a list of buttons that add numbers.
 *
 * <p>Built through {@link #of} rather than a constructor, because the
 * catalogue in {@code ActivityLibrary} would be unreadable as forty calls to
 * a twenty-argument constructor.
 */
public final class Activity {

    // ── Category constants ──────────────────────────────────────────────

    public static final String CAT_KNOWLEDGE = "Knowledge";
    public static final String CAT_TRADE     = "Trade";
    public static final String CAT_MILITARY  = "Arms";
    public static final String CAT_COURT     = "Court";
    public static final String CAT_FAITH     = "Faith";
    public static final String CAT_SHADOW    = "Shadow";
    public static final String CAT_FAMILY    = "Household";
    public static final String CAT_LEISURE   = "Leisure";

    /** The order the activity menu shows its sections in. */
    public static final List<String> CATEGORY_ORDER = List.of(
            CAT_FAITH,
            CAT_KNOWLEDGE,
            CAT_TRADE,
            CAT_MILITARY,
            CAT_COURT,
            CAT_FAMILY,
            CAT_LEISURE,
            CAT_SHADOW
    );

    // ── Fields ──────────────────────────────────────────────────────────

    private final String id;
    private final String name;
    private final String description;
    private final String category;

    /** null means the outcome is certain; otherwise a mini-game decides it. */
    private final String miniGameType;
    private final int difficulty;

    private final Map<String, Integer> successStatEffects;
    private final Map<String, Integer> failureStatEffects;
    private final Map<String, Integer> successFactionEffects;
    private final Map<String, Integer> failureFactionEffects;

    private final String successText;
    private final String failureText;

    private final List<String> successFlags;
    private final List<String> failureFlags;

    private final int minAge;
    private final int maxAge;
    private final String requiredStat;
    private final int requiredStatMin;
    private final int wealthCost;

    /** Coin earned on a good outcome, and lost on a bad one. */
    private final int successPay;
    private final int failurePay;

    private final Career requiredCareer;
    private final boolean requiresEmployment;
    private final boolean requiresChild;
    private final boolean oncePerLife;

    private Activity(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.category = builder.category;
        this.miniGameType = builder.miniGameType;
        this.difficulty = builder.difficulty;
        this.successStatEffects = Map.copyOf(builder.successStatEffects);
        this.failureStatEffects = Map.copyOf(builder.failureStatEffects);
        this.successFactionEffects = Map.copyOf(builder.successFactionEffects);
        this.failureFactionEffects = Map.copyOf(builder.failureFactionEffects);
        this.successText = builder.successText;
        this.failureText = builder.failureText;
        this.successFlags = List.copyOf(builder.successFlags);
        this.failureFlags = List.copyOf(builder.failureFlags);
        this.minAge = builder.minAge;
        this.maxAge = builder.maxAge;
        this.requiredStat = builder.requiredStat;
        this.requiredStatMin = builder.requiredStatMin;
        this.wealthCost = builder.wealthCost;
        this.successPay = builder.successPay;
        this.failurePay = builder.failurePay;
        this.requiredCareer = builder.requiredCareer;
        this.requiresEmployment = builder.requiresEmployment;
        this.requiresChild = builder.requiresChild;
        this.oncePerLife = builder.oncePerLife;
    }

    /** Opens a builder. Everything not set has a sensible neutral default. */
    public static Builder of(String id, String name, String category) {
        return new Builder(id, name, category);
    }

    // ── Queries ─────────────────────────────────────────────────────────

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public String category() { return category; }
    public String miniGameType() { return miniGameType; }
    public int difficulty() { return difficulty; }
    public Map<String, Integer> successStatEffects() { return successStatEffects; }
    public Map<String, Integer> failureStatEffects() { return failureStatEffects; }
    public Map<String, Integer> successFactionEffects() { return successFactionEffects; }
    public Map<String, Integer> failureFactionEffects() { return failureFactionEffects; }
    public String successText() { return successText; }
    public String failureText() { return failureText; }
    public List<String> successFlags() { return successFlags; }
    public List<String> failureFlags() { return failureFlags; }
    public int minAge() { return minAge; }
    public int maxAge() { return maxAge; }
    public String requiredStat() { return requiredStat; }
    public int requiredStatMin() { return requiredStatMin; }
    public int wealthCost() { return wealthCost; }
    public int successPay() { return successPay; }
    public int failurePay() { return failurePay; }
    public Career requiredCareer() { return requiredCareer; }
    public boolean requiresEmployment() { return requiresEmployment; }
    public boolean requiresChild() { return requiresChild; }
    public boolean oncePerLife() { return oncePerLife; }

    /** Whether attempting this opens a mini-game or simply resolves. */
    public boolean requiresMiniGame() {
        return miniGameType != null;
    }

    /**
     * Why this cannot be attempted right now, or an empty string when it can.
     *
     * <p>The reason is shown on the greyed-out row rather than hidden, so a
     * player can see what they are working toward instead of wondering why
     * half the menu is dead.
     *
     * @param hasChild whether the household contains a living child
     * @param doneBefore whether a once-in-a-life activity has already been done
     */
    public String lockedReason(PlayerCharacter player, boolean hasChild, boolean doneBefore) {
        if (player == null) {
            return "Not now.";
        }

        if (oncePerLife && doneBefore) {
            return "Once in a life is enough.";
        }

        if (player.hasSpentThisYear(id)) {
            return "Already done this year.";
        }

        if (player.getAge() < minAge) {
            return "Not until you are " + minAge + ".";
        }

        if (player.getAge() > maxAge) {
            return "You are past the age for this.";
        }

        if (requiredStat != null && player.getStatValue(requiredStat) < requiredStatMin) {
            return "Needs " + statName(requiredStat) + " " + requiredStatMin + ".";
        }

        if (requiresEmployment && !player.isEmployed()) {
            return "Needs a trade.";
        }

        if (requiredCareer != null && player.getCareer() != requiredCareer) {
            return "Only for a " + requiredCareer.displayName().toLowerCase() + ".";
        }

        if (requiresChild && !hasChild) {
            return "You have no children.";
        }

        if (wealthCost > 0 && player.getNetWorth() < wealthCost) {
            return "Costs " + wealthCost + " dirhams.";
        }

        return "";
    }

    private static String statName(String stat) {
        return switch (stat) {
            case "politicalPower" -> "Influence";
            case "familyLoyalty" -> "Family";
            default -> stat.substring(0, 1).toUpperCase() + stat.substring(1);
        };
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    // ── Builder ─────────────────────────────────────────────────────────

    /** Fluent construction, so the catalogue reads like content rather than code. */
    public static final class Builder {

        private final String id;
        private final String name;
        private final String category;

        private String description = "";
        private String miniGameType = null;
        private int difficulty = 2;

        private final Map<String, Integer> successStatEffects = new LinkedHashMap<>();
        private final Map<String, Integer> failureStatEffects = new LinkedHashMap<>();
        private final Map<String, Integer> successFactionEffects = new LinkedHashMap<>();
        private final Map<String, Integer> failureFactionEffects = new LinkedHashMap<>();

        private String successText = "It went well.";
        private String failureText = "It came to nothing.";

        private final List<String> successFlags = new ArrayList<>();
        private final List<String> failureFlags = new ArrayList<>();

        private int minAge = 0;
        private int maxAge = 200;
        private String requiredStat = null;
        private int requiredStatMin = 0;
        private int wealthCost = 0;
        private int successPay = 0;
        private int failurePay = 0;

        private Career requiredCareer = null;
        private boolean requiresEmployment = false;
        private boolean requiresChild = false;
        private boolean oncePerLife = false;

        private Builder(String id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }

        public Builder describe(String description) {
            this.description = description;
            return this;
        }

        /** Attaches a mini-game; without one the outcome is certain. */
        public Builder trial(String miniGameType, int difficulty) {
            this.miniGameType = miniGameType;
            this.difficulty = Math.max(1, Math.min(5, difficulty));
            return this;
        }

        public Builder ages(int minAge, int maxAge) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            return this;
        }

        public Builder from(int minAge) {
            this.minAge = minAge;
            return this;
        }

        public Builder needs(String stat, int minimum) {
            this.requiredStat = stat;
            this.requiredStatMin = minimum;
            return this;
        }

        public Builder costs(int dirhams) {
            this.wealthCost = dirhams;
            return this;
        }

        public Builder pays(int onSuccess, int onFailure) {
            this.successPay = onSuccess;
            this.failurePay = onFailure;
            return this;
        }

        public Builder onlyFor(Career career) {
            this.requiredCareer = career;
            return this;
        }

        public Builder needsWork() {
            this.requiresEmployment = true;
            return this;
        }

        public Builder needsChild() {
            this.requiresChild = true;
            return this;
        }

        public Builder once() {
            this.oncePerLife = true;
            return this;
        }

        public Builder won(String text) {
            this.successText = text;
            return this;
        }

        public Builder lost(String text) {
            this.failureText = text;
            return this;
        }

        public Builder stat(String stat, int delta) {
            successStatEffects.put(stat, delta);
            return this;
        }

        public Builder statOnFailure(String stat, int delta) {
            failureStatEffects.put(stat, delta);
            return this;
        }

        public Builder faction(String faction, int delta) {
            successFactionEffects.put(faction, delta);
            return this;
        }

        public Builder factionOnFailure(String faction, int delta) {
            failureFactionEffects.put(faction, delta);
            return this;
        }

        public Builder flag(String... flags) {
            for (String flag : flags) {
                successFlags.add(flag);
            }
            return this;
        }

        public Builder flagOnFailure(String... flags) {
            for (String flag : flags) {
                failureFlags.add(flag);
            }
            return this;
        }

        public Activity build() {
            return new Activity(this);
        }
    }
}
