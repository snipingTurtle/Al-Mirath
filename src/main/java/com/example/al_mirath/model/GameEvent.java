package com.example.al_mirath.model;

import java.util.List;

public class GameEvent {
    private final String title;
    private final String description;
    private final String lifeStage;
    private final List<Choice> choices;

    private final List<String> allowedEras;
    private final List<String> allowedOrigins;
    private final List<String> blockedFamilyConditions;
    private final List<String> allowedCurrentStatuses;

    private final List<String> requiredFlags;
    private final List<String> blockedFlags;

    public GameEvent(String title, String description, String lifeStage, List<Choice> choices) {
        this.title = title;
        this.description = description;
        this.lifeStage = lifeStage;
        this.choices = choices;

        this.allowedEras = List.of();
        this.allowedOrigins = List.of();
        this.blockedFamilyConditions = List.of();
        this.allowedCurrentStatuses = List.of();
        this.requiredFlags = List.of();
        this.blockedFlags = List.of();
    }

    public GameEvent(String title, String description, String lifeStage, List<Choice> choices,
                     List<String> allowedEras, List<String> allowedOrigins, List<String> blockedFamilyConditions) {
        this.title = title;
        this.description = description;
        this.lifeStage = lifeStage;
        this.choices = choices;

        this.allowedEras = allowedEras;
        this.allowedOrigins = allowedOrigins;
        this.blockedFamilyConditions = blockedFamilyConditions;
        this.allowedCurrentStatuses = List.of();
        this.requiredFlags = List.of();
        this.blockedFlags = List.of();
    }

    public GameEvent(String title, String description, String lifeStage, List<Choice> choices,
                     List<String> allowedEras, List<String> allowedOrigins, List<String> blockedFamilyConditions,
                     List<String> requiredFlags, List<String> blockedFlags) {
        this.title = title;
        this.description = description;
        this.lifeStage = lifeStage;
        this.choices = choices;

        this.allowedEras = allowedEras;
        this.allowedOrigins = allowedOrigins;
        this.blockedFamilyConditions = blockedFamilyConditions;
        this.allowedCurrentStatuses = List.of();
        this.requiredFlags = requiredFlags;
        this.blockedFlags = blockedFlags;
    }

    public GameEvent(String title, String description, String lifeStage, List<Choice> choices,
                     List<String> allowedEras, List<String> allowedOrigins, List<String> blockedFamilyConditions,
                     List<String> allowedCurrentStatuses,
                     List<String> requiredFlags, List<String> blockedFlags) {
        this.title = title;
        this.description = description;
        this.lifeStage = lifeStage;
        this.choices = choices;

        this.allowedEras = allowedEras;
        this.allowedOrigins = allowedOrigins;
        this.blockedFamilyConditions = blockedFamilyConditions;
        this.allowedCurrentStatuses = allowedCurrentStatuses;
        this.requiredFlags = requiredFlags;
        this.blockedFlags = blockedFlags;
    }

    public boolean isEligibleFor(PlayerCharacter player, WorldState worldState) {
        boolean eraAllowed = allowedEras.isEmpty()
                || allowedEras.contains(player.getEra());

        boolean originAllowed = allowedOrigins.isEmpty()
                || allowedOrigins.contains(player.getOrigin());

        boolean familyAllowed = !blockedFamilyConditions.contains(player.getFamilyCondition());

        boolean statusAllowed = allowedCurrentStatuses.isEmpty()
                || allowedCurrentStatuses.contains(player.getCurrentStatus());

        boolean requiredFlagsMet = worldState == null
                || worldState.hasAllFlags(requiredFlags);

        boolean blockedFlagsAbsent = worldState == null
                || !worldState.hasAnyFlag(blockedFlags);

        return eraAllowed
                && originAllowed
                && familyAllowed
                && statusAllowed
                && requiredFlagsMet
                && blockedFlagsAbsent;
    }

    public boolean hasRequiredFlags() {
        return !requiredFlags.isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public List<String> getAllowedEras() {
        return allowedEras;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public List<String> getBlockedFamilyConditions() {
        return blockedFamilyConditions;
    }

    public List<String> getAllowedCurrentStatuses() {
        return allowedCurrentStatuses;
    }

    public List<String> getRequiredFlags() {
        return requiredFlags;
    }

    public List<String> getBlockedFlags() {
        return blockedFlags;
    }
}