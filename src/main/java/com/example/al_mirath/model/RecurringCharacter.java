package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecurringCharacter {

    private final String id;
    private final String name;
    private final String background;
    private final String personality;

    private RelationshipType relationshipType;
    private String currentRole;

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
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.background = Objects.requireNonNull(background);
        this.personality = Objects.requireNonNull(personality);
        this.relationshipType = Objects.requireNonNull(relationshipType);
        this.currentRole = Objects.requireNonNull(currentRole);

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

    public void ageBy(int years) {
        age += Math.max(0, years);
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

    public List<NpcMemory> getMemories() {
        return List.copyOf(memories);
    }
}