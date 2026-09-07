package com.example.al_mirath.service;

import com.example.al_mirath.model.NpcMemory;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.RivalFeud;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class RecurringCharacterRegistry {

    private static final List<String> MALE_NAMES = List.of(
            "Hasan",
            "Yusuf",
            "Khalid",
            "Omar",
            "Tariq",
            "Ibrahim",
            "Salim",
            "Rashid",
            "Farid",
            "Nadir",
            "Hamza",
            "Zayd"
    );

    private static final List<String> FEMALE_NAMES = List.of(
            "Amina",
            "Layla",
            "Maryam",
            "Zaynab",
            "Safiya",
            "Nura",
            "Fatima",
            "Samira",
            "Hind",
            "Ruqayya",
            "Yasmin",
            "Salma"
    );

    private static final List<String> PERSONALITIES = List.of(
            "loyal but proud",
            "sharp-witted and cautious",
            "ambitious and charming",
            "quietly compassionate",
            "fearless but impulsive",
            "patient and observant",
            "deeply principled",
            "clever and secretive"
    );

    /**
     * The lives a childhood companion might grow into. One is drawn per run,
     * so the friend you met in the street becomes a different person each
     * time you play.
     */
    private static final List<List<String>> COMPANION_LADDERS = List.of(
            List.of(
                    "Childhood Companion",
                    "Conscript Soldier",
                    "Company Captain",
                    "Garrison Commander",
                    "General"
            ),
            List.of(
                    "Childhood Companion",
                    "Caravan Hand",
                    "Cloth Trader",
                    "Guild Merchant",
                    "Master of the Caravans"
            ),
            List.of(
                    "Childhood Companion",
                    "Mosque Servant",
                    "Reciter",
                    "Preacher",
                    "Voice of the Quarter"
            )
    );

    /**
     * Below this age nobody dies of age alone; above it the chance climbs.
     * Values are a percentage per year lived.
     */
    private static final int DEATH_AGE_FLOOR = 45;

    /** How strong a bond must be, either way, to leave someone behind. */
    private static final int LEGACY_BOND = 40;

    /**
     * The flag a story event uses to hand the player a student, with the
     * student's name after it. Keyed by name rather than by a fixed id
     * because the person is chosen when the offer is written, not here.
     */
    public static final String TAKE_STUDENT_PREFIX = "take_student_";

    /**
     * What a student grows into. The first rung exists only so the age gates
     * line up: somebody taken on at sixteen starts at "Your Student" and
     * climbs from there, and the last rung is reserved for a bond that
     * actually held, the way every other ladder in the cast works.
     */
    private static final List<String> STUDENT_LADDER = List.of(
            "Child at Your Door",
            "Your Student",
            "Your Copyist",
            "Master in Their Own Right",
            "Keeper of Your Name"
    );

    /** How warmly a student begins: you chose them and they know it. */
    private static final int STUDENT_STARTING_BOND = 45;

    private final Map<String, RecurringCharacter> characters =
            new LinkedHashMap<>();

    private final Random random;

    public RecurringCharacterRegistry() {
        this(new Random());
    }

    public RecurringCharacterRegistry(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public static RecurringCharacterRegistry createFor(
            PlayerCharacter player
    ) {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry();

        registry.generateInitialCast(player);

        return registry;
    }

    public void generateInitialCast(PlayerCharacter player) {
        if (!characters.isEmpty()) {
            return;
        }

        int playerAge = player.getAge();

        addGenerated(
                "childhood_companion",
                pickUniqueName(),
                "A child from the same district who grew up beside you.",
                RelationshipType.FRIEND,
                COMPANION_LADDERS.get(
                        random.nextInt(COMPANION_LADDERS.size())
                ),
                Math.max(
                        5,
                        playerAge + random.nextInt(5) - 2
                ),
                28
        );

        addGenerated(
                "early_rival",
                pickUniqueName(),
                "Someone whose path repeatedly crosses yours, never without tension.",
                RelationshipType.RIVAL,
                rivalLadder(player.getOrigin()),
                Math.max(
                        7,
                        playerAge + random.nextInt(7) - 1
                ),
                -25
        );

        addGenerated(
                "elder_mentor",
                pickUniqueName(),
                mentorBackground(player),
                RelationshipType.MENTOR,
                mentorLadder(player),
                playerAge + 18 + random.nextInt(20),
                35
        );
    }

    private void addGenerated(
            String id,
            String name,
            String background,
            RelationshipType relationshipType,
            List<String> roleLadder,
            int age,
            int relationship
    ) {
        RecurringCharacter character =
                new RecurringCharacter(
                        id,
                        name,
                        background,
                        PERSONALITIES.get(
                                random.nextInt(PERSONALITIES.size())
                        ),
                        relationshipType,
                        roleLadder.get(0),
                        age,
                        relationship,
                        true,
                        roleLadder,
                        0,
                        ""
                );

        // Settle them at whatever rung their age already earns, without
        // announcing it: an elder mentor should open the game already senior.
        character.advanceRoleForAge();

        characters.put(id, character);
    }

    /**
     * A name nobody in the cast is using.
     *
     * <p>This used to pick blind and then disambiguate a collision by
     * appending a number, which put characters called "Nadir" and "Nadir 2"
     * in the same run. With twenty-four names and up to six characters,
     * collisions were common. Choosing from the unused names instead makes
     * the numeric suffix what it should always have been: unreachable in
     * practice, and there only so a cast larger than the pool cannot fail.
     */
    private String pickUniqueName() {
        boolean preferMale = random.nextBoolean();

        List<String> preferred = preferMale ? MALE_NAMES : FEMALE_NAMES;
        List<String> fallback = preferMale ? FEMALE_NAMES : MALE_NAMES;

        String name = pickUnusedFrom(preferred);

        if (name == null) {
            name = pickUnusedFrom(fallback);
        }

        if (name != null) {
            return name;
        }

        // Every name in both pools is taken.
        String original = preferred.get(random.nextInt(preferred.size()));
        String candidate = original;
        int number = 2;

        while (containsName(candidate)) {
            candidate = original + " " + number;
            number++;
        }

        return candidate;
    }

    /** A random name from this pool that no living or dead character holds. */
    private String pickUnusedFrom(List<String> source) {
        List<String> free = new ArrayList<>();

        for (String name : source) {
            if (!containsName(name)) {
                free.add(name);
            }
        }

        return free.isEmpty()
                ? null
                : free.get(random.nextInt(free.size()));
    }

    private boolean containsName(String name) {
        for (RecurringCharacter character : characters.values()) {
            if (character.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private List<String> rivalLadder(String origin) {
        String value = origin.toLowerCase();

        if (value.contains("royal")
                || value.contains("noble")
                || value.contains("governor")) {

            return List.of(
                    "Young Court Rival",
                    "Court Page",
                    "Chamberlain's Man",
                    "Deputy Vizier",
                    "Grand Vizier"
            );
        }

        if (value.contains("soldier")
                || value.contains("military")) {

            return List.of(
                    "Barracks Rival",
                    "Sworn Soldier",
                    "Standard Bearer",
                    "Field Commander",
                    "Commander of the Host"
            );
        }

        if (value.contains("scholar")
                || value.contains("madrasa")
                || value.contains("scribe")) {

            return List.of(
                    "Rival Student",
                    "Copyist",
                    "Teacher of Law",
                    "Chief Jurist",
                    "Sheikh of the Madrasa"
            );
        }

        if (value.contains("merchant")) {
            return List.of(
                    "Merchant's Heir",
                    "Caravan Master",
                    "Guild Broker",
                    "Guild Master",
                    "Master of the Markets"
            );
        }

        return List.of(
                "Neighbourhood Rival",
                "Ward Enforcer",
                "Tax Farmer",
                "District Governor",
                "Lord of the Quarter"
        );
    }

    private String mentorBackground(PlayerCharacter player) {
        String origin = player.getOrigin().toLowerCase();

        if (origin.contains("military")
                || origin.contains("soldier")) {

            return "A veteran who sees discipline and danger in equal measure.";
        }

        if (origin.contains("scholar")
                || origin.contains("madrasa")
                || origin.contains("scribe")) {

            return "A learned teacher who believes knowledge creates duty.";
        }

        if (origin.contains("royal")
                || origin.contains("noble")) {

            return "An experienced adviser who has survived several rulers.";
        }

        if (origin.contains("merchant")) {
            return "A respected guild elder who understands both profit and loyalty.";
        }

        return "A respected elder who notices potential that others overlook.";
    }

    private List<String> mentorLadder(PlayerCharacter player) {
        String origin = player.getOrigin().toLowerCase();

        if (origin.contains("military")
                || origin.contains("soldier")) {

            return List.of(
                    "Drill Instructor",
                    "Veteran Instructor",
                    "Master of Arms",
                    "Marshal of the Garrison",
                    "Marshal of the Realm"
            );
        }

        if (origin.contains("scholar")
                || origin.contains("madrasa")
                || origin.contains("scribe")) {

            return List.of(
                    "Assistant Tutor",
                    "Learned Tutor",
                    "Master of the Madrasa",
                    "Chief Scholar",
                    "Sheikh of the Age"
            );
        }

        if (origin.contains("royal")
                || origin.contains("noble")) {

            return List.of(
                    "Court Attendant",
                    "Court Adviser",
                    "Keeper of the Seal",
                    "Senior Vizier",
                    "Regent of the Court"
            );
        }

        if (origin.contains("merchant")) {
            return List.of(
                    "Guild Clerk",
                    "Guild Patron",
                    "Elder of the Guild",
                    "Master of the Guild",
                    "Prince of Merchants"
            );
        }

        return List.of(
                "Local Elder",
                "Elder of the Quarter",
                "Keeper of the Quarter",
                "Judge of the Quarter",
                "Patriarch of the Quarter"
        );
    }

    public RecurringCharacter get(String id) {
        return characters.get(id);
    }

    public List<RecurringCharacter> all() {
        return List.copyOf(characters.values());
    }

    /** Whether somebody is currently being taught by the player. */
    public boolean hasLivingStudent() {
        for (RecurringCharacter character : characters.values()) {
            if (character.isAlive()
                    && character.getRelationshipType() == RelationshipType.STUDENT) {

                return true;
            }
        }

        return false;
    }

    /**
     * The name of whoever is currently asking to be taught.
     *
     * <p>Derived from the seed rather than rolled, so the offer keeps naming
     * the same person for as long as it stands. Cast events are rebuilt from
     * scratch every time the engine looks for one, and a rolled name would
     * mean a different stranger at the door each year — the same churn that
     * once had one life's city changing between decisions.
     */
    public String prospectiveStudentName(long seed) {
        List<String> pool = new ArrayList<>(MALE_NAMES);
        pool.addAll(FEMALE_NAMES);

        int start = (int) Math.floorMod(seed, pool.size());

        for (int step = 0; step < pool.size(); step++) {
            String candidate = pool.get((start + step) % pool.size());

            if (!containsName(candidate)) {
                return candidate;
            }
        }

        return pool.get(start);
    }

    /**
     * Takes somebody on as a student.
     *
     * <p>They join the cast as a person rather than as a stat: they age,
     * climb, can die, and can be turned against you like anyone else. What
     * makes them different is that the player chose them, which is what
     * makes them able to be handed the house later.
     *
     * @return false when there is already a student, or the name is unusable
     */
    public boolean takeStudent(String name, int playerAge) {
        if (name == null || name.isBlank() || hasLivingStudent()) {
            return false;
        }

        String id = "student_" + name.toLowerCase(java.util.Locale.ROOT);

        if (characters.containsKey(id)) {
            return false;
        }

        int age = 16 + random.nextInt(5);

        RecurringCharacter student =
                new RecurringCharacter(
                        id,
                        name,
                        "Somebody who asked to be taught what you know, and was "
                                + "not turned away.",
                        PERSONALITIES.get(
                                random.nextInt(PERSONALITIES.size())
                        ),
                        RelationshipType.STUDENT,
                        STUDENT_LADDER.get(1),
                        age,
                        STUDENT_STARTING_BOND,
                        true,
                        STUDENT_LADDER,
                        1,
                        ""
                );

        student.addMemory(
                new NpcMemory(
                        "took_student",
                        "You took them on when you did not have to.",
                        playerAge,
                        STUDENT_STARTING_BOND
                )
        );

        characters.put(id, student);

        return true;
    }

    public void changeRelationship(
            String characterId,
            int amount,
            String memoryId,
            String memoryDescription,
            int playerAge
    ) {
        RecurringCharacter character =
                characters.get(characterId);

        if (character == null) {
            return;
        }

        character.changeRelationship(amount);

        if (memoryId != null && !memoryId.isBlank()) {
            character.addMemory(
                    new NpcMemory(
                            memoryId,
                            memoryDescription == null
                                    ? ""
                                    : memoryDescription,
                            playerAge,
                            amount
                    )
            );
        }
    }

    public void applyStoryFlag(
            String flag,
            int playerAge
    ) {
        if (flag == null) {
            return;
        }

        // Taking a student names a person the event content chose, so it
        // cannot be a fixed case the way every other flag here is.
        if (flag.startsWith(TAKE_STUDENT_PREFIX)) {
            takeStudent(flag.substring(TAKE_STUDENT_PREFIX.length()), playerAge);
            return;
        }

        switch (flag) {

            case "npc_friend_secret_protected" ->
                    changeRelationship(
                            "childhood_companion",
                            18,
                            flag,
                            "You protected a secret entrusted to you.",
                            playerAge
                    );

            case "npc_friend_family_helped" ->
                    changeRelationship(
                            "childhood_companion",
                            24,
                            flag,
                            "You helped a vulnerable family together.",
                            playerAge
                    );

            case "npc_friend_betrayed" ->
                    changeRelationship(
                            "childhood_companion",
                            -55,
                            flag,
                            "You sold a childhood secret for personal advantage.",
                            playerAge
                    );

            case "npc_friend_welcomed_back" ->
                    changeRelationship(
                            "childhood_companion",
                            20,
                            flag,
                            "You welcomed your old companion into your household.",
                            playerAge
                    );

            case "npc_friend_became_agent" ->
                    changeRelationship(
                            "childhood_companion",
                            12,
                            flag,
                            "Your companion became one of your trusted agents.",
                            playerAge
                    );

            case "npc_friend_refused_service",
                 "npc_friend_distanced" ->
                    changeRelationship(
                            "childhood_companion",
                            -15,
                            flag,
                            "Distance grew between you and your childhood companion.",
                            playerAge
                    );

            case "npc_rival_outdebated" ->
                    changeRelationship(
                            "early_rival",
                            -12,
                            flag,
                            "You defeated your rival in public debate.",
                            playerAge
                    );

            case "npc_rival_humiliated_you" ->
                    changeRelationship(
                            "early_rival",
                            -22,
                            flag,
                            "Your rival humiliated you before witnesses.",
                            playerAge
                    );

            case "npc_rival_respected",
                 "npc_rival_reconciled" ->
                    changeRelationship(
                            "early_rival",
                            35,
                            flag,
                            "Your rivalry softened into mutual respect.",
                            playerAge
                    );

            case "npc_rival_publicly_humiliated",
                 "npc_rival_exposed" ->
                    changeRelationship(
                            "early_rival",
                            -35,
                            flag,
                            "You damaged your rival publicly.",
                            playerAge
                    );

            case "npc_rival_counterattacked" ->
                    changeRelationship(
                            "early_rival",
                            -30,
                            flag,
                            "You reached for your rival's throat and missed.",
                            playerAge
                    );

            case "npc_rival_emboldened" ->
                    changeRelationship(
                            "early_rival",
                            -18,
                            flag,
                            "Your rival mistook your restraint for weakness.",
                            playerAge
                    );

            case "npc_rival_rejected_peace" ->
                    changeRelationship(
                            "early_rival",
                            -20,
                            flag,
                            "Your rival refused the peace you offered.",
                            playerAge
                    );

            // Submission is humiliating, but a contest your rival considers
            // won is a contest they stop prosecuting.
            case "npc_rival_owes_protection" ->
                    changeRelationship(
                            "early_rival",
                            25,
                            flag,
                            "You bent, and your rival had no more to take.",
                            playerAge
                    );

            case "npc_rival_heir_reconciled" ->
                    changeDescendantRelationship(
                            RelationshipType.RIVAL,
                            40,
                            flag,
                            "You ended a quarrel the previous generation began.",
                            playerAge
                    );

            case "npc_rival_heir_crushed" ->
                    changeDescendantRelationship(
                            RelationshipType.RIVAL,
                            -45,
                            flag,
                            "You finished with the child what you began with the parent.",
                            playerAge
                    );

            case "npc_rival_heir_unconvinced" ->
                    changeDescendantRelationship(
                            RelationshipType.RIVAL,
                            -12,
                            flag,
                            "You gave your side of the quarrel and were not believed.",
                            playerAge
                    );

            case "npc_rival_heir_bought_off" ->
                    changeDescendantRelationship(
                            RelationshipType.RIVAL,
                            30,
                            flag,
                            "You paid a debt to the child that the parent never collected.",
                            playerAge
                    );

            case "npc_rival_bypassed" ->
                    changeRelationship(
                            "early_rival",
                            -10,
                            flag,
                            "You went around your rival, and they found out.",
                            playerAge
                    );

            // A blow that lands is a grievance partly paid.
            case "npc_rival_struck_home" ->
                    changeRelationship(
                            "early_rival",
                            12,
                            flag,
                            "Your rival hurt you, and was satisfied by it.",
                            playerAge
                    );

            case "npc_rival_house_withdrew" ->
                    changeRelationship(
                            "early_rival",
                            10,
                            flag,
                            "You put your household beyond reach and left "
                                    + "your rival the field.",
                            playerAge
                    );

            // Asking for more before you have earned the first thing.
            case "npc_mentor_request_refused" ->
                    changeRelationship(
                            "elder_mentor",
                            -10,
                            flag,
                            "You bargained with your teacher before you had "
                                    + "earned the right to.",
                            playerAge
                    );

            case "npc_mentor_guidance_accepted",
                 "npc_mentor_supported_family" ->
                    changeRelationship(
                            "elder_mentor",
                            20,
                            flag,
                            "You accepted your mentor's guidance.",
                            playerAge
                    );

            case "npc_mentor_rejected",
                 "npc_mentor_disowned_legacy" ->
                    changeRelationship(
                            "elder_mentor",
                            -30,
                            flag,
                            "You rejected the bond between mentor and student.",
                            playerAge
                    );

            case "npc_mentor_legacy_preserved",
                 "npc_mentor_final_reconciliation" ->
                    changeRelationship(
                            "elder_mentor",
                            30,
                            flag,
                            "You honoured your mentor near the end of their life.",
                            playerAge
                    );

            case "npc_descendant_sheltered" ->
                    changeDescendantRelationship(
                            30,
                            flag,
                            "You took in the child of a bond that outlived its owner.",
                            playerAge
                    );

            case "npc_descendant_paid" ->
                    changeDescendantRelationship(
                            5,
                            flag,
                            "You settled an inherited debt in silver.",
                            playerAge
                    );

            case "npc_descendant_refused" ->
                    changeDescendantRelationship(
                            -35,
                            flag,
                            "You turned away the child of an old bond.",
                            playerAge
                    );

            default -> {
            }
        }
    }

    /**
     * Descendants are keyed by their parent's id, so their flags are applied
     * by kind rather than by a name the story content cannot know in advance.
     */
    private void changeDescendantRelationship(
            int amount,
            String memoryId,
            String memoryDescription,
            int playerAge
    ) {
        for (RecurringCharacter character : characters.values()) {
            if (character.isDescendant() && character.isAlive()) {
                changeRelationship(
                        character.getId(),
                        amount,
                        memoryId,
                        memoryDescription,
                        playerAge
                );
            }
        }
    }

    /**
     * The same, narrowed to descendants who hold a particular stance toward
     * the player. A rival's heir and a friend's heir are both descendants, so
     * a flag meant for one of them must not land on the other.
     */
    private void changeDescendantRelationship(
            RelationshipType kind,
            int amount,
            String memoryId,
            String memoryDescription,
            int playerAge
    ) {
        for (RecurringCharacter character : characters.values()) {
            if (character.isDescendant()
                    && character.isAlive()
                    && character.getRelationshipType() == kind) {

                changeRelationship(
                        character.getId(),
                        amount,
                        memoryId,
                        memoryDescription,
                        playerAge
                );
            }
        }
    }

    /**
     * Moves the whole cast forward by the years the player just lived: they
     * age, climb their role ladders, and eventually die. A strong bond that
     * ends leaves a descendant behind.
     *
     * @return one line per change worth telling the player about, in the
     *         order it happened. Empty on a quiet passage of time.
     */
    public List<String> ageEveryone(int years) {
        List<String> announcements = new ArrayList<>();

        // Iterated over a copy so a descendant can join the cast mid-pass.
        for (RecurringCharacter character
                : new ArrayList<>(characters.values())) {

            if (!character.isAlive()) {
                continue;
            }

            character.ageBy(years);

            if (rollForDeath(character, years)) {
                character.markDead();

                announcements.add(
                        character.getName()
                                + " has died at "
                                + character.getAge()
                                + "."
                );

                RecurringCharacter heir =
                        createDescendant(character);

                if (heir != null) {
                    characters.put(heir.getId(), heir);

                    announcements.add(
                            heir.getName()
                                    + ", child of "
                                    + character.getName()
                                    + ", has come of age."
                    );
                }

                continue;
            }

            String newRole = character.advanceRoleForAge();

            if (newRole != null) {
                announcements.add(
                        character.getName()
                                + " is now "
                                + newRole
                                + "."
                );
            }
        }

        return announcements;
    }

    private boolean rollForDeath(
            RecurringCharacter character,
            int years
    ) {
        int chance =
                deathChanceFor(character.getAge())
                        * Math.max(1, years);

        return chance > 0
                && random.nextInt(100) < chance;
    }

    /** Percentage chance of dying per year lived, by age. */
    private int deathChanceFor(int age) {
        if (age >= 75) {
            return 10;
        }

        if (age >= 60) {
            return 5;
        }

        if (age >= DEATH_AGE_FLOOR) {
            return 2;
        }

        return 0;
    }

    /**
     * Someone the player loved or hated enough leaves a child behind, seeded
     * with half the inherited feeling and carrying the memory that mattered
     * most. Indifference leaves nothing.
     *
     * <p>A rival's child is not handed the same blank slate as a friend's. The
     * quarrel is the household they grew up in, so they arrive already knowing
     * their side of it — which is what lets a feud you started at fifteen
     * still be running after the person you started it with is dead.
     */
    private RecurringCharacter createDescendant(
            RecurringCharacter parent
    ) {
        if (Math.abs(parent.getRelationship()) < LEGACY_BOND) {
            return null;
        }

        if (parent.isDescendant()) {
            return null;
        }

        String id = parent.getId() + "_descendant";

        if (characters.containsKey(id)) {
            return null;
        }

        boolean feudInherited = inheritsFeud(parent);

        String role =
                feudInherited
                        ? "Heir to " + parent.getName() + "'s Quarrel"
                        : "Child of " + parent.getName();

        RecurringCharacter heir =
                new RecurringCharacter(
                        id,
                        pickUniqueName(),
                        feudInherited
                                ? "Raised on an account of you told by "
                                        + parent.getName()
                                        + ", in which you are not the wronged party."
                                : "Born into the story you shared with "
                                        + parent.getName()
                                        + ".",
                        PERSONALITIES.get(
                                random.nextInt(PERSONALITIES.size())
                        ),
                        inheritedStance(parent),
                        role,
                        18 + random.nextInt(8),
                        parent.getRelationship() / 2,
                        true,
                        List.of(role),
                        0,
                        parent.getName()
                );

        NpcMemory inherited = parent.strongestMemory();

        if (inherited != null) {
            heir.addMemory(inherited);
        }

        return heir;
    }

    /**
     * What a child starts out as. A rival's heir begins as a rival rather than
     * a stranger: they have been given a side before they ever met you.
     */
    private RelationshipType inheritedStance(RecurringCharacter parent) {
        return inheritsFeud(parent)
                ? RelationshipType.RIVAL
                : RelationshipType.STRANGER;
    }

    /**
     * Whether this parent leaves a quarrel behind them rather than a memory.
     * A rival the player made peace with dies with the feud settled; one who
     * died still hating the player hands that on.
     */
    private boolean inheritsFeud(RecurringCharacter parent) {
        return parent.getRelationshipType() == RelationshipType.RIVAL
                && parent.getRelationship() < 0;
    }

    public String relationshipSummary() {
        StringBuilder result = new StringBuilder();

        for (RecurringCharacter character : characters.values()) {
            if (!result.isEmpty()) {
                result.append("\n\n");
            }

            result.append(character.getName())
                    .append("\n")
                    .append(character.getRelationshipType().displayName())
                    .append(" — ")
                    .append(character.relationshipLabel())
                    .append(" ");

            if (character.getRelationship() >= 0) {
                result.append("+");
            }

            result.append(character.getRelationship())
                    .append("\n")
                    .append("Role: ")
                    .append(character.getCurrentRole())
                    .append("\n")
                    .append("Age: ")
                    .append(character.getAge())
                    .append("\n")
                    .append("Personality: ")
                    .append(character.getPersonality());

            NpcMemory strongest = character.strongestMemory();

            if (strongest != null) {
                result.append("\nRemembers: ")
                        .append(strongest.description())
                        .append(" (you were ")
                        .append(strongest.playerAge())
                        .append(")");
            }

            RivalFeud feud = character.feud();

            if (feud != RivalFeud.NONE) {
                result.append("\nFeud: ")
                        .append(feud.displayName());
            }

            if (!character.isAlive()) {
                result.append("\nDeceased");
            }
        }

        return result.toString();
    }

    public JSONObject toJson() {
        JSONArray array = new JSONArray();

        for (RecurringCharacter character : characters.values()) {
            JSONObject object = new JSONObject();

            object.put("id", character.getId());
            object.put("name", character.getName());
            object.put("background", character.getBackground());
            object.put("personality", character.getPersonality());

            object.put(
                    "relationshipType",
                    character.getRelationshipType().name()
            );

            object.put(
                    "currentRole",
                    character.getCurrentRole()
            );

            object.put("age", character.getAge());

            object.put(
                    "relationship",
                    character.getRelationship()
            );

            object.put("alive", character.isAlive());

            object.put(
                    "roleLadder",
                    new JSONArray(character.getRoleLadder())
            );

            object.put(
                    "roleRank",
                    character.getRoleRank()
            );

            object.put(
                    "parentName",
                    character.getParentName()
            );

            JSONArray memoryArray = new JSONArray();

            for (NpcMemory memory : character.getMemories()) {
                JSONObject memoryObject = new JSONObject();

                memoryObject.put("id", memory.id());
                memoryObject.put(
                        "description",
                        memory.description()
                );
                memoryObject.put(
                        "playerAge",
                        memory.playerAge()
                );
                memoryObject.put(
                        "emotionalWeight",
                        memory.emotionalWeight()
                );

                memoryArray.put(memoryObject);
            }

            object.put("memories", memoryArray);

            array.put(object);
        }

        return new JSONObject()
                .put("characters", array);
    }

    public static RecurringCharacterRegistry fromJson(
            JSONObject json
    ) {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry();

        JSONArray array =
                json.optJSONArray("characters");

        if (array == null) {
            return registry;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject object =
                    array.getJSONObject(i);

            String currentRole =
                    object.optString(
                            "currentRole",
                            "Acquaintance"
                    );

            // Saves written before role ladders existed simply carry the one
            // role they had, which the ladder logic treats as a finished climb.
            List<String> roleLadder =
                    new ArrayList<>();

            JSONArray ladderArray =
                    object.optJSONArray("roleLadder");

            if (ladderArray != null) {
                for (int rung = 0;
                     rung < ladderArray.length();
                     rung++) {

                    roleLadder.add(
                            ladderArray.getString(rung)
                    );
                }
            }

            if (roleLadder.isEmpty()) {
                roleLadder.add(currentRole);
            }

            RecurringCharacter character =
                    new RecurringCharacter(
                            object.getString("id"),
                            object.getString("name"),
                            object.optString(
                                    "background",
                                    ""
                            ),
                            object.optString(
                                    "personality",
                                    ""
                            ),
                            RelationshipType.valueOf(
                                    object.optString(
                                            "relationshipType",
                                            "STRANGER"
                                    )
                            ),
                            currentRole,
                            object.optInt("age", 0),
                            object.optInt(
                                    "relationship",
                                    0
                            ),
                            object.optBoolean(
                                    "alive",
                                    true
                            ),
                            roleLadder,
                            object.optInt("roleRank", 0),
                            object.optString(
                                    "parentName",
                                    ""
                            )
                    );

            JSONArray memories =
                    object.optJSONArray("memories");

            if (memories != null) {
                for (int memoryIndex = 0;
                     memoryIndex < memories.length();
                     memoryIndex++) {

                    JSONObject memory =
                            memories.getJSONObject(memoryIndex);

                    character.addMemory(
                            new NpcMemory(
                                    memory.getString("id"),
                                    memory.optString(
                                            "description",
                                            ""
                                    ),
                                    memory.optInt(
                                            "playerAge",
                                            0
                                    ),
                                    memory.optInt(
                                            "emotionalWeight",
                                            0
                                    )
                            )
                    );
                }
            }

            registry.characters.put(
                    character.getId(),
                    character
            );
        }

        return registry;
    }

    public void replaceWith(
            RecurringCharacterRegistry restored
    ) {
        characters.clear();

        if (restored != null) {
            characters.putAll(restored.characters);
        }
    }
}