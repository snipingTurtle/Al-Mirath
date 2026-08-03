package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;

public class Choice {

    private final String text;

    private final String checkStat;
    private final int difficulty;

    private final String successText;
    private final String failureText;

    private final Map<String, Integer> successStatEffects;
    private final Map<String, Integer> failureStatEffects;

    private final Map<String, Integer> successFactionEffects;
    private final Map<String, Integer> failureFactionEffects;

    private final List<ChoiceRequirement> requirements;

    private final List<String> successFlags;
    private final List<String> failureFlags;

    // Normal guaranteed choice
    public Choice(
            String text,
            String resultText,
            Map<String, Integer> statEffects,
            Map<String, Integer> factionEffects
    ) {
        this(
                text,
                resultText,
                statEffects,
                factionEffects,
                List.of(),
                List.of()
        );
    }

    // Normal guaranteed choice with requirements
    public Choice(
            String text,
            String resultText,
            Map<String, Integer> statEffects,
            Map<String, Integer> factionEffects,
            List<ChoiceRequirement> requirements
    ) {
        this(
                text,
                resultText,
                statEffects,
                factionEffects,
                requirements,
                List.of()
        );
    }

    // Normal guaranteed choice with requirements and flags
    public Choice(
            String text,
            String resultText,
            Map<String, Integer> statEffects,
            Map<String, Integer> factionEffects,
            List<ChoiceRequirement> requirements,
            List<String> successFlags
    ) {
        this.text = text;
        this.checkStat = null;
        this.difficulty = 0;

        this.successText = resultText;
        this.failureText = resultText;

        this.successStatEffects = statEffects;
        this.failureStatEffects = Map.of();

        this.successFactionEffects = factionEffects;
        this.failureFactionEffects = Map.of();

        this.requirements = requirements;
        this.successFlags = successFlags;
        this.failureFlags = List.of();
    }

    // Stat-check choice
    public Choice(
            String text,
            String checkStat,
            int difficulty,
            String successText,
            String failureText,
            Map<String, Integer> successStatEffects,
            Map<String, Integer> failureStatEffects,
            Map<String, Integer> successFactionEffects,
            Map<String, Integer> failureFactionEffects
    ) {
        this(
                text,
                checkStat,
                difficulty,
                successText,
                failureText,
                successStatEffects,
                failureStatEffects,
                successFactionEffects,
                failureFactionEffects,
                List.of(),
                List.of(),
                List.of()
        );
    }

    // Stat-check choice with requirements and flags
    public Choice(
            String text,
            String checkStat,
            int difficulty,
            String successText,
            String failureText,
            Map<String, Integer> successStatEffects,
            Map<String, Integer> failureStatEffects,
            Map<String, Integer> successFactionEffects,
            Map<String, Integer> failureFactionEffects,
            List<ChoiceRequirement> requirements,
            List<String> successFlags,
            List<String> failureFlags
    ) {
        this.text = text;
        this.checkStat = checkStat;
        this.difficulty = difficulty;

        this.successText = successText;
        this.failureText = failureText;

        this.successStatEffects = successStatEffects;
        this.failureStatEffects = failureStatEffects;

        this.successFactionEffects = successFactionEffects;
        this.failureFactionEffects = failureFactionEffects;

        this.requirements = requirements;

        this.successFlags = successFlags;
        this.failureFlags = failureFlags;
    }

    public String getText() {
        return text;
    }

    public boolean requiresStatCheck() {
        return checkStat != null;
    }

    public String getCheckStat() {
        return checkStat;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getSuccessText() {
        return successText;
    }

    public String getFailureText() {
        return failureText;
    }

    public Map<String, Integer> getSuccessStatEffects() {
        return successStatEffects;
    }

    public Map<String, Integer> getFailureStatEffects() {
        return failureStatEffects;
    }

    public Map<String, Integer> getSuccessFactionEffects() {
        return successFactionEffects;
    }

    public Map<String, Integer> getFailureFactionEffects() {
        return failureFactionEffects;
    }

    public List<ChoiceRequirement> getRequirements() {
        return requirements;
    }

    public List<String> getSuccessFlags() {
        return successFlags;
    }

    public List<String> getFailureFlags() {
        return failureFlags;
    }
}