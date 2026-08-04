package com.example.al_mirath.service.arcs;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Arcs of trade: the long road from a first shared cargo to a trading house,
 * and the quieter, uglier war fought between guilds inside the city walls.
 */
public final class MerchantArcs {

    private MerchantArcs() {
    }

    public static void addMerchantCaravanArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "A Share in the Cargo",
                "A caravan master offers you a quarter share in a load of dyed cloth heading "
                        + "east. The margin is excellent. The road crosses two weeks of country "
                        + "where the last three caravans were robbed.",
                "Youth",
                List.of(
                        new Choice(
                                "Put everything you have into the share",
                                "wealth",
                                25,
                                "The caravan arrives intact. Your quarter share returns four times "
                                        + "what you put in, and the caravan master starts using the word "
                                        + "'partner' rather than 'boy'.",
                                "Bandits take the cloth twelve days out. Your entire stake is gone, "
                                        + "and you learn the oldest lesson in trade in the most expensive "
                                        + "possible way.",
                                Map.of("wealth", 25, "politicalPower", 6, "stress", 8),
                                Map.of("wealth", -22, "stress", 16, "reputation", -5),
                                Map.of("merchants", 15),
                                Map.of("merchants", -6),
                                List.of(
                                        ChoiceRequirement.statAtLeast("wealth", 12,
                                                "Locked: Requires Wealth 12.")
                                ),
                                List.of("caravan_arc_started", "first_caravan_paid"),
                                List.of("caravan_arc_started", "first_caravan_lost")
                        ),
                        new Choice(
                                "Take a smaller share and insure it against loss",
                                "You take an eighth instead of a quarter and pay a guarantor to cover "
                                        + "the loss. The return is modest. You are still solvent, which is "
                                        + "more than several of your competitors can say by winter.",
                                Map.of("wealth", 8, "education", 6, "stress", -4),
                                Map.of("merchants", 10),
                                List.of(),
                                List.of("caravan_arc_started", "learned_to_hedge")
                        ),
                        new Choice(
                                "Decline, and sell the caravan master your knowledge of the route instead",
                                "You have no capital, so you sell the only thing you do have: three "
                                        + "years of listening to drivers complain about which wells are dry. "
                                        + "He pays, and remembers that you were worth paying.",
                                Map.of("wealth", 6, "education", 8, "politicalPower", 5),
                                Map.of("merchants", 8, "commonPeople", 4),
                                List.of(),
                                List.of("caravan_arc_started", "sold_knowledge_not_goods")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Toll at the Pass",
                "A minor governor has decided that the mountain pass now carries a toll, "
                        + "payable to him, at a rate that erases your margin entirely. Every "
                        + "caravan on the road faces the same demand.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Pay the toll and raise your prices to cover it",
                                "You pay. Your customers pay. The governor is delighted and doubles "
                                        + "it the following season, having established that the road will bear it.",
                                Map.of("wealth", -12, "stress", 8),
                                Map.of("court", 6, "merchants", -5, "commonPeople", -6),
                                List.of(),
                                List.of("paid_the_toll")
                        ),
                        new Choice(
                                "Organise every trading house into refusing together",
                                "politicalPower",
                                45,
                                "The road empties for six weeks. The governor's revenue collapses "
                                        + "entirely and the toll is quietly withdrawn. The other houses now "
                                        + "know exactly who organised it.",
                                "Two houses break ranks in the first fortnight and take the whole "
                                        + "season's trade. Your warehouse sits full while theirs empty, and "
                                        + "the toll stands.",
                                Map.of("politicalPower", 16, "reputation", 14, "stress", 12),
                                Map.of("wealth", -18, "reputation", -10, "stress", 16),
                                Map.of("merchants", 20, "court", -12),
                                Map.of("merchants", -12, "court", -6),
                                List.of(),
                                List.of("organised_the_houses"),
                                List.of("boycott_collapsed")
                        ),
                        new Choice(
                                "Find the smugglers' route around the pass",
                                "There is always another way through a mountain, and always someone "
                                        + "who will show it to you for a share. Your goods move. Your new "
                                        + "partners are not the kind who forget a debt.",
                                Map.of("wealth", 10, "morality", -8, "stress", 10),
                                Map.of("shadowNetwork", 16, "court", -8, "merchants", 5),
                                List.of(),
                                List.of("used_the_smugglers_road", "used_shadow_contacts")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("caravan_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Letter of Credit",
                "You have enough standing now to issue a suftaja — a letter redeemable for "
                        + "coin in a city a thousand miles away, backed by nothing but the belief "
                        + "that your name is good. The first man to ask for one is asking for a "
                        + "sum larger than you actually hold.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Issue it, and trust the season's returns to cover it",
                                "wealth",
                                45,
                                "The returns land before the letter is presented. Your paper is now "
                                        + "treated as coin from Basra to Cordoba, which is a kind of power "
                                        + "that has nothing to do with what is in your strongroom.",
                                "The letter is presented three weeks before your caravan arrives. You "
                                        + "cannot honour it. Word of a dishonoured suftaja travels faster "
                                        + "than any caravan ever has.",
                                Map.of("wealth", 20, "politicalPower", 14, "stress", 10),
                                Map.of("wealth", -20, "reputation", -20, "stress", 20),
                                Map.of("merchants", 20, "court", 6),
                                Map.of("merchants", -22, "nobles", -8),
                                List.of(),
                                List.of("built_a_credit_network"),
                                List.of("dishonoured_a_letter")
                        ),
                        new Choice(
                                "Issue it only for what you can actually cover today",
                                "You write the letter for the sum you hold and not a dirham more. The "
                                        + "client is irritated. Your name, however, is never once questioned "
                                        + "in forty years of trade.",
                                Map.of("wealth", 8, "reputation", 12, "stress", -6),
                                Map.of("merchants", 14),
                                List.of(),
                                List.of("conservative_banker")
                        ),
                        new Choice(
                                "Refuse, and stay in goods rather than paper",
                                "You want to touch what you own. It is not the path to a great house, "
                                        + "but no ledger will ever destroy you.",
                                Map.of("wealth", 5, "stress", -8),
                                Map.of("merchants", 4),
                                List.of(),
                                List.of("stayed_in_goods")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("caravan_arc_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "What the House Becomes",
                "Your trading house will outlive you. What it does after you are gone "
                        + "depends entirely on what you build into it now, while people still do "
                        + "what you say.",
                "Legacy",
                List.of(
                        new Choice(
                                "Endow a caravanserai on the road you first travelled",
                                "You build the inn where you were once robbed, and you make it free "
                                        + "to any traveller who cannot pay. It stands, and carries your name, "
                                        + "long after the house that funded it has been divided among heirs.",
                                Map.of("wealth", -22, "reputation", 18, "morality", 16),
                                Map.of("commonPeople", 20, "merchants", 10, "scholars", 6),
                                List.of(
                                        ChoiceRequirement.statAtLeast("wealth", 22,
                                                "Locked: Requires Wealth 22.")
                                ),
                                List.of("endowed_a_caravanserai")
                        ),
                        new Choice(
                                "Divide the house cleanly among your heirs",
                                "You split it evenly and in writing, which prevents the feud that has "
                                        + "destroyed every other great house in the district within a "
                                        + "generation of its founder's death.",
                                Map.of("familyLoyalty", 16, "wealth", -8),
                                Map.of("familyCouncil", 18, "merchants", 6),
                                List.of(),
                                List.of("divided_the_house_cleanly")
                        ),
                        new Choice(
                                "Consolidate everything under a single heir to keep it whole",
                                "One heir takes all of it, undivided and formidable. The others take "
                                        + "the resentment, which compounds faster than any interest you ever "
                                        + "charged.",
                                Map.of("politicalPower", 14, "familyLoyalty", -16),
                                Map.of("merchants", 14, "familyCouncil", -14),
                                List.of(),
                                List.of("consolidated_under_one_heir")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("caravan_arc_started"),
                List.of()
        ));
    }

    public static void addGuildWarArc(List<GameEvent> eventPool) {

        eventPool.add(new GameEvent(
                "The Guild Closes Its Books",
                "The guild has voted to stop admitting new masters. The stated reason is "
                        + "quality. The actual reason is that the existing masters would like to "
                        + "keep the work. You are on the wrong side of the vote.",
                "Youth",
                List.of(
                        new Choice(
                                "Work under a master's name until they let you in",
                                "You do the work; he takes the credit and most of the fee. It is "
                                        + "humiliating and it is the fastest road in, and you take it.",
                                Map.of("wealth", 6, "education", 10, "stress", 10, "reputation", -5),
                                Map.of("merchants", 8),
                                List.of(),
                                List.of("guild_war_started", "worked_under_another_name")
                        ),
                        new Choice(
                                "Set up outside the guild entirely and undercut them",
                                "You take work the guild will not touch at prices it cannot match. "
                                        + "Within two years they hate you, and within three they need you.",
                                Map.of("wealth", 14, "politicalPower", 8, "stress", 12),
                                Map.of("merchants", -10, "commonPeople", 12),
                                List.of(),
                                List.of("guild_war_started", "went_independent")
                        ),
                        new Choice(
                                "Organise the excluded craftsmen into a rival body",
                                "You gather every man the guild locked out and give them a structure "
                                        + "of their own. It is not yet a guild. It is already a threat.",
                                Map.of("politicalPower", 14, "reputation", 10, "stress", 12),
                                Map.of("commonPeople", 15, "merchants", -12),
                                List.of(),
                                List.of("guild_war_started", "founded_rival_body")
                        )
                ),
                List.of(),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Fire in the Workshops",
                "Three workshops burned last night, all belonging to one side of the "
                        + "dispute. Nobody believes it was an accident. Everyone is waiting to see "
                        + "whether the answer comes in court or in kind.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Take it to the qadi and demand a legal ruling",
                                "education",
                                45,
                                "You bring a case built on witnesses, ledgers, and precedent. The qadi "
                                        + "rules, compensation is ordered, and the dispute becomes a matter "
                                        + "of record rather than of arson.",
                                "The case collapses on a procedural point you did not anticipate. The "
                                        + "other side reads the ruling as permission.",
                                Map.of("reputation", 14, "morality", 10, "politicalPower", 8),
                                Map.of("reputation", -10, "stress", 16),
                                Map.of("scholars", 12, "court", 10, "merchants", 6),
                                Map.of("merchants", -10, "commonPeople", -5),
                                List.of(),
                                List.of("settled_in_court"),
                                List.of("case_collapsed")
                        ),
                        new Choice(
                                "Answer in kind, and make sure they know it was you",
                                "Two of their workshops burn the following week. The message is "
                                        + "received. So is the fact that you are now a man who sends that "
                                        + "kind of message.",
                                Map.of("politicalPower", 14, "morality", -18, "stress", 14),
                                Map.of("shadowNetwork", 16, "merchants", -10, "court", -10),
                                List.of(),
                                List.of("answered_with_fire")
                        ),
                        new Choice(
                                "Rebuild quietly and refuse to escalate",
                                "You put the workshops back up and say nothing. It costs you a great "
                                        + "deal of money and it starves the feud of the fuel it needed.",
                                Map.of("wealth", -16, "morality", 12, "stress", 6),
                                Map.of("commonPeople", 12, "merchants", 8),
                                List.of(),
                                List.of("refused_to_escalate")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("guild_war_started"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Court Takes an Interest",
                "The dispute has grown large enough that the governor's office has noticed. "
                        + "Officials do not intervene in guild matters out of concern for craftsmen. "
                        + "They intervene because a controlled guild is a reliable source of revenue.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Invite the court in as arbiter and accept its terms",
                                "The dispute ends immediately. The guild is now answerable to a "
                                        + "government office, pays a levy it never paid before, and no longer "
                                        + "decides anything important on its own.",
                                Map.of("politicalPower", 12, "wealth", -10, "stress", -8),
                                Map.of("court", 16, "merchants", -12, "commonPeople", -5),
                                List.of(),
                                List.of("invited_the_court_in")
                        ),
                        new Choice(
                                "Settle privately with your rivals before the court can act",
                                "politicalPower",
                                50,
                                "You and the men who burned your workshops sit in a room and carve up "
                                        + "the trade between you. The court arrives to find nothing left to "
                                        + "arbitrate, and the guild keeps its independence.",
                                "Your rivals take the meeting, learn your position, and go straight to "
                                        + "the governor with it. You negotiated away your own leverage.",
                                Map.of("politicalPower", 16, "wealth", 12, "stress", 8),
                                Map.of("politicalPower", -14, "wealth", -12, "stress", 18),
                                Map.of("merchants", 18, "court", -6),
                                Map.of("merchants", -14, "court", -10),
                                List.of(),
                                List.of("kept_the_guild_independent"),
                                List.of("outmanoeuvred_by_rivals")
                        ),
                        new Choice(
                                "Use the moment to have your rivals ruined by the officials",
                                "You supply the governor's men with a very complete account of your "
                                        + "rivals' irregularities. They are ruined within the year. The guild "
                                        + "is yours, and so is what everyone knows about how you got it.",
                                Map.of("politicalPower", 18, "wealth", 14, "morality", -16),
                                Map.of("court", 12, "merchants", -16, "shadowNetwork", 10),
                                List.of(),
                                List.of("destroyed_rivals_through_court")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("guild_war_started"),
                List.of()
        ));
    }
}
