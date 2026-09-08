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

    /**
     * Whether anything has actually passed between this person and the
     * player. Until it has they are somebody in the same city, not somebody
     * in the player's life, and the Bonds panel has no business naming them.
     */
    private boolean met;

    /**
     * Whether they have ever stood against the player. Kept after the warmth
     * has recovered, because peace made with an enemy is a different thing
     * from never having quarrelled, and it is what an alliance is made of.
     */
    private boolean everRival;

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

    /**
     * Ends what was between you, whatever it was worth.
     *
     * <p>Some things are not a large withdrawal from an account. Selling a
     * childhood secret does not leave a devoted friend merely less devoted:
     * it leaves them someone you used to know. Without this, a friendship
     * deep enough could absorb a betrayal and still be filed under Friend,
     * which is the thing that made these numbers feel like weather rather
     * than like consequences.
     *
     * @param ceiling the most they can feel about you from here
     */
    public void breakBond(int ceiling) {
        met = true;

        if (isConferred(relationshipType)) {
            relationshipType = RelationshipType.STRANGER;
        }

        relationship = Math.min(relationship, clampRelationship(ceiling));

        updateRelationshipType();
    }

    public void changeRelationship(int amount) {
        met = true;
        relationship = clampRelationship(relationship + amount);
        updateRelationshipType();
    }

    public void addMemory(NpcMemory memory) {
        met = true;

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

    /**
     * How many times something passed between you that this character took as
     * an injury.
     *
     * <p>The relationship score says how badly they think of you now; this
     * says how many separate occasions taught them to. A feud built from one
     * catastrophe reads differently from one built from five slights, and
     * {@link RivalFeud} treats them differently.
     */
    public int woundCount() {
        int wounds = 0;

        for (NpcMemory memory : memories) {
            if (memory.emotionalWeight() < 0) {
                wounds++;
            }
        }

        return wounds;
    }

    /** The feud currently standing between this character and the player. */
    public RivalFeud feud() {
        return RivalFeud.of(this);
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

    /** Warmth at which somebody is a friend rather than a familiar face. */
    private static final int FRIENDSHIP = 45;

    /** Warmth at which somebody has stopped being on your side. */
    private static final int HOSTILITY = -30;

    /** Warmth at which a quarrel is over, though not forgotten. */
    private static final int PEACE = 10;

    /**
     * What somebody is to the player, given how they feel about each other.
     *
     * <p>This used to only ever promote: a friend you sold out was still
     * filed under Friend at eighty points of hatred, and the only way a
     * relationship could change its nature was upward. What a person is to
     * you has to be able to be lost, or none of the choices about them mean
     * anything.
     *
     * <p>Two kinds of bond behave differently. Most are grown into and out of
     * with warmth alone. A few — teacher, student, patron, friend of the
     * house — are conferred: they are agreed to rather than drifted into, so
     * warmth cannot create one, and they hold until the warmth behind them
     * actually turns.
     */
    static RelationshipType stanceFor(
            int warmth,
            RelationshipType current,
            boolean everRival
    ) {
        // Nothing survives real hostility, whatever it was called before.
        if (warmth <= HOSTILITY) {
            return RelationshipType.RIVAL;
        }

        if (isConferred(current)) {
            return current;
        }

        if (warmth >= FRIENDSHIP) {
            return RelationshipType.FRIEND;
        }

        // Somebody who fought you and stopped is not a stranger again.
        if (everRival && warmth >= PEACE) {
            return RelationshipType.ALLY;
        }

        return RelationshipType.STRANGER;
    }

    /** Bonds that are agreed to rather than drifted into. */
    private static boolean isConferred(RelationshipType type) {
        return type == RelationshipType.MENTOR
                || type == RelationshipType.STUDENT
                || type == RelationshipType.PATRON
                || type == RelationshipType.FAMILY_FRIEND;
    }

    /**
     * Agrees a bond that warmth alone cannot produce.
     *
     * <p>An elder the player has been polite to for thirty years is somebody
     * they know well. They are a teacher only if the player took the lesson,
     * which is a thing that happens in a scene rather than on a number.
     */
    public void confer(RelationshipType bond) {
        relationshipType = Objects.requireNonNull(bond);
        met = true;

        updateRelationshipType();
    }

    private void updateRelationshipType() {
        if (relationshipType == RelationshipType.RIVAL) {
            everRival = true;
        }

        relationshipType = stanceFor(relationship, relationshipType, everRival);

        if (relationshipType == RelationshipType.RIVAL) {
            everRival = true;
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

    /** True once anything has actually passed between them and the player. */
    public boolean isMet() {
        return met;
    }

    /** True once they have stood against the player, even if that is over. */
    public boolean wasEverRival() {
        return everRival;
    }

    /**
     * Restores the two facts a save has to carry that nothing else can be
     * worked out from: whether the player has met them at all, and whether
     * the warmth between them was ever hostility.
     */
    public void restoreHistory(boolean met, boolean everRival) {
        this.met = met;
        this.everRival = everRival || relationshipType == RelationshipType.RIVAL;
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