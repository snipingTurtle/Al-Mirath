package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs of the sword: the barracks, the frontier fortress, and the peculiar
 * politics of a slave-soldier corps that discovers it can make and unmake
 * the rulers it was bought to serve.
 */
public final class MilitaryArcs {

    private MilitaryArcs() {
    }

    public static void addMilitaryBarracksArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Oath and the Iron",
                "You are issued a sword that is not yours, a bed that is not yours, and a "
                        + "name the sergeant will use for the next twenty years. On the first "
                        + "night, a recruit two beds down is beaten for something he did not do, "
                        + "and the barracks watches to see who moves.",
                "Youth",
                List.of(
                        new Choice(
                                "Step in and take part of the beating yourself",
                                "health",
                                40,
                                "You take the blows meant for him and stay standing. The sergeant "
                                        + "stops. From that night the barracks decides, without a word being "
                                        + "said, that you are someone worth standing near.",
                                "You step in and are put down hard, and he is beaten anyway. Nothing "
                                        + "is gained but a broken rib and the knowledge that courage without "
                                        + "strength changes very little.",
                                Map.of("health", -8, "reputation", 16, "morality", 12),
                                Map.of("health", -20, "reputation", 6, "stress", 12),
                                Map.of("military", 18, "commonPeople", 8),
                                Map.of("military", 5),
                                List.of(),
                                List.of("barracks_arc_started", "stood_for_a_comrade"),
                                List.of("barracks_arc_started", "tried_and_failed_to_help")
                        ),
                        new Choice(
                                "Stay still, and learn who hands out beatings and why",
                                "You watch, and you memorise. Within a month you understand the "
                                        + "barracks' real hierarchy better than the officers do. It cost you "
                                        + "nothing but a certain look you now get from the man in that bed.",
                                Map.of("politicalPower", 12, "education", 6, "morality", -8),
                                Map.of("military", 6, "shadowNetwork", 8),
                                List.of(),
                                List.of("barracks_arc_started", "learned_the_hierarchy")
                        ),
                        new Choice(
                                "Report the sergeant to the officer above him",
                                "The officer thanks you and does nothing, then mentions your name to "
                                        + "the sergeant. You have learned in one night exactly how far the "
                                        + "chain of command protects a recruit.",
                                Map.of("reputation", -8, "stress", 14, "education", 8),
                                Map.of("military", -10, "court", 5),
                                List.of(),
                                List.of("barracks_arc_started", "went_over_his_head")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Pay Does Not Arrive",
                "The corps has not been paid in four months. The treasury says next season. "
                        + "The men in your barracks have stopped talking about it in front of "
                        + "officers, which is considerably more dangerous than complaining.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Carry the men's grievance to the commander openly",
                                "reputation",
                                45,
                                "You put it plainly and without threat, and the commander — who has "
                                        + "also not been paid — takes it up the chain hard enough that coin "
                                        + "arrives within the month. The men know who did that.",
                                "You are heard out, thanked, and marked as the man who speaks for "
                                        + "discontent. The pay arrives eventually. Your name is now in a "
                                        + "different kind of file.",
                                Map.of("reputation", 16, "politicalPower", 10, "stress", 8),
                                Map.of("reputation", -6, "stress", 14, "politicalPower", -5),
                                Map.of("military", 18, "court", 5),
                                Map.of("military", 10, "court", -12),
                                List.of(),
                                List.of("spoke_for_the_men"),
                                List.of("marked_as_agitator")
                        ),
                        new Choice(
                                "Keep the barracks quiet by any means until the coin comes",
                                "You lean on the loudest men personally. The barracks stays quiet. "
                                        + "Order is preserved, and a certain amount of trust is spent that "
                                        + "you will not get back.",
                                Map.of("politicalPower", 14, "morality", -10, "stress", 10),
                                Map.of("court", 14, "military", -8),
                                List.of(),
                                List.of("suppressed_the_grievance")
                        ),
                        new Choice(
                                "Quietly encourage the men to organise",
                                "You do not lead it. You simply make sure the right men are talking "
                                        + "to each other. When it breaks, it will break without your name "
                                        + "attached — unless someone talks.",
                                Map.of("politicalPower", 12, "morality", -5, "stress", 14),
                                Map.of("military", 14, "shadowNetwork", 10, "court", -10),
                                List.of(),
                                List.of("stoked_the_mutiny")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("barracks_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Night the Corps Decides",
                "It has come. The corps is armed, the gates are held from the inside, and "
                        + "the commander is barricaded in his quarters. The men are looking for "
                        + "someone to tell them what this is: a mutiny, a negotiation, or a coup.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Put yourself between the men and the commander's door",
                                "health",
                                55,
                                "You hold the corridor and talk for four hours until the anger burns "
                                        + "down into terms. Nobody dies. Both sides find, afterwards, that "
                                        + "they owe you something they resent owing.",
                                "You hold the corridor and they go through you. The commander does not "
                                        + "survive the night, and neither does most of your body's former "
                                        + "capacity for soldiering.",
                                Map.of("reputation", 20, "politicalPower", 14, "stress", 16),
                                Map.of("health", -28, "stress", 22, "reputation", 8),
                                Map.of("military", 14, "court", 16),
                                Map.of("military", -6, "court", 8),
                                List.of(),
                                List.of("held_the_corridor"),
                                List.of("mutiny_succeeded", "broken_in_the_mutiny")
                        ),
                        new Choice(
                                "Lead it, and make the corps' terms yourself",
                                "politicalPower",
                                55,
                                "You take command of the rising and turn it into a negotiation with "
                                        + "the palace conducted from a position of overwhelming strength. The "
                                        + "corps gets paid. You get noticed by everyone who matters.",
                                "You take command of a rising that fails. The reinforcements arrive "
                                        + "two days early, and the men who followed you are extremely easy "
                                        + "to identify.",
                                Map.of("politicalPower", 22, "reputation", 14, "stress", 18),
                                Map.of("politicalPower", -20, "reputation", -18, "stress", 25, "health", -12),
                                Map.of("military", 22, "court", -14),
                                Map.of("military", -14, "court", -22),
                                List.of(),
                                List.of("led_the_mutiny", "mutiny_succeeded"),
                                List.of("led_the_mutiny", "mutiny_crushed")
                        ),
                        new Choice(
                                "Slip out and inform the palace before it begins",
                                "The reinforcements arrive before dawn. The rising is over in an hour. "
                                        + "You are rewarded generously, and you will never again turn your "
                                        + "back on a man from that barracks.",
                                Map.of("politicalPower", 16, "wealth", 14, "morality", -18, "stress", 16),
                                Map.of("court", 20, "military", -25),
                                List.of(),
                                List.of("betrayed_the_corps", "betrayed_comrade")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("barracks_arc_started"),
                List.of()
        ));
    }

    public static void addFrontierCommanderArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Ribat at the Edge",
                "You are given a frontier fortress: forty men, walls that need work, and a "
                        + "supply line that arrives when it feels like it. It is not a reward. It "
                        + "is what the court does with officers it has not decided about yet.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Rebuild the walls before anything else, whatever it costs",
                                "You spend the season on stone. The men grumble about masonry instead "
                                        + "of soldiering, right up until the first raid breaks against a wall "
                                        + "that holds.",
                                Map.of("wealth", -14, "health", -6, "politicalPower", 10, "stress", 8),
                                Map.of("military", 14, "court", 5),
                                List.of(),
                                List.of("frontier_arc_started", "rebuilt_the_walls")
                        ),
                        new Choice(
                                "Make terms with the raiders instead of fighting them",
                                "You meet their chief, and you find a man with the same supply problem "
                                        + "and the same distant, uninterested overlords. You reach an "
                                        + "arrangement. The frontier goes quiet, and quiet frontiers make "
                                        + "courts suspicious.",
                                Map.of("politicalPower", 12, "wealth", 8, "stress", -6),
                                Map.of("military", -6, "court", -8, "commonPeople", 12),
                                List.of(),
                                List.of("frontier_arc_started", "made_terms_with_raiders")
                        ),
                        new Choice(
                                "Raid across the border first and make a reputation",
                                "health",
                                45,
                                "You go over the line and come back with cattle, prisoners, and a "
                                        + "story that reaches the capital. The frontier learns your name "
                                        + "before it learns your face.",
                                "The raid goes badly. You come back with fewer men than you left with "
                                        + "and nothing to show the court but a list of names.",
                                Map.of("reputation", 16, "politicalPower", 12, "wealth", 10, "stress", 10),
                                Map.of("health", -16, "reputation", -12, "stress", 18),
                                Map.of("military", 16, "court", 8),
                                Map.of("military", -12, "commonPeople", -6),
                                List.of(),
                                List.of("frontier_arc_started", "raided_across_the_line"),
                                List.of("frontier_arc_started", "raid_went_badly")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Siege",
                "They came in force this time, and they are not leaving. The walls hold for "
                        + "now. The water will last eleven days. Relief was promised, in writing, "
                        + "by a court that has promised many things in writing.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Hold, and trust the relief column",
                                "politicalPower",
                                50,
                                "The column arrives on the ninth day. The siege breaks. You held a "
                                        + "position everyone in the capital had privately written off, and "
                                        + "the capital now has to explain why it wrote it off.",
                                "No column comes. On the fourteenth day you surrender a fortress full "
                                        + "of men who trusted you because you trusted a letter.",
                                Map.of("reputation", 22, "politicalPower", 18, "health", -10, "stress", 16),
                                Map.of("reputation", -16, "politicalPower", -14, "health", -18, "stress", 25),
                                Map.of("military", 22, "court", 12, "commonPeople", 10),
                                Map.of("military", -12, "court", -10),
                                List.of(),
                                List.of("held_the_ribat"),
                                List.of("lost_the_ribat")
                        ),
                        new Choice(
                                "Negotiate a surrender that spares the garrison",
                                "You give up the fortress and walk your men out alive. The court calls "
                                        + "it cowardice. Forty families call it something else entirely.",
                                Map.of("reputation", -10, "morality", 14, "stress", 10),
                                Map.of("court", -14, "military", 8, "commonPeople", 12),
                                List.of(),
                                List.of("surrendered_to_save_the_men")
                        ),
                        new Choice(
                                "Break out at night and abandon the position",
                                "You get most of the garrison out through the postern in the dark. The "
                                        + "fortress falls the next morning, undefended, and the frontier "
                                        + "shifts thirty miles in a single night because of it.",
                                Map.of("health", -12, "politicalPower", -8, "stress", 14),
                                Map.of("military", -8, "court", -16, "commonPeople", -10),
                                List.of(),
                                List.of("abandoned_the_ribat")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("frontier_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "What the Frontier Owes You",
                "The border is quiet now, and quiet borders are administered by clerks "
                        + "rather than soldiers. There is a question of what is owed to the man "
                        + "who held it, and who exactly is going to pay it.",
                "Legacy",
                List.of(
                        new Choice(
                                "Petition the court for the land grant you were promised",
                                "politicalPower",
                                50,
                                "The grant is confirmed. Your family holds land on the frontier it "
                                        + "bled for, and holds it by document rather than by sufferance.",
                                "The petition is acknowledged, filed, and forgotten. The land goes to "
                                        + "a courtier who has never been east of the river.",
                                Map.of("wealth", 22, "politicalPower", 12, "familyLoyalty", 10),
                                Map.of("reputation", -8, "stress", 14, "politicalPower", -8),
                                Map.of("court", 12, "nobles", 12),
                                Map.of("court", -14, "military", 6),
                                List.of(),
                                List.of("granted_frontier_land"),
                                List.of("petition_ignored")
                        ),
                        new Choice(
                                "Settle your veterans on the land and ask the court nothing",
                                "You divide what you hold among the men who held it with you. The "
                                        + "court never confirms it and never quite dares to reverse it.",
                                Map.of("wealth", -14, "reputation", 16, "morality", 14),
                                Map.of("military", 20, "commonPeople", 14, "court", -8),
                                List.of(),
                                List.of("settled_the_veterans")
                        ),
                        new Choice(
                                "Take a comfortable posting in the capital and forget the border",
                                "You accept a title, a stipend, and a room with a view of a garden. "
                                        + "The men who held the wall with you write for a few years, and "
                                        + "then stop writing.",
                                Map.of("wealth", 16, "politicalPower", 10, "morality", -12, "stress", -10),
                                Map.of("court", 14, "military", -16),
                                List.of(),
                                List.of("took_the_soft_posting")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("frontier_arc_started"),
                List.of()
        ));
    }

    public static void addJanissaryLoyaltyArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "Taken for the Corps",
                "You were taken from your village as a boy, converted, trained, and made "
                        + "into something the empire values far more than it valued the child. The "
                        + "corps is your family now, by decree. Whether it becomes one in fact is "
                        + "still open.",
                "Youth",
                List.of(
                        new Choice(
                                "Give yourself to the corps completely",
                                "You let the old life go entirely — the village, the language, the "
                                        + "name. What replaces it is discipline, brotherhood, and a "
                                        + "belonging that is real even though it was imposed.",
                                Map.of("health", 12, "politicalPower", 12, "familyLoyalty", -14, "stress", -6),
                                Map.of("military", 20, "court", 8),
                                List.of(),
                                List.of("janissary_arc_started", "gave_self_to_corps")
                        ),
                        new Choice(
                                "Serve well, but keep the memory of where you came from",
                                "You are an excellent soldier who still, privately, counts in his "
                                        + "first language. It is a small treason and it keeps something of "
                                        + "you alive.",
                                Map.of("politicalPower", 8, "familyLoyalty", 10, "stress", 8),
                                Map.of("military", 10, "commonPeople", 8),
                                List.of(),
                                List.of("janissary_arc_started", "kept_the_old_memory")
                        ),
                        new Choice(
                                "Learn the corps' politics from the first day",
                                "You work out immediately that the corps is not only an army; it is a "
                                        + "constituency, and constituencies choose rulers. You begin making "
                                        + "friends accordingly.",
                                Map.of("politicalPower", 16, "morality", -6, "stress", 10),
                                Map.of("military", 12, "shadowNetwork", 8),
                                List.of(),
                                List.of("janissary_arc_started", "learned_corps_politics")
                        )
                ),
                List.of("Ottoman Era", "Mamluk Era"),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Kettle Is Overturned",
                "The corps has overturned its cooking kettles in the square — the old "
                        + "signal that it no longer accepts the government. Within a day there "
                        + "will be a new grand vizier or a great many dead soldiers. You have "
                        + "standing enough that your position matters.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Stand with the corps",
                                "You stand where the corps stands. The vizier falls by the end of the "
                                        + "week. The corps has now learned, again, that it can do this, and "
                                        + "will do it again within your lifetime.",
                                Map.of("politicalPower", 18, "stress", 12),
                                Map.of("military", 22, "court", -16, "nobles", -8),
                                List.of(),
                                List.of("stood_with_the_corps")
                        ),
                        new Choice(
                                "Stand with the throne against your own corps",
                                "politicalPower",
                                60,
                                "You hold enough of the corps back that the rising fails. The palace "
                                        + "will reward you extravagantly and never fully trust you, and "
                                        + "neither will any soldier who was in that square.",
                                "You misjudge your own standing badly. The men do not follow you, the "
                                        + "rising succeeds, and you are the officer who tried to stop it.",
                                Map.of("politicalPower", 20, "wealth", 18, "stress", 18),
                                Map.of("politicalPower", -18, "health", -14, "stress", 24),
                                Map.of("court", 22, "military", -20),
                                Map.of("military", -24, "court", 6),
                                List.of(),
                                List.of("saved_the_throne"),
                                List.of("failed_against_the_corps")
                        ),
                        new Choice(
                                "Take neither side and position yourself for the aftermath",
                                "You are unavailable, convincingly, for the entire crisis. Whoever "
                                        + "wins, you are not their enemy — and whoever wins knows exactly "
                                        + "what that was worth.",
                                Map.of("politicalPower", 8, "morality", -10, "stress", 10),
                                Map.of("shadowNetwork", 14, "military", -6, "court", -5),
                                List.of(),
                                List.of("sat_out_the_kettle")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("janissary_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Village You Came From",
                "A petition crosses your desk from a district you have not thought about "
                        + "in forty years. You recognise the name of the village. You may even "
                        + "recognise the family name on the petition.",
                "Legacy",
                List.of(
                        new Choice(
                                "Grant the petition and say nothing about why",
                                "The village gets its relief. Nobody there ever learns that the "
                                        + "official who signed it was the boy they lost, and that is exactly "
                                        + "how you want it.",
                                Map.of("morality", 16, "familyLoyalty", 12, "wealth", -10, "stress", -8),
                                Map.of("commonPeople", 18),
                                List.of(),
                                List.of("quietly_helped_the_village")
                        ),
                        new Choice(
                                "Go there yourself, and tell them who you are",
                                "You ride back as a man of the empire that took you. Some weep. Some "
                                        + "cannot look at you. Both reactions are correct.",
                                Map.of("familyLoyalty", 18, "stress", 14, "reputation", 8),
                                Map.of("commonPeople", 14, "familyCouncil", 12),
                                List.of(),
                                List.of("returned_to_the_village")
                        ),
                        new Choice(
                                "Refuse it like any other petition",
                                "You apply the same standard you apply to everything. It is fair, and "
                                        + "it is the last door closing on a life you were taken from.",
                                Map.of("morality", -12, "familyLoyalty", -14, "politicalPower", 6),
                                Map.of("court", 8, "commonPeople", -10),
                                List.of(),
                                List.of("refused_the_village")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("janissary_arc_started"),
                List.of()
        ));
    }
}
