package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs of the palace and the dark beneath it: informing, debt to people who
 * do not take repayment in coin, and the seasons when one dynasty gives way
 * to another and everyone in the capital must choose early and correctly.
 */
public final class CourtArcs {

    private CourtArcs() {
    }

    public static void addPalaceSpyArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Quiet Offer",
                "A man who does not give his office takes you aside and observes that you "
                        + "hear a great deal in the course of your duties. He would like you to "
                        + "keep hearing it, and to tell him. He mentions a sum. He does not "
                        + "mention what happens if you decline.",
                "Youth",
                List.of(
                        new Choice(
                                "Accept, and report faithfully",
                                "You take the work. It is easier than you expected, which is the "
                                        + "genuinely frightening part.",
                                Map.of("wealth", 12, "politicalPower", 10, "morality", -10, "stress", 10),
                                Map.of("court", 12, "shadowNetwork", 10),
                                List.of(),
                                List.of("spy_arc_started", "became_an_informant")
                        ),
                        new Choice(
                                "Decline, politely and without pretending you misunderstood",
                                "You say no clearly enough that there is no ambiguity, and respectfully "
                                        + "enough that there is no insult. He nods and leaves. Your name "
                                        + "goes on a different list.",
                                Map.of("morality", 12, "stress", 12, "politicalPower", -5),
                                Map.of("court", -8, "scholars", 5),
                                List.of(),
                                List.of("spy_arc_started", "refused_to_inform")
                        ),
                        new Choice(
                                "Accept, and immediately tell the man you are meant to watch",
                                "You take the coin and warn the target the same evening. You are now "
                                        + "running a double game in your first month at court, which is "
                                        + "either very clever or the shortest road to a canal.",
                                Map.of("politicalPower", 14, "morality", -5, "stress", 18),
                                Map.of("shadowNetwork", 14, "court", 6),
                                List.of(),
                                List.of("spy_arc_started", "became_double_agent")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Report They Want",
                "Your handler wants something specific this time: evidence against a "
                        + "particular official. You have watched the man for two months. He is "
                        + "dull, competent, and — as far as you can tell — entirely innocent.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Report honestly that there is nothing to find",
                                "You write that the man is clean. Your handler is visibly "
                                        + "disappointed, which tells you that the answer was never the point "
                                        + "and the file was always going to be filled.",
                                Map.of("morality", 12, "politicalPower", -6, "stress", 10),
                                Map.of("court", -8, "shadowNetwork", -5),
                                List.of(),
                                List.of("reported_honestly")
                        ),
                        new Choice(
                                "Manufacture enough to satisfy the request",
                                "You assemble a document from true fragments arranged into a false "
                                        + "shape. The official is removed within the month. You are trusted "
                                        + "with the next one.",
                                Map.of("politicalPower", 16, "wealth", 10, "morality", -20, "stress", 14),
                                Map.of("court", 14, "shadowNetwork", 12),
                                List.of(),
                                List.of("framed_an_innocent_man", "framed_innocent")
                        ),
                        new Choice(
                                "Warn the official and help him disappear",
                                "politicalPower",
                                45,
                                "He is gone before the file is complete, with his family and enough "
                                        + "money to start again. Your handler concludes the man was warned "
                                        + "by someone. He does not conclude it was you.",
                                "He is caught at the gate with your warning still in his hand. There "
                                        + "is now a document in the world with your handwriting on it, in a "
                                        + "file with your name at the top.",
                                Map.of("morality", 16, "politicalPower", 6, "stress", 16),
                                Map.of("stress", 26, "politicalPower", -16, "reputation", -12),
                                Map.of("shadowNetwork", 10, "commonPeople", 8),
                                Map.of("court", -22, "shadowNetwork", -10),
                                List.of(),
                                List.of("saved_the_target"),
                                List.of("caught_warning_the_target", "court_suspicion")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("spy_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Net Draws In",
                "There is an investigation into the informant network itself. Someone above "
                        + "your handler has decided the apparatus has grown too large and knows "
                        + "too much. Everyone in it is now a liability to everyone else in it.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Give up your handler before he gives up you",
                                "You reach the investigators first with everything you have. He is "
                                        + "arrested within days. You survive, having proven you will do "
                                        + "precisely this, which is now known about you.",
                                Map.of("politicalPower", 12, "morality", -16, "stress", 16),
                                Map.of("court", 14, "shadowNetwork", -20),
                                List.of(),
                                List.of("burned_the_handler")
                        ),
                        new Choice(
                                "Destroy every record of your involvement and vanish from the network",
                                "politicalPower",
                                55,
                                "You erase yourself thoroughly enough that when the investigation "
                                        + "closes, you are simply not in it. It is the cleanest thing you "
                                        + "have ever done.",
                                "You miss one ledger. It is, inevitably, the one they read first.",
                                Map.of("stress", 12, "politicalPower", 8),
                                Map.of("stress", 26, "reputation", -18, "politicalPower", -16),
                                Map.of("shadowNetwork", 8),
                                Map.of("court", -20, "shadowNetwork", -12),
                                List.of(),
                                List.of("erased_yourself_cleanly"),
                                List.of("exposed_as_informant", "court_suspicion")
                        ),
                        new Choice(
                                "Hold your position and refuse to name anyone",
                                "You say nothing about anyone, including your handler, including the "
                                        + "men below you. It costs you badly. It also means that of "
                                        + "everyone in that network, you are the only one still owed favours.",
                                Map.of("stress", 20, "health", -10, "morality", 14, "reputation", 8),
                                Map.of("shadowNetwork", 20, "court", -10),
                                List.of(),
                                List.of("held_the_line_in_the_net")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("spy_arc_started"),
                List.of()
        ));
    }

    public static void addShadowDebtArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "A Small Favour",
                "Someone solves a problem for you that you did not ask them to solve. A "
                        + "creditor stops calling. A rival withdraws a complaint. Nothing is "
                        + "demanded in return, and the absence of a demand is itself the "
                        + "transaction.",
                "Youth",
                List.of(
                        new Choice(
                                "Accept the favour and say thank you",
                                "You take it. It costs nothing today, and the ledger it opened does "
                                        + "not have a visible balance.",
                                Map.of("wealth", 8, "stress", -6, "morality", -6),
                                Map.of("shadowNetwork", 14),
                                List.of(),
                                List.of("shadow_debt_started", "accepted_the_favour")
                        ),
                        new Choice(
                                "Insist on paying for it in coin, immediately",
                                "You force money on them. They take it, amused, and both of you know "
                                        + "the payment does not actually close anything.",
                                Map.of("wealth", -10, "morality", 8, "stress", 6),
                                Map.of("shadowNetwork", 6),
                                List.of(),
                                List.of("shadow_debt_started", "tried_to_pay_cash")
                        ),
                        new Choice(
                                "Undo the favour publicly and refuse the arrangement",
                                "You reinstate the complaint, pay the creditor yourself, and make it "
                                        + "known you did. It is expensive, awkward, and it keeps the ledger "
                                        + "closed.",
                                Map.of("wealth", -16, "morality", 16, "reputation", 8, "stress", 10),
                                Map.of("shadowNetwork", -14, "commonPeople", 8),
                                List.of(),
                                List.of("shadow_debt_started", "refused_the_favour")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Interest Compounds",
                "They have done three more favours since, none requested. Now there is a "
                        + "request: a document moved from one office to another, nothing violent, "
                        + "nothing anyone would notice. It is the smallest possible thing, which "
                        + "is why it is the one they are asking for.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Move the document",
                                "It takes four minutes. Nothing happens. Nothing happens for a very "
                                        + "long time, and then a man you never met is executed on the "
                                        + "strength of a file that was in the wrong place.",
                                Map.of("politicalPower", 10, "morality", -14, "stress", 12),
                                Map.of("shadowNetwork", 16, "court", -5),
                                List.of(),
                                List.of("moved_the_document", "owed_shadow_debt")
                        ),
                        new Choice(
                                "Refuse, and offer them anything else instead",
                                "You say no to this and yes to almost anything else. They accept the "
                                        + "substitution with a patience that makes it clear the ledger is "
                                        + "long and they are in no hurry.",
                                Map.of("wealth", -14, "morality", 10, "stress", 14),
                                Map.of("shadowNetwork", 8),
                                List.of(),
                                List.of("deflected_the_ask")
                        ),
                        new Choice(
                                "Take the request to the authorities",
                                "politicalPower",
                                50,
                                "You hand the whole arrangement over. Three arrests follow. The "
                                        + "network is damaged badly enough that its grip on you is broken.",
                                "Your report reaches an official who is himself on their ledger. The "
                                        + "network knows within a day that you tried, and they now regard "
                                        + "you as a problem rather than an asset.",
                                Map.of("morality", 18, "reputation", 12, "stress", 14),
                                Map.of("stress", 25, "health", -12, "politicalPower", -12),
                                Map.of("court", 14, "shadowNetwork", -22),
                                Map.of("shadowNetwork", 20, "court", -10),
                                List.of(),
                                List.of("broke_the_network"),
                                List.of("shadow_network_enemy")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("shadow_debt_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "What They Ask For Last",
                "The final request is not small. It is not a document. They explain it "
                        + "calmly, with the air of men presenting an invoice, and they are correct "
                        + "that the balance supports it.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Do it",
                                "You do it. The ledger closes. You will spend the rest of your life "
                                        + "discovering how many rooms in your own head you can no longer "
                                        + "enter.",
                                Map.of("politicalPower", 18, "morality", -25, "stress", 22),
                                Map.of("shadowNetwork", 22, "court", 5),
                                List.of(),
                                List.of("paid_the_final_price", "served_shadow_network")
                        ),
                        new Choice(
                                "Refuse and pay whatever they take instead",
                                "You say no. They take your warehouse, your reputation in three "
                                        + "districts, and a great deal of your health. They do not take the "
                                        + "thing they asked for.",
                                Map.of("wealth", -25, "health", -14, "morality", 20, "stress", 20),
                                Map.of("shadowNetwork", -10, "merchants", -12, "commonPeople", 10),
                                List.of(),
                                List.of("refused_the_final_price")
                        ),
                        new Choice(
                                "Turn the network's own creditors against it",
                                "politicalPower",
                                60,
                                "You spend a year quietly finding everyone else on their ledger, and "
                                        + "then introduce them to each other. The network comes apart from "
                                        + "the inside, owed by too many people at once.",
                                "You are identified halfway through. What follows is not survivable "
                                        + "quickly, and the network makes an example that is discussed for "
                                        + "years.",
                                Map.of("politicalPower", 20, "reputation", 16, "stress", 18),
                                Map.of("health", -25, "stress", 28, "politicalPower", -18),
                                Map.of("shadowNetwork", -25, "merchants", 12, "commonPeople", 12),
                                Map.of("shadowNetwork", 18),
                                List.of(),
                                List.of("dismantled_the_network"),
                                List.of("made_an_example_of")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("shadow_debt_started"),
                List.of()
        ));
    }

    public static void addDynastyTransitionArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Old Order Thins",
                "The dynasty is not falling yet. It is doing something slower and more "
                        + "obvious: appointing weaker men, paying later, and answering petitions "
                        + "from provinces it no longer controls. Everyone can see it. Nobody has "
                        + "moved.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Bind yourself tighter to the ruling house",
                                "You make your loyalty conspicuous while it is still cheap to buy. If "
                                        + "they survive, you are indispensable. If they do not, you are "
                                        + "identified.",
                                Map.of("politicalPower", 14, "wealth", 10, "stress", 10),
                                Map.of("court", 18, "nobles", 8, "commonPeople", -6),
                                List.of(),
                                List.of("dynasty_arc_started", "bound_to_the_old_house", "declared_loyalty")
                        ),
                        new Choice(
                                "Open quiet correspondence with the rising faction",
                                "You write carefully to people who may matter in ten years, saying "
                                        + "nothing that could be read as treason and everything that could "
                                        + "be read as availability.",
                                Map.of("politicalPower", 12, "morality", -8, "stress", 14),
                                Map.of("shadowNetwork", 12, "court", -8, "nobles", 6),
                                List.of(),
                                List.of("dynasty_arc_started", "hedged_toward_the_rising")
                        ),
                        new Choice(
                                "Move your family and wealth out of the capital",
                                "You quietly relocate everything that matters to a province nobody "
                                        + "will fight over. It looks like cowardice, right up until it looks "
                                        + "like foresight.",
                                Map.of("wealth", -12, "familyLoyalty", 14, "stress", -8),
                                Map.of("familyCouncil", 14, "court", -6),
                                List.of(),
                                List.of("dynasty_arc_started", "moved_the_family_out")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Turning",
                "It happened in nine days. The old house is finished, its treasury seized, "
                        + "its officials being sorted into those who will serve and those who will "
                        + "be examples. Someone is going through the correspondence.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Present yourself to the new order and offer your competence",
                                "politicalPower",
                                50,
                                "They need administrators far more than they need revenge. You are "
                                        + "confirmed in office before the month is out, and the men who "
                                        + "replaced your patrons find you extremely useful.",
                                "Your record is read less charitably than you hoped. You are not "
                                        + "executed. You are simply never employed again, which for a man "
                                        + "of your training is a slower version of the same thing.",
                                Map.of("politicalPower", 16, "wealth", 10, "stress", 10),
                                Map.of("politicalPower", -20, "wealth", -18, "stress", 22),
                                Map.of("court", 18, "nobles", 6),
                                Map.of("court", -20, "nobles", -10),
                                List.of(),
                                List.of("served_the_new_order"),
                                List.of("purged_from_office")
                        ),
                        new Choice(
                                "Shelter the old dynasty's people at your own risk",
                                "You hide three families who would otherwise be examples. It is the "
                                        + "most dangerous thing you will ever do and the one your "
                                        + "descendants will repeat most often.",
                                Map.of("morality", 20, "reputation", 12, "stress", 22, "wealth", -14),
                                Map.of("nobles", 16, "commonPeople", 12, "court", -18),
                                List.of(),
                                List.of("sheltered_the_fallen")
                        ),
                        new Choice(
                                "Denounce your former patrons to establish your position",
                                "You go first and loudest. It works. Every person in the room "
                                        + "understands precisely what you are, and files it for the next "
                                        + "time the order changes.",
                                Map.of("politicalPower", 18, "morality", -22, "stress", 16),
                                Map.of("court", 20, "nobles", -18, "commonPeople", -10),
                                List.of(),
                                List.of("denounced_the_old_house")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("dynasty_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Who Survived, and How",
                "A generation on, the transition is history rather than danger, and "
                        + "history is being written by people who need to know who behaved well. "
                        + "Your conduct is a matter of record, and of interpretation.",
                "Legacy",
                List.of(
                        new Choice(
                                "Tell your heirs exactly what you did and why",
                                "You give them the whole thing, including the parts that do not "
                                        + "reflect well. They will make their own judgements, which is the "
                                        + "point.",
                                Map.of("morality", 14, "familyLoyalty", 12, "reputation", 6),
                                Map.of("familyCouncil", 14, "scholars", 8),
                                List.of(),
                                List.of("told_them_the_truth_of_it")
                        ),
                        new Choice(
                                "Teach them that survival is the only loyalty that matters",
                                "You tell them the lesson you actually learned: houses fall, and the "
                                        + "families that endure are the ones that never mistook a dynasty "
                                        + "for a permanent thing.",
                                Map.of("politicalPower", 12, "morality", -10, "familyLoyalty", 8),
                                Map.of("familyCouncil", 10, "court", 6, "nobles", 6),
                                List.of(),
                                List.of("taught_survival_above_loyalty")
                        ),
                        new Choice(
                                "Endow a chronicle that records the fall honestly",
                                "You pay scholars to write down what happened, including what you "
                                        + "did, and you do not ask to review it first.",
                                Map.of("wealth", -18, "reputation", 16, "morality", 16),
                                Map.of("scholars", 20, "commonPeople", 8, "court", -6),
                                List.of(
                                        ChoiceRequirement.statAtLeast("wealth", 18,
                                                "Locked: Requires Wealth 18.")
                                ),
                                List.of("endowed_an_honest_chronicle")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("dynasty_arc_started"),
                List.of()
        ));
    }
}
