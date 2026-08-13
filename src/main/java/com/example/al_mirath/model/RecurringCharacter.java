package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecurringCharacter {

    /**
     * The age at which each rung of a role ladder is reached. A character
     * generated already past one of these gates simply starts further along,
     * which is why an elder mentor opens the game holding a senior title.
     */
    private static final int[] ROLE_AGE_GATES = {0, 16, 30, 45, 60};

    /**
     * The final rung of a ladder is reserved for characters the player
     * actually shaped, in either direction. Someone you never engaged with
     * ends their life one step short of it.
     */
    private static final int STRONG_BOND = 50;

    private final String id;
    private final String name;
    private final String background;
    private final String personality;

    /** Empty for a character whose parent was never part of the story. */
    private final String parentName;

    /** Titles this character passes through as they age, earliest first. */
    private final List<String> roleLadder;

    private RelationshipType relationshipType;
    private String currentRole;
    private int roleRank;

    private int age;
    private int relationship;
    private boolean alive;

    private final List<NpcMemory> memories = new ArrayList<>();

    public RecurringCharacter(
            String id,
            String name,
            String background,
            String personality,
            RelationshipType relationshipType,
            String currentRole,
            int age,
            int relationship,
            boolean alive
    ) {
        this(
                id,
                name,
                background,
                personality,
                relationshipType,
                currentRole,
                age,
                relationship,
                alive,
                List.of(currentRole),
                0,
                ""
        );
    }

    public RecurringCharacter(
            String id,
            String name,
            String background,
            String personality,
            RelationshipType relationshipType,
            String currentRole,
            int age,
            int relationship,
            boolean alive,
            List<String> roleLadder,
            int roleRank,
            String parentName
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.background = Objects.requireNonNull(background);
        this.personality = Objects.requireNonNull(personality);
        this.relationshipType = Objects.requireNonNull(relationshipType);
        this.currentRole = Objects.requireNonNull(currentRole);
        this.parentName = parentName == null ? "" : parentName;

        this.roleLadder =
                roleLadder == null || roleLadder.isEmpty()
                        ? List.of(currentRole)
                        : List.copyOf(roleLadder);

        this.roleRank =
                Math.max(
                        0,
                        Math.min(roleRank, this.roleLadder.size() - 1)
                );

        this.age = Math.max(0, age);
        this.relationship = clampRelationship(relationship);
        this.alive = alive;
    }

    public void changeRelationship(int amount) {
        relationship = clampRelationship(relationship + amount);
        updateRelationshipType();
    }

    public void addMemory(NpcMemory memory) {
        if (memory == null || hasMemory(memory.id())) {
            return;
        }

        memories.add(memory);
    }

    public boolean hasMemory(String memoryId) {
        return memories.stream()
                .anyMatch(memory -> memory.id().equals(memoryId));
    }

    /**
     * The moment between the two of you that carried the most weight, in
     * either direction. This is what the character brings up when they next
     * appear; null while nothing has passed between you yet.
     */
    public NpcMemory strongestMemory() {
        NpcMemory strongest = null;

        for (NpcMemory memory : memories) {
            if (strongest == null
                    || Math.abs(memory.emotionalWeight())
                    > Math.abs(strongest.emotionalWeight())) {

                strongest = memory;
            }
        }

        return strongest;
    }

    public void ageBy(int years) {
        age += Math.max(0, years);
    }

    /**
     * Moves the character up their role ladder if their age now earns it.
     *
     * @return the new role, or null when nothing changed. Callers use the
     *         non-null case to announce the change to the player.
     */
    public String advanceRoleForAge() {
        int earned = 0;

        for (int rung = 0;
             rung < roleLadder.size() && rung < ROLE_AGE_GATES.length;
             rung++) {

            if (age >= ROLE_AGE_GATES[rung]) {
                earned = rung;
            }
        }

        boolean atFinalRung =
                roleLadder.size() > 1
                        && earned == roleLadder.size() - 1;

        if (atFinalRung && Math.abs(relationship) < STRONG_BOND) {
            earned--;
        }

        if (earned <= roleRank) {
            return null;
        }

        roleRank = earned;
        currentRole = roleLadder.get(roleRank);

        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = Objects.requireNonNull(currentRole);
    }

    public void markDead() {
        alive = false;
    }

    private void updateRelationshipType() {
        if (relationship <= -45) {
            relationshipType = RelationshipType.RIVAL;
            return;
        }

        if (relationship >= 60
                && relationshipType == RelationshipType.STRANGER) {

            relationshipType = RelationshipType.FRIEND;
        }

        if (relationship >= 35
                && relationshipType == RelationshipType.RIVAL) {

            relationshipType = RelationshipType.ALLY;
        }
    }

    public String relationshipLabel() {
        if (relationship >= 75) {
            return "Devoted";
        }

        if (relationship >= 45) {
            return "Trusted";
        }

        if (relationship >= 15) {
            return "Warm";
        }

        if (relationship > -15) {
            return "Uncertain";
        }

        if (relationship > -45) {
            return "Cold";
        }

        if (relationship > -75) {
            return "Hostile";
        }

        return "Bitter Enemy";
    }

    private int clampRelationship(int value) {
        return Math.max(-100, Math.min(100, value));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBackground() {
        return background;
    }

    public String getPersonality() {
        return personality;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public int getAge() {
        return age;
    }

    public int getRelationship() {
        return relationship;
    }

    public boolean isAlive() {
        return alive;
    }

    public List<String> getRoleLadder() {
        return roleLadder;
    }

    public int getRoleRank() {
        return roleRank;
    }

    /** Empty unless this character was born out of an earlier bond. */
    public String getParentName() {
        return parentName;
    }

    public boolean isDescendant() {
        return !parentName.isBlank();
    }

    public List<NpcMemory> getMemories() {
        return List.copyOf(memories);
    }
}