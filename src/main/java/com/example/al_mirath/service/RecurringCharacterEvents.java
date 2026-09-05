package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.NpcMemory;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.RivalFeud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecurringCharacterEvents {

    private RecurringCharacterEvents() {
    }

    /**
     * Builds the cast's events against their state at this moment.
     *
     * <p>The engine calls this every time it looks for an event rather than
     * once at startup, so the text always carries the title a character has
     * climbed to, the memory that still sits between you, and anyone who
     * joined the cast after the run began. The dead take no further part.
     */
    public static List<GameEvent> create(
            RecurringCharacterRegistry registry
    ) {
        List<GameEvent> events = new ArrayList<>();

        for (RecurringCharacter character : registry.all()) {
            if (!character.isAlive()) {
                continue;
            }

            if (character.isDescendant()) {
                addDescendantEvent(events, character);
                continue;
            }

            switch (character.getId()) {

                case "childhood_companion" ->
                        addCompanionEvents(events, character);

                case "early_rival" ->
                        addRivalEvents(events, character);

                case "elder_mentor" ->
                        addMentorEvents(events, character);

                default -> {
                }
            }
        }

        return events;
    }

    /**
     * Opens an event on the moment between the two of you that still carries
     * the most weight, so a reunion lands on shared history instead of on
     * nothing. Returns the description untouched while nothing has passed
     * between you yet.
     */
    private static String recalling(
            RecurringCharacter character,
            String description
    ) {
        NpcMemory memory = character.strongestMemory();

        if (memory == null) {
            return description;
        }

        // A descendant was not there for the memory they inherited; they were
        // raised on it. The only memories they own outright are the ones this
        // file's own descendant flags write.
        boolean inherited =
                character.isDescendant()
                        && !memory.id().startsWith("npc_descendant_");

        String opening =
                inherited
                        ? character.getName()
                        + " was raised on the story of what passed between you and "
                        + character.getParentName()
                        + " when you were "
                        : character.getName()
                        + " has not forgotten what passed between you when you were ";

        return opening
                + memory.playerAge()
                + ": "
                + memory.description()
                + "\n\n"
                + description;
    }

    private static void addCompanionEvents(
            List<GameEvent> events,
            RecurringCharacter companion
    ) {
        String name = companion.getName();

        events.add(
                new GameEvent(
                        name + " Shares a Secret",

                        recalling(
                                companion,

                                name
                                        + ", the child who grew up beside you, reveals where "
                                        + "a frightened family has hidden food from the tax collector. "
                                        + "The secret could save them, enrich you, or buy official favour."
                        ),

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
                                                "npc_friend_met",
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
                                                "npc_friend_met",
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
                                                "npc_friend_met",
                                                "npc_friend_betrayed"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "A Familiar Face Returns",

                        recalling(
                                companion,

                                "Years have passed. "
                                        + name
                                        + " returns as "
                                        + companion.getCurrentRole().toLowerCase()
                                        + ", no longer the child you remember. "
                                        + "Your shared past still shapes the silence between you."
                        ),

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
                                "npc_friend_met"
                        ),

                        List.of(
                                "npc_friend_betrayed"
                        )
                )
        );
    }

    /**
     * The rung a rival must have climbed before they can act on a grudge
     * rather than merely hold one. Below this they have the will but not the
     * standing.
     */
    private static final int RANK_TO_OBSTRUCT = 2;
    private static final int RANK_TO_STRIKE = 3;

    private static void addRivalEvents(
            List<GameEvent> events,
            RecurringCharacter rival
    ) {
        String name = rival.getName();

        addRivalRetaliation(events, rival);
        addRivalIntroduction(events, rival);

        events.add(
                new GameEvent(
                        "The Rival's Challenge",

                        recalling(
                                rival,

                                name
                                        + " has spent years measuring every achievement "
                                        + "against yours. Before witnesses, "
                                        + name
                                        + " challenges your competence and your right "
                                        + "to be respected."
                        ),

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
                                                "npc_rival_met",
                                                "npc_rival_outdebated"
                                        ),

                                        List.of(
                                                "npc_rival_met",
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
                                                "npc_rival_met",
                                                "npc_rival_respected"
                                        ),

                                        List.of(
                                                "npc_rival_met",
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
                                                "npc_rival_met",
                                                "npc_rival_publicly_humiliated"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "Your Rival Holds Power",

                        recalling(
                                rival,

                                name
                                        + " is "
                                        + rival.getCurrentRole().toLowerCase()
                                        + " now, and occupies a position from which a signature "
                                        + "could protect your household or ruin it. "
                                        + "Neither of you has forgotten the past."
                        ),

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
                        ),

                        List.of(),

                        List.of(),

                        List.of(),

                        List.of(
                                "npc_rival_met"
                        ),

                        List.of()
                )
        );
    }

    /**
     * What the rival does to you, rather than what they say to you.
     *
     * <p>Everything else in this file waits for the game to hand the player a
     * prompt. A rival who has climbed above you and has cause to hate you
     * should be doing something about it in the meantime, and these are the
     * moves available to them — gated on the feud being personal enough to
     * act on and on their holding the standing to act with.
     *
     * <p>Generated fresh each lookup like the rest, so the obstruction is
     * signed with whatever title they hold at that moment.
     */
    private static void addRivalRetaliation(
            List<GameEvent> events,
            RecurringCharacter rival
    ) {
        RivalFeud feud = rival.feud();
        String name = rival.getName();
        String role = rival.getCurrentRole().toLowerCase();

        if (feud.isAtLeast(RivalFeud.RESENTFUL)
                && rival.getRoleRank() >= RANK_TO_OBSTRUCT) {

            events.add(obstructionEvent(rival, name, role));
        }

        if (feud.isAtLeast(RivalFeud.VENGEFUL)
                && rival.getRoleRank() >= RANK_TO_STRIKE) {

            events.add(strikeEvent(rival, name, role));
        }
    }

    /** The quiet version: nothing you can prove, and nothing that moves. */
    private static GameEvent obstructionEvent(
            RecurringCharacter rival,
            String name,
            String role
    ) {
        return new GameEvent(
                "The Hand Behind the Door",

                recalling(
                        rival,

                        "A petition of yours has been sitting unanswered for a season. "
                                + "The clerk is apologetic and useless. Eventually someone tells "
                                + "you what everyone else already knows: it reached the desk of "
                                + name
                                + ", now "
                                + role
                                + ", and it has not moved since. There is nothing to appeal. "
                                + "Nothing was refused. It simply will not proceed."
                ),

                "Adulthood",

                List.of(
                        new Choice(
                                "Pay someone who can reach around " + name,

                                "The money finds a route the petition could not. It is "
                                        + "settled quietly, and it stays settled — but you have "
                                        + "learned what it costs to need something your rival "
                                        + "can touch.",

                                Map.of(
                                        "wealth", -14,
                                        "politicalPower", 4,
                                        "stress", 4
                                ),

                                Map.of(
                                        "shadowNetwork", 7,
                                        "court", -3
                                ),

                                List.of(),

                                List.of("npc_rival_bypassed")
                        ),

                        new Choice(
                                "Name " + name + " in open court",

                                "politicalPower",

                                58,

                                "You say the name aloud where it cannot be unsaid. The "
                                        + "obstruction ends within the week, because it now has "
                                        + "a witness. So does the enmity.",

                                "You accuse a superior of pettiness and are heard as "
                                        + "someone complaining about paperwork. "
                                        + name
                                        + " does not even need to answer it.",

                                Map.of(
                                        "politicalPower", 8,
                                        "reputation", 5
                                ),

                                Map.of(
                                        "reputation", -10,
                                        "stress", 12
                                ),

                                Map.of("court", 4),

                                Map.of("court", -8, "nobles", -5),

                                List.of(),

                                List.of("npc_rival_publicly_humiliated"),

                                List.of("npc_rival_counterattacked")
                        ),

                        new Choice(
                                "Withdraw the petition and find another way",

                                "You take the loss without making it a quarrel. It is the "
                                        + "cheapest thing you could have done, and "
                                        + name
                                        + " notes exactly how little it cost to stop you.",

                                Map.of(
                                        "stress", -4,
                                        "politicalPower", -7
                                ),

                                Map.of("court", -4),

                                List.of(),

                                List.of("npc_rival_emboldened")
                        )
                ),

                List.of(),

                List.of(),

                List.of(),

                List.of("npc_rival_met"),

                List.of()
        );
    }

    /** The loud version: they have the standing now, and they use it. */
    private static GameEvent strikeEvent(
            RecurringCharacter rival,
            String name,
            String role
    ) {
        return new GameEvent(
                name + " Moves Against Your House",

                recalling(
                        rival,

                        name
                                + " is "
                                + role
                                + " now, and has stopped being careful about it. An "
                                + "audit of your household's affairs has been ordered, the "
                                + "grounds are thin, and the official appointed to conduct it owes "
                                + name
                                + " their position. This is not a warning shot. It is the "
                                + "beginning of the thing your rival has been waiting to do."
                ),

                "Political Crisis",

                List.of(
                        new Choice(
                                "Meet the audit with a better record than theirs",

                                "education",

                                60,

                                "You hand over books so complete that the audit becomes an "
                                        + "embarrassment to whoever ordered it. "
                                        + name
                                        + " loses standing for having reached and missed.",

                                "The books do not hold. What began as a fishing expedition "
                                        + "becomes a finding, and the finding has your name on it.",

                                Map.of(
                                        "reputation", 10,
                                        "politicalPower", 6
                                ),

                                Map.of(
                                        "wealth", -18,
                                        "reputation", -14,
                                        "stress", 15
                                ),

                                Map.of("court", 8, "scholars", 5),

                                Map.of("court", -14, "nobles", -8),

                                List.of(),

                                List.of("npc_rival_counterattacked"),

                                List.of("npc_rival_struck_home")
                        ),

                        new Choice(
                                "Ruin " + name + " before the audit reports",

                                "You move first, and you move dirty. It works. You are also "
                                        + "now someone who did that, and the people who helped you "
                                        + "do it know where you keep your throat.",

                                Map.of(
                                        "politicalPower", 12,
                                        "morality", -14,
                                        "stress", 14
                                ),

                                Map.of(
                                        "shadowNetwork", 12,
                                        "court", -6,
                                        "commonPeople", -5
                                ),

                                List.of(),

                                List.of("npc_rival_exposed")
                        ),

                        new Choice(
                                "Send your household out of reach and wait it out",

                                "You move what matters beyond the audit's arm and let it "
                                        + "find an empty room. Your family is safe. They also "
                                        + "understand, now, that your quarrels are theirs.",

                                Map.of(
                                        "wealth", -20,
                                        "familyLoyalty", -8,
                                        "stress", 8,
                                        "health", 4
                                ),

                                Map.of("familyCouncil", -6),

                                List.of(),

                                List.of("npc_rival_house_withdrew")
                        )
                ),

                List.of(),

                List.of(),

                List.of(),

                List.of("npc_rival_met"),

                List.of()
        );
    }

    /**
     * Where the rivalry starts, years before it is worth anything.
     *
     * <p>The Youth challenge assumed a rivalry the player had never actually
     * seen begin. This is the afternoon it begins, and it is deliberately
     * small: a recitation in front of a teacher, decided by children who do
     * not yet know they are deciding anything.
     */
    private static void addRivalIntroduction(
            List<GameEvent> events,
            RecurringCharacter rival
    ) {
        String name = rival.getName();

        events.add(
                new GameEvent(
                        name + " Answers First",

                        recalling(
                                rival,

                                "The teacher sets the class a passage to recite and "
                                        + "asks who will go first. "
                                        + name
                                        + ", who has answered first every day this month, "
                                        + "is already standing. The room is waiting, and it "
                                        + "is waiting to see what you do about it."
                        ),

                        "Childhood",

                        List.of(
                                new Choice(
                                        "Stand up and recite it better",

                                        "education",

                                        45,

                                        "You get through it without a stumble, and the "
                                                + "teacher says so. "
                                                + name
                                                + " sits down slowly. Neither of you has "
                                                + "words for what just changed, but you both "
                                                + "felt it.",

                                        "You lose the thread halfway and finish badly. "
                                                + name
                                                + " finishes it for you, correctly, without "
                                                + "being asked.",

                                        Map.of(
                                                "education", 5,
                                                "reputation", 5
                                        ),

                                        Map.of(
                                                "reputation", -4,
                                                "stress", 5
                                        ),

                                        Map.of("scholars", 5),

                                        Map.of("scholars", -3),

                                        List.of(),

                                        List.of(
                                                "npc_rival_met",
                                                "npc_rival_outdebated"
                                        ),

                                        List.of(
                                                "npc_rival_met",
                                                "npc_rival_humiliated_you"
                                        )
                                ),

                                new Choice(
                                        "Point out the mistake " + name + " just made",

                                        "You wait until "
                                                + name
                                                + " is nearly finished, then name the error. "
                                                + "The teacher agrees. The other children laugh, "
                                                + "and go on laughing after the teacher has "
                                                + "stopped finding it funny.",

                                        Map.of(
                                                "reputation", 4,
                                                "morality", -4,
                                                "education", 2
                                        ),

                                        Map.of("scholars", 3),

                                        List.of(),

                                        List.of(
                                                "npc_rival_met",
                                                "npc_rival_publicly_humiliated"
                                        )
                                ),

                                new Choice(
                                        "Let " + name + " have it",

                                        "You stay in your seat. It costs you nothing today, "
                                                + "and "
                                                + name
                                                + " learns today that going first is free.",

                                        Map.of(
                                                "stress", -3,
                                                "reputation", -3
                                        ),

                                        Map.of(),

                                        List.of(),

                                        List.of(
                                                "npc_rival_met",
                                                "npc_rival_emboldened"
                                        )
                                )
                        )
                )
        );
    }

    /**
     * The mentor noticing the player, before there is anything in it for
     * either of them. The Youth event bargains over an obligation; this is
     * the moment the obligation becomes possible.
     */
    private static void addMentorIntroduction(
            List<GameEvent> events,
            RecurringCharacter mentor
    ) {
        String name = mentor.getName();

        events.add(
                new GameEvent(
                        name + " Stops You in the Doorway",

                        recalling(
                                mentor,

                                name
                                        + " has been watching the class from the doorway "
                                        + "for a week, which nobody has explained. Today "
                                        + name
                                        + " stops you on the way out and asks a question "
                                        + "that was not on the lesson, and waits for the "
                                        + "answer longer than a polite adult would."
                        ),

                        "Childhood",

                        List.of(
                                new Choice(
                                        "Give the honest answer, even unfinished",

                                        "You say what you actually think, including the "
                                                + "part you cannot yet defend. "
                                                + name
                                                + " does not praise it. "
                                                + name
                                                + " asks a second question, which is how you "
                                                + "learn you have passed something.",

                                        Map.of(
                                                "education", 6,
                                                "morality", 4
                                        ),

                                        Map.of("scholars", 6),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_met",
                                                "npc_mentor_guidance_accepted"
                                        )
                                ),

                                new Choice(
                                        "Give the answer the teacher would want",

                                        "education",

                                        40,

                                        "You produce the correct, expected answer, and it "
                                                + "is good enough that "
                                                + name
                                                + " nods and lets you go. You are not sure "
                                                + "whether that was the outcome you wanted.",

                                        name
                                                + " listens to you repeat a lesson badly, "
                                                + "thanks you, and does not stand in that "
                                                + "doorway again for a long while.",

                                        Map.of(
                                                "education", 4,
                                                "reputation", 3
                                        ),

                                        Map.of("stress", 4),

                                        Map.of("scholars", 3),

                                        Map.of("scholars", -3),

                                        List.of(),

                                        List.of("npc_mentor_met"),

                                        List.of(
                                                "npc_mentor_met",
                                                "npc_mentor_rejected"
                                        )
                                ),

                                new Choice(
                                        "Say nothing and keep walking",

                                        "You are a child being kept late by an adult you "
                                                + "do not know, so you leave. "
                                                + name
                                                + " does not stop you, and does not ask again "
                                                + "for years.",

                                        Map.of(
                                                "stress", -2,
                                                "education", -2
                                        ),

                                        Map.of("scholars", -4),

                                        List.of(),

                                        List.of(
                                                "npc_mentor_met",
                                                "npc_mentor_rejected"
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

        addMentorIntroduction(events, mentor);

        events.add(
                new GameEvent(
                        "The Mentor's Price",

                        recalling(
                                mentor,

                                name
                                        + ", your "
                                        + mentor.getCurrentRole().toLowerCase()
                                        + ", offers access to knowledge and influence. "
                                        + "In return, you must accept discipline and obligation."
                        ),

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
                                                "npc_mentor_met",
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
                                                "npc_mentor_met",
                                                "npc_mentor_supported_family"
                                        ),

                                        List.of(
                                                "npc_mentor_met",
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
                                                "npc_mentor_met",
                                                "npc_mentor_rejected"
                                        )
                                )
                        )
                )
        );

        events.add(
                new GameEvent(
                        "The Last Lesson",

                        recalling(
                                mentor,

                                name
                                        + " has grown old — "
                                        + mentor.getCurrentRole().toLowerCase()
                                        + " at "
                                        + mentor.getAge()
                                        + ". The person who once judged "
                                        + "your potential now asks what you intend "
                                        + "to leave behind."
                        ),

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
                        ),

                        List.of(),

                        List.of(),

                        List.of(),

                        List.of(
                                "npc_mentor_met"
                        ),

                        List.of()
                )
        );
    }

    /**
     * The last beat of a bond that outlived the person who formed it. The
     * child arrives carrying whatever their parent could not let go of.
     *
     * <p>What they carry decides which door they come to. A friend's child
     * comes asking; a rival's child comes collecting.
     */
    private static void addDescendantEvent(
            List<GameEvent> events,
            RecurringCharacter heir
    ) {
        if (heir.getRelationshipType() == RelationshipType.RIVAL) {
            addInheritedFeudEvent(events, heir);
            return;
        }

        addPetitioningHeirEvent(events, heir);
    }

    /**
     * The quarrel outliving the person who started it.
     *
     * <p>This is the point of keeping a rival alive across a whole run: the
     * humiliation you handed someone at fifteen is still being answered after
     * they are dead, by someone who only ever heard their side.
     */
    private static void addInheritedFeudEvent(
            List<GameEvent> events,
            RecurringCharacter heir
    ) {
        String name = heir.getName();
        String parent = heir.getParentName();

        events.add(
                new GameEvent(
                        name + " Finishes It",

                        recalling(
                                heir,

                                name
                                        + ", child of "
                                        + parent
                                        + ", has spent an inheritance on lawyers and "
                                        + "witnesses, and is now close enough to your household "
                                        + "to do it harm. "
                                        + parent
                                        + " is years dead. The quarrel is not, because "
                                        + name
                                        + " was raised inside it and has never heard your half."
                        ),

                        "Legacy",

                        List.of(
                                new Choice(
                                        "Tell " + name + " what actually happened",

                                        "reputation",

                                        55,

                                        "You give the account no one gave them: not flattering "
                                                + "to you, and true. "
                                                + name
                                                + " does not forgive you. But they stop, which "
                                                + "is the most anyone could have asked.",

                                        name
                                                + " hears an old enemy explaining why the injury "
                                                + "was reasonable, which is exactly what they were "
                                                + "told to expect from you.",

                                        Map.of(
                                                "morality", 10,
                                                "stress", -8
                                        ),

                                        Map.of(
                                                "stress", 12,
                                                "reputation", -8
                                        ),

                                        Map.of(
                                                "commonPeople", 6,
                                                "familyCouncil", 5
                                        ),

                                        Map.of("court", -6),

                                        List.of(),

                                        List.of("npc_rival_heir_reconciled"),

                                        List.of("npc_rival_heir_unconvinced")
                                ),

                                new Choice(
                                        "Break " + name + " as you broke " + parent,

                                        "You do it well, because you have had a lifetime of "
                                                + "practice on this family. The quarrel ends. It "
                                                + "ends the way it was always going to, and your "
                                                + "own children watch you end it.",

                                        Map.of(
                                                "politicalPower", 8,
                                                "morality", -16,
                                                "familyLoyalty", -10,
                                                "stress", 10
                                        ),

                                        Map.of(
                                                "shadowNetwork", 8,
                                                "commonPeople", -10,
                                                "familyCouncil", -6
                                        ),

                                        List.of(),

                                        List.of("npc_rival_heir_crushed")
                                ),

                                new Choice(
                                        "Settle on " + name + " what " + parent
                                                + " was never given",

                                        "You pay the debt your rival always claimed you owed, "
                                                + "to someone who did not incur it. It is not an "
                                                + "apology and "
                                                + name
                                                + " does not take it as one, but the case is "
                                                + "withdrawn.",

                                        Map.of(
                                                "wealth", -22,
                                                "morality", 6,
                                                "stress", -5
                                        ),

                                        Map.of(
                                                "merchants", 4,
                                                "commonPeople", 5
                                        ),

                                        List.of(),

                                        List.of("npc_rival_heir_bought_off")
                                )
                        )
                )
        );
    }

    private static void addPetitioningHeirEvent(
            List<GameEvent> events,
            RecurringCharacter heir
    ) {
        String name = heir.getName();
        String parent = heir.getParentName();

        events.add(
                new GameEvent(
                        name + " Comes Asking",

                        recalling(
                                heir,

                                name
                                        + ", child of "
                                        + parent
                                        + ", stands at your gate. "
                                        + parent
                                        + " is gone, and everything that passed between "
                                        + "the two of you has been handed down to someone "
                                        + "who was not there to see it."
                        ),

                        "Legacy",

                        List.of(
                                new Choice(
                                        "Take " + name + " into your household",

                                        "What began in one generation continues into "
                                                + "another. "
                                                + name
                                                + " will remember who opened the door.",

                                        Map.of(
                                                "morality", 8,
                                                "familyLoyalty", 7,
                                                "wealth", -6
                                        ),

                                        Map.of(
                                                "familyCouncil", 8,
                                                "commonPeople", 6
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_descendant_sheltered"
                                        )
                                ),

                                new Choice(
                                        "Settle the old debt in silver",

                                        "You pay what the past is worth to you and "
                                                + "watch "
                                                + name
                                                + " leave with it.",

                                        Map.of(
                                                "wealth", -10,
                                                "reputation", 3,
                                                "stress", -2
                                        ),

                                        Map.of(
                                                "merchants", 4
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_descendant_paid"
                                        )
                                ),

                                new Choice(
                                        "Refuse the claim entirely",

                                        "You owe the dead nothing, you tell yourself. "
                                                + name
                                                + " does not argue, which is worse.",

                                        Map.of(
                                                "morality", -10,
                                                "stress", 5,
                                                "politicalPower", 3
                                        ),

                                        Map.of(
                                                "commonPeople", -8,
                                                "familyCouncil", -5
                                        ),

                                        List.of(),

                                        List.of(
                                                "npc_descendant_refused"
                                        )
                                )
                        )
                )
        );
    }
}
