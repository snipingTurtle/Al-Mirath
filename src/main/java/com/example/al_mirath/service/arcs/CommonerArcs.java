package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs from below: bread, taxes, famine, and the patron who lifts an orphan
 * out of the street and expects to be repaid in a currency the orphan does
 * not yet understand.
 */
public final class CommonerArcs {

    private CommonerArcs() {
    }

    public static void addBreadRebellionArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Price of Bread Doubles",
                "It happened overnight and without explanation. The bakers blame the grain "
                        + "merchants, the merchants blame the governor's storehouses, and the "
                        + "district is standing in the street doing arithmetic it cannot make work.",
                "Youth",
                List.of(
                        new Choice(
                                "Find out who is actually holding the grain",
                                "You spend a week asking careful questions and establish, fairly "
                                        + "conclusively, that the shortage is manufactured and where. "
                                        + "Knowledge is not bread, but it is the beginning of something.",
                                Map.of("education", 12, "politicalPower", 8, "stress", 8),
                                Map.of("commonPeople", 12, "merchants", -8),
                                List.of(),
                                List.of("bread_arc_started", "traced_the_hoarders")
                        ),
                        new Choice(
                                "Organise the district to buy collectively",
                                "You get forty households to pool their coin and buy direct. The price "
                                        + "they pay drops by a third, and a number of middlemen learn your "
                                        + "name in a context they dislike.",
                                Map.of("politicalPower", 12, "reputation", 12, "wealth", -6, "stress", 10),
                                Map.of("commonPeople", 16, "merchants", -12),
                                List.of(),
                                List.of("bread_arc_started", "organised_collective_buying")
                        ),
                        new Choice(
                                "Take work with the grain merchants instead",
                                "If the shortage is where the money is, you would rather be inside it "
                                        + "than under it. The work is steady. Your neighbours notice which "
                                        + "gate you walk through each morning.",
                                Map.of("wealth", 14, "morality", -10, "stress", -4),
                                Map.of("merchants", 12, "commonPeople", -12),
                                List.of(),
                                List.of("bread_arc_started", "joined_the_hoarders")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Crowd Finds a Voice",
                "It has stopped being a queue and started being a crowd. They are outside "
                        + "the storehouse, and they are looking for someone who can put words to "
                        + "what they already intend to do.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Speak, and keep it to demands rather than violence",
                                "reputation",
                                50,
                                "You hold four hundred angry people to a list of demands and a "
                                        + "deputation. The storehouse opens by negotiation. Nobody is killed, "
                                        + "and everyone knows who managed that.",
                                "You are shouted down by men who came for the door, not for a speech. "
                                        + "The storehouse is stormed anyway, and your name is on the list of "
                                        + "people who addressed the crowd.",
                                Map.of("reputation", 20, "politicalPower", 14, "stress", 14),
                                Map.of("reputation", -8, "stress", 20, "politicalPower", -6),
                                Map.of("commonPeople", 20, "court", -8),
                                Map.of("commonPeople", 8, "court", -18),
                                List.of(),
                                List.of("led_the_crowd_peacefully", "protected_commoners"),
                                List.of("crowd_turned_violent")
                        ),
                        new Choice(
                                "Lead them through the storehouse doors",
                                "The doors come down. There is more grain inside than the governor "
                                        + "admitted existed, which proves the point and does not undo the "
                                        + "fact that you led a riot.",
                                Map.of("politicalPower", 16, "reputation", 12, "morality", -6, "stress", 18),
                                Map.of("commonPeople", 22, "court", -20, "merchants", -14),
                                List.of(),
                                List.of("stormed_the_storehouse", "protected_commoners")
                        ),
                        new Choice(
                                "Get your own family clear and let it happen",
                                "You take your household three streets away and wait. It is the "
                                        + "sensible thing. You will hear about that night for the rest of "
                                        + "your life from people who stayed.",
                                Map.of("familyLoyalty", 10, "stress", -6, "reputation", -12),
                                Map.of("familyCouncil", 10, "commonPeople", -14),
                                List.of(),
                                List.of("stayed_out_of_the_riot")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("bread_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Reckoning After",
                "Order has been restored, which means the arrests have begun. The governor "
                        + "requires names, and there are a great many people in the district who "
                        + "could supply yours.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Take the blame yourself to spare the others",
                                "health",
                                45,
                                "You present yourself as the organiser. The prosecution has its "
                                        + "figurehead and loses interest in the rest. You survive it, "
                                        + "diminished, and the district does not forget the arithmetic.",
                                "You present yourself, and they take you and forty others regardless. "
                                        + "The gesture cost you everything and saved nobody.",
                                Map.of("health", -14, "reputation", 22, "morality", 20, "stress", 18),
                                Map.of("health", -25, "reputation", 10, "morality", 18, "stress", 25),
                                Map.of("commonPeople", 25, "court", -12),
                                Map.of("commonPeople", 14, "court", -16),
                                List.of(),
                                List.of("took_the_blame"),
                                List.of("sacrifice_was_wasted")
                        ),
                        new Choice(
                                "Give the governor the names of the actual hoarders instead",
                                "education",
                                50,
                                "You hand over a documented case against the men who manufactured the "
                                        + "shortage. The governor, needing a culprit and preferring a rich "
                                        + "one, takes it. Three merchants are ruined and the district "
                                        + "escapes.",
                                "Your evidence is thin where it matters and the governor is not "
                                        + "interested in a case that implicates his own storehouse.",
                                Map.of("politicalPower", 16, "reputation", 16, "stress", 12),
                                Map.of("stress", 18, "reputation", -8),
                                Map.of("commonPeople", 18, "merchants", -20, "court", 8),
                                Map.of("court", -12, "merchants", -8),
                                List.of(),
                                List.of("redirected_the_blame"),
                                List.of("evidence_rejected")
                        ),
                        new Choice(
                                "Give them the names they want",
                                "You name eleven people, most of whom were simply hungry. You are "
                                        + "released the same day, and you will never again be able to buy "
                                        + "bread on that street.",
                                Map.of("morality", -22, "stress", 16, "reputation", -16),
                                Map.of("court", 16, "commonPeople", -25),
                                List.of(),
                                List.of("named_the_hungry")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("bread_arc_started"),
                List.of()
        ));
    }

    public static void addTaxResistanceArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Assessment Is Wrong",
                "The tax assessment on your district has been calculated on land that "
                        + "flooded two years ago and has produced nothing since. The assessor knows. "
                        + "The assessor has a quota.",
                "Youth",
                List.of(
                        new Choice(
                                "Document the flooding and file a formal appeal",
                                "education",
                                35,
                                "You produce witnesses, the previous survey, and a very boring "
                                        + "document that is impossible to argue with. The assessment is "
                                        + "revised. It takes eight months and it works.",
                                "Your appeal is correct, complete, and rejected on the grounds that "
                                        + "the filing period closed before you learned there was one.",
                                Map.of("education", 12, "reputation", 12, "wealth", 8),
                                Map.of("wealth", -12, "stress", 14, "education", 6),
                                Map.of("commonPeople", 14, "court", 6),
                                Map.of("court", -8),
                                List.of(),
                                List.of("tax_arc_started", "won_the_appeal"),
                                List.of("tax_arc_started", "appeal_rejected")
                        ),
                        new Choice(
                                "Pay it and say nothing",
                                "You find the money somehow. The assessor records a district that "
                                        + "pays without complaint, and next year's figure reflects that "
                                        + "discovery.",
                                Map.of("wealth", -16, "stress", 12),
                                Map.of("court", 8, "commonPeople", -6),
                                List.of(),
                                List.of("tax_arc_started", "paid_without_protest")
                        ),
                        new Choice(
                                "Organise the district to withhold together",
                                "Nobody pays. It is remarkably effective and remarkably illegal, and "
                                        + "it holds exactly as long as the least frightened household holds.",
                                Map.of("politicalPower", 14, "reputation", 10, "stress", 16),
                                Map.of("commonPeople", 18, "court", -16),
                                List.of(),
                                List.of("tax_arc_started", "organised_withholding")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Collector Returns With Soldiers",
                "He has come back, and this time he has brought men with him and a list "
                        + "with your household on it. He would like to resolve this quietly, by "
                        + "which he means expensively.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Pay him privately to make the list shorter",
                                "You buy your household off the list. Somebody else's household stays "
                                        + "on it, and that arithmetic is now permanently attached to you.",
                                Map.of("wealth", -18, "morality", -14, "stress", 8),
                                Map.of("court", 6, "commonPeople", -14, "shadowNetwork", 8),
                                List.of(),
                                List.of("bought_off_the_list", "used_bribery")
                        ),
                        new Choice(
                                "Stand in front of the soldiers with the district behind you",
                                "reputation",
                                55,
                                "Four hundred people arrive to watch, and soldiers are considerably "
                                        + "less enthusiastic in front of four hundred witnesses. He leaves "
                                        + "with a revised figure and a grudge.",
                                "The district does not arrive. You stand alone in front of armed men "
                                        + "and discover exactly how much support you actually had.",
                                Map.of("reputation", 20, "politicalPower", 12, "stress", 16),
                                Map.of("health", -14, "reputation", -10, "wealth", -16, "stress", 20),
                                Map.of("commonPeople", 22, "court", -14),
                                Map.of("commonPeople", -8, "court", -8),
                                List.of(),
                                List.of("faced_down_the_collector", "protected_commoners"),
                                List.of("stood_alone_and_lost")
                        ),
                        new Choice(
                                "Take the matter over his head to the provincial court",
                                "politicalPower",
                                45,
                                "You go around him entirely to an office that does not like "
                                        + "freelancing collectors. He is transferred. The district's "
                                        + "assessment is fixed for a decade.",
                                "The provincial court returns your petition to the very man you were "
                                        + "complaining about, with your name at the top.",
                                Map.of("politicalPower", 14, "reputation", 14, "stress", 10),
                                Map.of("stress", 20, "wealth", -14, "politicalPower", -8),
                                Map.of("court", 12, "commonPeople", 16),
                                Map.of("court", -14, "commonPeople", 5),
                                List.of(),
                                List.of("went_over_the_collector"),
                                List.of("petition_came_back_to_him", "collector_reported_you")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("tax_arc_started"),
                List.of()
        ));
    }

    public static void addFamineReliefArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Granary Is Yours to Open",
                "The harvest failed, you control a granary, and the district knows both "
                        + "facts. What you do in the next week will define what your name means "
                        + "here for two generations.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Open it and distribute free until it is empty",
                                "You give away everything. Within eleven days there is nothing left "
                                        + "and roughly two hundred people who would otherwise have starved. "
                                        + "You are ruined and you are theirs.",
                                Map.of("wealth", -25, "reputation", 22, "morality", 22, "stress", 10),
                                Map.of("commonPeople", 25, "merchants", -12, "scholars", 10),
                                List.of(
                                        ChoiceRequirement.statAtLeast("wealth", 20,
                                                "Locked: Requires Wealth 20.")
                                ),
                                List.of("famine_arc_started", "emptied_the_granary", "protected_commoners")
                        ),
                        new Choice(
                                "Ration it carefully to last the whole season",
                                "You give out less than people need and more than they would have "
                                        + "had, every day, for four months. Fewer are grateful. More survive.",
                                Map.of("wealth", -14, "reputation", 14, "morality", 14, "education", 6),
                                Map.of("commonPeople", 16, "merchants", -5),
                                List.of(),
                                List.of("famine_arc_started", "rationed_the_grain")
                        ),
                        new Choice(
                                "Sell at the price the shortage supports",
                                "You make a fortune in a season. It is entirely legal, it is what "
                                        + "every other granary is doing, and there is a particular silence "
                                        + "that follows you through the district afterwards.",
                                Map.of("wealth", 28, "morality", -22, "reputation", -16),
                                Map.of("merchants", 16, "commonPeople", -25),
                                List.of(),
                                List.of("famine_arc_started", "profited_from_famine")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "What the Famine Left",
                "The rains came back. The district is thinner and quieter, and it has a "
                        + "very exact memory of who did what during the bad year.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Rebuild the district's grain reserve as common property",
                                "You put the new granary under a trust that no single family can "
                                        + "close. It is the most useful thing you will ever build and it "
                                        + "permanently reduces your own leverage.",
                                Map.of("wealth", -18, "politicalPower", -6, "morality", 18, "reputation", 16),
                                Map.of("commonPeople", 22, "scholars", 10, "merchants", -8),
                                List.of(),
                                List.of("built_a_common_granary")
                        ),
                        new Choice(
                                "Buy the land of families who could not survive the year",
                                "They are selling because they must, at prices that reflect that. It "
                                        + "is a legitimate transaction repeated forty times, and at the end "
                                        + "of it you own most of the valley.",
                                Map.of("wealth", 25, "politicalPower", 14, "morality", -18),
                                Map.of("nobles", 12, "commonPeople", -20, "merchants", 8),
                                List.of(),
                                List.of("bought_the_ruined_out")
                        ),
                        new Choice(
                                "Petition the court to forgive the district's arrears",
                                "politicalPower",
                                45,
                                "You get the year's taxes written off entirely. It is invisible, "
                                        + "unglamorous, and worth more to these households than anything "
                                        + "that happened in the square.",
                                "The petition fails. The arrears stand, and families that survived "
                                        + "the famine are ruined by the paperwork that followed it.",
                                Map.of("reputation", 16, "politicalPower", 10, "stress", 10),
                                Map.of("reputation", -6, "stress", 14),
                                Map.of("commonPeople", 20, "court", -6),
                                Map.of("commonPeople", -8, "court", -8),
                                List.of(),
                                List.of("forgave_the_arrears"),
                                List.of("arrears_stood")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("famine_arc_started"),
                List.of()
        ));
    }

    public static void addOrphanPatronArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Hand That Lifts You",
                "A man of standing has noticed you sleeping in a doorway and has decided "
                        + "to do something about it. He offers a bed, food, and schooling. He is "
                        + "either the best thing that will ever happen to you or the beginning of "
                        + "a very long account.",
                "Childhood",
                List.of(
                        new Choice(
                                "Accept gratefully and give him your loyalty",
                                "You take the hand. He is, as far as you can tell across the next ten "
                                        + "years, exactly what he appeared to be — and you spend those years "
                                        + "waiting for the bill regardless.",
                                Map.of("education", 14, "wealth", 8, "familyLoyalty", 10, "stress", -6),
                                Map.of("nobles", 12, "scholars", 8),
                                List.of(),
                                List.of("orphan_patron_started", "accepted_the_patron")
                        ),
                        new Choice(
                                "Accept, but keep one foot on the street",
                                "You take the bed and the schooling and you keep your old contacts, "
                                        + "your old routes, and the habit of knowing where the exits are.",
                                Map.of("education", 10, "politicalPower", 8, "stress", 6),
                                Map.of("nobles", 6, "shadowNetwork", 10, "commonPeople", 6),
                                List.of(),
                                List.of("orphan_patron_started", "kept_one_foot_on_the_street")
                        ),
                        new Choice(
                                "Refuse, and stay free of any man's ledger",
                                "You say no to the only offer of comfort your childhood will produce. "
                                        + "It is a decision made by a hungry child about dignity, and you "
                                        + "will reconsider it many times and never quite regret it.",
                                Map.of("morality", 12, "health", -10, "stress", 12, "education", -6),
                                Map.of("commonPeople", 12, "nobles", -8),
                                List.of(),
                                List.of("orphan_patron_started", "refused_the_patron")
                        )
                ),
                List.of(),
                // Only a child already on the street can be lifted off it.
                List.of(
                        "Poor Village Child",
                        "Poor Urban Child",
                        "Village Orphan",
                        "Mawali Convert's Child",
                        "Bedouin Nomad"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Patron Falls",
                "The man who raised you has been accused, credibly, of something serious. "
                        + "His household is being abandoned by everyone who can afford to abandon "
                        + "it. You are one of the few people whose testimony would matter.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Testify for him, whatever it costs you",
                                "You stand up in front of a hostile court and say what you know. It "
                                        + "does not save him. It does mean that when people describe his "
                                        + "fall, they mention that one person stayed.",
                                Map.of("morality", 20, "reputation", 12, "politicalPower", -12, "stress", 18),
                                Map.of("court", -14, "nobles", 10, "commonPeople", 12),
                                List.of(),
                                List.of("stood_by_the_patron")
                        ),
                        new Choice(
                                "Stay away and let it happen",
                                "You are unavailable. He is convicted without you, and he never asks "
                                        + "why you did not come, which is worse than being asked.",
                                Map.of("stress", 14, "morality", -14, "politicalPower", 5),
                                Map.of("court", 8, "nobles", -8),
                                List.of(),
                                List.of("abandoned_the_patron")
                        ),
                        new Choice(
                                "Testify against him in exchange for his position",
                                "You give the court what it needs and are appointed to the office he "
                                        + "vacated, in a ceremony held eleven days after his conviction. "
                                        + "Everyone present does the arithmetic.",
                                Map.of("politicalPower", 20, "wealth", 16, "morality", -25, "stress", 16),
                                Map.of("court", 16, "nobles", -16, "commonPeople", -12),
                                List.of(),
                                List.of("took_the_patrons_place", "betrayed_comrade")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("orphan_patron_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "A Child in a Doorway",
                "You are old, and comfortable, and there is a child asleep in a doorway on "
                        + "the street you now own most of. The symmetry is not subtle and it does "
                        + "not care whether you find it subtle.",
                "Legacy",
                List.of(
                        new Choice(
                                "Do for the child exactly what was done for you",
                                "You take the child in. Whatever else your ledger holds, this entry "
                                        + "balances one that has been open a very long time.",
                                Map.of("wealth", -14, "morality", 20, "familyLoyalty", 12, "stress", -10),
                                Map.of("commonPeople", 18, "scholars", 8, "familyCouncil", 8),
                                List.of(),
                                List.of("passed_it_on")
                        ),
                        new Choice(
                                "Endow a foundation so it is never one man's charity again",
                                "You put the money into a standing institution rather than a personal "
                                        + "gesture. It will help far more children and none of them will "
                                        + "ever owe you anything, which is the entire point.",
                                Map.of("wealth", -22, "morality", 18, "reputation", 16),
                                Map.of("commonPeople", 20, "scholars", 14),
                                List.of(
                                        ChoiceRequirement.statAtLeast("wealth", 22,
                                                "Locked: Requires Wealth 22.")
                                ),
                                List.of("endowed_an_orphanage")
                        ),
                        new Choice(
                                "Walk past",
                                "You walk past. You are not obliged to close every circle life "
                                        + "presents, and you have closed a great many others.",
                                Map.of("morality", -16, "stress", 8),
                                Map.of("commonPeople", -12),
                                List.of(),
                                List.of("walked_past_the_child")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("orphan_patron_started"),
                List.of()
        ));
    }
}
