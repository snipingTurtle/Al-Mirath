package com.example.al_mirath.service;

import com.example.al_mirath.model.Activity;
import com.example.al_mirath.model.PlayerCharacter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything a character can decide to do with a year.
 *
 * <p>The catalogue is deliberately wide rather than deep: a life should offer
 * more than one honest way to raise learning, and a player who cannot read
 * should still have somewhere to put a year. Most entries that ask for real
 * skill carry a mini-game, so the difference between a scholar and someone
 * who owns books is something the player has to demonstrate.
 *
 * <p>Ages gate almost everything, which is what makes ageing up feel like
 * progress: the menu a seven-year-old sees is not the menu a vizier sees.
 */
public final class ActivityLibrary {

    private static final List<Activity> ALL = buildCatalogue();

    private ActivityLibrary() {
    }

    public static List<Activity> all() {
        return ALL;
    }

    public static Activity byId(String id) {
        for (Activity activity : ALL) {
            if (activity.id().equals(id)) {
                return activity;
            }
        }

        return null;
    }

    /**
     * Everything the character could plausibly see, grouped for the menu.
     *
     * <p>Locked entries are kept — a greyed row reading "Not until you are 18"
     * is a goal, while a hidden row is a feature the player never learns
     * exists. Entries locked purely by age are dropped once they are decades
     * away, so a child is not shown the whole of adult life at once.
     */
    public static Map<String, List<Activity>> menuFor(PlayerCharacter player) {
        Map<String, List<Activity>> grouped = new LinkedHashMap<>();

        for (String category : Activity.CATEGORY_ORDER) {
            List<Activity> inCategory = new ArrayList<>();

            for (Activity activity : ALL) {
                if (!activity.category().equals(category)) {
                    continue;
                }

                if (!worthShowing(activity, player)) {
                    continue;
                }

                inCategory.add(activity);
            }

            if (!inCategory.isEmpty()) {
                grouped.put(category, inCategory);
            }
        }

        return grouped;
    }

    /** How far ahead of the character an age-locked activity may be shown. */
    private static final int LOOKAHEAD_YEARS = 6;

    private static boolean worthShowing(Activity activity, PlayerCharacter player) {
        if (player == null) {
            return false;
        }

        if (player.getAge() > activity.maxAge()) {
            return false;
        }

        return player.getAge() + LOOKAHEAD_YEARS >= activity.minAge();
    }

    // ── The catalogue ───────────────────────────────────────────────────

    private static List<Activity> buildCatalogue() {
        List<Activity> catalogue = new ArrayList<>();

        faith(catalogue);
        knowledge(catalogue);
        trade(catalogue);
        arms(catalogue);
        court(catalogue);
        household(catalogue);
        leisure(catalogue);
        shadow(catalogue);

        return List.copyOf(catalogue);
    }

    // ── Faith ───────────────────────────────────────────────────────────

    private static void faith(List<Activity> out) {
        out.add(Activity.of("pray", "Keep the five prayers", Activity.CAT_FAITH)
                .describe("A year of them, at their times, whatever else the year held.")
                .from(6)
                .stat("morality", 2).stat("stress", -6)
                .won("You kept the prayers through the year. Little else was steady, but that was.")
                .build());

        out.add(Activity.of("memorise", "Memorise a portion of the Qur'an", Activity.CAT_FAITH)
                .describe("Sit with a teacher and hold what you are given.")
                .from(6)
                .trial("scribe", 2)
                .stat("education", 4).stat("morality", 3)
                .faction("scholars", 3)
                .statOnFailure("stress", 4)
                .won("You held the passage clean through, and the teacher marked the page.")
                .lost("The words slipped. The teacher marks nothing and tells you to come back.")
                .build());

        out.add(Activity.of("alms", "Give alms", Activity.CAT_FAITH)
                .describe("Quietly, and to people who will not be able to repay you.")
                .from(8).costs(50)
                .stat("morality", 4).stat("reputation", 2)
                .faction("commonPeople", 5)
                .won("The money went where it was needed, and the quarter noticed who sent it.")
                .build());

        out.add(Activity.of("fast_extra", "Keep the voluntary fasts", Activity.CAT_FAITH)
                .describe("Beyond what is required. It is hard on the body and quiet on the mind.")
                .from(12)
                .stat("morality", 4).stat("stress", -5).stat("health", -2)
                .faction("scholars", 2)
                .won("A thinner year, and a steadier one.")
                .build());

        out.add(Activity.of("preach", "Speak at the Friday gathering", Activity.CAT_FAITH)
                .describe("The pulpit is not given to everyone, and it is not forgiving.")
                .from(20).needs("morality", 55)
                .trial("orator", 3)
                .stat("reputation", 6).stat("politicalPower", 3)
                .faction("commonPeople", 6).faction("scholars", 4)
                .statOnFailure("reputation", -4)
                .factionOnFailure("scholars", -3)
                .won("You held them. Afterwards men repeated your phrasing to each other in the street.")
                .lost("You lost the thread halfway, and a hundred people watched you find it again.")
                .build());

        out.add(Activity.of("hajj", "Make the pilgrimage", Activity.CAT_FAITH)
                .describe("Months on the road to Mecca. It costs a fortune and changes what your name means.")
                .from(18).costs(1200).once()
                .trial("caravan", 3)
                .stat("morality", 12).stat("reputation", 10).stat("stress", -12)
                .faction("commonPeople", 8).faction("scholars", 6)
                .statOnFailure("health", -10).statOnFailure("stress", 12)
                .flag("made_pilgrimage")
                .won("You came back with the title of Hajji and the particular quiet of someone who has done the thing they meant to do.")
                .lost("The road broke you before Mecca did. You turned back with the money spent and nothing to show.")
                .build());
    }

    // ── Knowledge ───────────────────────────────────────────────────────

    private static void knowledge(List<Activity> out) {
        out.add(Activity.of("maktab", "Sit in the maktab", Activity.CAT_KNOWLEDGE)
                .describe("Letters, numbers, and a teacher with a cane.")
                .ages(5, 14)
                .stat("education", 5).stat("stress", 2)
                .won("Another year of letters. You can read a little more than you could.")
                .build());

        out.add(Activity.of("recite", "Recite before the class", Activity.CAT_KNOWLEDGE)
                .describe("Stand up and get the metre right in front of everybody.")
                .ages(7, 20)
                .trial("prosody", 2)
                .stat("education", 4).stat("reputation", 3)
                .statOnFailure("stress", 5).statOnFailure("reputation", -2)
                .won("You got it right, and the room knew it.")
                .lost("You stumbled on the metre. Somebody behind you laughed.")
                .build());

        out.add(Activity.of("copy_manuscript", "Copy a manuscript", Activity.CAT_KNOWLEDGE)
                .describe("Careful work, paid by the page, and nobody pays for a page with a mistake on it.")
                .from(10)
                .trial("scribe", 3).pays(60, 0)
                .stat("education", 4)
                .faction("scholars", 4)
                .statOnFailure("stress", 5)
                .won("The copy was clean. The stationer paid without arguing.")
                .lost("Two lines wrong on the fourth page. The sheet is wasted and so is the day.")
                .build());

        out.add(Activity.of("madrasa", "Attend the madrasa", Activity.CAT_KNOWLEDGE)
                .describe("Law, grammar, and argument, taught by men who enjoy winning.")
                .from(12)
                .stat("education", 6).stat("stress", 3)
                .faction("scholars", 4)
                .won("A year of lectures. Your notes are thicker and your opinions are worse defended than you think.")
                .build());

        out.add(Activity.of("geometry", "Study geometry and the astrolabe", Activity.CAT_KNOWLEDGE)
                .describe("Proportion, the stars, and instruments that only work if you are exact.")
                .from(11)
                .trial("geometer", 3)
                .stat("education", 6)
                .faction("scholars", 4)
                .statOnFailure("stress", 4)
                .won("The proof closed. You sat back and understood why it had to.")
                .lost("The figure would not resolve. You put the compasses down before you snapped them.")
                .build());

        out.add(Activity.of("medicine", "Study the humours", Activity.CAT_KNOWLEDGE)
                .describe("Herbs, pulses, and the difference between a patient who will live and one who will not.")
                .from(14).needs("education", 30)
                .trial("physician", 3)
                .stat("education", 6).stat("morality", 2)
                .faction("scholars", 3).faction("commonPeople", 3)
                .statOnFailure("stress", 5)
                .won("You read the case right. The teacher said nothing, which from him is praise.")
                .lost("You read the case wrong, and were told exactly how wrong in front of everyone.")
                .build());

        out.add(Activity.of("debate", "Debate in the scholars' circle", Activity.CAT_KNOWLEDGE)
                .describe("Public, minuted, and remembered by people who write things down.")
                .from(16).needs("education", 40)
                .trial("orator", 3)
                .stat("reputation", 6).stat("education", 3)
                .faction("scholars", 7)
                .statOnFailure("reputation", -4)
                .factionOnFailure("scholars", -5)
                .flag("won_public_debate")
                .won("You took the argument apart in public, politely, and the circle noticed.")
                .lost("You were taken apart in public, politely, and the circle noticed that too.")
                .build());

        out.add(Activity.of("treatise", "Write a treatise", Activity.CAT_KNOWLEDGE)
                .describe("A year's work that will outlive you, or be used to wrap fish.")
                .from(24).needs("education", 60)
                .trial("scribe", 4)
                .stat("education", 8).stat("reputation", 9).stat("stress", 6)
                .faction("scholars", 10).faction("court", 3)
                .statOnFailure("stress", 8)
                .flag("wrote_treatise")
                .won("It was copied. That is the only measure that matters, and it was copied.")
                .lost("You finished it. Nobody copied it. It sits in your own house, in your own hand.")
                .build());

        out.add(Activity.of("teach_maktab", "Teach the neighbourhood children", Activity.CAT_KNOWLEDGE)
                .describe("Twenty of them, none of whom want to be there.")
                .from(18).needs("education", 45)
                .pays(40, 0)
                .stat("education", 2).stat("reputation", 3).stat("stress", 3)
                .faction("commonPeople", 6)
                .won("A year of letters drilled into children who will remember your name longer than you expect.")
                .build());
    }

    // ── Trade ───────────────────────────────────────────────────────────

    private static void trade(List<Activity> out) {
        out.add(Activity.of("run_errands", "Run errands in the bazaar", Activity.CAT_TRADE)
                .describe("Carry, fetch, wait, and be paid in small coin.")
                .ages(7, 16)
                .pays(25, 0)
                .stat("health", 1).stat("wealth", 1)
                .faction("merchants", 2)
                .won("A year of other people's parcels, and a purse that is not empty.")
                .build());

        out.add(Activity.of("haggle", "Haggle in the bazaar", Activity.CAT_TRADE)
                .describe("Find the seller's floor without insulting him into walking away.")
                .from(9)
                .trial("haggle", 2).pays(90, -20)
                .stat("wealth", 3)
                .faction("merchants", 3)
                .statOnFailure("reputation", -2)
                .won("You got it under his floor, and he shook your hand anyway.")
                .lost("He put the cloth back on the shelf and looked past you at the next customer.")
                .build());

        out.add(Activity.of("sell_souk", "Keep a stall at the souk", Activity.CAT_TRADE)
                .describe("A day of small sums, and every one of them has to add up.")
                .from(12)
                .trial("merchant", 3).pays(160, -40)
                .stat("wealth", 4)
                .faction("merchants", 4)
                .won("The takings balanced to the dirham and there was something left over.")
                .lost("The takings did not balance. Somewhere in the day you gave away more than you sold.")
                .build());

        out.add(Activity.of("caravan_share", "Buy a share in a caravan", Activity.CAT_TRADE)
                .describe("Pick the road. Water, bandits, and the passes decide the rest.")
                .from(16).costs(250)
                .trial("caravan", 3).pays(900, 0)
                .stat("wealth", 7)
                .faction("merchants", 6)
                .statOnFailure("stress", 8).statOnFailure("wealth", -4)
                .won("The caravan came in whole. Your share was worth more than the whole of last year.")
                .lost("The caravan came in thin, and your share of thin is nothing.")
                .build());

        out.add(Activity.of("ledgers", "Go through your ledgers", Activity.CAT_TRADE)
                .describe("Dull, necessary, and it always finds something.")
                .from(14)
                .trial("merchant", 2).pays(70, 0)
                .stat("wealth", 3).stat("stress", 3)
                .won("Two errors in your own favour and one against. Corrected, and noted who made them.")
                .lost("The columns would not reconcile. You closed the book rather than find out why.")
                .build());

        out.add(Activity.of("lend", "Lend money at interest", Activity.CAT_TRADE)
                .describe("Profitable, and not everyone will look at you the same way afterwards.")
                .from(18).costs(300).pays(430, 0)
                .stat("wealth", 5).stat("morality", -5)
                .faction("merchants", 4).faction("scholars", -3)
                .flag("lent_at_interest")
                .won("The debt came back with its increase. Two of the men who paid it will not greet you now.")
                .build());

        out.add(Activity.of("commission_workshop", "Commission work from a workshop", Activity.CAT_TRADE)
                .describe("Put money behind a craftsman and take a cut of what he makes.")
                .from(20).costs(450).pays(700, -100)
                .trial("haggle", 3)
                .stat("wealth", 6)
                .faction("merchants", 5).faction("commonPeople", 3)
                .won("The terms were yours and the work was good. That is the whole of the trade.")
                .lost("You paid his price, and his price was the reason he agreed so quickly.")
                .build());
    }

    // ── Arms ────────────────────────────────────────────────────────────

    private static void arms(List<Activity> out) {
        out.add(Activity.of("wrestle", "Wrestle in the yard", Activity.CAT_MILITARY)
                .describe("Boys, dust, and a ring of people shouting.")
                .ages(8, 30)
                .stat("health", 4).stat("stress", -4)
                .faction("commonPeople", 2)
                .won("You came home filthy and slept properly for the first time in a month.")
                .build());

        out.add(Activity.of("archery", "Train at the butts", Activity.CAT_MILITARY)
                .describe("The bow does not care who your father was.")
                .from(10)
                .trial("archery", 2)
                .stat("health", 4).stat("reputation", 2)
                .faction("military", 4)
                .statOnFailure("stress", 3)
                .won("Three in the gold. The captain watching said nothing and remembered your face.")
                .lost("You could not find the mark all afternoon. Your arm knew it before you did.")
                .build());

        out.add(Activity.of("drill", "Drill with the garrison", Activity.CAT_MILITARY)
                .describe("Formation, footwork, and doing it again until it is not thinking.")
                .from(15)
                .trial("courier", 3)
                .stat("health", 5).stat("stress", 3)
                .faction("military", 6)
                .statOnFailure("health", -3)
                .won("You held the line through every turn of it. The sergeant moved you up the file.")
                .lost("You broke the line twice. The sergeant made the whole file run it again because of you.")
                .build());

        out.add(Activity.of("hunt", "Hunt with the falcon", Activity.CAT_MILITARY)
                .describe("A noble's sport, and a good place to be seen by nobles.")
                .from(14).costs(60)
                .trial("archery", 3)
                .stat("health", 3).stat("reputation", 4).stat("stress", -5)
                .faction("nobles", 5)
                .won("You took the hare cleanly in front of men whose opinion carries.")
                .lost("You missed twice while a governor's son watched, and he was kind about it.")
                .build());

        out.add(Activity.of("frontier", "Ride the frontier patrol", Activity.CAT_MILITARY)
                .describe("Weeks in the saddle on the edge of the map. People do not always come back.")
                .from(18).needs("health", 45)
                .trial("archery", 4).pays(200, 0)
                .stat("health", 4).stat("reputation", 7).stat("politicalPower", 4)
                .faction("military", 9).faction("court", 3)
                .statOnFailure("health", -14).statOnFailure("stress", 10)
                .flag("rode_the_frontier")
                .won("You came back with the patrol whole and a reputation you did not have in spring.")
                .lost("You came back carried. The wound will close; the year will not come back.")
                .build());

        out.add(Activity.of("teach_arms", "Train the young in arms", Activity.CAT_MILITARY)
                .describe("Pass on what kept you alive to people who do not yet believe they need it.")
                .from(28).needs("health", 40)
                .pays(90, 0)
                .stat("reputation", 4).stat("health", 2)
                .faction("military", 6).faction("commonPeople", 3)
                .won("Twenty boys who can hold a line now, because of a year of your shouting.")
                .build());
    }

    // ── Court ───────────────────────────────────────────────────────────

    private static void court(List<Activity> out) {
        out.add(Activity.of("attend_audience", "Attend the ruler's audience", Activity.CAT_COURT)
                .describe("Stand where you can be seen, and say the right amount of nothing.")
                .from(16).needs("reputation", 25)
                .trial("orator", 3)
                .stat("politicalPower", 5).stat("reputation", 3)
                .faction("court", 6).faction("nobles", 3)
                .statOnFailure("politicalPower", -3)
                .factionOnFailure("court", -4)
                .won("You were addressed by name. In that room, that is the whole of the achievement.")
                .lost("You spoke half a sentence too long, and the room's attention moved on without you.")
                .build());

        out.add(Activity.of("patron", "Cultivate a patron", Activity.CAT_COURT)
                .describe("Gifts, visits, and patience. It is an investment, not a friendship.")
                .from(17).costs(150)
                .stat("politicalPower", 4)
                .faction("nobles", 6).faction("court", 3)
                .won("He takes your visits now without being asked. That took a year and it was cheap at the price.")
                .build());

        out.add(Activity.of("petition", "Petition the governor", Activity.CAT_COURT)
                .describe("Ask for something in front of people who will remember that you asked.")
                .from(18)
                .trial("orator", 4)
                .stat("politicalPower", 6).stat("reputation", 5)
                .faction("court", 7).faction("commonPeople", 4)
                .statOnFailure("reputation", -5).statOnFailure("stress", 6)
                .factionOnFailure("court", -5)
                .won("The petition was granted, and half the quarter knows who asked for it.")
                .lost("It was refused, in public, at length. You will be some time living that down.")
                .build());

        out.add(Activity.of("bribe", "Grease a clerk's palm", Activity.CAT_COURT)
                .describe("Fast, effective, and the sort of thing that comes back years later.")
                .from(17).costs(250)
                .stat("politicalPower", 6).stat("morality", -7)
                .faction("court", 6).faction("shadowNetwork", 4)
                .flag("used_bribery")
                .won("The paper moved to the top of the pile. Nobody will ever mention why — until somebody does.")
                .build());

        out.add(Activity.of("majlis", "Host a majlis", Activity.CAT_COURT)
                .describe("An evening of poets, jurists and people worth knowing, at your expense.")
                .from(22).costs(400)
                .stat("reputation", 7).stat("politicalPower", 3).stat("stress", -4)
                .faction("nobles", 5).faction("scholars", 5).faction("court", 3)
                .flag("hosted_majlis")
                .won("The right people came, stayed late, and argued well. Your house has a character now.")
                .build());

        out.add(Activity.of("arbitrate", "Arbitrate a dispute", Activity.CAT_COURT)
                .describe("Two families, one wall, and forty years of grievance about it.")
                .from(25).needs("reputation", 45)
                .trial("orator", 4)
                .stat("reputation", 6).stat("morality", 4).stat("politicalPower", 3)
                .faction("commonPeople", 7).faction("familyCouncil", 4)
                .statOnFailure("reputation", -5).statOnFailure("stress", 7)
                .flag("fair_scholar_judgment")
                .won("Both sides left unhappy in equal measure, which is what a good judgment looks like.")
                .lost("One side left triumphant. That is how you know you got it wrong.")
                .build());
    }

    // ── Household ───────────────────────────────────────────────────────

    private static void household(List<Activity> out) {
        out.add(Activity.of("family_evening", "Spend the evenings with your household", Activity.CAT_FAMILY)
                .describe("Nothing happens. That is rather the point of it.")
                .from(4)
                .stat("familyLoyalty", 5).stat("stress", -6)
                .faction("familyCouncil", 4)
                .won("A year of ordinary evenings, which is the kind nobody writes down and everybody misses.")
                .build());

        out.add(Activity.of("teach_child", "Teach your children", Activity.CAT_FAMILY)
                .describe("Letters, trade, or arms — whatever you have that is worth handing on.")
                .from(20).needsChild()
                .stat("familyLoyalty", 7).stat("education", 2)
                .faction("familyCouncil", 6)
                .won("They have something from you now that does not depend on your surviving.")
                .build());

        out.add(Activity.of("visit_graves", "Visit the graves of your dead", Activity.CAT_FAMILY)
                .describe("Sit with them for an afternoon. It settles something.")
                .from(10)
                .stat("morality", 3).stat("stress", -5).stat("familyLoyalty", 3)
                .won("You sat until the afternoon went cold, and walked back lighter than you came.")
                .build());

        out.add(Activity.of("settle_feud", "Settle a family quarrel", Activity.CAT_FAMILY)
                .describe("Somebody has to say the first sentence, and nobody wants it to be them.")
                .from(18)
                .trial("orator", 3)
                .stat("familyLoyalty", 8).stat("stress", -3)
                .faction("familyCouncil", 8)
                .statOnFailure("familyLoyalty", -5).statOnFailure("stress", 6)
                .factionOnFailure("familyCouncil", -5)
                .won("They are speaking again. It took the whole evening and it held.")
                .lost("You said the first sentence and it was the wrong one. It is worse now than it was.")
                .build());

        out.add(Activity.of("household_repairs", "Put the house in order", Activity.CAT_FAMILY)
                .describe("Roof, cistern, and the door that has not shut properly for two years.")
                .from(16).costs(80)
                .stat("familyLoyalty", 4).stat("stress", -4).stat("health", 2)
                .won("The door shuts. You are unreasonably pleased about the door.")
                .build());
    }

    // ── Leisure ─────────────────────────────────────────────────────────

    private static void leisure(List<Activity> out) {
        out.add(Activity.of("garden", "Walk in the garden", Activity.CAT_LEISURE)
                .describe("Water, shade, and an hour where nobody wants anything from you.")
                .from(4)
                .stat("stress", -5).stat("health", 1)
                .won("An hour a day of it, all year. It is the cheapest thing that works.")
                .build());

        out.add(Activity.of("bathhouse", "Take the baths", Activity.CAT_LEISURE)
                .describe("Steam, gossip, and everybody's business discussed at volume.")
                .from(6).costs(15)
                .stat("stress", -7).stat("health", 3).stat("reputation", 1)
                .faction("commonPeople", 2)
                .won("You came out clean and knowing four things you had no business knowing.")
                .build());

        out.add(Activity.of("poets", "Sit with the poets", Activity.CAT_LEISURE)
                .describe("Listen, and when the line comes round to you, do not embarrass yourself.")
                .from(8)
                .trial("prosody", 2)
                .stat("reputation", 4).stat("education", 2).stat("stress", -4)
                .faction("nobles", 2).faction("scholars", 2)
                .statOnFailure("reputation", -2)
                .won("Your line landed. Somebody repeated it later without crediting you, which is fame.")
                .lost("Your line did not scan and the man beside you finished it for you.")
                .build());

        out.add(Activity.of("shatranj", "Play shatranj", Activity.CAT_LEISURE)
                .describe("The board is honest. That is why people who are not enjoy it.")
                .from(8)
                .trial("geometer", 2)
                .stat("education", 3).stat("stress", -4)
                .faction("nobles", 2)
                .won("You saw it four moves out and he did not. Nothing else that week felt as good.")
                .lost("He saw it four moves out and you did not.")
                .build());

        out.add(Activity.of("gamble", "Wager at the dice", Activity.CAT_LEISURE)
                .describe("Forbidden, common, and never quite as harmless as it looks.")
                .from(13).costs(60)
                .trial("haggle", 3).pays(220, 0)
                .stat("stress", -3).stat("morality", -3)
                .faction("shadowNetwork", 3)
                .statOnFailure("stress", 6).statOnFailure("wealth", -3)
                .won("You walked away while you were ahead, which is the only skill the game has.")
                .lost("You did not walk away while you were ahead.")
                .build());

        out.add(Activity.of("feast", "Hold a feast for the quarter", Activity.CAT_LEISURE)
                .describe("Everyone eats, everyone remembers, and it costs what it costs.")
                .from(20).costs(250)
                .stat("reputation", 6).stat("stress", -5)
                .faction("commonPeople", 8).faction("merchants", 3)
                .won("Half the quarter ate at your expense and will say so for years.")
                .build());

        out.add(Activity.of("travel_study", "Travel to hear a famous teacher", Activity.CAT_LEISURE)
                .describe("Months on the road for a few weeks in a room with someone worth hearing.")
                .from(16).costs(200)
                .trial("caravan", 2)
                .stat("education", 7).stat("reputation", 3).stat("stress", 4)
                .faction("scholars", 6)
                .statOnFailure("stress", 8).statOnFailure("health", -4)
                .won("You got there, you heard him, and you are not the same about the subject.")
                .lost("You got most of the way there, and then the road and the money both ran out.")
                .build());
    }

    // ── Shadow ──────────────────────────────────────────────────────────

    private static void shadow(List<Activity> out) {
        out.add(Activity.of("pick_pockets", "Work the crowd", Activity.CAT_SHADOW)
                .describe("Wait for the guard to look elsewhere. Know when to stop.")
                .from(8)
                .trial("lighthand", 2).pays(70, -30)
                .stat("morality", -4)
                .faction("shadowNetwork", 4).faction("commonPeople", -2)
                .statOnFailure("reputation", -5).statOnFailure("health", -4).statOnFailure("stress", 8)
                .factionOnFailure("court", -4)
                .flag("worked_the_crowd")
                .won("Four purses and nobody's hand on your shoulder.")
                .lost("A hand on your shoulder. They took it out of you in the yard behind the guardhouse.")
                .build());

        out.add(Activity.of("smuggle", "Run contraband past the gate", Activity.CAT_SHADOW)
                .describe("The watch turns at the corner. Move then, and not a breath sooner.")
                .from(15)
                .trial("courier", 3).pays(260, -80)
                .stat("wealth", 4).stat("morality", -4).stat("stress", 5)
                .faction("shadowNetwork", 7).faction("merchants", 3)
                .statOnFailure("health", -6).statOnFailure("reputation", -5)
                .factionOnFailure("court", -6)
                .flag("used_shadow_contacts")
                .won("Through the gate and unloaded before dawn. You are known now, in the useful way.")
                .lost("A lantern at the wrong moment. The goods are the gate captain's and so is your name.")
                .build());

        out.add(Activity.of("fence", "Fence goods with no history", Activity.CAT_SHADOW)
                .describe("Everything here belonged to someone. Do not ask who.")
                .from(15)
                .trial("haggle", 3).pays(300, -60)
                .stat("wealth", 5).stat("morality", -5)
                .faction("shadowNetwork", 6).faction("merchants", -3)
                .won("You got a fair price for goods that had no business being fairly priced.")
                .lost("He knew what you had and what it was worth to you. You took what he offered.")
                .build());

        out.add(Activity.of("informers", "Keep informers of your own", Activity.CAT_SHADOW)
                .describe("Small money to a lot of people who see a lot of things.")
                .from(20).costs(200)
                .stat("politicalPower", 6).stat("morality", -3)
                .faction("shadowNetwork", 8).faction("court", 2)
                .flag("keeps_informers")
                .won("You hear things a week before the court does. That week is worth the money.")
                .build());

        out.add(Activity.of("burgle", "Enter a rich man's house at night", Activity.CAT_SHADOW)
                .describe("One room, one window, and a household asleep on the other side of the wall.")
                .from(16)
                .trial("lighthand", 4).pays(900, -120)
                .stat("wealth", 7).stat("morality", -9).stat("stress", 8)
                .faction("shadowNetwork", 9)
                .statOnFailure("health", -10).statOnFailure("reputation", -8).statOnFailure("stress", 14)
                .factionOnFailure("court", -8).factionOnFailure("commonPeople", -5)
                .flag("burgled_a_house")
                .won("Out over the same wall with the strongbox emptied and the house still asleep.")
                .lost("A dog, a shout, and a drop into the street that did your ankle no good at all.")
                .build());
    }
}
