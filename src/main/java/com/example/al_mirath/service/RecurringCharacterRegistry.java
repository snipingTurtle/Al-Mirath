package com.example.al_mirath.service;

import com.example.al_mirath.model.NpcMemory;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import org.json.JSONArray;
import org.json.JSONObject;

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
                "Childhood Companion",
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
                roleForOrigin(player.getOrigin()),
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
                mentorRole(player),
                playerAge + 18 + random.nextInt(20),
                35
        );
    }

    private void addGenerated(
            String id,
            String name,
            String background,
            RelationshipType relationshipType,
            String currentRole,
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
                        currentRole,
                        age,
                        relationship,
                        true
                );

        characters.put(id, character);
    }

    private String pickUniqueName() {
        List<String> source =
                random.nextBoolean()
                        ? MALE_NAMES
                        : FEMALE_NAMES;

        String original =
                source.get(random.nextInt(source.size()));

        String candidate = original;
        int number = 2;

        while (containsName(candidate)) {
            candidate = original + " " + number;
            number++;
        }

        return candidate;
    }

    private boolean containsName(String name) {
        for (RecurringCharacter character : characters.values()) {
            if (character.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private String roleForOrigin(String origin) {
        String value = origin.toLowerCase();

        if (value.contains("royal")
                || value.contains("noble")
                || value.contains("governor")) {

            return "Young Court Rival";
        }

        if (value.contains("soldier")
                || value.contains("military")) {

            return "Barracks Rival";
        }

        if (value.contains("scholar")
                || value.contains("madrasa")
                || value.contains("scribe")) {

            return "Rival Student";
        }

        if (value.contains("merchant")) {
            return "Merchant's Heir";
        }

        return "Neighbourhood Rival";
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

    private String mentorRole(PlayerCharacter player) {
        String origin = player.getOrigin().toLowerCase();

        if (origin.contains("military")
                || origin.contains("soldier")) {

            return "Veteran Instructor";
        }

        if (origin.contains("scholar")
                || origin.contains("madrasa")
                || origin.contains("scribe")) {

            return "Learned Tutor";
        }

        if (origin.contains("royal")
                || origin.contains("noble")) {

            return "Court Adviser";
        }

        if (origin.contains("merchant")) {
            return "Guild Patron";
        }

        return "Local Elder";
    }

    public RecurringCharacter get(String id) {
        return characters.get(id);
    }

    public List<RecurringCharacter> all() {
        return List.copyOf(characters.values());
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

            default -> {
            }
        }
    }

    public void ageEveryone(int years) {
        for (RecurringCharacter character : characters.values()) {
            character.ageBy(years);
        }
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

            if (!character.getMemories().isEmpty()) {
                NpcMemory latest =
                        character.getMemories()
                                .get(character.getMemories().size() - 1);

                result.append("\nRemembers: ")
                        .append(latest.description());
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
                            object.optString(
                                    "currentRole",
                                    "Acquaintance"
                            ),
                            object.optInt("age", 0),
                            object.optInt(
                                    "relationship",
                                    0
                            ),
                            object.optBoolean(
                                    "alive",
                                    true
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