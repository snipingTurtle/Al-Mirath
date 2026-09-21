package com.example.al_mirath.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A piece of property owned by the player character — a house, shop,
 * farmland, or workshop that generates passive income and prestige.
 *
 * <p>Condition degrades each year and affects income. War, plague, and
 * political upheaval can damage properties. The player can spend wealth
 * to maintain them.</p>
 */
public class Property {

    // ── Property types ──────────────────────────────────────────────────

    public enum Type {
        HOUSE("House",
                "A dwelling for your household.",
                0, -3, 5, 500),
        SHOP("Shop",
                "A storefront in the bazaar quarter.",
                40, 0, 3, 800),
        FARMLAND("Farmland",
                "Arable land outside the city walls.",
                60, 0, 2, 600),
        WORKSHOP("Workshop",
                "A craftsman's workshop producing goods.",
                80, 0, 4, 1200),
        CARAVANSARY("Caravansary",
                "An inn and warehouse for travelling merchants.",
                150, 0, 6, 3000),
        BATHHOUSE("Bathhouse",
                "A public bathhouse — a place of commerce and culture.",
                120, -2, 5, 2500),
        GARDEN("Garden",
                "A walled garden — a refuge from the city's dust.",
                20, -5, 3, 400);

        private final String displayName;
        private final String description;
        private final int baseIncome;
        private final int stressModifier;
        private final int prestige;
        private final int basePrice;

        Type(String displayName, String description, int baseIncome,
             int stressModifier, int prestige, int basePrice) {
            this.displayName = displayName;
            this.description = description;
            this.baseIncome = baseIncome;
            this.stressModifier = stressModifier;
            this.prestige = prestige;
            this.basePrice = basePrice;
        }

        public String displayName() { return displayName; }
        public String description() { return description; }
        public int baseIncome() { return baseIncome; }
        public int stressModifier() { return stressModifier; }
        public int prestige() { return prestige; }
        public int basePrice() { return basePrice; }
    }

    // ── Fields ──────────────────────────────────────────────────────────

    private final String name;
    private final Type type;
    private final int purchasePrice;
    private int condition;         // 0-100, affects effective income

    private static final int ANNUAL_DEGRADATION = 3;
    private static final int MIN_USEFUL_CONDITION = 15;

    // ── Construction ────────────────────────────────────────────────────

    public Property(String name, Type type, int purchasePrice) {
        this.name = name;
        this.type = type;
        this.purchasePrice = purchasePrice;
        this.condition = 100;
    }

    public Property(String name, Type type, int purchasePrice, int condition) {
        this.name = name;
        this.type = type;
        this.purchasePrice = purchasePrice;
        this.condition = Math.max(0, Math.min(100, condition));
    }

    // ── Queries ─────────────────────────────────────────────────────────

    public String name() { return name; }
    public Type type() { return type; }
    public int purchasePrice() { return purchasePrice; }
    public int condition() { return condition; }

    /**
     * Effective annual income, scaled by property condition.
     * A property at 100 condition earns full base income;
     * below {@value MIN_USEFUL_CONDITION} it earns nothing.
     */
    public int effectiveIncome() {
        if (condition < MIN_USEFUL_CONDITION) return 0;
        return (int) (type.baseIncome * (condition / 100.0));
    }

    /** Annual stress modifier (negative means stress reduction). */
    public int stressEffect() {
        if (condition < MIN_USEFUL_CONDITION) return 0;
        return type.stressModifier;
    }

    /**
     * What a buyer would pay for it today.
     *
     * <p>Property never fetches what it cost — the sale is quick, the buyer
     * knows it, and a ruin fetches little more than the ground under it.
     */
    public int resaleValue() {
        return (int) (purchasePrice * 0.70 * (0.35 + 0.65 * (condition / 100.0)));
    }

    /** Prestige contribution toward reputation. */
    public int prestige() {
        return condition >= 50 ? type.prestige : type.prestige / 2;
    }

    /** User-facing summary line for drawers and logs. */
    public String summary() {
        String status;
        if (condition >= 80) status = "Excellent";
        else if (condition >= 60) status = "Good";
        else if (condition >= 40) status = "Fair";
        else if (condition >= MIN_USEFUL_CONDITION) status = "Poor";
        else status = "Ruined";
        return String.format("%s (%s) — %s, Income: %d/yr",
                name, type.displayName, status, effectiveIncome());
    }

    // ── Mutations ───────────────────────────────────────────────────────

    /** Annual natural degradation. */
    public void degrade() {
        condition = Math.max(0, condition - ANNUAL_DEGRADATION);
    }

    /**
     * What the masons would want to put this right, without doing it.
     *
     * <p>Quoted separately from {@link #repair()} so a caller can show the
     * price and check the purse before the work is committed to.
     */
    public int repairQuote() {
        int deficit = 100 - condition;

        if (deficit <= 0) {
            return 0;
        }

        return deficit * (purchasePrice / 200 + 1);
    }

    /**
     * Does the work.
     * @return what it cost, which is {@link #repairQuote()} at the time
     */
    public int repair() {
        int cost = repairQuote();
        condition = 100;
        return cost;
    }

    /** Apply damage from war, plague, fire, etc. */
    public void damage(int amount) {
        condition = Math.max(0, condition - Math.abs(amount));
    }

    // ── Serialization ───────────────────────────────────────────────────

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("type", type.name());
        obj.put("purchasePrice", purchasePrice);
        obj.put("condition", condition);
        return obj;
    }

    public static Property fromJson(JSONObject obj) {
        return new Property(
                obj.getString("name"),
                Type.valueOf(obj.getString("type")),
                obj.getInt("purchasePrice"),
                obj.getInt("condition")
        );
    }

    public static JSONArray listToJson(List<Property> properties) {
        JSONArray arr = new JSONArray();
        for (Property p : properties) arr.put(p.toJson());
        return arr;
    }

    public static List<Property> listFromJson(JSONArray arr) {
        List<Property> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(fromJson(arr.getJSONObject(i)));
        }
        return list;
    }
}
