package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs of the household: the marriage that is also a treaty, the inheritance
 * dispute that outlives everyone who started it, and the final question of
 * which child receives what you spent a life assembling.
 */
public final class FamilyArcs {

    private FamilyArcs() {
    }

    public static void addMarriageAllianceArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Contract Is Negotiated",
                "The marriage contract is being argued over by people who are not you, in "
                        + "a room you are not in. The dower, the conditions, and the clause about "
                        + "what happens if the alliance sours are all still open.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Insist on being present and negotiate the terms yourself",
                                "You walk into a negotiation that did not expect you and argue your "
                                        + "own contract. It is considered improper. It also produces terms "
                                        + "that protect you rather than the two households.",
                                Map.of("politicalPower", 12, "education", 8, "stress", 10, "familyLoyalty", -6),
                                Map.of("familyCouncil", -6, "nobles", 8),
                                List.of(),
                                List.of("marriage_arc_started", "negotiated_own_contract")
                        ),
                        new Choice(
                                "Let the families settle it and accept what they produce",
                                "You accept the terms unread, which is what is expected and which "
                                        + "means you will spend twenty years discovering what is in them.",
                                Map.of("familyLoyalty", 12, "stress", 6),
                                Map.of("familyCouncil", 14, "nobles", 6),
                                List.of(),
                                List.of("marriage_arc_started", "accepted_family_terms")
                        ),
                        new Choice(
                                "Insist on meeting your intended spouse before anything is signed",
                                "You cause a small scandal by treating the marriage as something that "
                                        + "will happen to two people rather than two houses. The meeting is "
                                        + "brief, awkward, and it changes everything that follows.",
                                Map.of("morality", 10, "familyLoyalty", -5, "stress", 8, "reputation", -5),
                                Map.of("familyCouncil", -8, "commonPeople", 8),
                                List.of(),
                                List.of("marriage_arc_started", "insisted_on_meeting")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Your Spouse's House Wants Something",
                "The alliance is producing exactly what alliances produce: a request. Your "
                        + "spouse's family wants your support in a dispute that is entirely theirs "
                        + "and entirely against your own interests.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Support them fully and absorb the cost",
                                "You back them completely. It costs you a great deal and it converts "
                                        + "a contract into something closer to an actual bond.",
                                Map.of("wealth", -16, "familyLoyalty", 16, "politicalPower", -6, "stress", 10),
                                Map.of("familyCouncil", 18, "nobles", 8),
                                List.of(),
                                List.of("backed_the_in_laws")
                        ),
                        new Choice(
                                "Refuse, and remind them what the contract actually says",
                                "You produce the document and read the relevant clause aloud. You are "
                                        + "entirely within your rights and the temperature in the house "
                                        + "drops for a decade.",
                                Map.of("politicalPower", 10, "familyLoyalty", -14, "wealth", 6, "stress", 12),
                                Map.of("familyCouncil", -14, "court", 5),
                                List.of(),
                                List.of("refused_the_in_laws")
                        ),
                        new Choice(
                                "Support them publicly and undermine them privately",
                                "You are seen to stand with them and you make certain the dispute goes "
                                        + "against them anyway. Both things are true and only one is known.",
                                Map.of("politicalPower", 14, "morality", -16, "stress", 16),
                                Map.of("shadowNetwork", 12, "familyCouncil", 6, "nobles", -5),
                                List.of(),
                                List.of("betrayed_the_in_laws")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("marriage_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Forty Years of It",
                "The alliance has outlasted the reason it was made. The households that "
                        + "arranged it are gone. What remains is two people who have been in the "
                        + "same rooms for four decades, and a question about what that amounted to.",
                "Legacy",
                List.of(
                        new Choice(
                                "Say plainly what the marriage actually meant to you",
                                "You say it late, badly, and truthfully. It is not a scene anyone "
                                        + "would write down. It is the most honest conversation of your "
                                        + "adult life.",
                                Map.of("familyLoyalty", 18, "morality", 12, "stress", -14),
                                Map.of("familyCouncil", 16),
                                List.of(),
                                List.of("spoke_honestly_at_the_end")
                        ),
                        new Choice(
                                "Secure your spouse's position and property beyond your death",
                                "You spend your last influence making certain they cannot be turned "
                                        + "out by your own heirs. It is not affection expressed in words, "
                                        + "and it is not less than affection.",
                                Map.of("wealth", -14, "familyLoyalty", 16, "morality", 14),
                                Map.of("familyCouncil", 14, "court", 6),
                                List.of(),
                                List.of("secured_the_spouse")
                        ),
                        new Choice(
                                "Leave it as it was: a contract, correctly performed",
                                "Neither of you pretends it was more than it was. There is a kind of "
                                        + "respect in refusing to invent a warmth that never existed.",
                                Map.of("morality", 5, "familyLoyalty", -6, "stress", -6),
                                Map.of("familyCouncil", 5),
                                List.of(),
                                List.of("left_it_a_contract")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("marriage_arc_started"),
                List.of()
        ));
    }

    public static void addFamilyFeudArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Estate Is Divided Badly",
                "Your grandfather's estate was divided by a document that three branches "
                        + "of the family read differently. The disagreement is nominally about a "
                        + "watercourse. It is actually about which branch is senior, and it has "
                        + "been running since before you were born.",
                "Youth",
                List.of(
                        new Choice(
                                "Study the original deed until you understand it completely",
                                "education",
                                30,
                                "You read the actual document, which it emerges nobody living has "
                                        + "done. The wording is unambiguous and favours a branch that is "
                                        + "not yours. You now hold a fact that could end the feud or "
                                        + "detonate it.",
                                "The deed is water-damaged in precisely the section that matters, and "
                                        + "your careful reading produces nothing but eye strain and a "
                                        + "reputation for meddling.",
                                Map.of("education", 14, "politicalPower", 10, "stress", 8),
                                Map.of("education", 6, "stress", 10, "familyLoyalty", -5),
                                Map.of("familyCouncil", 8, "scholars", 8),
                                Map.of("familyCouncil", -6),
                                List.of(),
                                List.of("feud_arc_started", "read_the_deed"),
                                List.of("feud_arc_started", "deed_unreadable")
                        ),
                        new Choice(
                                "Take your branch's side loudly and early",
                                "You commit publicly and completely. Your branch is delighted. The "
                                        + "other two now regard you as a combatant rather than a relative.",
                                Map.of("politicalPower", 10, "familyLoyalty", 12, "stress", 12),
                                Map.of("familyCouncil", -10, "nobles", 5),
                                List.of(),
                                List.of("feud_arc_started", "took_a_side_early")
                        ),
                        new Choice(
                                "Refuse to participate in a quarrel older than you are",
                                "You decline to inherit the argument. Every branch finds this "
                                        + "irritating, which is a reasonable indication that it was the "
                                        + "correct decision.",
                                Map.of("stress", -8, "morality", 10, "familyLoyalty", -8),
                                Map.of("familyCouncil", -5),
                                List.of(),
                                List.of("feud_arc_started", "refused_the_feud")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Blood in the Watercourse",
                "A cousin from the other branch has been found dead beside the disputed "
                        + "channel. It may have been an accident. Nobody in the family believes "
                        + "it was an accident, and the funeral is going to be a council of war.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Insist on an outside investigation before anyone acts",
                                "politicalPower",
                                45,
                                "You get a magistrate involved before the family can settle it "
                                        + "privately. The death is ruled an accident, on evidence, and the "
                                        + "verdict holds because it came from outside.",
                                "The family closes ranks against outside interference and the "
                                        + "magistrate withdraws. Your attempt is remembered as a betrayal "
                                        + "by both branches simultaneously.",
                                Map.of("reputation", 14, "morality", 16, "stress", 14),
                                Map.of("familyLoyalty", -14, "stress", 20, "reputation", -8),
                                Map.of("court", 12, "scholars", 10, "familyCouncil", 6),
                                Map.of("familyCouncil", -18),
                                List.of(),
                                List.of("brought_in_the_magistrate"),
                                List.of("family_closed_ranks")
                        ),
                        new Choice(
                                "Answer it the way the family expects",
                                "Someone from the other branch does not survive the season. The "
                                        + "watercourse is unchanged. The number of funerals is not.",
                                Map.of("politicalPower", 12, "morality", -25, "stress", 20, "health", -8),
                                Map.of("familyCouncil", 8, "shadowNetwork", 14, "court", -14),
                                List.of(),
                                List.of("escalated_the_feud")
                        ),
                        new Choice(
                                "Go to the other branch alone and offer terms",
                                "reputation",
                                50,
                                "You walk into their house without escort, which is either brave or "
                                        + "insane, and you come out with a settlement. The watercourse is "
                                        + "shared. Nobody else dies.",
                                "You walk into their house without escort and are thrown out of it. "
                                        + "The gesture is read as weakness and the feud accelerates.",
                                Map.of("reputation", 20, "morality", 18, "familyLoyalty", 8),
                                Map.of("reputation", -10, "stress", 18, "health", -8),
                                Map.of("familyCouncil", 20, "commonPeople", 10),
                                Map.of("familyCouncil", -10),
                                List.of(),
                                List.of("ended_the_feud"),
                                List.of("peace_attempt_failed")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("feud_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Quarrel You Hand Down",
                "You are the oldest living person who remembers why any of it started. "
                        + "When you die, the feud becomes something inherited by people who only "
                        + "know that they are supposed to hate that house.",
                "Legacy",
                List.of(
                        new Choice(
                                "Bring both branches together and settle it in writing",
                                "You spend your last political capital on a document every branch "
                                        + "signs. It is dull, precise, and it ends a thing that killed four "
                                        + "people across sixty years.",
                                Map.of("politicalPower", -12, "morality", 20, "reputation", 16, "stress", -12),
                                Map.of("familyCouncil", 22, "court", 8, "scholars", 8),
                                List.of(),
                                List.of("settled_the_feud_permanently")
                        ),
                        new Choice(
                                "Tell your heirs the whole history and let them decide",
                                "You give them the facts without the instruction. Some will keep the "
                                        + "quarrel. At least they will keep it knowingly.",
                                Map.of("morality", 10, "familyLoyalty", 8, "education", 6),
                                Map.of("familyCouncil", 10),
                                List.of(),
                                List.of("handed_down_the_history")
                        ),
                        new Choice(
                                "Bind your heirs to continue it",
                                "You extract a promise on your deathbed. It will be kept for at least "
                                        + "two generations by people who never met the man it was about.",
                                Map.of("politicalPower", 8, "morality", -20, "familyLoyalty", 10),
                                Map.of("familyCouncil", 8, "commonPeople", -10),
                                List.of(),
                                List.of("bound_them_to_the_feud")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("feud_arc_started"),
                List.of()
        ));
    }

    public static void addLegacyHeirArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "Which of Them Receives It",
                "You have more than one heir and one estate. The obvious choice is the "
                        + "eldest. The capable choice is not the eldest. The choice that keeps the "
                        + "household from tearing itself apart may be neither.",
                "Legacy",
                List.of(
                        new Choice(
                                "Choose by capability, and say so openly",
                                "You name the one who can actually do it and you explain your "
                                        + "reasoning to all of them at once. The estate survives. Two "
                                        + "relationships do not.",
                                Map.of("politicalPower", 14, "familyLoyalty", -12, "education", 6),
                                Map.of("familyCouncil", -8, "merchants", 10, "court", 8),
                                List.of(),
                                List.of("heir_arc_started", "chose_by_merit")
                        ),
                        new Choice(
                                "Follow custom exactly and name the eldest",
                                "You do the expected thing. Nobody can accuse you of favouritism and "
                                        + "everybody can see that the estate is now in the hands of a person "
                                        + "unsuited to holding it.",
                                Map.of("familyLoyalty", 12, "politicalPower", -8),
                                Map.of("familyCouncil", 14, "nobles", 8),
                                List.of(),
                                List.of("heir_arc_started", "followed_custom")
                        ),
                        new Choice(
                                "Divide it and make them govern it jointly",
                                "You force them to work together by making it impossible to do "
                                        + "otherwise. It is either the wisest thing you ever did or a "
                                        + "guaranteed feud, and you will not live to find out which.",
                                Map.of("politicalPower", -6, "familyLoyalty", 8, "morality", 8),
                                Map.of("familyCouncil", 8),
                                List.of(),
                                List.of("heir_arc_started", "divided_jointly")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Last Instruction",
                "There is time for one more thing: a final instruction to the person who "
                        + "will hold everything after you. Whatever you say now is what they will "
                        + "quote for the rest of their life.",
                "Legacy",
                List.of(
                        new Choice(
                                "Tell them to protect the people who depend on the house",
                                "You tell them that an estate is a set of obligations wearing the "
                                        + "costume of property. They will remember it, and roughly half of "
                                        + "them will act on it.",
                                Map.of("morality", 18, "reputation", 12, "familyLoyalty", 10),
                                Map.of("commonPeople", 18, "familyCouncil", 12, "scholars", 8),
                                List.of(),
                                List.of("last_instruction_duty")
                        ),
                        new Choice(
                                "Tell them never to trust the court",
                                "You give them the lesson your own life actually taught: that power "
                                        + "smiles, files, and waits. It is bitter and it will keep them "
                                        + "alive.",
                                Map.of("politicalPower", 12, "morality", -6, "stress", 6),
                                Map.of("shadowNetwork", 10, "familyCouncil", 8, "court", -8),
                                List.of(),
                                List.of("last_instruction_distrust")
                        ),
                        new Choice(
                                "Tell them to spend it, and not to build a monument to you",
                                "You instruct them to use the money on the living rather than on your "
                                        + "tomb. It is the least self-interested thing you have ever said "
                                        + "and it startles everyone in the room.",
                                Map.of("morality", 20, "wealth", -10, "stress", -12),
                                Map.of("commonPeople", 16, "scholars", 10, "familyCouncil", -5),
                                List.of(),
                                List.of("last_instruction_spend_it")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("heir_arc_started"),
                List.of()
        ));
    }
}
