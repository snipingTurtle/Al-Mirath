package com.example.al_mirath.model;

public class ChoiceRequirement {

    private final String type;
    private final String key;
    private final int minValue;
    private final String expectedValue;
    private final String failMessage;

    private ChoiceRequirement(
            String type,
            String key,
            int minValue,
            String expectedValue,
            String failMessage
    ) {
        this.type = type;
        this.key = key;
        this.minValue = minValue;
        this.expectedValue = expectedValue;
        this.failMessage = failMessage;
    }

    public static ChoiceRequirement statAtLeast(String stat, int minValue, String failMessage) {
        return new ChoiceRequirement("statMin", stat, minValue, null, failMessage);
    }

    public static ChoiceRequirement factionAtLeast(String faction, int minValue, String failMessage) {
        return new ChoiceRequirement("factionMin", faction, minValue, null, failMessage);
    }

    public static ChoiceRequirement familyIs(String familyCondition, String failMessage) {
        return new ChoiceRequirement("familyIs", null, 0, familyCondition, failMessage);
    }

    public static ChoiceRequirement familyNot(String familyCondition, String failMessage) {
        return new ChoiceRequirement("familyNot", null, 0, familyCondition, failMessage);
    }

    public static ChoiceRequirement originIs(String origin, String failMessage) {
        return new ChoiceRequirement("originIs", null, 0, origin, failMessage);
    }

    public static ChoiceRequirement hasTitle(String title, String failMessage) {
        return new ChoiceRequirement("title", null, 0, title, failMessage);
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public int getMinValue() {
        return minValue;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public String getFailMessage() {
        return failMessage;
    }
}
