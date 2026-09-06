package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.City;
import com.example.al_mirath.model.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Events that come from where the player is standing.
 *
 * <p>Built fresh on every lookup, like the cast's and the household's, so the
 * scene describes the city as it is this year rather than as it was when the
 * run started. A siege only produces siege events while the siege is on, and
 * the same player standing in a flourishing Basra and a starving one is not
 * offered the same life.
 *
 * <p>The choices here change the city as well as the player. They do it
 * through the ordinary story-flag channel, which {@link CityRegistry} reads —
 * so a player who opens the granary leaves a measurably less desperate place
 * behind them.
 */
public final class CityEvents {

    private CityEvents() {
    }

    public static List<GameEvent> create(CityRegistry cities) {
        List<GameEvent> events = new ArrayList<>();

        if (cities == null) {
            return events;
        }

        City here = cities.currentCity();

        if (here == null) {
            return events;
        }

        switch (here.condition()) {
            case BESIEGED -> {
                events.add(theWalls(here));
                events.add(willNotHold(here));
            }
            case PLAGUE_STRICKEN -> {
                events.add(theSickness(here));
                events.add(whatTheFeverTook(here));
            }
            case LAWLESS -> {
                events.add(afterDark(here));
                events.add(theQuarterThatPays(here));
            }
            case LEARNED -> {
                events.add(theCopyists(here));
                events.add(theChair(here));
            }
            case FLOURISHING -> events.add(theGoodYears(here));
            case STRUGGLING -> events.add(theHungryQuarter(here));
            case QUIET -> events.add(theOrdinaryYear(here));
        }

        City elsewhere = cities.somewhereElse();

        if (elsewhere != null) {
            events.add(theRoadOut(here, elsewhere));
        }

        return events;
    }

    /** The flag that moves a life to another city. */
    static String travelFlagFor(City destination) {
        return "travel_to_" + destination.getName().toLowerCase(Locale.ROOT);
    }

    // ---- under siege -----------------------------------------------------

    private static GameEvent theWalls(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Walls of " + name,

                "The war has been close for a season and is now simply here. "
                        + name + " has shut its gates, and the men on the wall "
                        + "are the men who were in the market last month. "
                        + "Someone with your standing is asked, not ordered, "
                        + "which is how they ask when they are short.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Stand on the wall with them",
                                "health",
                                45,
                                "You take a watch like anyone else, and the city "
                                        + "sees you take it. The siege lifts in its own "
                                        + "time, but afterwards the ones who were up "
                                        + "there know who else was.",
                                "You take a watch and it very nearly takes you. You "
                                        + "come off the wall carried, and the wall "
                                        + "holds without you.",
                                Map.of("reputation", 14, "stress", 10, "health", -6),
                                Map.of("health", -20, "stress", 16, "reputation", 5),
                                Map.of("military", 16, "commonPeople", 12),
                                Map.of("military", 6, "commonPeople", 5),
                                List.of(),
                                List.of("city_walls_held", "held_the_corridor"),
                                List.of("city_walls_held")
                        ),

                        new Choice(
                                "Pay for the walls instead of standing on them",
                                "Your money buys timber, arrowheads and a month of "
                                        + "bread for men you never meet. It is the more "
                                        + "useful contribution and everyone knows it, "
                                        + "which does not stop them noticing you slept "
                                        + "indoors.",
                                Map.of("wealth", -20, "reputation", 6),
                                Map.of("military", 10, "commonPeople", 4),
                                List.of(),
                                List.of("city_walls_held")
                        ),

                        new Choice(
                                "Open talks with the besiegers",
                                "politicalPower",
                                50,
                                "You get terms. The gates open on an agreement rather "
                                        + "than a breach, and " + name + " is spared the "
                                        + "worst night of its history. Not everyone will "
                                        + "ever call that a rescue.",
                                "You are seen going out and seen coming back, and "
                                        + "the terms you bring are refused. What "
                                        + "follows is remembered with your name "
                                        + "attached to it.",
                                Map.of("politicalPower", 12, "reputation", -8, "stress", 12),
                                Map.of("reputation", -20, "stress", 20, "politicalPower", -10),
                                Map.of("court", 10, "commonPeople", -12, "military", -14),
                                Map.of("commonPeople", -18, "military", -20),
                                List.of(),
                                List.of("city_gates_opened"),
                                List.of("city_gates_opened")
                        )
                )
        );
    }

    private static GameEvent willNotHold(City city) {
        String name = city.getName();

        return new GameEvent(
                name + " Will Not Hold",

                "The garrison is counting arrows out loud now, which is what "
                        + "they do when there is nothing left to count them "
                        + "against. Whatever " + name + " is in a year, it will "
                        + "not be what it was. People are deciding tonight "
                        + "whether they are the kind who leave.",

                "Political Crisis",

                List.of(
                        new Choice(
                                "Organise the ones who cannot leave",
                                "You spend the last quiet week moving the old, the "
                                        + "sick and the children into the deepest "
                                        + "stone in the city, and arguing with people "
                                        + "who want that space for grain. Some of them "
                                        + "live because of where you put them.",
                                Map.of("stress", 18, "morality", 14, "reputation", 12),
                                Map.of("commonPeople", 20, "familyCouncil", 8),
                                List.of(),
                                List.of("protected_commoners", "city_walls_held")
                        ),

                        new Choice(
                                "Get your own household out tonight",
                                "You go while the road is still a road. It is the "
                                        + "correct decision and it is made in the dark "
                                        + "for a reason. Your people live. You hear "
                                        + "what happened to the others later, from "
                                        + "someone who stayed.",
                                Map.of("familyLoyalty", 12, "reputation", -12, "stress", 10),
                                Map.of("commonPeople", -16, "familyCouncil", 12),
                                List.of(),
                                List.of("city_gates_opened")
                        )
                )
        );
    }

    // ---- plague ----------------------------------------------------------

    private static GameEvent theSickness(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Sickness in " + name,

                "It started in the quarter by the water and it is not there "
                        + "any more, it is everywhere. The physicians disagree "
                        + "loudly in public, which tells everyone how little "
                        + "they know. " + name + " is deciding whether to be a "
                        + "city or a set of locked doors.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Fund the quarantine and enforce it",
                                "politicalPower",
                                42,
                                "The streets are closed and hated and they work. "
                                        + "The sickness burns down to nothing weeks "
                                        + "earlier than it should have, and a great "
                                        + "many people never know they were saved.",
                                "You close the streets and cannot hold them closed. "
                                        + "The measure collapses halfway, which is "
                                        + "worse than either doing it or not.",
                                Map.of("wealth", -15, "politicalPower", 10, "stress", 12),
                                Map.of("wealth", -15, "reputation", -10, "stress", 16),
                                Map.of("court", 10, "commonPeople", 8),
                                Map.of("commonPeople", -12),
                                List.of(),
                                List.of("city_plague_contained"),
                                List.of("city_plague_spread")
                        ),

                        new Choice(
                                "Work the sick houses yourself",
                                "health",
                                55,
                                "You carry water and close eyes for a season. You do "
                                        + "not get it. People who were there will say "
                                        + "your name in a particular way for the rest "
                                        + "of your life.",
                                "You carry water and close eyes for a season, and "
                                        + "then it is your turn. You come out of it "
                                        + "thinner and slower and it never entirely "
                                        + "leaves you.",
                                Map.of("morality", 16, "reputation", 16, "stress", 12),
                                Map.of("health", -25, "morality", 16, "reputation", 14),
                                Map.of("commonPeople", 22, "scholars", 8),
                                Map.of("commonPeople", 20),
                                List.of(),
                                List.of("city_plague_contained", "protected_commoners"),
                                List.of("city_plague_contained")
                        ),

                        new Choice(
                                "Leave the city until it passes",
                                "You go to clean air and wait. Everyone who could "
                                        + "afford to did the same, which is exactly the "
                                        + "problem the people who could not will "
                                        + "remember about this year.",
                                Map.of("health", 6, "reputation", -14, "morality", -8),
                                Map.of("commonPeople", -20, "nobles", 4),
                                List.of(),
                                List.of("city_plague_spread")
                        )
                )
        );
    }

    private static GameEvent whatTheFeverTook(City city) {
        return new GameEvent(
                "What the Fever Took",

                "The sickness has thinned " + city.getName() + " and left the "
                        + "usual mess behind it: houses with nobody's name on "
                        + "them, children with nobody's name on them, and a "
                        + "queue of people explaining why both are rightfully "
                        + "theirs.",

                "Youth",

                List.of(
                        new Choice(
                                "Take in one of the children",
                                "You take one. It costs what it costs and you do not "
                                        + "get to feel noble about it for long, because "
                                        + "the child is a person and not a decision.",
                                Map.of("wealth", -10, "morality", 14, "familyLoyalty", 8),
                                Map.of("commonPeople", 14, "familyCouncil", 6),
                                List.of(),
                                List.of("quietly_helped_the_village")
                        ),

                        new Choice(
                                "Buy the empty houses cheap",
                                "Nobody outbids you, because nobody else has the "
                                        + "money or the stomach. In ten years the "
                                        + "quarter is worth four times what you paid "
                                        + "and everyone has agreed not to discuss "
                                        + "when you bought it.",
                                Map.of("wealth", 22, "morality", -12, "reputation", -6),
                                Map.of("merchants", 14, "commonPeople", -12),
                                List.of(),
                                List.of("profited_from_famine")
                        )
                )
        );
    }

    // ---- lawless ---------------------------------------------------------

    private static GameEvent afterDark(City city) {
        String name = city.getName();

        return new GameEvent(
                name + " After Dark",

                "The watch has stopped going down certain streets and everyone "
                        + "has quietly agreed which ones. You are on one of them "
                        + "later than you meant to be, and the two men ahead are "
                        + "not walking anywhere in particular.",

                "Youth",

                List.of(
                        new Choice(
                                "Talk your way past them",
                                "education",
                                40,
                                "You find the one thing to say that makes you more "
                                        + "trouble than you are worth. They let you by "
                                        + "and you walk the rest of it very carefully.",
                                "You say the wrong thing confidently. They take what "
                                        + "you have and leave you the walk home to "
                                        + "think about it.",
                                Map.of("education", 4, "stress", 6),
                                Map.of("wealth", -12, "health", -10, "stress", 12),
                                Map.of(),
                                Map.of(),
                                List.of(),
                                List.of(),
                                List.of()
                        ),

                        new Choice(
                                "Find out who they answer to",
                                "You do not go home. You follow the question up one "
                                        + "level and then another, and by the end of "
                                        + "the month you know a name that most people "
                                        + "in " + name + " only half believe exists. "
                                        + "Knowing it is not free.",
                                Map.of("stress", 12, "education", 6, "morality", -6),
                                Map.of("shadowNetwork", 18, "court", -4),
                                List.of(),
                                List.of("used_shadow_contacts", "city_crime_joined")
                        ),

                        new Choice(
                                "Report the street to the watch",
                                "You give names and locations to men who write them "
                                        + "down. Something is actually done, for once, "
                                        + "and the street is walkable again. The people "
                                        + "who ran it are not gone, only inconvenienced, "
                                        + "and they were told who inconvenienced them.",
                                Map.of("reputation", 8, "stress", 10),
                                Map.of("court", 10, "shadowNetwork", -18, "commonPeople", 6),
                                List.of(),
                                List.of("city_crime_broken")
                        )
                )
        );
    }

    private static GameEvent theQuarterThatPays(City city) {
        return new GameEvent(
                "The Quarter That Pays",

                "There is an arrangement in " + city.getName() + " that everyone "
                        + "of standing knows about and nobody of standing "
                        + "describes. The money is real, the protection is real, "
                        + "and this year somebody has decided you are senior "
                        + "enough to be offered a share of both.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Take the share",
                                "The money arrives quarterly and never has a name on "
                                        + "it. You are careful, and being careful about "
                                        + "it every quarter for years is its own slow "
                                        + "cost.",
                                Map.of("wealth", 25, "morality", -16, "stress", 8),
                                Map.of("shadowNetwork", 20, "court", -8, "commonPeople", -10),
                                List.of(),
                                List.of("city_crime_joined", "used_shadow_contacts")
                        ),

                        new Choice(
                                "Break it, publicly",
                                "politicalPower",
                                55,
                                "You take it apart in daylight, with records, in front "
                                        + "of people who cannot afford to look away. "
                                        + "The quarter is quieter for a decade and you "
                                        + "are watched for the rest of your life.",
                                "You move against it and it moves first. The records "
                                        + "you were relying on are not where you left "
                                        + "them, and you are the one who looks like a "
                                        + "man settling a score.",
                                Map.of("reputation", 20, "morality", 12, "stress", 18),
                                Map.of("reputation", -14, "stress", 22, "politicalPower", -12),
                                Map.of("commonPeople", 20, "court", 12, "shadowNetwork", -30),
                                Map.of("shadowNetwork", -20, "court", -10),
                                List.of(),
                                List.of("city_crime_broken", "protected_commoners"),
                                List.of()
                        ),

                        new Choice(
                                "Decline and say nothing",
                                "You say no in a way that costs nobody anything, and "
                                        + "the offer is not repeated. You keep your "
                                        + "hands clean and the arrangement keeps "
                                        + "running, which are both true at once.",
                                Map.of("morality", 6, "stress", 4),
                                Map.of(),
                                List.of(),
                                List.of()
                        )
                )
        );
    }

    // ---- learning --------------------------------------------------------

    private static GameEvent theCopyists(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Copyists of " + name,

                "The copy-houses of " + name + " run through the night and take "
                        + "anyone who can hold a pen straight. The pay is "
                        + "insulting. What you are actually being offered is "
                        + "every book that passes through your hands.",

                "Youth",

                List.of(
                        new Choice(
                                "Take the work and read everything",
                                "education",
                                35,
                                "You copy badly for a month and well for a year, and "
                                        + "you read all of it. It is the cheapest "
                                        + "education anyone in this city will ever get "
                                        + "and you are one of the few who noticed.",
                                "The hours break you before the learning takes. You "
                                        + "leave with a cramped hand and a few pages "
                                        + "you can still recite.",
                                Map.of("education", 20, "wealth", -4, "stress", 10),
                                Map.of("education", 6, "health", -8, "stress", 12),
                                Map.of("scholars", 14),
                                Map.of("scholars", 4),
                                List.of(),
                                List.of("faithful_copyist"),
                                List.of()
                        ),

                        new Choice(
                                "Sell better copies under a better name",
                                "You learn whose hand sells and you learn to do it. "
                                        + "The work is good. The attribution is not, "
                                        + "and there are men in this city who would "
                                        + "know the difference if they looked.",
                                Map.of("wealth", 16, "education", 10, "morality", -14),
                                Map.of("merchants", 10, "scholars", -12),
                                List.of(),
                                List.of("corrupted_a_text")
                        )
                )
        );
    }

    private static GameEvent theChair(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Chair at " + name,

                "A teaching chair has come open, and " + name + " being what it "
                        + "is, the shortlist is long and well-connected. You are "
                        + "on it. So is somebody with a worse mind and a better "
                        + "family.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Win it on the argument",
                                "education",
                                65,
                                "You take the disputation apart in front of everyone "
                                        + "who matters, politely. There is no way to "
                                        + "award it to anyone else afterwards, which "
                                        + "is precisely what several people wanted to "
                                        + "do.",
                                "You are better and it does not matter, because you "
                                        + "are better in a room that had already "
                                        + "decided. You are thanked for a stimulating "
                                        + "contribution.",
                                Map.of("education", 12, "reputation", 18, "politicalPower", 10),
                                Map.of("stress", 14, "reputation", -6),
                                Map.of("scholars", 22, "court", 8),
                                Map.of("scholars", 6, "nobles", -8),
                                List.of(),
                                List.of("known_for_fairness", "city_scholars_endowed"),
                                List.of()
                        ),

                        new Choice(
                                "Endow the chair and take it",
                                "You pay for the chair you then sit in. It is legal, "
                                        + "it is common, and it is the first thing "
                                        + "anyone mentions about you in that hall for "
                                        + "twenty years.",
                                Map.of("wealth", -25, "politicalPower", 12, "reputation", 6),
                                Map.of("scholars", 8, "nobles", 10, "commonPeople", -6),
                                List.of(),
                                List.of("city_scholars_endowed")
                        )
                )
        );
    }

    // ---- good years and bad ----------------------------------------------

    private static GameEvent theGoodYears(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Good Years in " + name,

                "Everything is arriving at once — grain, silk, silver, people. "
                        + name + " has not been this loud in living memory and "
                        + "the price of everything is moving daily. Good years "
                        + "do not announce how long they intend to last.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Put everything into the caravan trade",
                                "wealth",
                                45,
                                "You are in early and heavy, and the boom does what "
                                        + "booms do for the people who were early and "
                                        + "heavy. The money changes what rooms you are "
                                        + "invited into.",
                                "You are in late and heavy. The boom turns while your "
                                        + "goods are still on the road, and you spend "
                                        + "years explaining that to people you owe.",
                                Map.of("wealth", 30, "reputation", 10, "stress", 8),
                                Map.of("wealth", -25, "stress", 20, "reputation", -8),
                                Map.of("merchants", 20),
                                Map.of("merchants", -14),
                                List.of(),
                                List.of("first_caravan_paid", "city_trade_opened"),
                                List.of()
                        ),

                        new Choice(
                                "Build something that outlasts the boom",
                                "You put it into a caravanserai, a well and a wall. "
                                        + "None of it makes what the trade would have "
                                        + "made. All of it is still standing when the "
                                        + "trade has moved to another city.",
                                Map.of("wealth", -22, "reputation", 16, "morality", 10),
                                Map.of("commonPeople", 18, "merchants", 10),
                                List.of(),
                                List.of("endowed_a_caravanserai", "city_trade_opened")
                        )
                )
        );
    }

    private static GameEvent theHungryQuarter(City city) {
        String name = city.getName();

        return new GameEvent(
                "The Hungry Quarter",

                "The bad year in " + name + " has stopped being a bad year and "
                        + "become the way things are. The granaries are not "
                        + "empty — they are simply not opening — and everyone in "
                        + "the lower quarter knows exactly which building their "
                        + "hunger is stored in.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Open a granary, whoever's it is",
                                "politicalPower",
                                50,
                                "The doors come open and the grain goes out and "
                                        + "nobody dies in the queue, which took more "
                                        + "arranging than the grain did. The owners are "
                                        + "compensated eventually and loudly.",
                                "You force it and it goes wrong in front of everyone. "
                                        + "The grain is taken rather than given, and "
                                        + "the difference between those two words is "
                                        + "the rest of your reputation.",
                                Map.of("reputation", 18, "morality", 14, "wealth", -12),
                                Map.of("reputation", -12, "stress", 18, "politicalPower", -10),
                                Map.of("commonPeople", 24, "nobles", -14),
                                Map.of("commonPeople", 8, "nobles", -20, "court", -12),
                                List.of(),
                                List.of("city_granary_opened", "fed_people_during_riot"),
                                List.of("city_granary_opened")
                        ),

                        new Choice(
                                "Buy grain now and sell it in the spring",
                                "You buy at the bottom and hold. In the spring you are "
                                        + "considerably richer and the quarter is "
                                        + "considerably thinner, and both of those "
                                        + "facts have your name on them.",
                                Map.of("wealth", 28, "morality", -18, "reputation", -12),
                                Map.of("merchants", 16, "commonPeople", -24),
                                List.of(),
                                List.of("profited_from_famine")
                        ),

                        new Choice(
                                "Feed who you can out of your own house",
                                "You cook for the street until you cannot afford to "
                                        + "any more, which is sooner than you hoped. It "
                                        + "does not fix anything. It is remembered "
                                        + "longer than several things that did.",
                                Map.of("wealth", -14, "morality", 12, "reputation", 10),
                                Map.of("commonPeople", 16),
                                List.of(),
                                List.of("named_the_hungry")
                        )
                )
        );
    }

    private static GameEvent theOrdinaryYear(City city) {
        String name = city.getName();

        return new GameEvent(
                "An Ordinary Year in " + name,

                "Nothing is happening to " + name + " this year. The gates open "
                        + "on time, the price of bread is the price of bread, and "
                        + "the loudest argument in the street is about a wall "
                        + "somebody built eighteen inches too far out. Ordinary "
                        + "years are when positions are quietly taken.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Make yourself useful to the city itself",
                                "You spend the quiet year on drains, weights and the "
                                        + "grain register — unglamorous things that "
                                        + "several hundred people depend on without "
                                        + "knowing your name. Some of them find out "
                                        + "later.",
                                Map.of("reputation", 10, "politicalPower", 8, "stress", 6),
                                Map.of("commonPeople", 12, "court", 8),
                                List.of(),
                                List.of("city_trade_opened")
                        ),

                        new Choice(
                                "Use the quiet to build your own position",
                                "You spend the year on the people who decide things "
                                        + "rather than the things being decided. It is "
                                        + "time badly spent for the city and extremely "
                                        + "well spent for you.",
                                Map.of("politicalPower", 14, "wealth", 8, "morality", -6),
                                Map.of("court", 14, "nobles", 8, "commonPeople", -6),
                                List.of(),
                                List.of()
                        )
                )
        );
    }

    // ---- leaving ---------------------------------------------------------

    private static GameEvent theRoadOut(City here, City there) {
        return new GameEvent(
                "The Road Out of " + here.getName(),

                "There is a caravan forming for " + there.getName() + ", which "
                        + "by every account anyone brings back is "
                        + there.condition().displayName().toLowerCase(Locale.ROOT)
                        + " while " + here.getName() + " is "
                        + here.condition().displayName().toLowerCase(Locale.ROOT)
                        + ". You have a month to decide whether that is a reason "
                        + "or an excuse.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Go to " + there.getName(),
                                "You go. The road takes what roads take and you arrive "
                                        + "knowing nobody, which is the point and also "
                                        + "the cost. Everything you had built in "
                                        + here.getName() + " stays in "
                                        + here.getName() + ".",
                                Map.of("wealth", -12, "stress", 10, "politicalPower", -10),
                                Map.of("court", -8, "commonPeople", -8, "merchants", 6),
                                List.of(),
                                List.of(travelFlagFor(there))
                        ),

                        new Choice(
                                "Stay where your name already means something",
                                "You let the caravan go. What you have in "
                                        + here.getName() + " took years to build and "
                                        + "does not travel, and you would rather be "
                                        + "someone here than nobody there.",
                                Map.of("politicalPower", 6, "stress", -4),
                                Map.of("commonPeople", 6, "court", 4),
                                List.of(),
                                List.of()
                        )
                )
        );
    }
}
