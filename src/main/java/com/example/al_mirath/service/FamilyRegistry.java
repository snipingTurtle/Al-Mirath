package com.example.al_mirath.service;

import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.PlayerCharacter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The household: who the player was born to, who they married, and who
 * came after.
 *
 * <p>The game already had a "family condition" — a phrase on the character
 * sheet that moved a few stats at birth and then meant nothing. This turns
 * that phrase into people. The condition still decides the shape of the
 * household the player opens with: an orphan starts without parents, a
 * disgraced bloodline starts estranged from them.
 */
public final class FamilyRegistry {

    private static final List<String> MALE_NAMES = List.of(
            "Bilal", "Jafar", "Musa", "Harun", "Uthman", "Anas",
            "Sufyan", "Malik", "Qasim", "Nuh", "Ayyub", "Idris"
    );

    private static final List<String> FEMALE_NAMES = List.of(
            "Khadija", "Asma", "Rabia", "Zahra", "Umm Kulthum", "Sawda",
            "Juwayriyya", "Habiba", "Zaynab al-Sughra", "Nusayba", "Rayhana", "Barira"
    );

    private static final List<String> TRAITS = List.of(
            "steady and unspectacular",
            "quick-tempered",
            "devout",
            "sharp with money",
            "easily led",
            "stubborn past all reason",
            "gentle, which the world punishes",
            "watchful",
            "generous to a fault",
            "proud of the family name"
    );

    /** Roughly how much older a parent is than the player. */
    private static final int PARENT_AGE_GAP = 26;

    private static final int MARRIAGE_AGE = 20;
    private static final int LAST_FERTILE_AGE = 55;
    private static final int MAX_CHILDREN = 4;
    private static final int GRANDPARENT_AGE = 25;

    /**
     * Years that must pass between births. Without it the whole brood arrived
     * inside six years and then the household never changed again.
     */
    private static final int YEARS_BETWEEN_BIRTHS = 3;

    private static final int BIRTH_CHANCE_PER_YEAR = 18;

    private final Map<String, FamilyMember> members = new LinkedHashMap<>();
    private final Random random;

    private int nextId = 1;

    /** The player's age when the last child arrived; -1 before any have. */
    private int lastBirthAge = -1;

    public FamilyRegistry() {
        this(new Random());
    }

    public FamilyRegistry(Random random) {
        this.random = random == null ? new Random() : random;
    }

    public static FamilyRegistry createFor(PlayerCharacter player) {
        FamilyRegistry registry = new FamilyRegistry();
        registry.generateInitialFamily(player);

        return registry;
    }

    // ---- birth ----------------------------------------------------------

    /**
     * Builds the household the player is born into, shaped by the family
     * condition they were generated with.
     */
    public void generateInitialFamily(PlayerCharacter player) {
        if (player == null || !members.isEmpty()) {
            return;
        }

        String condition = player.getFamilyCondition();
        boolean orphaned = "Recently Orphaned".equals(condition);
        int warmth = startingWarmth(condition);

        FamilyMember father = elder(
                Kinship.FATHER, player.getAge(), warmth, MALE_NAMES
        );

        FamilyMember mother = elder(
                Kinship.MOTHER, player.getAge(), warmth, FEMALE_NAMES
        );

        if (orphaned) {
            father.markDead("Died before you were old enough to remember much.");
            mother.markDead("Died before you were old enough to remember much.");
        }

        members.put(father.getId(), father);
        members.put(mother.getId(), mother);

        int siblings = random.nextInt(4);

        for (int i = 0; i < siblings; i++) {
            boolean brother = random.nextBoolean();

            FamilyMember sibling = new FamilyMember(
                    nextId(),
                    pickName(brother ? MALE_NAMES : FEMALE_NAMES),
                    Kinship.SIBLING,
                    pickTrait(),
                    Math.max(1, player.getAge() + random.nextInt(13) - 6),
                    warmth + random.nextInt(20) - 10,
                    true,
                    LifePath.UNDECIDED,
                    ""
            );

            members.put(sibling.getId(), sibling);
        }
    }

    /**
     * How close the household starts out. The condition the player was
     * generated with is the whole point of this: it used to be a label.
     */
    private int startingWarmth(String condition) {
        return switch (condition == null ? "" : condition) {
            case "Stable Household", "Favored by Local Scholars" -> 55;
            case "Secret Noble Blood" -> 40;
            case "Debt-Burdened Family", "Exiled Branch" -> 25;
            case "Disgraced Bloodline", "Political Enemy of the Court" -> 10;
            case "Family Divided by Rivalry" -> -20;
            case "Recently Orphaned" -> 30;
            default -> 35;
        };
    }

    private FamilyMember elder(
            Kinship kinship,
            int playerAge,
            int warmth,
            List<String> names
    ) {
        return new FamilyMember(
                nextId(),
                pickName(names),
                kinship,
                pickTrait(),
                playerAge + PARENT_AGE_GAP + random.nextInt(9) - 4,
                warmth + random.nextInt(20) - 10,
                true,
                LifePath.UNDECIDED,
                ""
        );
    }

    // ---- the years passing ----------------------------------------------

    /**
     * Moves the household forward by the years the player just lived: people
     * age, marry, are born, choose a life, and die.
     *
     * @return one line per change worth telling the player about
     */
    public List<String> advanceYears(int years, PlayerCharacter player) {
        List<String> announcements = new ArrayList<>();

        if (years <= 0 || player == null) {
            return announcements;
        }

        // Copied, because a birth can join the household mid-pass.
        for (FamilyMember member : new ArrayList<>(members.values())) {
            if (!member.isAlive()) {
                continue;
            }

            member.ageBy(years);

            LifePath chosen = member.comeOfAge(player, random);

            if (chosen != null) {
                announcements.add(
                        member.getName()
                                + " has come of age. "
                                + chosen.describe(member.getName())
                );
            }

            if (rollForDeath(member, years)) {
                member.markDead(deathOf(member));

                announcements.add(
                        member.getName()
                                + ", your "
                                + member.getKinship().displayName().toLowerCase()
                                + ", has died at "
                                + member.getAge()
                                + "."
                );
            }
        }

        announcements.addAll(marriageAndBirths(years, player));
        announcements.addAll(grandchildren());

        return announcements;
    }

    private List<String> marriageAndBirths(int years, PlayerCharacter player) {
        List<String> announcements = new ArrayList<>();

        FamilyMember spouse = spouse();

        if (spouse == null
                && player.getAge() >= MARRIAGE_AGE
                && random.nextInt(100) < 25 * Math.max(1, years)) {

            FamilyMember married = new FamilyMember(
                    nextId(),
                    pickName(random.nextBoolean() ? MALE_NAMES : FEMALE_NAMES),
                    Kinship.SPOUSE,
                    pickTrait(),
                    Math.max(18, player.getAge() + random.nextInt(9) - 4),
                    45 + random.nextInt(25),
                    true,
                    LifePath.UNDECIDED,
                    ""
            );

            members.put(married.getId(), married);

            announcements.add(
                    "You have married "
                            + married.getName()
                            + ", who is "
                            + married.getTrait()
                            + "."
            );

            return announcements;
        }

        if (spouse == null
                || !spouse.isAlive()
                || player.getAge() > LAST_FERTILE_AGE
                || childrenCount() >= MAX_CHILDREN
                || (lastBirthAge >= 0
                        && player.getAge() - lastBirthAge < YEARS_BETWEEN_BIRTHS)) {

            return announcements;
        }

        if (random.nextInt(100) < BIRTH_CHANCE_PER_YEAR * Math.max(1, years)) {
            FamilyMember child = new FamilyMember(
                    nextId(),
                    pickName(random.nextBoolean() ? MALE_NAMES : FEMALE_NAMES),
                    Kinship.CHILD,
                    pickTrait(),
                    0,
                    60,
                    true,
                    LifePath.UNDECIDED,
                    ""
            );

            members.put(child.getId(), child);
            lastBirthAge = player.getAge();

            announcements.add(
                    child.getName()
                            + " has been born into your household."
            );
        }

        return announcements;
    }

    private List<String> grandchildren() {
        List<String> announcements = new ArrayList<>();

        for (FamilyMember child : new ArrayList<>(members.values())) {
            if (child.getKinship() != Kinship.CHILD
                    || !child.isAlive()
                    || child.getAge() < GRANDPARENT_AGE
                    || hasGrandchildFrom(child.getId())) {

                continue;
            }

            if (random.nextInt(100) >= 40) {
                continue;
            }

            FamilyMember grandchild = new FamilyMember(
                    nextId(),
                    pickName(random.nextBoolean() ? MALE_NAMES : FEMALE_NAMES),
                    Kinship.GRANDCHILD,
                    pickTrait(),
                    0,
                    55,
                    true,
                    LifePath.UNDECIDED,
                    child.getId()
            );

            members.put(grandchild.getId(), grandchild);

            announcements.add(
                    child.getName()
                            + " has had a child, "
                            + grandchild.getName()
                            + ". Your name goes on another generation."
            );
        }

        return announcements;
    }

    private boolean rollForDeath(FamilyMember member, int years) {
        int chance = deathChanceFor(member.getAge()) * Math.max(1, years);

        return chance > 0 && random.nextInt(100) < chance;
    }

    private int deathChanceFor(int age) {
        if (age >= 75) {
            return 12;
        }

        if (age >= 60) {
            return 6;
        }

        if (age >= 45) {
            return 2;
        }

        return 0;
    }

    private String deathOf(FamilyMember member) {
        if (member.getAge() >= 70) {
            return "Died old, at " + member.getAge() + ", with the household around them.";
        }

        if (member.getLifePath() == LifePath.SOLDIER) {
            return "Died on campaign, far from anyone who knew their name.";
        }

        if (member.getLifePath() == LifePath.CRIMINAL) {
            return "Died in the manner their work made likely.";
        }

        return "Died of an illness the household could not afford to treat properly.";
    }

    // ---- story flags ----------------------------------------------------

    /** Moves the household's feeling toward the player after a choice. */
    public void applyStoryFlag(String flag) {
        switch (flag) {
            case "family_stood_by_them" -> changeAll(12);
            case "family_provided_for" -> changeAll(8);
            case "family_chose_ambition" -> changeAll(-10);
            case "family_abandoned" -> changeAll(-22);
            case "family_disowned_child" -> changeKinship(Kinship.CHILD, -35);
            case "family_honoured_parents" -> changeKinship(Kinship.FATHER, 15);
            case "family_spouse_supported" -> changeKinship(Kinship.SPOUSE, 20);
            case "family_spouse_neglected" -> changeKinship(Kinship.SPOUSE, -20);
            case "family_sibling_helped" -> changeKinship(Kinship.SIBLING, 20);
            case "family_sibling_refused" -> changeKinship(Kinship.SIBLING, -18);

            // Steering a child settles their path before the stat-driven roll
            // at eighteen ever runs. A decision made now is the whole point.
            case "family_child_to_scholar" -> steerEldestUndecided(LifePath.SCHOLAR);
            case "family_child_to_merchant" -> steerEldestUndecided(LifePath.MERCHANT);
            case "family_child_to_courtier" -> steerEldestUndecided(LifePath.COURTIER);

            default -> {
            }
        }

        if ("family_honoured_parents".equals(flag)) {
            changeKinship(Kinship.MOTHER, 15);
        }
    }

    /**
     * Settles the path of the oldest child still waiting on one — the same
     * child the steering event was generated for, since that event only
     * appears for an undecided child and the oldest is offered first.
     */
    private void steerEldestUndecided(LifePath path) {
        FamilyMember eldest = null;

        for (FamilyMember member : members.values()) {
            if (member.getKinship() != Kinship.CHILD
                    || !member.isAlive()
                    || member.getLifePath() != LifePath.UNDECIDED) {

                continue;
            }

            if (eldest == null || member.getAge() > eldest.getAge()) {
                eldest = member;
            }
        }

        if (eldest != null) {
            eldest.setLifePath(path);
            eldest.changeAffection(8);
        }
    }

    private void changeAll(int amount) {
        for (FamilyMember member : members.values()) {
            if (member.isAlive()) {
                member.changeAffection(amount);
            }
        }
    }

    private void changeKinship(Kinship kinship, int amount) {
        for (FamilyMember member : members.values()) {
            if (member.isAlive() && member.getKinship() == kinship) {
                member.changeAffection(amount);
            }
        }
    }

    /**
     * Rebuilds the household around whichever descendant is taking the name.
     *
     * <p>The heir stops being a member and becomes the player. Everyone whose
     * place in the house was defined by the person who just died leaves with
     * them: the forebear's parents, their spouse, and the dead. What remains
     * is the heir's own generation — their brothers and sisters — and any
     * children already theirs.
     *
     * @return true when the heir was found and the household was reshaped
     */
    public boolean succeedTo(String heirId) {
        FamilyMember heir = members.get(heirId);

        if (heir == null || !heir.getKinship().isDescendant()) {
            return false;
        }

        Map<String, FamilyMember> household = new LinkedHashMap<>();

        for (FamilyMember member : members.values()) {
            if (member.getId().equals(heirId) || !member.isAlive()) {
                continue;
            }

            Kinship becomes = kinshipUnder(heir, member);

            if (becomes == null) {
                continue;
            }

            household.put(
                    member.getId(),
                    new FamilyMember(
                            member.getId(),
                            member.getName(),
                            becomes,
                            member.getTrait(),
                            member.getAge(),
                            // Halved toward neutral, the way the factions and
                            // the name are. What the forebear felt for their
                            // children is not what their children feel for
                            // each other.
                            member.getAffection() / 2,
                            true,
                            member.getLifePath(),
                            becomes == Kinship.CHILD ? "" : member.getParentId()
                    )
            );
        }

        members.clear();
        members.putAll(household);

        // The heir has had no children of their own on this clock yet.
        lastBirthAge = -1;

        return true;
    }

    /**
     * What a surviving member becomes once the heir is the one holding the
     * house, or null for anyone whose place in it has gone.
     */
    private Kinship kinshipUnder(FamilyMember heir, FamilyMember member) {
        // The heir's own children come with them; everyone else's do not.
        if (member.getKinship() == Kinship.GRANDCHILD) {
            return heir.getId().equals(member.getParentId()) ? Kinship.CHILD : null;
        }

        // The forebear's other children are the heir's brothers and sisters.
        if (member.getKinship() == Kinship.CHILD) {
            return Kinship.SIBLING;
        }

        // Parents, spouse and the heir's aunts and uncles belonged to the life
        // that has just ended.
        return null;
    }

    // ---- reading the household ------------------------------------------

    public List<FamilyMember> all() {
        return List.copyOf(members.values());
    }

    public FamilyMember get(String id) {
        return members.get(id);
    }

    public FamilyMember spouse() {
        return firstOf(Kinship.SPOUSE);
    }

    public List<FamilyMember> livingChildren() {
        List<FamilyMember> children = new ArrayList<>();

        for (FamilyMember member : members.values()) {
            if (member.getKinship() == Kinship.CHILD && member.isAlive()) {
                children.add(member);
            }
        }

        return children;
    }

    private FamilyMember firstOf(Kinship kinship) {
        for (FamilyMember member : members.values()) {
            if (member.getKinship() == kinship && member.isAlive()) {
                return member;
            }
        }

        return null;
    }

    private int childrenCount() {
        int count = 0;

        for (FamilyMember member : members.values()) {
            if (member.getKinship() == Kinship.CHILD) {
                count++;
            }
        }

        return count;
    }

    private boolean hasGrandchildFrom(String childId) {
        for (FamilyMember member : members.values()) {
            if (member.getKinship() == Kinship.GRANDCHILD
                    && childId.equals(member.getParentId())) {

                return true;
            }
        }

        return false;
    }

    /** The Household panel's text. */
    public String householdSummary() {
        StringBuilder result = new StringBuilder();

        for (FamilyMember member : members.values()) {
            if (!result.isEmpty()) {
                result.append("\n\n");
            }

            result.append(member.getName())
                    .append("\n")
                    .append(member.getKinship().displayName())
                    .append(" — ")
                    .append(member.affectionLabel())
                    .append("\nAge: ")
                    .append(member.getAge());

            if (member.getKinship().takesALifePath()
                    && member.getLifePath() != LifePath.UNDECIDED) {

                result.append("\nPath: ")
                        .append(member.getLifePath().displayName());
            }

            if (!member.getTrait().isBlank()) {
                result.append("\nKnown for being ")
                        .append(member.getTrait());
            }

            if (!member.isAlive()) {
                result.append("\nDeceased — ")
                        .append(member.getDeathReason());
            }
        }

        return result.toString();
    }

    // ---- names ----------------------------------------------------------

    private String nextId() {
        return "family_" + nextId++;
    }

    /**
     * A name nobody in the household already holds, chosen from the unused
     * ones rather than picked blind and then numbered.
     */
    private String pickName(List<String> source) {
        List<String> free = new ArrayList<>();

        for (String name : source) {
            if (!containsName(name)) {
                free.add(name);
            }
        }

        if (free.isEmpty()) {
            List<String> other = source == MALE_NAMES ? FEMALE_NAMES : MALE_NAMES;

            for (String name : other) {
                if (!containsName(name)) {
                    free.add(name);
                }
            }
        }

        if (free.isEmpty()) {
            return source.get(random.nextInt(source.size())) + " the Younger";
        }

        return free.get(random.nextInt(free.size()));
    }

    private boolean containsName(String name) {
        for (FamilyMember member : members.values()) {
            if (member.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private String pickTrait() {
        return TRAITS.get(random.nextInt(TRAITS.size()));
    }

    // ---- persistence ----------------------------------------------------

    public JSONObject toJson() {
        JSONArray array = new JSONArray();

        for (FamilyMember member : members.values()) {
            JSONObject object = new JSONObject();

            object.put("id", member.getId());
            object.put("name", member.getName());
            object.put("kinship", member.getKinship().name());
            object.put("trait", member.getTrait());
            object.put("parentId", member.getParentId());
            object.put("age", member.getAge());
            object.put("affection", member.getAffection());
            object.put("alive", member.isAlive());
            object.put("lifePath", member.getLifePath().name());
            object.put("deathReason", member.getDeathReason());

            array.put(object);
        }

        JSONObject root = new JSONObject();
        root.put("members", array);
        root.put("nextId", nextId);
        root.put("lastBirthAge", lastBirthAge);

        return root;
    }

    public static FamilyRegistry fromJson(JSONObject root) {
        FamilyRegistry registry = new FamilyRegistry();

        if (root == null) {
            return registry;
        }

        registry.nextId = root.optInt("nextId", 1);
        registry.lastBirthAge = root.optInt("lastBirthAge", -1);

        JSONArray array = root.optJSONArray("members");

        if (array == null) {
            return registry;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            FamilyMember member = new FamilyMember(
                    object.getString("id"),
                    object.getString("name"),
                    Kinship.valueOf(object.getString("kinship")),
                    object.optString("trait", ""),
                    object.optInt("age", 0),
                    object.optInt("affection", 0),
                    object.optBoolean("alive", true),
                    LifePath.valueOf(
                            object.optString("lifePath", LifePath.UNDECIDED.name())
                    ),
                    object.optString("parentId", "")
            );

            if (!member.isAlive()) {
                member.markDead(object.optString("deathReason", ""));
            }

            registry.members.put(member.getId(), member);
        }

        return registry;
    }

    public void replaceWith(FamilyRegistry restored) {
        members.clear();

        if (restored != null) {
            members.putAll(restored.members);
            nextId = restored.nextId;
            lastBirthAge = restored.lastBirthAge;
        }
    }
}
