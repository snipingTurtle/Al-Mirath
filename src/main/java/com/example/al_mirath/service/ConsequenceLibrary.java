package com.example.al_mirath.service;

import com.example.al_mirath.model.DelayedConsequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps a world flag set by a choice to the long-delayed consequences it plants.
 *
 * <p>The design rule here is that no problem is ever solved permanently: each
 * solution opens a new vector. Bribing an official buys silence now and an
 * investigation later; taking a patron's gold buys advancement now and an
 * audit later. Several consequences can be defused by a specific later
 * choice, so a player who notices the danger has a way to act on it.
 */
public final class ConsequenceLibrary {

    /**
     * Every flag that plants an echo. Kept alongside the switch below so a
     * saved consequence can be rebuilt from its id on load.
     */
    private static final List<String> ECHO_FLAGS = List.of(
            "used_bribery",
            "sold_palace_secret",
            "betrayed_comrade",
            "angered_scholars",
            "learned_palace_secrets",
            "accepted_private_patronage",
            "protected_commoners",
            "fair_scholar_judgment",
            "mentored_youth",
            "child_groomed_for_legacy",
            "devoted_parent",
            "undermined_rival",
            "framed_an_innocent_man",
            "betrayed_the_corps",
            "corrupted_a_text",
            "took_the_patrons_place",
            "stood_by_the_patron",
            "profited_from_famine",
            "emptied_the_granary",
            "gave_no_names",
            "held_the_ribat"
    );

    private ConsequenceLibrary() {
    }

    /**
     * Rebuilds a scheduled consequence from its id, restoring the trigger age
     * recorded in the save. Returns null for an id no longer in the library,
     * which lets removed content drop out of old saves cleanly.
     */
    public static DelayedConsequence byId(String id, int triggerAge) {
        for (String flag : ECHO_FLAGS) {
            for (DelayedConsequence template : consequencesFor(flag, 0)) {
                if (template.id().equals(id)) {
                    return new DelayedConsequence(
                            template.id(),
                            triggerAge,
                            template.title(),
                            template.description(),
                            template.statEffects(),
                            template.factionEffects(),
                            template.flagsToAdd(),
                            template.cancelledByFlag(),
                            template.requiredFlags()
                    );
                }
            }
        }

        return null;
    }

    /**
     * Returns the consequences a newly set flag should schedule.
     *
     * @param flag        the flag just recorded
     * @param currentAge  the character's age now, used to offset trigger ages
     */
    public static List<DelayedConsequence> consequencesFor(String flag, int currentAge) {
        List<DelayedConsequence> scheduled = new ArrayList<>();

        switch (flag) {

            // --- Corruption echoes ---
            case "used_bribery" -> scheduled.add(new DelayedConsequence(
                    "echo_bribery_audit",
                    currentAge + 22,
                    "The Ledger Is Reopened",
                    "An imperial auditor, decades late and utterly thorough, finds the gap in "
                            + "the tax records from the year of your childhood. The sum is trivial. "
                            + "The pattern is not. Questions are asked about a household that paid "
                            + "less than it owed and was never penalised.",
                    Map.of("reputation", -12, "stress", 10),
                    Map.of("court", -14, "scholars", -6),
                    List.of("old_corruption_exposed"),
                    "confessed_bribery",
                    List.of()
            ));

            case "sold_palace_secret" -> scheduled.add(new DelayedConsequence(
                    "echo_sold_secret",
                    currentAge + 18,
                    "The Secret Has a Long Memory",
                    "The knowledge you sold has travelled further than you ever intended. It "
                            + "surfaces now in the hands of someone who knows exactly where it came "
                            + "from, and who has waited patiently for the moment it would be worth "
                            + "the most to them.",
                    Map.of("stress", 14, "politicalPower", -10),
                    Map.of("court", -16, "shadowNetwork", 10),
                    List.of("secret_returned_to_haunt"),
                    "evidence_destroyed",
                    List.of()
            ));

            case "betrayed_comrade" -> scheduled.add(new DelayedConsequence(
                    "echo_betrayed_comrade",
                    currentAge + 20,
                    "The Man You Reported",
                    "The recruit you named all those years ago did not vanish. He rose, slowly "
                            + "and without forgetting, and now sits somewhere in the chain of command "
                            + "above you. He greets you by name. He remembers yours perfectly.",
                    Map.of("stress", 12, "reputation", -8),
                    Map.of("military", -15),
                    List.of("betrayal_came_due"),
                    "",
                    List.of()
            ));

            // --- Scholarly echoes ---
            case "angered_scholars" -> scheduled.add(new DelayedConsequence(
                    "echo_angered_scholars",
                    currentAge + 25,
                    "The Marginalia",
                    "A rival produces a manuscript from your youth, your own careless notes "
                            + "still in the margins, and reads them aloud to a room of jurists. What "
                            + "was arrogance in a boy is presented now as the settled character of a "
                            + "man who never learned humility.",
                    Map.of("reputation", -14, "stress", 8),
                    Map.of("scholars", -18, "court", -6),
                    List.of("marginalia_used_against_you"),
                    "fair_scholar_judgment",
                    List.of()
            ));

            case "learned_palace_secrets" -> scheduled.add(new DelayedConsequence(
                    "echo_palace_knowledge",
                    currentAge + 24,
                    "What the Servants Told You",
                    "The whispers you collected as a child turn out to describe a family still "
                            + "in power. You know something about them that they believe no living "
                            + "person knows. That knowledge is worth a great deal, and it is also "
                            + "extremely dangerous to hold.",
                    Map.of("politicalPower", 14, "stress", 10),
                    Map.of("shadowNetwork", 12, "court", -5),
                    List.of("holds_dangerous_knowledge"),
                    "",
                    List.of()
            ));

            // --- Patronage echoes ---
            case "accepted_private_patronage" -> scheduled.add(new DelayedConsequence(
                    "echo_patronage_debt",
                    currentAge + 14,
                    "The Favour Is Called In",
                    "Your patron has never once mentioned the arrangement, which is precisely "
                            + "how these arrangements work. He mentions it now. What he wants is not "
                            + "illegal, merely something you would never have chosen to do, and "
                            + "refusing would undo two decades of quiet advancement.",
                    Map.of("morality", -10, "stress", 12),
                    Map.of("court", 8, "commonPeople", -8),
                    List.of("patron_debt_collected"),
                    "refused_private_patronage",
                    List.of()
            ));

            // --- Virtue echoes: not every consequence is a punishment ---
            case "protected_commoners" -> scheduled.add(new DelayedConsequence(
                    "echo_protected_commoners",
                    currentAge + 28,
                    "They Never Forgot",
                    "A delegation arrives at your door, led by a man old enough to have been a "
                            + "child when you stood between his village and a tax collector. He has "
                            + "told the story so many times that it has outgrown the facts. The "
                            + "district considers you theirs.",
                    Map.of("reputation", 14, "stress", -8),
                    Map.of("commonPeople", 18, "court", -4),
                    List.of("remembered_by_the_people"),
                    "",
                    List.of()
            ));

            case "fair_scholar_judgment" -> scheduled.add(new DelayedConsequence(
                    "echo_fair_judgment",
                    currentAge + 20,
                    "The Judgment Is Cited",
                    "A jurist in a distant city cites a ruling of yours as settled precedent. "
                            + "You had thought it a small matter, decided in an afternoon. It has "
                            + "been quietly shaping decisions in courts you will never visit.",
                    Map.of("reputation", 12, "education", 6),
                    Map.of("scholars", 16),
                    List.of("judgment_became_precedent"),
                    "",
                    List.of()
            ));

            case "mentored_youth" -> scheduled.add(new DelayedConsequence(
                    "echo_mentored_youth",
                    currentAge + 22,
                    "The Student Returns",
                    "The young person you taught without expecting anything in return has "
                            + "become someone of consequence. They have not forgotten who gave them "
                            + "their first honest hour of instruction, and they are now in a "
                            + "position to say so publicly.",
                    Map.of("reputation", 10, "politicalPower", 8),
                    Map.of("scholars", 10, "court", 6),
                    List.of("student_became_powerful"),
                    "",
                    List.of()
            ));

            // --- Family echoes ---
            case "child_groomed_for_legacy" -> scheduled.add(new DelayedConsequence(
                    "echo_groomed_child",
                    currentAge + 20,
                    "The Weight You Placed on Them",
                    "The child you shaped from birth has become exactly what you designed, and "
                            + "resents you precisely for it. They are capable, ambitious, and "
                            + "entirely unwilling to be grateful.",
                    Map.of("familyLoyalty", -14, "politicalPower", 8),
                    Map.of("familyCouncil", -10, "nobles", 6),
                    List.of("heir_resents_you"),
                    "",
                    List.of()
            ));

            case "devoted_parent" -> scheduled.add(new DelayedConsequence(
                    "echo_devoted_parent",
                    currentAge + 20,
                    "The Child You Raised",
                    "The child you gave your time to has grown into someone who defends your "
                            + "name without being asked. Whatever else your life becomes, that much "
                            + "is already secured.",
                    Map.of("familyLoyalty", 16, "stress", -8),
                    Map.of("familyCouncil", 14),
                    List.of("heir_loyal"),
                    "",
                    List.of()
            ));

            case "undermined_rival" -> scheduled.add(new DelayedConsequence(
                    "echo_undermined_rival",
                    currentAge + 16,
                    "The Rival's Long Patience",
                    "The person you quietly sabotaged in your youth rebuilt themselves without "
                            + "your noticing. They have spent the intervening years assembling a "
                            + "precise account of what you did, and an audience willing to hear it.",
                    Map.of("reputation", -12, "stress", 12),
                    Map.of("nobles", -10, "court", -8),
                    List.of("rival_struck_back"),
                    "made_peace_with_rival",
                    List.of()
            ));

            // --- Echoes planted by the long arcs ---
            case "framed_an_innocent_man" -> scheduled.add(new DelayedConsequence(
                    "echo_framed_official",
                    currentAge + 19,
                    "His Son Reads the File",
                    "The official you buried under a manufactured file left a son, and that "
                            + "son has spent his career working toward the archive where it is "
                            + "kept. He has read it. He can tell, as anyone careful could, that "
                            + "the fragments were arranged rather than found.",
                    Map.of("reputation", -16, "stress", 14),
                    Map.of("court", -18, "scholars", -8),
                    List.of("framing_exposed"),
                    "",
                    List.of()
            ));

            case "betrayed_the_corps" -> scheduled.add(new DelayedConsequence(
                    "echo_betrayed_corps",
                    currentAge + 15,
                    "Soldiers Have Long Memories",
                    "The corps was punished, reorganised, and eventually rebuilt. The men in "
                            + "it now are not the men you informed on, but they were taught the "
                            + "story by those men, and they were taught your name with it.",
                    Map.of("stress", 16, "health", -8),
                    Map.of("military", -22, "shadowNetwork", 8),
                    List.of("corps_took_its_revenge"),
                    "",
                    List.of()
            ));

            case "corrupted_a_text" -> scheduled.add(new DelayedConsequence(
                    "echo_corrupted_text",
                    currentAge + 26,
                    "The Errors Are Traced",
                    "A physician follows a dosage in a copied treatise and a patient dies of "
                            + "it. The error is traced back through the copies to the hand that "
                            + "first introduced it. Your hand is quite distinctive.",
                    Map.of("reputation", -18, "morality", -8, "stress", 16),
                    Map.of("scholars", -20, "commonPeople", -10),
                    List.of("corruption_of_text_traced"),
                    "",
                    List.of()
            ));

            case "took_the_patrons_place" -> scheduled.add(new DelayedConsequence(
                    "echo_took_patrons_place",
                    currentAge + 17,
                    "The Office You Inherited",
                    "You have held his office for years now, and it has become clear that "
                            + "the accusation you helped confirm was, in the part that mattered, "
                            + "false. The records are in your own strongroom. You have read them "
                            + "more than once.",
                    Map.of("morality", -12, "stress", 18),
                    Map.of("nobles", -12, "court", -6),
                    List.of("learned_the_patron_was_innocent"),
                    "",
                    List.of()
            ));

            case "stood_by_the_patron" -> scheduled.add(new DelayedConsequence(
                    "echo_stood_by_patron",
                    currentAge + 21,
                    "The House Remembers Its Friend",
                    "The family you refused to abandon has recovered — slowly, partially, and "
                            + "with a very exact list of who spoke for them when it cost something. "
                            + "You are near the top of a very short list.",
                    Map.of("reputation", 14, "politicalPower", 12),
                    Map.of("nobles", 18, "familyCouncil", 8),
                    List.of("old_loyalty_repaid"),
                    "",
                    List.of()
            ));

            case "profited_from_famine" -> scheduled.add(new DelayedConsequence(
                    "echo_famine_profit",
                    currentAge + 24,
                    "The Year They Still Count From",
                    "The district still dates things from the famine. It also still knows, "
                            + "precisely, what a measure of your grain cost that winter. No court "
                            + "will ever hear the case. It is settled anyway, permanently, in "
                            + "every conversation you are not present for.",
                    Map.of("reputation", -18, "stress", 12),
                    Map.of("commonPeople", -25, "merchants", 6),
                    List.of("famine_profiteer_remembered"),
                    "",
                    List.of()
            ));

            case "emptied_the_granary" -> scheduled.add(new DelayedConsequence(
                    "echo_emptied_granary",
                    currentAge + 25,
                    "Two Hundred Households",
                    "A generation has grown up in this district on the strength of a winter "
                            + "you paid for. They are voters in every way that matters short of the "
                            + "formal one, and there is nothing you could ask for here that you "
                            + "would not be given.",
                    Map.of("reputation", 20, "politicalPower", 14),
                    Map.of("commonPeople", 25, "court", -6),
                    List.of("famine_generosity_remembered"),
                    "",
                    List.of()
            ));

            case "gave_no_names" -> scheduled.add(new DelayedConsequence(
                    "echo_gave_no_names",
                    currentAge + 18,
                    "The Men You Did Not Name",
                    "Three men are alive and working because you did not say their names in "
                            + "a room where saying them would have ended your discomfort "
                            + "immediately. They have never mentioned it to you. They have "
                            + "mentioned it to everyone else.",
                    Map.of("reputation", 16, "stress", -10),
                    Map.of("scholars", 18, "commonPeople", 10),
                    List.of("silence_repaid"),
                    "",
                    List.of()
            ));

            case "held_the_ribat" -> scheduled.add(new DelayedConsequence(
                    "echo_held_the_ribat",
                    currentAge + 20,
                    "The Fortress Is Named for You",
                    "The frontier has moved on and the fortress is a customs post now, but "
                            + "the name stuck, and men who were not born when you held it use it "
                            + "without knowing it was ever a person.",
                    Map.of("reputation", 18, "politicalPower", 10),
                    Map.of("military", 20, "court", 8),
                    List.of("the_ribat_bears_your_name"),
                    "",
                    List.of()
            ));

            default -> {
                // Most flags carry no long-term echo.
            }
        }

        return scheduled;
    }
}
