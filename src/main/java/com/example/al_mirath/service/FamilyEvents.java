package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Events generated from whoever is currently in the household.
 *
 * <p>Built fresh on every lookup, the same way the recurring cast's events
 * are, so the text names the people who are actually alive right now and the
 * ages they have actually reached. A household with no children never sees a
 * question about an heir.
 */
public final class FamilyEvents {

    /** Old enough for their decline to be the player's problem. */
    private static final int AGEING_PARENT = 58;

    /** Old enough to be steered, too young to have chosen. */
    private static final int STEERABLE_FROM = 13;

    private FamilyEvents() {
    }

    /**
     * What the household has heard said about the player, prepended to the
     * scene. They live with the name as much as the player does.
     */
    private static String asHeardAtHome(RenownRegistry renown) {
        if (renown == null) {
            return "";
        }

        String line = renown.greetingFrom("familyCouncil");

        return line == null ? "" : line + "\n\n";
    }

    public static List<GameEvent> create(FamilyRegistry family) {
        return create(family, null);
    }

    /**
     * @param renown what the player is known for. The household hears the
     *               same stories as everyone else, and has its own opinion
     *               about them.
     */
    public static List<GameEvent> create(
            FamilyRegistry family,
            RenownRegistry renown
    ) {
        List<GameEvent> events = new ArrayList<>();

        if (family == null) {
            return events;
        }

        for (FamilyMember member : family.all()) {
            if (!member.isAlive()) {
                continue;
            }

            if (member.getKinship().isElder()
                    && member.getAge() >= AGEING_PARENT) {

                events.add(ageingParent(member, renown));
            }

            if (member.getKinship() == Kinship.SIBLING) {
                events.add(siblingAtTheDoor(member, renown));
            }

            if (member.getKinship() == Kinship.SPOUSE) {
                events.add(spouseCounsel(member, renown));
            }

            if (member.getKinship() == Kinship.CHILD
                    && member.getLifePath() == LifePath.UNDECIDED
                    && member.getAge() >= STEERABLE_FROM) {

                events.add(steerTheChild(member, renown));
            }

            if (member.getKinship() == Kinship.CHILD
                    && member.getLifePath() == LifePath.CRIMINAL) {

                events.add(theChildWhoWentWrong(member, renown));
            }

            if (member.getKinship() == Kinship.CHILD
                    && member.getLifePath() == LifePath.RULER) {

                events.add(theChildWhoRose(member, renown));
            }
        }

        return events;
    }

    // ---- the generation above -------------------------------------------

    private static GameEvent ageingParent(FamilyMember parent, RenownRegistry renown) {
        String name = parent.getName();
        String relation = parent.getKinship().displayName().toLowerCase();

        return new GameEvent(
                name + " Is Failing",

                asHeardAtHome(renown)
                        + name
                        + ", your "
                        + relation
                        + ", is "
                        + parent.getAge()
                        + " and can no longer manage a household alone. They were "
                        + parent.getTrait()
                        + ", and are still, which makes the asking harder for both "
                        + "of you. Nobody says the word care out loud.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Take " + name + " into your own house",

                                "You make room, and the room costs you. The work of it "
                                        + "falls on your household daily, and your "
                                        + relation
                                        + " knows exactly what it costs, which is its own "
                                        + "kind of weight between you.",

                                Map.of(
                                        "wealth", -12,
                                        "stress", 8,
                                        "familyLoyalty", 15,
                                        "morality", 6
                                ),

                                Map.of("familyCouncil", 12),

                                List.of(),

                                List.of("family_honoured_parents")
                        ),

                        new Choice(
                                "Pay for someone else to do it",

                                "wealth",

                                45,

                                "The arrangement is good, the people are kind, and you "
                                        + "visit when you can. It is the correct solution "
                                        + "and everyone treats it as one.",

                                "You cannot cover it. What you send is not enough, and "
                                        + name
                                        + " is cared for badly by people who are paid too "
                                        + "little to care.",

                                Map.of("wealth", -18, "familyLoyalty", 5),

                                Map.of("wealth", -8, "familyLoyalty", -12, "stress", 8),

                                Map.of("familyCouncil", 5),

                                Map.of("familyCouncil", -10),

                                List.of(),

                                List.of("family_provided_for"),

                                List.of("family_chose_ambition")
                        ),

                        new Choice(
                                "You have your own life to build",

                                "You do not go. There is always a reason not to go, and "
                                        + "each one is true. "
                                        + name
                                        + " stops expecting you, which is easier for a "
                                        + "while and then is not.",

                                Map.of(
                                        "familyLoyalty", -18,
                                        "morality", -8,
                                        "stress", -4
                                ),

                                Map.of("familyCouncil", -15),

                                List.of(),

                                List.of("family_abandoned")
                        )
                )
        );
    }

    private static GameEvent siblingAtTheDoor(FamilyMember sibling, RenownRegistry renown) {
        String name = sibling.getName();

        return new GameEvent(
                name + " Asks for Help",

                asHeardAtHome(renown)
                        + name
                        + ", your sibling, is "
                        + sibling.getTrait()
                        + " and has run out of people to ask. The debt is real, the "
                        + "fault is partly theirs, and you are the one with something "
                        + "to give. They have not asked before, which is what makes it "
                        + "cost them to ask now.",

                "Youth",

                List.of(
                        new Choice(
                                "Cover it, and say nothing about whose fault it was",

                                "You pay, and you let them keep their dignity, which is "
                                        + "the more expensive half. "
                                        + name
                                        + " does not forget either part.",

                                Map.of(
                                        "wealth", -14,
                                        "familyLoyalty", 12,
                                        "morality", 8
                                ),

                                Map.of("familyCouncil", 10),

                                List.of(),

                                List.of("family_sibling_helped")
                        ),

                        new Choice(
                                "Cover it, and make sure they know what it cost",

                                "The money changes hands along with a full account of "
                                        + "what it means. The debt is settled. Something "
                                        + "else now sits in its place.",

                                Map.of(
                                        "wealth", -14,
                                        "familyLoyalty", 2,
                                        "reputation", 3
                                ),

                                Map.of("familyCouncil", 3),

                                List.of(),

                                List.of("family_provided_for")
                        ),

                        new Choice(
                                "Refuse",

                                "You say no, and the reasons are good ones. "
                                        + name
                                        + " accepts it without argument, which is worse "
                                        + "than an argument would have been.",

                                Map.of(
                                        "familyLoyalty", -15,
                                        "stress", 5,
                                        "wealth", 3
                                ),

                                Map.of("familyCouncil", -12),

                                List.of(),

                                List.of("family_sibling_refused")
                        )
                )
        );
    }

    // ---- the household you built ----------------------------------------

    private static GameEvent spouseCounsel(FamilyMember spouse, RenownRegistry renown) {
        String name = spouse.getName();

        return new GameEvent(
                "What " + name + " Sees",

                asHeardAtHome(renown)
                        + name
                        + ", who is "
                        + spouse.getTrait()
                        + ", has been watching the thing you are about to do more "
                        + "carefully than you have. The warning is not delivered as a "
                        + "warning. It is delivered as a question you would rather not "
                        + "have been asked.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Listen, and change course",

                                "You take the advice, and it is good advice. You also "
                                        + "establish, in front of your own household, that "
                                        + name
                                        + " can stop you — which is either a strength or "
                                        + "a debt, depending on the year.",

                                Map.of(
                                        "familyLoyalty", 14,
                                        "stress", -8,
                                        "politicalPower", -4
                                ),

                                Map.of("familyCouncil", 12),

                                List.of(),

                                List.of("family_spouse_supported")
                        ),

                        new Choice(
                                "Hear it out, then do it anyway",

                                "You let the question sit, answer it honestly, and "
                                        + "proceed. "
                                        + name
                                        + " does not raise it again. You notice, later, "
                                        + "that they have also stopped raising other things.",

                                Map.of(
                                        "politicalPower", 6,
                                        "familyLoyalty", -8,
                                        "stress", 5
                                ),

                                Map.of("familyCouncil", -6),

                                List.of(),

                                List.of("family_spouse_neglected")
                        ),

                        new Choice(
                                "Tell them the household is not their department",

                                "It ends the conversation. It ends several later ones "
                                        + "too, before they start.",

                                Map.of(
                                        "familyLoyalty", -18,
                                        "morality", -6,
                                        "politicalPower", 4
                                ),

                                Map.of("familyCouncil", -14),

                                List.of(),

                                List.of("family_spouse_neglected", "family_chose_ambition")
                        )
                )
        );
    }

    /**
     * The suggestion's whole point, in one event: a decision made decades
     * before a child is grown deciding what they grow into.
     */
    private static GameEvent steerTheChild(FamilyMember child, RenownRegistry renown) {
        String name = child.getName();

        return new GameEvent(
                "What " + name + " Will Become",

                asHeardAtHome(renown)
                        + name
                        + " is "
                        + child.getAge()
                        + ", "
                        + child.getTrait()
                        + ", and old enough that the question can no longer be put "
                        + "off. Whatever you arrange now — a teacher, a trade, a "
                        + "position — is the door they will spend the rest of their "
                        + "life walking through or away from.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Buy " + name + " a scholar's education",

                                "wealth",

                                40,

                                "The fees are found and the place is secured. "
                                        + name
                                        + " turns out to have the patience for it, which "
                                        + "you had no way of knowing in advance.",

                                "You cannot raise the fees in time. The place goes to "
                                        + "someone whose father could, and "
                                        + name
                                        + " learns early what that difference means.",

                                Map.of("wealth", -16, "education", 5, "familyLoyalty", 8),

                                Map.of("wealth", -6, "familyLoyalty", -8, "stress", 6),

                                Map.of("scholars", 10),

                                Map.of("scholars", -4),

                                List.of(),

                                List.of("family_child_to_scholar"),

                                List.of("family_chose_ambition")
                        ),

                        new Choice(
                                "Put " + name + " into the family trade",

                                "It is the safe answer and you both know it. "
                                        + name
                                        + " learns the ledgers, learns the routes, and "
                                        + "will be comfortable in a way you were not.",

                                Map.of(
                                        "wealth", 8,
                                        "familyLoyalty", 10,
                                        "reputation", 3
                                ),

                                Map.of("merchants", 10, "familyCouncil", 6),

                                List.of(),

                                List.of("family_child_to_merchant")
                        ),

                        new Choice(
                                "Place " + name + " where the power is",

                                "politicalPower",

                                50,

                                "You spend what standing you have to put your child in a "
                                        + "room they have not earned yet. It works. "
                                        + name
                                        + " will spend a career being useful to people "
                                        + "who remember that.",

                                "The favour is not granted, and asking for it publicly "
                                        + "cost you more than the refusal did.",

                                Map.of("politicalPower", -6, "reputation", 6, "familyLoyalty", 6),

                                Map.of("politicalPower", -10, "reputation", -6, "stress", 8),

                                Map.of("court", 10, "nobles", 5),

                                Map.of("court", -8),

                                List.of(),

                                List.of("family_child_to_courtier"),

                                List.of("family_chose_ambition")
                        )
                )
        );
    }

    private static GameEvent theChildWhoWentWrong(FamilyMember child, RenownRegistry renown) {
        String name = child.getName();

        return new GameEvent(
                name + " Is Not What You Raised",

                asHeardAtHome(renown)
                        + "The reports about "
                        + name
                        + " have stopped being rumours. They keep company you have "
                        + "decided not to ask about, and now a magistrate has asked "
                        + "about it instead. Your name is attached to theirs on a "
                        + "document you have not read.",

                "Political Crisis",

                List.of(
                        new Choice(
                                "Stand between " + name + " and the magistrate",

                                "politicalPower",

                                55,

                                "You spend your standing on it and the matter closes. "
                                        + name
                                        + " is grateful in the way people are grateful when "
                                        + "they intend to need it again.",

                                "You cannot cover this. What you spend is wasted, and now "
                                        + "the court has watched you spend it on an outlaw.",

                                Map.of("politicalPower", -10, "familyLoyalty", 15, "morality", -6),

                                Map.of("politicalPower", -16, "reputation", -12, "stress", 14),

                                Map.of("familyCouncil", 12, "court", -6),

                                Map.of("court", -14, "nobles", -8),

                                List.of(),

                                List.of("family_stood_by_them"),

                                List.of("family_chose_ambition")
                        ),

                        new Choice(
                                "Disown " + name + " publicly, and quickly",

                                "You cut them off in front of witnesses, because the "
                                        + "witnesses are the point. The household survives "
                                        + "it. Your other children watch you do it and file "
                                        + "it away.",

                                Map.of(
                                        "reputation", 8,
                                        "familyLoyalty", -25,
                                        "morality", -10,
                                        "stress", 10
                                ),

                                Map.of("court", 8, "familyCouncil", -18),

                                List.of(),

                                List.of("family_disowned_child")
                        ),

                        new Choice(
                                "Buy " + name + " passage somewhere far away",

                                "You pay for distance. It is not forgiveness and it is "
                                        + "not justice, but it is the only thing on offer "
                                        + "that keeps them alive and keeps the household "
                                        + "standing.",

                                Map.of(
                                        "wealth", -20,
                                        "familyLoyalty", 5,
                                        "stress", -4
                                ),

                                Map.of("shadowNetwork", 8, "familyCouncil", 4),

                                List.of(),

                                List.of("family_provided_for")
                        )
                )
        );
    }

    private static GameEvent theChildWhoRose(FamilyMember child, RenownRegistry renown) {
        String name = child.getName();

        return new GameEvent(
                name + " Governs Now",

                asHeardAtHome(renown)
                        + name
                        + " holds an office you never came close to, and holds it "
                        + "because of arrangements you made before they could walk. "
                        + "People who would not receive you now write to you carefully. "
                        + name
                        + " has asked what you want, and means it, and that is a more "
                        + "dangerous question than it sounds.",

                "Legacy",

                List.of(
                        new Choice(
                                "Ask for nothing, and let them govern",

                                "You take no favour and give no instruction. It is the "
                                        + "hardest thing you have done and nobody will ever "
                                        + "know you did it. "
                                        + name
                                        + " governs as themselves, which is the whole of "
                                        + "what you were building.",

                                Map.of(
                                        "morality", 15,
                                        "familyLoyalty", 20,
                                        "reputation", 10
                                ),

                                Map.of("commonPeople", 12, "familyCouncil", 15),

                                List.of(),

                                List.of("family_stood_by_them")
                        ),

                        new Choice(
                                "Settle your old accounts through them",

                                "The people who wronged you find their affairs suddenly "
                                        + "difficult. It is satisfying for about a year. "
                                        + "After that it is simply what your child's office "
                                        + "is for, and everyone has noticed.",

                                Map.of(
                                        "politicalPower", 15,
                                        "morality", -14,
                                        "reputation", -8
                                ),

                                Map.of("court", -10, "nobles", 8, "commonPeople", -10),

                                List.of(),

                                List.of("family_chose_ambition")
                        ),

                        new Choice(
                                "Ask them to provide for the rest of the household",

                                "You spend your one request on your other children, who "
                                        + "did not become rulers and were never going to. "
                                        + "It is not a large ask and it is granted at once.",

                                Map.of(
                                        "familyLoyalty", 18,
                                        "wealth", 12,
                                        "morality", 5
                                ),

                                Map.of("familyCouncil", 14),

                                List.of(),

                                List.of("family_provided_for")
                        )
                )
        );
    }
}
