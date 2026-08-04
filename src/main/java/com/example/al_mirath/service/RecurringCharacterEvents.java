package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.RecurringCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecurringCharacterEvents {

    private RecurringCharacterEvents() {
    }

    public static List<GameEvent> create(
            RecurringCharacterRegistry registry
    ) {
        List<GameEvent> events = new ArrayList<>();

        RecurringCharacter companion =
                registry.get("childhood_companion");

        RecurringCharacter rival =
                registry.get("early_rival");

        RecurringCharacter mentor =
                registry.get("elder_mentor");

        if (companion != null) {
            addCompanionEvents(events, companion);
        }

        if (rival != null) {
            addRivalEvents(events, rival);
        }

        if (mentor != null) {
            addMentorEvents(events, mentor);
        }

        return events;
    }

    private static void addCompanionEvents(
            List<GameEvent> events,
            RecurringCharacter companion
    ) {
        String name = companion.getName();

        events.add(
                new GameEvent(
                        name + " Shares a Secret",

                        name
                                + ", the child who grew up beside you, reveals where "
                                + "a frightened family has hidden food from the tax collector. "
                                + "The secret could save them, enrich you, or buy official favour.",

                        "Childhood",

                        List.of(
                                new Choice(
                                        "Protect " + name + "'s secret",

                                        name
                                                + " never forgets that you chose loyalty "
                                                + "over personal advantage.",

                                        Map.of(
                                                "morality", 6,
                                                "familyLoyalty", 4,
                                                "stress", 2
                                        ),

                                        Map.of(
                                                "commonPeople", 7
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_secret_protected"
                                        )
                                ),

                                new Choice(
                                        "Help the hidden family",

                                        "Together, you move part of the food before "
                                                + "the officials arrive. "
                                                + name
                                                + " sees courage and compassion in you.",

                                        Map.of(
                                                "reputation", 7,
                                                "morality", 8,
                                                "stress", 5
                                        ),

                                        Map.of(
                                                "commonPeople", 10,
                                                "court", -4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_family_helped"
                                        )
                                ),

                                new Choice(
                                        "Sell the secret to the collector",

                                        name
                                                + " looks at you as if meeting a stranger. "
                                                + "The payment is real. So is the betrayal.",

                                        Map.of(
                                                "wealth", 10,
                                                "morality", -12,
                                                "stress", 6
                                        ),

                                        Map.of(
                                                "court", 6,
                                                "commonPeople", -10,
                                                "shadowNetwork", 4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_betrayed"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "A Familiar Face Returns",

                        "Years have passed. "
                                + name
                                + " returns, no longer the child you remember. "
                                + "Your shared past still shapes the silence between you.",

                        "Adulthood",

                        List.of(
                                new Choice(
                                        "Welcome " + name + " into your household",

                                        "The old bond becomes a living alliance "
                                                + "rather than a childhood memory.",

                                        Map.of(
                                                "familyLoyalty", 6,
                                                "reputation", 4,
                                                "stress", -5
                                        ),

                                        Map.of(
                                                "familyCouncil", 8,
                                                "commonPeople", 5
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_welcomed_back"
                                        )
                                ),

                                new Choice(
                                        "Ask " + name + " to become your trusted agent",

                                        "politicalPower",

                                        52,

                                        name
                                                + " accepts and becomes your eyes "
                                                + "beyond the court.",

                                        name
                                                + " refuses, wounded that friendship "
                                                + "has become another instrument of power.",

                                        Map.of(
                                                "politicalPower", 8,
                                                "reputation", 4
                                        ),

                                        Map.of(
                                                "stress", 7,
                                                "reputation", -5
                                        ),

                                        Map.of(
                                                "shadowNetwork", 8,
                                                "commonPeople", 4
                                        ),

                                        Map.of(
                                                "commonPeople", -5
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_became_agent"
                                        ),

                                        List.of(
                                                "npc_friend_refused_service"
                                        )
                                ),

                                new Choice(
                                        "Keep your distance",

                                        "You preserve your position, but another thread "
                                                + "connecting you to your beginnings grows thin.",

                                        Map.of(
                                                "stress", -3,
                                                "familyLoyalty", -5
                                        ),

                                        Map.of(
                                                "commonPeople", -4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_friend_distanced"
                                        )
                                )
                        ),

                        List.of(),

                        List.of(),

                        List.of(),

                        List.of(
                                "npc_friend_secret_protected"
                        ),

                        List.of(
                                "npc_friend_betrayed"
                        )
                )
        );
    }

    private static void addRivalEvents(
            List<GameEvent> events,
            RecurringCharacter rival
    ) {
        String name = rival.getName();

        events.add(
                new GameEvent(
                        "The Rival's Challenge",

                        name
                                + " has spent years measuring every achievement "
                                + "against yours. Before witnesses, "
                                + name
                                + " challenges your competence and your right "
                                + "to be respected.",

                        "Youth",

                        List.of(
                                new Choice(
                                        "Defeat " + name + " through knowledge",

                                        "education",

                                        55,

                                        "Your answer is precise and calm. "
                                                + name
                                                + " is forced to acknowledge your ability.",

                                        "Your argument collapses under questioning, "
                                                + "and "
                                                + name
                                                + " enjoys every moment.",

                                        Map.of(
                                                "education", 5,
                                                "reputation", 10
                                        ),

                                        Map.of(
                                                "reputation", -8,
                                                "stress", 8
                                        ),

                                        Map.of(
                                                "scholars", 8
                                        ),

                                        Map.of(
                                                "scholars", -4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_rival_outdebated"
                                        ),

                                        List.of(
                                                "npc_rival_humiliated_you"
                                        )
                                ),

                                new Choice(
                                        "Turn competition into mutual respect",

                                        "reputation",

                                        50,

                                        "You praise "
                                                + name
                                                + "'s strengths without surrendering "
                                                + "your own dignity. Rivalry becomes respect.",

                                        name
                                                + " mistakes restraint for weakness "
                                                + "and grows more aggressive.",

                                        Map.of(
                                                "morality", 6,
                                                "reputation", 6
                                        ),

                                        Map.of(
                                                "stress", 6,
                                                "politicalPower", -3
                                        ),

                                        Map.of(
                                                "commonPeople", 6
                                        ),

                                        Map.of(),

                                        List.of(),

                                        List.of(
                                                "npc_rival_respected"
                                        ),

                                        List.of(
                                                "npc_rival_emboldened"
                                        )
                                ),

                                new Choice(
                                        "Humiliate " + name + " publicly",

                                        "You win the room, but transform competition "
                                                + "into something much more dangerous.",

                                        Map.of(
                                                "politicalPower", 7,
                                                "morality", -6,
                                                "stress", 4
                                        ),

                                        Map.of(
                                                "nobles", 4,
                                                "shadowNetwork", 3
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_rival_publicly_humiliated"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "Your Rival Holds Power",

                        name
                                + " now occupies a position from which a signature "
                                + "could protect your household or ruin it. "
                                + "Neither of you has forgotten the past.",

                        "Political Crisis",

                        List.of(
                                new Choice(
                                        "Offer a sincere reconciliation",

                                        "morality",

                                        58,

                                        name
                                                + " accepts cautiously. Years of hostility "
                                                + "do not disappear, but bloodshed may "
                                                + "have been avoided.",

                                        name
                                                + " rejects the offer and treats it as "
                                                + "proof that you are afraid.",

                                        Map.of(
                                                "morality", 8,
                                                "stress", -4
                                        ),

                                        Map.of(
                                                "stress", 8,
                                                "reputation", -4
                                        ),

                                        Map.of(
                                                "court", 5
                                        ),

                                        Map.of(
                                                "court", -6
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_rival_reconciled"
                                        ),

                                        List.of(
                                                "npc_rival_rejected_peace"
                                        )
                                ),

                                new Choice(
                                        "Expose " + name + "'s old wrongdoing",

                                        "politicalPower",

                                        65,

                                        "The evidence holds. "
                                                + name
                                                + "'s influence cracks under scrutiny.",

                                        "The evidence is dismissed as revenge, and "
                                                + name
                                                + " strikes back.",

                                        Map.of(
                                                "politicalPower", 12,
                                                "reputation", 5,
                                                "morality", -4
                                        ),

                                        Map.of(
                                                "politicalPower", -10,
                                                "reputation", -8,
                                                "stress", 12
                                        ),

                                        Map.of(
                                                "court", 5,
                                                "shadowNetwork", 6
                                        ),

                                        Map.of(
                                                "court", -10
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_rival_exposed"
                                        ),

                                        List.of(
                                                "npc_rival_counterattacked"
                                        )
                                ),

                                new Choice(
                                        "Submit and request protection",

                                        name
                                                + " grants protection, but makes certain "
                                                + "everyone understands the new balance "
                                                + "between you.",

                                        Map.of(
                                                "health", 6,
                                                "politicalPower", -10,
                                                "stress", 8
                                        ),

                                        Map.of(
                                                "court", 6,
                                                "nobles", -5
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_rival_owes_protection"
                                        )
                                )
                        )
                )
        );
    }

    private static void addMentorEvents(
            List<GameEvent> events,
            RecurringCharacter mentor
    ) {
        String name = mentor.getName();

        events.add(
                new GameEvent(
                        "The Mentor's Price",

                        name
                                + ", your "
                                + mentor.getCurrentRole().toLowerCase()
                                + ", offers access to knowledge and influence. "
                                + "In return, you must accept discipline and obligation.",

                        "Youth",

                        List.of(
                                new Choice(
                                        "Accept " + name + "'s guidance",

                                        "The lessons are demanding, but "
                                                + name
                                                + " begins treating your future "
                                                + "as a personal responsibility.",

                                        Map.of(
                                                "education", 12,
                                                "stress", 5,
                                                "reputation", 4
                                        ),

                                        Map.of(
                                                "scholars", 8
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_guidance_accepted"
                                        )
                                ),

                                new Choice(
                                        "Ask " + name + " to teach your household too",

                                        "reputation",

                                        48,

                                        name
                                                + " agrees, impressed that your ambition "
                                                + "includes the people who raised you.",

                                        name
                                                + " believes you are bargaining before "
                                                + "earning trust.",

                                        Map.of(
                                                "education", 7,
                                                "familyLoyalty", 10,
                                                "reputation", 4
                                        ),

                                        Map.of(
                                                "reputation", -4,
                                                "stress", 5
                                        ),

                                        Map.of(
                                                "familyCouncil", 8,
                                                "scholars", 5
                                        ),

                                        Map.of(
                                                "scholars", -4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_supported_family"
                                        ),

                                        List.of(
                                                "npc_mentor_request_refused"
                                        )
                                ),

                                new Choice(
                                        "Reject the obligation",

                                        "You keep your independence, but the door "
                                                + name
                                                + " opened begins to close.",

                                        Map.of(
                                                "stress", -5,
                                                "education", -4
                                        ),

                                        Map.of(
                                                "scholars", -6
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_rejected"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "The Last Lesson",

                        name
                                + " has grown old. The person who once judged "
                                + "your potential now asks what you intend "
                                + "to leave behind.",

                        "Legacy",

                        List.of(
                                new Choice(
                                        "Preserve " + name + "'s teachings",

                                        "You spend the evening recording lessons "
                                                + "that might otherwise vanish with one life.",

                                        Map.of(
                                                "education", 8,
                                                "morality", 7,
                                                "stress", -4
                                        ),

                                        Map.of(
                                                "scholars", 10
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_legacy_preserved"
                                        )
                                ),

                                new Choice(
                                        "Admit where you failed their expectations",

                                        "Your honesty brings a final, difficult peace "
                                                + "between teacher and student.",

                                        Map.of(
                                                "morality", 9,
                                                "reputation", 4,
                                                "stress", -8
                                        ),

                                        Map.of(
                                                "scholars", 5
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_final_reconciliation"
                                        )
                                ),

                                new Choice(
                                        "Claim your achievements belong to you alone",

                                        name
                                                + " says nothing. The disappointment "
                                                + "in the room feels heavier than anger.",

                                        Map.of(
                                                "politicalPower", 5,
                                                "morality", -9,
                                                "stress", 6
                                        ),

                                        Map.of(
                                                "scholars", -9
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_disowned_legacy"
                                        )
                                )
                        )
                )
        );
    }
}