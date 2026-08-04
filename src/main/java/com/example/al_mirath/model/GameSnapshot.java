package com.example.al_mirath.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An immutable copy of everything a choice can change.
 *
 * <p>The engine records one of these before applying a choice, so a successful
 * Threads of Fate challenge can rewind the run to the exact moment before the
 * decision was made. Every collection is defensively copied on the way in and
 * on the way out, which keeps a restored snapshot from sharing mutable state
 * with the live game.
 */
public final class GameSnapshot {

    private final String characterName;
    private final int age;
    private final boolean alive;
    private final String deathReason;
    private final String currentStatus;
    private final List<String> legacyTitles;

    private final Map<String, Integer> stats;
    private final Map<String, Integer> factions;
    private final Set<String> worldFlags;

    private final int currentStageIndex;
    private final Map<String, Integer> stageEventsPlayed;
    private final Set<String> playedEventTitles;
    private final String currentEventTitle;

    private final String eventTitle;
    private final String eventStage;
    private final String choiceText;

    public GameSnapshot(
            String characterName,
            int age,
            boolean alive,
            String deathReason,
            String currentStatus,
            List<String> legacyTitles,
            Map<String, Integer> stats,
            Map<String, Integer> factions,
            Set<String> worldFlags,
            int currentStageIndex,
            Map<String, Integer> stageEventsPlayed,
            Set<String> playedEventTitles,
            String currentEventTitle,
            String eventTitle,
            String eventStage,
            String choiceText
    ) {
        this.characterName = characterName;
        this.age = age;
        this.alive = alive;
        this.deathReason = deathReason;
        this.currentStatus = currentStatus;
        this.legacyTitles = List.copyOf(legacyTitles);

        this.stats = new LinkedHashMap<>(stats);
        this.factions = new LinkedHashMap<>(factions);
        this.worldFlags = new HashSet<>(worldFlags);

        this.currentStageIndex = currentStageIndex;
        this.stageEventsPlayed = new HashMap<>(stageEventsPlayed);
        this.playedEventTitles = new HashSet<>(playedEventTitles);
        this.currentEventTitle = currentEventTitle;

        this.eventTitle = eventTitle;
        this.eventStage = eventStage;
        this.choiceText = choiceText;
    }

    public String getCharacterName() {
        return characterName;
    }

    public int getAge() {
        return age;
    }

    public boolean isAlive() {
        return alive;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public List<String> getLegacyTitles() {
        return legacyTitles;
    }

    public Map<String, Integer> getStats() {
        return new LinkedHashMap<>(stats);
    }

    public Map<String, Integer> getFactions() {
        return new LinkedHashMap<>(factions);
    }

    public Set<String> getWorldFlags() {
        return new HashSet<>(worldFlags);
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public Map<String, Integer> getStageEventsPlayed() {
        return new HashMap<>(stageEventsPlayed);
    }

    public Set<String> getPlayedEventTitles() {
        return new HashSet<>(playedEventTitles);
    }

    public String getCurrentEventTitle() {
        return currentEventTitle;
    }

    /** Title of the event the player was answering when this was captured. */
    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventStage() {
        return eventStage;
    }

    /** The choice that is about to be undone, shown on the rewind screen. */
    public String getChoiceText() {
        return choiceText;
    }
}
