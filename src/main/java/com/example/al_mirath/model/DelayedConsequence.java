package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;

/**
 * A consequence scheduled to surface years after the choice that caused it.
 *
 * <p>This is what stops the game from being a sequence of self-contained
 * prompts. A choice made at twelve can plant something that only comes due at
 * forty-five: a governor's son who remembers a slight, an auditor who finds an
 * old gap in the records, a forbidden book that resurfaces in the wrong hands.
 *
 * <p>A consequence fires when the character reaches {@code triggerAge}, unless
 * a {@code cancelledByFlag} has been set in the meantime — which lets a later
 * choice genuinely defuse an earlier mistake rather than merely delaying it.
 *
 * @param id               unique key; also the flag recorded once it has fired
 * @param triggerAge       age at which this comes due
 * @param title            popup heading when it fires
 * @param description      narrative body
 * @param statEffects      stat deltas applied on firing
 * @param factionEffects   faction deltas applied on firing
 * @param flagsToAdd       world flags set on firing, for further chaining
 * @param cancelledByFlag  if this flag is present when due, the consequence is
 *                         discarded silently; empty means it cannot be avoided
 * @param requiredFlags    all must be present when due, or it is discarded
 */
public record DelayedConsequence(
        String id,
        int triggerAge,
        String title,
        String description,
        Map<String, Integer> statEffects,
        Map<String, Integer> factionEffects,
        List<String> flagsToAdd,
        String cancelledByFlag,
        List<String> requiredFlags
) {

    public DelayedConsequence {
        statEffects = statEffects == null ? Map.of() : statEffects;
        factionEffects = factionEffects == null ? Map.of() : factionEffects;
        flagsToAdd = flagsToAdd == null ? List.of() : flagsToAdd;
        cancelledByFlag = cancelledByFlag == null ? "" : cancelledByFlag;
        requiredFlags = requiredFlags == null ? List.of() : requiredFlags;
    }

    public boolean hasCancelFlag() {
        return !cancelledByFlag.isBlank();
    }
}
