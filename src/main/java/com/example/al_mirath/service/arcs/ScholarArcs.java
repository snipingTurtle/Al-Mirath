package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs for the life of learning: a rise through the madrasa, and the danger
 * that comes with asking the wrong question in front of the wrong jurist.
 *
 * <p>Each arc is a chain, not a single prompt. An opening event sets a
 * {@code *_arc_started} flag; later events require it and branch on which
 * path the earlier choice took, so a run follows one continuous thread
 * instead of drawing unrelated scenes.
 */
public final class ScholarArcs {

    private ScholarArcs() {
    }

    public static void addScholarsRiseArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Circle of the Teacher",
                "You are permitted to sit at the edge of a teaching circle. You understand "
                        + "perhaps a third of what is said. The teacher notices you copying every "
                        + "word regardless, and after the lesson he asks whether you understood "
                        + "any of it. How you answer will decide what he does with you.",
                "Youth",
                List.of(
                        new Choice(
                                "Admit honestly that you understood almost nothing",
                                "He laughs, not unkindly, and tells you that an honest ignorance is "
                                        + "worth more than a clever lie. He begins teaching you separately, "
                                        + "slowly, from the beginning.",
                                Map.of("education", 14, "morality", 6, "stress", 3),
                                Map.of("scholars", 12),
                                List.of(),
                                List.of("scholars_rise_started", "honest_student")
                        ),
                        new Choice(
                                "Repeat back the phrases you memorised without understanding",
                                "You perform well enough to impress the room. The teacher says nothing, "
                                        + "but from then on he watches you the way one watches a person who "
                                        + "has shown they will perform rather than admit a gap.",
                                Map.of("education", 8, "reputation", 8, "morality", -6),
                                Map.of("scholars", 6, "court", 4),
                                List.of(),
                                List.of("scholars_rise_started", "performing_student")
                        ),
                        new Choice(
                                "Ask him the one question that genuinely confused you",
                                "education",
                                30,
                                "Your question turns out to be the exact difficulty the lesson had "
                                        + "skirted. The circle goes quiet. The teacher answers you properly, "
                                        + "and afterwards asks your name.",
                                "Your question lands badly, revealing you had followed none of the "
                                        + "argument. A few students smile. The teacher moves on without "
                                        + "answering.",
                                Map.of("education", 16, "reputation", 10, "stress", 5),
                                Map.of("reputation", -6, "stress", 8),
                                Map.of("scholars", 15),
                                Map.of("scholars", -5),
                                List.of(),
                                List.of("scholars_rise_started", "asked_the_real_question"),
                                List.of("scholars_rise_started", "embarrassed_in_circle")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Copyist's Wage",
                "Your hand is good enough now to be paid for it. A wealthy patron wants a "
                        + "private copy of a medical treatise. A rival madrasa wants the same text "
                        + "copied badly, with three errors buried where a careless reader would "
                        + "not find them, to discredit the school that holds the original.",
                "Youth",
                List.of(
                        new Choice(
                                "Copy faithfully for the patron and refuse the other commission",
                                "You are paid once instead of twice, and you sleep easily. The patron "
                                        + "mentions your name to people who commission a great deal of work.",
                                Map.of("wealth", 10, "education", 8, "morality", 8),
                                Map.of("scholars", 10, "nobles", 5),
                                List.of(),
                                List.of("faithful_copyist")
                        ),
                        new Choice(
                                "Take both commissions and bury the errors as asked",
                                "The money is very good. Somewhere a physician will one day follow "
                                        + "your corrupted dosage and a patient will pay for it, but that is "
                                        + "far away and the coin is here.",
                                Map.of("wealth", 20, "morality", -16, "stress", 8),
                                Map.of("scholars", -8, "merchants", 6),
                                List.of(),
                                List.of("corrupted_a_text")
                        ),
                        new Choice(
                                "Take the second commission, then quietly warn the rival school",
                                "You take their money and betray the plot in the same week. Both "
                                        + "houses now owe you something, and neither is entirely sure "
                                        + "what you are.",
                                Map.of("wealth", 12, "politicalPower", 8, "morality", -5, "stress", 10),
                                Map.of("scholars", 6, "shadowNetwork", 10),
                                List.of(),
                                List.of("played_the_schools")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("scholars_rise_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Chair Falls Vacant",
                "The old teacher is dead, and his chair at the madrasa is open. You are not "
                        + "the obvious successor. The obvious successor is competent, well-connected, "
                        + "and entirely uninterested in students who cannot pay.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Make your case on the strength of your scholarship alone",
                                "reputation",
                                45,
                                "You argue your case in front of the assembled jurists and win it on "
                                        + "merit. The victory is clean, and everyone in the room knows it.",
                                "Merit is not the currency of this room. Your rival's uncle endows the "
                                        + "library. You are thanked for your interest.",
                                Map.of("politicalPower", 16, "reputation", 16, "education", 6),
                                Map.of("reputation", -10, "stress", 14),
                                Map.of("scholars", 18, "commonPeople", 8),
                                Map.of("scholars", -6, "nobles", -6),
                                List.of(),
                                List.of("won_the_chair", "won_it_cleanly"),
                                List.of("lost_the_chair")
                        ),
                        new Choice(
                                "Secure the chair by promising the endowment board favourable rulings",
                                "The chair is yours before the week is out. It cost you nothing you "
                                        + "can point to, which is precisely the problem: you will be paying "
                                        + "for it in small rulings for the rest of your career.",
                                Map.of("politicalPower", 18, "morality", -14, "stress", 10),
                                Map.of("scholars", 8, "nobles", 12, "commonPeople", -8),
                                List.of(),
                                List.of("won_the_chair", "bought_the_chair")
                        ),
                        new Choice(
                                "Step aside and open a free teaching circle in the courtyard",
                                "You take no chair. You teach in the open air, to anyone who comes, "
                                        + "and within two years the courtyard is more crowded than the hall.",
                                Map.of("reputation", 14, "morality", 14, "wealth", -10, "stress", -6),
                                Map.of("commonPeople", 18, "scholars", 6, "nobles", -6),
                                List.of(),
                                List.of("teacher_of_the_courtyard")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("scholars_rise_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "What the Students Carry",
                "You are old now, and your students are scattered across cities you will "
                        + "never see. What they carry from you is the only part of your work that "
                        + "will outlive the paper it was written on.",
                "Legacy",
                List.of(
                        new Choice(
                                "Teach them to doubt, including doubting you",
                                "You tell them that a teacher who cannot be questioned has stopped "
                                        + "being a teacher. Some find it unsettling. The best of them find "
                                        + "it the most valuable thing you ever said.",
                                Map.of("education", 8, "morality", 12, "reputation", 10),
                                Map.of("scholars", 16, "court", -5),
                                List.of(),
                                List.of("taught_them_to_doubt")
                        ),
                        new Choice(
                                "Teach them the arts of patronage and advancement",
                                "You teach them how the world actually distributes chairs and stipends. "
                                        + "It is not noble instruction, but every one of them will eat.",
                                Map.of("politicalPower", 12, "wealth", 8, "morality", -6),
                                Map.of("court", 12, "nobles", 8, "scholars", -4),
                                List.of(),
                                List.of("taught_them_the_game")
                        ),
                        new Choice(
                                "Give them your library and say nothing further",
                                "You hand over the books and let the work speak without you standing "
                                        + "beside it. It is the last lesson, and the quietest.",
                                Map.of("wealth", -14, "morality", 10, "stress", -10),
                                Map.of("scholars", 14, "familyCouncil", -5),
                                List.of(),
                                List.of("gave_away_the_library")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("scholars_rise_started"),
                List.of()
        ));
    }

    public static void addHeresyAccusationArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Question That Should Not Be Asked",
                "In a late-night argument among students, someone raises a question about "
                        + "the created nature of scripture — the question that has ended careers "
                        + "and, in living memory, ended lives. The room looks to you. Someone in "
                        + "this room will repeat what you say tomorrow.",
                "Youth",
                List.of(
                        new Choice(
                                "Argue the position you actually believe",
                                "You say what you think, carefully and completely. Two students look "
                                        + "at you with new respect. One looks at you the way a man looks at "
                                        + "something he intends to remember.",
                                Map.of("education", 12, "morality", 10, "stress", 12),
                                Map.of("scholars", 10, "court", -8),
                                List.of(),
                                List.of("heresy_arc_started", "spoke_your_mind")
                        ),
                        new Choice(
                                "Give the safe, orthodox answer and change the subject",
                                "You recite the settled position without a flicker. The conversation "
                                        + "moves on. You have learned something about yourself that you would "
                                        + "rather not have learned.",
                                Map.of("stress", -4, "morality", -6, "education", 4),
                                Map.of("scholars", 4, "court", 6),
                                List.of(),
                                List.of("heresy_arc_started", "chose_orthodoxy")
                        ),
                        new Choice(
                                "Refuse to answer and name the question itself as a trap",
                                "You point out, evenly, that the question is being asked in a room "
                                        + "with a door and that doors have people behind them. The room goes "
                                        + "cold. The argument ends there.",
                                Map.of("politicalPower", 8, "stress", 8, "reputation", 5),
                                Map.of("shadowNetwork", 8, "scholars", -4),
                                List.of(),
                                List.of("heresy_arc_started", "named_the_trap")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Accusation Is Filed",
                "A document has been submitted to the chief jurist accusing you of holding "
                        + "an innovation contrary to the consensus. It quotes you accurately. That "
                        + "is the worst part — someone was listening carefully, and wrote it down "
                        + "correctly, and waited.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Recant fully and publicly",
                                "You stand and disavow the position in front of the assembly. The "
                                        + "danger passes within the month. Something else passes with it, and "
                                        + "you are not sure it comes back.",
                                Map.of("morality", -12, "reputation", -8, "stress", -10),
                                Map.of("court", 10, "scholars", -6),
                                List.of(),
                                List.of("recanted_publicly")
                        ),
                        new Choice(
                                "Defend the position on legal and scriptural grounds",
                                "education",
                                60,
                                "You dismantle the accusation using the very sources it cited. The "
                                        + "chief jurist rules the charge unfounded, and your name is now "
                                        + "spoken with a certain caution in every school in the city.",
                                "Your defence is learned and entirely unpersuasive to a room that had "
                                        + "decided beforehand. The charge stands. You are barred from "
                                        + "teaching for a term, and the bar is remembered longer than the term.",
                                Map.of("reputation", 18, "education", 10, "politicalPower", 10, "stress", 12),
                                Map.of("reputation", -18, "stress", 20, "politicalPower", -12),
                                Map.of("scholars", 18, "commonPeople", 8),
                                Map.of("scholars", -14, "court", -12),
                                List.of(),
                                List.of("defended_and_won"),
                                List.of("convicted_of_innovation")
                        ),
                        new Choice(
                                "Find out who filed it, and make the problem go away",
                                "politicalPower",
                                50,
                                "The document is withdrawn. The man who filed it accepts a posting in "
                                        + "a distant province and does not write again. No one asks how.",
                                "Your approach reaches the wrong ear. Now there are two accusations: "
                                        + "the original, and the far more serious charge of interfering with "
                                        + "a jurist's court.",
                                Map.of("politicalPower", 12, "morality", -14, "stress", 10),
                                Map.of("stress", 22, "reputation", -16, "politicalPower", -10),
                                Map.of("shadowNetwork", 16, "court", -5),
                                Map.of("court", -18, "scholars", -12),
                                List.of(),
                                List.of("suppressed_the_charge"),
                                List.of("caught_interfering")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("heresy_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Inquisitor's Season",
                "The court has decided that doctrinal firmness is politically useful this "
                        + "year. Panels are convened. Old files are reopened. Your name is in an "
                        + "old file.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Cooperate fully and name others to demonstrate your loyalty",
                                "You survive the season comfortably. Three other men do not. You will "
                                        + "meet their students for the rest of your life, and they will know "
                                        + "exactly who you are.",
                                Map.of("politicalPower", 14, "morality", -20, "stress", 14),
                                Map.of("court", 16, "scholars", -20, "commonPeople", -8),
                                List.of(),
                                List.of("named_others_to_the_panel")
                        ),
                        new Choice(
                                "Say nothing about anyone, whatever it costs you",
                                "health",
                                40,
                                "You endure the questioning without giving them a single name. It "
                                        + "costs you months and a great deal of health, and it buys you a "
                                        + "reputation that outlives the dynasty that tested you.",
                                "You give them nothing, and they take it out of you anyway. You come "
                                        + "out of it permanently diminished in body, and unbroken in the only "
                                        + "way that mattered to you.",
                                Map.of("reputation", 18, "morality", 18, "health", -10, "stress", 12),
                                Map.of("health", -25, "reputation", 12, "morality", 18, "stress", 18),
                                Map.of("scholars", 20, "commonPeople", 14, "court", -10),
                                Map.of("scholars", 16, "commonPeople", 12, "court", -14),
                                List.of(),
                                List.of("gave_no_names"),
                                List.of("gave_no_names", "broken_by_the_panel")
                        ),
                        new Choice(
                                "Leave the city before the summons arrives",
                                "You are gone within three days. Exile is not comfortable, but it is "
                                        + "survivable, and the panel cannot question a man it cannot find.",
                                Map.of("wealth", -16, "stress", 10, "politicalPower", -10),
                                Map.of("court", -12, "scholars", 5, "shadowNetwork", 8),
                                List.of(),
                                List.of("fled_the_inquisition")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("heresy_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Verdict History Keeps",
                "The doctrinal fashion has turned again, as it always does. What was "
                        + "dangerous to say is now unremarkable, and what was safe is now suspect. "
                        + "Your conduct during the season is being reassessed by people who were "
                        + "not there.",
                "Legacy",
                List.of(
                        new Choice(
                                "Set down a full and honest account, including what you regret",
                                "You write it all: the fear, the compromises, the names you did or did "
                                        + "not give. It is not a flattering document. It is believed, which "
                                        + "flattering documents rarely are.",
                                Map.of("reputation", 12, "morality", 14, "stress", -8),
                                Map.of("scholars", 16, "commonPeople", 8),
                                List.of(),
                                List.of("wrote_an_honest_account")
                        ),
                        new Choice(
                                "Let your defenders tell the version that serves you",
                                "Your students construct a cleaner story than the one you lived. You "
                                        + "do not correct them. It hardens into record within a generation.",
                                Map.of("reputation", 14, "morality", -10),
                                Map.of("scholars", 10, "court", 6),
                                List.of(),
                                List.of("allowed_the_clean_version")
                        ),
                        new Choice(
                                "Refuse to discuss it at all",
                                "You will not perform contrition and you will not perform pride. The "
                                        + "silence is read, variously, as guilt, as dignity, and as both.",
                                Map.of("stress", -10, "reputation", -5),
                                Map.of("scholars", 4),
                                List.of(),
                                List.of("kept_silent_about_the_season")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("heresy_arc_started"),
                List.of()
        ));
    }
}
