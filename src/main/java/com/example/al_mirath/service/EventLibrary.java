package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventLibrary {

    private EventLibrary() {
    }

    public static List<GameEvent> createEventPool() {
        List<GameEvent> eventPool = new ArrayList<>();

        addChildhoodEvents(eventPool);
        addYouthEvents(eventPool);
        addAdulthoodEvents(eventPool);
        addPoliticalCrisisEvents(eventPool);
        addLegacyEvents(eventPool);

        addRoyalSuccessionArc(eventPool);
        addDisgracedBloodlineArc(eventPool);
        addExileReturnArc(eventPool);
        addPalaceSpyArc(eventPool);
        addMarriageAllianceArc(eventPool);
        addScholarsRiseArc(eventPool);
        addHeresyAccusationArc(eventPool);
        addMerchantCaravanArc(eventPool);
        addGuildWarArc(eventPool);
        addBreadRebellionArc(eventPool);
        addTaxResistanceArc(eventPool);
        addOrphanPatronArc(eventPool);
        addMilitaryBarracksArc(eventPool);
        addFrontierCommanderArc(eventPool);
        addJanissaryLoyaltyArc(eventPool);
        addShadowDebtArc(eventPool);
        addDynastyTransitionArc(eventPool);
        addFamineReliefArc(eventPool);
        addFamilyFeudArc(eventPool);
        addLegacyHeirArc(eventPool);

        addConsequenceEvents(eventPool);

        return eventPool;
    }

    private static void addChildhoodEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "The Tax Collector Arrives",
                "A harsh tax collector enters your district. Your household cannot pay the full amount. The village waits to see what you will do.",
                "Childhood",
                List.of(
                        new Choice(
                                "Pay using your remaining savings",
                                "You survive the day, but your savings are almost gone. The officials mark you as obedient.",
                                Map.of(
                                        "wealth", -15,
                                        "familyLoyalty", 10,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 8,
                                        "commonPeople", -5,
                                        "familyCouncil", 5
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                5,
                                                "Locked: Requires Wealth 5."
                                        )
                                )
                        ),
                        new Choice(
                                "Organize the villagers to resist",
                                "The villagers remember your courage, but the court hears your name whispered with suspicion.",
                                Map.of(
                                        "reputation", 10,
                                        "politicalPower", 5,
                                        "stress", 15
                                ),
                                Map.of(
                                        "commonPeople", 15,
                                        "court", -15,
                                        "shadowNetwork", 5
                                ),
                                List.of(),
                                List.of("protected_commoners")
                        ),
                        new Choice(
                                "Secretly bribe the collector",
                                "The collector leaves quietly. You solved the problem, but corruption has entered your path.",
                                Map.of(
                                        "wealth", -8,
                                        "morality", -10,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 3,
                                        "shadowNetwork", 10,
                                        "scholars", -5
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                10,
                                                "Locked: Requires Wealth 10."
                                        )
                                ),
                                List.of("used_bribery")
                        )
                ),
                List.of(),
                List.of(
                        "Poor Village Child",
                        "Poor Urban Child",
                        "Mawali Convert's Child",
                        "Village Orphan"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Court Tutor Arrives",
                "A respected tutor is sent to your household. Your family expects you to become worthy of your bloodline, but court education comes with pressure.",
                "Childhood",
                List.of(
                        new Choice(
                                "Study law, history, and court etiquette",
                                "You begin to understand the language of power. The court sees potential in you.",
                                Map.of(
                                        "education", 15,
                                        "politicalPower", 5,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 10,
                                        "scholars", 10
                                )
                        ),
                        new Choice(
                                "Mock the tutor and enjoy noble privilege",
                                "You laugh at the tutor's warnings. The room grows silent. Scholars do not forget humiliation.",
                                Map.of(
                                        "wealth", 5,
                                        "education", -10,
                                        "reputation", -5,
                                        "stress", -5
                                ),
                                Map.of(
                                        "nobles", 5,
                                        "scholars", -15
                                ),
                                List.of(),
                                List.of("angered_scholars")
                        ),
                        new Choice(
                                "Secretly learn court secrets from servants",
                                "You learn less from books and more from whispers. Dangerous knowledge begins to gather around you.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -5,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 15,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("learned_palace_secrets")
                        )
                ),
                List.of(),
                List.of(
                        "Umayyad Noble Heir",
                        "Provincial Governor's Child",
                        "Royal Prince"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "A Lesson in Survival",
                "Even as a child, you learn that the world is not equal. Some children inherit comfort. Others inherit burden. Today, you must decide what kind of person hardship will make you.",
                "Childhood",
                List.of(
                        new Choice(
                                "Help your household with work",
                                "You take on responsibilities early. Your family relies on you more, but childhood feels shorter.",
                                Map.of(
                                        "familyLoyalty", 10,
                                        "stress", 8,
                                        "wealth", 5
                                ),
                                Map.of(
                                        "familyCouncil", 10,
                                        "commonPeople", 5
                                ),
                                List.of(),
                                List.of("early_family_responsibility")
                        ),
                        new Choice(
                                "Spend time learning from elders",
                                "You listen carefully to stories, warnings, and old lessons. Knowledge begins quietly.",
                                Map.of(
                                        "education", 10,
                                        "reputation", 3,
                                        "stress", 3
                                ),
                                Map.of(
                                        "scholars", 5,
                                        "familyCouncil", 5
                                ),
                                List.of(),
                                List.of("learned_from_elders")
                        ),
                        new Choice(
                                "Hide from responsibility whenever possible",
                                "You avoid pressure for now, but others begin to doubt your discipline.",
                                Map.of(
                                        "stress", -8,
                                        "familyLoyalty", -5,
                                        "reputation", -3
                                ),
                                Map.of(
                                        "familyCouncil", -5
                                ),
                                List.of(),
                                List.of("avoided_childhood_responsibility")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "The First Public Shame",
                "A small mistake becomes public. Some laugh, some pity you, and some watch how you respond. Even childhood can teach the price of reputation.",
                "Childhood",
                List.of(
                        new Choice(
                                "Accept the shame quietly",
                                "You swallow the pain and learn patience. People forget the mistake faster than expected.",
                                Map.of(
                                        "stress", 5,
                                        "morality", 5,
                                        "reputation", 3
                                ),
                                Map.of(
                                        "scholars", 3,
                                        "familyCouncil", 3
                                ),
                                List.of(),
                                List.of("learned_humility")
                        ),
                        new Choice(
                                "Blame another child",
                                "You escape the shame for now, but dishonesty becomes easier the next time.",
                                Map.of(
                                        "morality", -8,
                                        "stress", -3,
                                        "reputation", 2
                                ),
                                Map.of(
                                        "shadowNetwork", 5,
                                        "familyCouncil", -5
                                ),
                                List.of(),
                                List.of("blamed_childhood_mistake")
                        ),
                        new Choice(
                                "Fight those who mocked you",
                                "The laughter stops, but fear replaces sympathy.",
                                Map.of(
                                        "health", -5,
                                        "reputation", 5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "commonPeople", -3,
                                        "military", 5
                                ),
                                List.of(),
                                List.of("answered_shame_with_force")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Stranger Offers Help",
                "A stranger notices your situation and offers help. The offer may be kindness, opportunity, or a hidden debt.",
                "Childhood",
                List.of(
                        new Choice(
                                "Accept the help gratefully",
                                "You receive help when you need it most. Gratitude becomes part of your memory.",
                                Map.of(
                                        "wealth", 5,
                                        "morality", 5,
                                        "stress", -5
                                ),
                                Map.of(
                                        "commonPeople", 5,
                                        "familyCouncil", 5
                                ),
                                List.of(),
                                List.of("accepted_childhood_help")
                        ),
                        new Choice(
                                "Refuse because of pride",
                                "You keep your pride, but hardship grows heavier.",
                                Map.of(
                                        "reputation", 5,
                                        "wealth", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "nobles", 3,
                                        "commonPeople", -3
                                ),
                                List.of(),
                                List.of("childhood_pride")
                        ),
                        new Choice(
                                "Ask what they want in return",
                                "You learn early that help often comes with conditions.",
                                Map.of(
                                        "education", 5,
                                        "politicalPower", 3,
                                        "stress", 5
                                ),
                                Map.of(
                                        "shadowNetwork", 5
                                ),
                                List.of(),
                                List.of("learned_suspicion_early")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Family Argument",
                "Your household argues over money, honor, and survival. You are young, but your response shapes how the family sees you.",
                "Childhood",
                List.of(
                        new Choice(
                                "Try to calm everyone",
                                "Your words do not solve everything, but your family notices your care.",
                                Map.of(
                                        "familyLoyalty", 10,
                                        "morality", 5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "familyCouncil", 10
                                ),
                                List.of(),
                                List.of("family_peacemaker")
                        ),
                        new Choice(
                                "Take one side strongly",
                                "You gain one person's trust and another person's resentment.",
                                Map.of(
                                        "politicalPower", 4,
                                        "stress", 8,
                                        "familyLoyalty", -3
                                ),
                                Map.of(
                                        "familyCouncil", -3
                                ),
                                List.of(),
                                List.of("family_side_taken")
                        ),
                        new Choice(
                                "Leave the room silently",
                                "You avoid the argument, but distance grows between you and the household.",
                                Map.of(
                                        "stress", -5,
                                        "familyLoyalty", -6
                                ),
                                Map.of(
                                        "familyCouncil", -5
                                ),
                                List.of(),
                                List.of("avoided_family_conflict")
                        )
                )
        ));
    }

    private static void addYouthEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "A Patron Notices You",
                "A learned patron sees promise in you and offers education. It could change your future, but survival still demands sacrifice.",
                "Youth",
                List.of(
                        new Choice(
                                "Accept the patron's teaching",
                                "You gain knowledge and a new path opens before you.",
                                Map.of(
                                        "education", 20,
                                        "stress", 5
                                ),
                                Map.of(
                                        "scholars", 20,
                                        "commonPeople", 5
                                )
                        ),
                        new Choice(
                                "Refuse and seek work instead",
                                "You choose immediate survival over future learning.",
                                Map.of(
                                        "wealth", 10,
                                        "education", -5,
                                        "stress", -5
                                ),
                                Map.of(
                                        "merchants", 10,
                                        "scholars", -5
                                )
                        ),
                        new Choice(
                                "Ask the patron to support your household",
                                "reputation",
                                45,
                                "You turn knowledge into protection. The patron respects your concern and gives your household support.",
                                "The patron sees your request as opportunistic and withdraws his warmth.",
                                Map.of(
                                        "wealth", 10,
                                        "familyLoyalty", 10,
                                        "reputation", 5
                                ),
                                Map.of(
                                        "reputation", -10,
                                        "stress", 10
                                ),
                                Map.of(
                                        "scholars", 10,
                                        "familyCouncil", 10
                                ),
                                Map.of(
                                        "scholars", -10
                                )
                        )
                ),
                List.of(),
                List.of(
                        "Poor Village Child",
                        "Poor Urban Child",
                        "Mawali Convert's Child",
                        "Scholar's Child",
                        "Madrasa Student",
                        "Scribe's Child"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Barracks Test",
                "Your commander watches the recruits closely. A harsh test will decide who earns respect and who remains invisible.",
                "Youth",
                List.of(
                        new Choice(
                                "Endure the training without complaint",
                                "Your discipline impresses the soldiers around you.",
                                Map.of(
                                        "health", 10,
                                        "stress", 10,
                                        "reputation", 5
                                ),
                                Map.of(
                                        "military", 15
                                )
                        ),
                        new Choice(
                                "Challenge a stronger recruit",
                                "health",
                                55,
                                "You defeat him and the barracks remembers your name.",
                                "You are defeated and humiliated. The barracks laughs, and your body remembers the pain.",
                                Map.of(
                                        "reputation", 15,
                                        "politicalPower", 10,
                                        "stress", 10
                                ),
                                Map.of(
                                        "health", -15,
                                        "reputation", -10,
                                        "stress", 15
                                ),
                                Map.of(
                                        "military", 20
                                ),
                                Map.of(
                                        "military", -10
                                )
                        ),
                        new Choice(
                                "Win favor by reporting another recruit",
                                "You gain attention, but the barracks begins to distrust your loyalty.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -10,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 5,
                                        "military", -5,
                                        "shadowNetwork", 10
                                ),
                                List.of(),
                                List.of("betrayed_comrade")
                        )
                ),
                List.of(),
                List.of(
                        "Mamluk Trainee",
                        "Janissary Recruit",
                        "Military Servant",
                        "Frontier Soldier's Child"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Shape of Ambition",
                "As youth arrives, people begin asking what you will become. Your answer may shape the path of your entire life.",
                "Youth",
                List.of(
                        new Choice(
                                "Seek knowledge and discipline",
                                "You commit yourself to study and self-control. Some find you serious beyond your years.",
                                Map.of(
                                        "education", 15,
                                        "stress", 6,
                                        "morality", 5
                                ),
                                Map.of(
                                        "scholars", 12
                                ),
                                List.of(),
                                List.of("chose_scholarly_path")
                        ),
                        new Choice(
                                "Seek influence among powerful people",
                                "You begin watching the powerful carefully. You learn that status often matters more than truth.",
                                Map.of(
                                        "politicalPower", 12,
                                        "morality", -3,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 8,
                                        "nobles", 5
                                ),
                                List.of(),
                                List.of("chose_power_path")
                        ),
                        new Choice(
                                "Seek money before honor",
                                "You decide that survival needs resources first. Honor can wait.",
                                Map.of(
                                        "wealth", 12,
                                        "morality", -5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "merchants", 10
                                ),
                                List.of(),
                                List.of("chose_wealth_path")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Rival Appears",
                "Someone close to your age begins competing with you. Their success threatens your pride and your future.",
                "Youth",
                List.of(
                        new Choice(
                                "Compete honorably",
                                "You push yourself harder without abandoning your principles.",
                                Map.of(
                                        "education", 8,
                                        "reputation", 8,
                                        "stress", 6
                                ),
                                Map.of(
                                        "scholars", 5,
                                        "commonPeople", 5
                                ),
                                List.of(),
                                List.of("honorable_rivalry")
                        ),
                        new Choice(
                                "Undermine the rival secretly",
                                "Your rival stumbles, but you learn the usefulness of hidden cruelty.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -10,
                                        "stress", 6
                                ),
                                Map.of(
                                        "shadowNetwork", 10,
                                        "scholars", -5
                                ),
                                List.of(),
                                List.of("undermined_rival")
                        ),
                        new Choice(
                                "Avoid the rivalry",
                                "You avoid direct conflict, but others may see your silence as weakness.",
                                Map.of(
                                        "stress", -5,
                                        "reputation", -5
                                ),
                                Map.of(
                                        "nobles", -3,
                                        "commonPeople", -3
                                ),
                                List.of(),
                                List.of("avoided_rivalry")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "First Taste of Authority",
                "You are given responsibility over others for the first time. It is small power, but power reveals character.",
                "Youth",
                List.of(
                        new Choice(
                                "Use authority fairly",
                                "People respect your fairness, even when your decisions are difficult.",
                                Map.of(
                                        "reputation", 8,
                                        "morality", 8,
                                        "stress", 5
                                ),
                                Map.of(
                                        "commonPeople", 8,
                                        "scholars", 5
                                ),
                                List.of(),
                                List.of("used_authority_fairly")
                        ),
                        new Choice(
                                "Use authority to reward friends",
                                "Your friends benefit, but others begin to doubt your judgment.",
                                Map.of(
                                        "familyLoyalty", 8,
                                        "morality", -5,
                                        "reputation", -3
                                ),
                                Map.of(
                                        "familyCouncil", 8,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("favored_inner_circle")
                        ),
                        new Choice(
                                "Use authority harshly",
                                "People obey quickly, but obedience born from fear does not become loyalty.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -8,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 5,
                                        "commonPeople", -8
                                ),
                                List.of(),
                                List.of("harsh_young_authority")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Dangerous Invitation",
                "A group of ambitious youths invites you into their circle. They speak of opportunity, influence, and secrets.",
                "Youth",
                List.of(
                        new Choice(
                                "Join them carefully",
                                "You enter the circle but keep your eyes open.",
                                Map.of(
                                        "politicalPower", 6,
                                        "stress", 5,
                                        "education", 3
                                ),
                                Map.of(
                                        "shadowNetwork", 8,
                                        "nobles", 3
                                ),
                                List.of(),
                                List.of("joined_ambitious_circle")
                        ),
                        new Choice(
                                "Reject the invitation",
                                "You avoid possible trouble, but some doors close quietly.",
                                Map.of(
                                        "morality", 5,
                                        "stress", -3,
                                        "politicalPower", -3
                                ),
                                Map.of(
                                        "scholars", 5,
                                        "shadowNetwork", -5
                                ),
                                List.of(),
                                List.of("rejected_secret_circle")
                        ),
                        new Choice(
                                "Use the group for information",
                                "You listen more than you speak. Secrets become a kind of currency.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "shadowNetwork", 12
                                ),
                                List.of(),
                                List.of("youth_information_network")
                        )
                )
        ));
    }

    private static void addAdulthoodEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "The Merchant's Risk",
                "A trade opportunity appears, but the route is unsafe and the profit is uncertain.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Invest in the caravan",
                                "Your money enters the trade road. Fortune may remember your courage.",
                                Map.of(
                                        "wealth", 15,
                                        "stress", 10
                                ),
                                Map.of(
                                        "merchants", 15
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                15,
                                                "Locked: Requires Wealth 15."
                                        )
                                )
                        ),
                        new Choice(
                                "Avoid the risk",
                                "You keep what you have, but the guild sees no ambition in you.",
                                Map.of(
                                        "stress", -5
                                ),
                                Map.of(
                                        "merchants", -5
                                )
                        ),
                        new Choice(
                                "Use hidden contacts to secure the route",
                                "You make the road safer, but now darker people know your name.",
                                Map.of(
                                        "politicalPower", 5,
                                        "morality", -5
                                ),
                                Map.of(
                                        "merchants", 10,
                                        "shadowNetwork", 15
                                ),
                                List.of(),
                                List.of("used_shadow_contacts")
                        )
                ),
                List.of(),
                List.of(
                        "Baghdad Merchant's Child",
                        "Cairo Merchant's Child",
                        "Istanbul Guild Merchant"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Palace Whisper",
                "While serving near powerful people, you overhear a dangerous conversation. What you do with this knowledge may decide your rise or ruin.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Report the secret to a superior",
                                "Your loyalty is noticed, but someone powerful now knows you heard too much.",
                                Map.of(
                                        "politicalPower", 10,
                                        "stress", 15
                                ),
                                Map.of(
                                        "court", 15,
                                        "shadowNetwork", -5
                                ),
                                List.of(),
                                List.of("reported_palace_secret")
                        ),
                        new Choice(
                                "Sell the secret quietly",
                                "The secret becomes money. But secrets rarely stay buried.",
                                Map.of(
                                        "wealth", 15,
                                        "morality", -10,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 20,
                                        "court", -10
                                ),
                                List.of(),
                                List.of("sold_palace_secret")
                        ),
                        new Choice(
                                "Stay silent and remember it",
                                "You choose patience. The information may become useful later.",
                                Map.of(
                                        "stress", 5,
                                        "politicalPower", 5
                                ),
                                Map.of(
                                        "shadowNetwork", 5
                                ),
                                List.of(),
                                List.of("remembered_palace_secret")
                        )
                ),
                List.of(),
                List.of(
                        "Court Servant",
                        "Palace Clerk's Child",
                        "Royal Prince",
                        "Umayyad Noble Heir"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "A Reputation Tested",
                "By adulthood, your name has begun to mean something. A public dispute now tests whether people see you as honorable, useful, dangerous, or weak.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Resolve the dispute fairly",
                                "People respect your fairness, though fairness rarely pleases everyone.",
                                Map.of(
                                        "reputation", 10,
                                        "morality", 8,
                                        "stress", 6
                                ),
                                Map.of(
                                        "commonPeople", 10,
                                        "scholars", 5
                                ),
                                List.of(),
                                List.of("known_for_fairness")
                        ),
                        new Choice(
                                "Use the dispute to gain influence",
                                "You turn conflict into opportunity. Some admire your skill; others distrust your ambition.",
                                Map.of(
                                        "politicalPower", 12,
                                        "morality", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 8,
                                        "nobles", 5,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("used_dispute_for_power")
                        ),
                        new Choice(
                                "Stay out of it",
                                "You avoid trouble, but silence has its own cost.",
                                Map.of(
                                        "stress", -5,
                                        "reputation", -5
                                ),
                                Map.of(
                                        "commonPeople", -3,
                                        "court", -3
                                ),
                                List.of(),
                                List.of("avoided_public_dispute")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "Marriage Alliance",
                "Your household discusses a marriage alliance. It could bring stability, wealth, power, or resentment.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Choose family stability",
                                "The alliance strengthens your household, though personal ambition must bend.",
                                Map.of(
                                        "familyLoyalty", 12,
                                        "stress", 5,
                                        "reputation", 5
                                ),
                                Map.of(
                                        "familyCouncil", 15,
                                        "nobles", 5
                                ),
                                List.of(),
                                List.of("secured_family_alliance")
                        ),
                        new Choice(
                                "Choose political advantage",
                                "The alliance opens doors in powerful circles, but your household feels used.",
                                Map.of(
                                        "politicalPower", 12,
                                        "familyLoyalty", -8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 10,
                                        "nobles", 8,
                                        "familyCouncil", -5
                                ),
                                List.of(),
                                List.of("political_marriage")
                        ),
                        new Choice(
                                "Reject the arrangement",
                                "You choose independence, angering those who expected obedience.",
                                Map.of(
                                        "reputation", 5,
                                        "stress", 10,
                                        "familyLoyalty", -10
                                ),
                                Map.of(
                                        "familyCouncil", -12,
                                        "nobles", -5
                                ),
                                List.of(),
                                List.of("rejected_family_arrangement")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Chance to Mentor",
                "Someone younger seeks your guidance. Your response may shape not only their future, but your own reputation.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Teach them patiently",
                                "Your patience earns quiet respect.",
                                Map.of(
                                        "education", 5,
                                        "morality", 8,
                                        "reputation", 8
                                ),
                                Map.of(
                                        "scholars", 8,
                                        "commonPeople", 5
                                ),
                                List.of(),
                                List.of("mentored_youth")
                        ),
                        new Choice(
                                "Use them as an assistant",
                                "You gain help, but your guidance becomes more practical than generous.",
                                Map.of(
                                        "wealth", 5,
                                        "politicalPower", 5,
                                        "morality", -3
                                ),
                                Map.of(
                                        "merchants", 5,
                                        "familyCouncil", 3
                                ),
                                List.of(),
                                List.of("used_youth_as_assistant")
                        ),
                        new Choice(
                                "Turn them away",
                                "You avoid extra burden, but lose a chance to build loyalty.",
                                Map.of(
                                        "stress", -5,
                                        "reputation", -3
                                ),
                                Map.of(
                                        "commonPeople", -5,
                                        "scholars", -3
                                ),
                                List.of(),
                                List.of("refused_to_mentor")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "Debt Comes Due",
                "An old financial obligation returns. Whether it came from your family, your own ambition, or someone else's mistake, it now demands an answer.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Pay the debt honestly",
                                "You lose wealth, but your name remains clean.",
                                Map.of(
                                        "wealth", -15,
                                        "morality", 8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "merchants", 5,
                                        "scholars", 3
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                15,
                                                "Locked: Requires Wealth 15."
                                        )
                                ),
                                List.of("paid_debt_honestly")
                        ),
                        new Choice(
                                "Delay payment with promises",
                                "You buy time, but trust weakens.",
                                Map.of(
                                        "stress", 10,
                                        "reputation", -5
                                ),
                                Map.of(
                                        "merchants", -10
                                ),
                                List.of(),
                                List.of("delayed_debt")
                        ),
                        new Choice(
                                "Use influence to erase the debt",
                                "The debt disappears from paper, but not from memory.",
                                Map.of(
                                        "politicalPower", 5,
                                        "morality", -10,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 5,
                                        "merchants", -8,
                                        "shadowNetwork", 8
                                ),
                                List.of(),
                                List.of("erased_debt_through_power")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Private Offer",
                "A powerful person offers you advancement in exchange for quiet obedience. The offer is tempting, but the cost is hidden.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Accept the offer",
                                "Your rise becomes easier, but your independence weakens.",
                                Map.of(
                                        "politicalPower", 15,
                                        "morality", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 12,
                                        "nobles", 8
                                ),
                                List.of(),
                                List.of("accepted_private_patronage")
                        ),
                        new Choice(
                                "Refuse the offer",
                                "You keep your independence, but advancement becomes harder.",
                                Map.of(
                                        "morality", 8,
                                        "stress", 8,
                                        "politicalPower", -5
                                ),
                                Map.of(
                                        "scholars", 5,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("refused_private_patronage")
                        ),
                        new Choice(
                                "Pretend to accept while preparing your own path",
                                "You learn to smile while planning differently.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -5,
                                        "stress", 12
                                ),
                                Map.of(
                                        "shadowNetwork", 10,
                                        "court", 5
                                ),
                                List.of(),
                                List.of("deceptive_patronage")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Scholar Asked to Judge",
                "Because of your growing reputation for learning, people ask you to judge a difficult dispute. Your words may bring justice, power, or anger.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Judge with fairness",
                                "Your judgment is balanced. Scholars and common people respect your name.",
                                Map.of(
                                        "morality", 10,
                                        "reputation", 10,
                                        "stress", 6
                                ),
                                Map.of(
                                        "scholars", 12,
                                        "commonPeople", 8
                                ),
                                List.of(),
                                List.of("fair_scholar_judgment")
                        ),
                        new Choice(
                                "Favor the powerful side",
                                "The powerful are pleased, but people quietly question your integrity.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -10,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 10,
                                        "nobles", 8,
                                        "commonPeople", -10
                                ),
                                List.of(),
                                List.of("biased_scholar_judgment")
                        ),
                        new Choice(
                                "Refuse to judge",
                                "You avoid danger, but some expected courage from you.",
                                Map.of(
                                        "stress", -5,
                                        "reputation", -5
                                ),
                                Map.of(
                                        "scholars", -5,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("refused_scholar_judgment")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("Respected Scholar", "Educated Aspirant"),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Merchants Seek Your Protection",
                "Merchants approach you for protection against unfair officials. Helping them may bring wealth, influence, or enemies.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Protect the merchants openly",
                                "The merchants reward your protection and begin trusting your leadership.",
                                Map.of(
                                        "wealth", 10,
                                        "reputation", 6,
                                        "stress", 6
                                ),
                                Map.of(
                                        "merchants", 15,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("protected_merchants")
                        ),
                        new Choice(
                                "Demand payment first",
                                "You gain wealth, but your help looks selfish.",
                                Map.of(
                                        "wealth", 18,
                                        "morality", -8,
                                        "reputation", -3
                                ),
                                Map.of(
                                        "merchants", 8,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("profited_from_merchants")
                        ),
                        new Choice(
                                "Report them to officials",
                                "The court appreciates your loyalty, but merchants remember your betrayal.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -8,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 12,
                                        "merchants", -15
                                ),
                                List.of(),
                                List.of("reported_merchants")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("Influential Merchant", "Stable Householder"),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "Command Over Restless Soldiers",
                "Your military reputation gives you command over restless soldiers. They respect strength, but they also test weakness.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Lead with discipline",
                                "Your order strengthens the soldiers without losing control.",
                                Map.of(
                                        "politicalPower", 8,
                                        "reputation", 8,
                                        "stress", 6
                                ),
                                Map.of(
                                        "military", 15,
                                        "court", 5
                                ),
                                List.of(),
                                List.of("disciplined_soldiers")
                        ),
                        new Choice(
                                "Promise them rewards",
                                "The soldiers become loyal for now, but loyalty bought with promises can become expensive.",
                                Map.of(
                                        "politicalPower", 10,
                                        "wealth", -8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "military", 18,
                                        "merchants", -5
                                ),
                                List.of(),
                                List.of("promised_soldier_rewards")
                        ),
                        new Choice(
                                "Rule them through fear",
                                "They obey quickly, but fear creates hidden resentment.",
                                Map.of(
                                        "politicalPower", 15,
                                        "morality", -12,
                                        "stress", 10
                                ),
                                Map.of(
                                        "military", 10,
                                        "commonPeople", -10,
                                        "shadowNetwork", 5
                                ),
                                List.of(),
                                List.of("ruled_soldiers_by_fear")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("Military Commander"),
                List.of(),
                List.of()
        ));
    }

    private static void addPoliticalCrisisEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "Rumors of Unrest",
                "Across the city, people whisper that the ruling dynasty is weakening. Some call for loyalty. Others speak of rebellion.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Speak in favor of the current dynasty",
                                "Loyalists remember your words. The angry public does too.",
                                Map.of(
                                        "politicalPower", 10,
                                        "reputation", 5,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 15,
                                        "commonPeople", -10
                                ),
                                List.of(),
                                List.of("declared_loyalty")
                        ),
                        new Choice(
                                "Quietly listen to rebel voices",
                                "You do not commit yet, but dangerous doors begin to open.",
                                Map.of(
                                        "politicalPower", 5,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 10,
                                        "commonPeople", 10,
                                        "court", -10
                                ),
                                List.of(),
                                List.of("heard_rebel_voices")
                        ),
                        new Choice(
                                "Avoid politics and protect your household",
                                "You survive quietly for now, but history rarely respects silence.",
                                Map.of(
                                        "familyLoyalty", 10,
                                        "stress", -5,
                                        "politicalPower", -5
                                ),
                                Map.of(
                                        "familyCouncil", 10,
                                        "court", -3,
                                        "commonPeople", -3
                                ),
                                List.of(),
                                List.of("avoided_politics")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "Pressure from the Powerful",
                "A powerful figure demands your support. Refusing may be dangerous, but obedience may stain your name.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Support the powerful figure openly",
                                "You gain protection, but people begin questioning your independence.",
                                Map.of(
                                        "politicalPower", 12,
                                        "reputation", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 12,
                                        "nobles", 8,
                                        "commonPeople", -8
                                ),
                                List.of(),
                                List.of("served_powerful_patron")
                        ),
                        new Choice(
                                "Refuse politely",
                                "reputation",
                                50,
                                "Your refusal is respected. Even some rivals admire your courage.",
                                "Your refusal is seen as weakness. The powerful figure begins working against you.",
                                Map.of(
                                        "reputation", 10,
                                        "morality", 8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "reputation", -10,
                                        "stress", 15,
                                        "politicalPower", -8
                                ),
                                Map.of(
                                        "commonPeople", 10,
                                        "scholars", 5
                                ),
                                Map.of(
                                        "court", -12,
                                        "nobles", -8
                                ),
                                List.of(),
                                List.of("resisted_powerful_patron"),
                                List.of("angered_powerful_patron")
                        ),
                        new Choice(
                                "Secretly play both sides",
                                "You survive the moment through deception. But double games create double enemies.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -10,
                                        "stress", 12
                                ),
                                Map.of(
                                        "shadowNetwork", 15,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("played_both_sides")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "The Bread Riot",
                "Food prices rise and anger fills the streets. The poor demand relief while officials demand order.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Distribute grain from your stores",
                                "The people remember your mercy, but your resources shrink.",
                                Map.of(
                                        "wealth", -12,
                                        "reputation", 12,
                                        "morality", 8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "commonPeople", 18,
                                        "merchants", -5
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                12,
                                                "Locked: Requires Wealth 12."
                                        )
                                ),
                                List.of("fed_people_during_riot")
                        ),
                        new Choice(
                                "Support the officials",
                                "Order returns, but resentment grows among the hungry.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -5,
                                        "stress", 6
                                ),
                                Map.of(
                                        "court", 12,
                                        "commonPeople", -15
                                ),
                                List.of(),
                                List.of("sided_with_order_against_hungry")
                        ),
                        new Choice(
                                "Use the chaos to gain contacts",
                                "While others panic, you build hidden relationships.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -8,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 15,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("profited_from_riot")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Trial in Public",
                "A public trial becomes the talk of the city. People expect you to take a position, even if silence would be safer.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Defend the accused if the law supports them",
                                "Your courage earns respect among scholars and common people.",
                                Map.of(
                                        "education", 5,
                                        "reputation", 12,
                                        "stress", 10
                                ),
                                Map.of(
                                        "scholars", 12,
                                        "commonPeople", 10,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("defended_accused_publicly")
                        ),
                        new Choice(
                                "Side with the court judgment",
                                "The court appreciates your loyalty, though some question your conscience.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", 12,
                                        "scholars", -5
                                ),
                                List.of(),
                                List.of("sided_with_court_trial")
                        ),
                        new Choice(
                                "Avoid taking any position",
                                "You avoid danger, but your silence is noticed.",
                                Map.of(
                                        "stress", -5,
                                        "reputation", -6
                                ),
                                Map.of(
                                        "commonPeople", -5,
                                        "scholars", -3
                                ),
                                List.of(),
                                List.of("silent_during_trial")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "A Faction Demands Loyalty",
                "One faction demands that you prove your loyalty. Refusal may turn them into enemies.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Side with the court",
                                "The court rewards your loyalty, but others see you as part of the ruling order.",
                                Map.of(
                                        "politicalPower", 10,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 15,
                                        "commonPeople", -8,
                                        "shadowNetwork", -5
                                ),
                                List.of(),
                                List.of("court_loyalty_confirmed")
                        ),
                        new Choice(
                                "Side with the people",
                                "The people trust you more, but officials begin watching your movements.",
                                Map.of(
                                        "reputation", 12,
                                        "stress", 10
                                ),
                                Map.of(
                                        "commonPeople", 15,
                                        "court", -10
                                ),
                                List.of(),
                                List.of("people_loyalty_confirmed")
                        ),
                        new Choice(
                                "Promise loyalty to no one",
                                "You keep your independence, but no faction fully trusts you.",
                                Map.of(
                                        "morality", 5,
                                        "stress", 12
                                ),
                                Map.of(
                                        "court", -5,
                                        "commonPeople", -5,
                                        "nobles", -5
                                ),
                                List.of(),
                                List.of("remained_independent")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "Court Demands Proof of Loyalty",
                "Your influence at court has grown enough that rivals now demand proof of your loyalty. A wrong move could weaken your position.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Publicly defend the court",
                                "Your loyalty is rewarded, but the people become colder toward your name.",
                                Map.of(
                                        "politicalPower", 12,
                                        "reputation", -4,
                                        "stress", 8
                                ),
                                Map.of(
                                        "court", 15,
                                        "commonPeople", -10
                                ),
                                List.of(),
                                List.of("proved_court_loyalty")
                        ),
                        new Choice(
                                "Defend the people while staying respectful",
                                "reputation",
                                60,
                                "You balance courage and diplomacy. Both sides are forced to respect you.",
                                "Your words satisfy no one. Court rivals use the moment against you.",
                                Map.of(
                                        "reputation", 12,
                                        "politicalPower", 8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "reputation", -10,
                                        "politicalPower", -8,
                                        "stress", 15
                                ),
                                Map.of(
                                        "court", 5,
                                        "commonPeople", 10,
                                        "scholars", 5
                                ),
                                Map.of(
                                        "court", -12,
                                        "nobles", -8
                                ),
                                List.of(),
                                List.of("balanced_court_and_people"),
                                List.of("court_rivals_attack")
                        ),
                        new Choice(
                                "Secretly prepare an escape plan",
                                "You do not fully trust the court anymore. Survival becomes your real loyalty.",
                                Map.of(
                                        "stress", 10,
                                        "politicalPower", 5,
                                        "morality", -3
                                ),
                                Map.of(
                                        "shadowNetwork", 12,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("prepared_escape_plan")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("Powerful Court Figure", "Rising Political Actor"),
                List.of(),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Shadow Network Collects",
                "Your hidden contacts remind you that favors are never free. They now ask for something dangerous in return.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Pay them to leave you alone",
                                "The payment buys temporary peace, but hidden debts rarely disappear forever.",
                                Map.of(
                                        "wealth", -15,
                                        "stress", 8
                                ),
                                Map.of(
                                        "shadowNetwork", -5,
                                        "merchants", -5
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                15,
                                                "Locked: Requires Wealth 15."
                                        )
                                ),
                                List.of("paid_shadow_network")
                        ),
                        new Choice(
                                "Do what they ask",
                                "You obey the hidden world. Your protection remains, but your conscience pays the cost.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -15,
                                        "stress", 12
                                ),
                                Map.of(
                                        "shadowNetwork", 15,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("served_shadow_network")
                        ),
                        new Choice(
                                "Refuse and expose them",
                                "politicalPower",
                                65,
                                "You expose enough of the network to weaken their grip on you.",
                                "Your refusal fails. The network now sees you as a threat.",
                                Map.of(
                                        "reputation", 12,
                                        "morality", 10,
                                        "stress", 10
                                ),
                                Map.of(
                                        "stress", 20,
                                        "reputation", -10,
                                        "politicalPower", -8
                                ),
                                Map.of(
                                        "scholars", 8,
                                        "commonPeople", 8,
                                        "shadowNetwork", -20
                                ),
                                Map.of(
                                        "shadowNetwork", 20,
                                        "court", -10
                                ),
                                List.of(),
                                List.of("exposed_shadow_network"),
                                List.of("shadow_network_enemy")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("Shadow Broker"),
                List.of(),
                List.of()
        ));
    }

    private static void addLegacyEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "A Question of Legacy",
                "Years pass, and your choices begin to shape how your bloodline will be remembered. Before the next chapter begins, you must decide what matters most.",
                "Legacy",
                List.of(
                        new Choice(
                                "Preserve the family's honor",
                                "You teach your household that survival means nothing without honor.",
                                Map.of(
                                        "familyLoyalty", 10,
                                        "morality", 5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "familyCouncil", 15,
                                        "scholars", 5
                                )
                        ),
                        new Choice(
                                "Secure wealth for the next generation",
                                "You focus on property, savings, and trade. Some call it wisdom; others call it greed.",
                                Map.of(
                                        "wealth", 15,
                                        "morality", -5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "merchants", 15,
                                        "familyCouncil", 5
                                )
                        ),
                        new Choice(
                                "Seek influence before death",
                                "You spend your final strength building connections with the powerful.",
                                Map.of(
                                        "politicalPower", 15,
                                        "stress", 10,
                                        "familyLoyalty", -5
                                ),
                                Map.of(
                                        "court", 10,
                                        "nobles", 10
                                )
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "The Last Household Council",
                "Your household gathers to discuss the future. Younger voices now ask what should be protected after you are gone.",
                "Legacy",
                List.of(
                        new Choice(
                                "Teach unity above ambition",
                                "You warn them that a divided house can lose everything.",
                                Map.of(
                                        "familyLoyalty", 15,
                                        "morality", 5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "familyCouncil", 15
                                ),
                                List.of(),
                                List.of("taught_family_unity")
                        ),
                        new Choice(
                                "Teach ambition above comfort",
                                "You tell them that history rewards those who reach for power.",
                                Map.of(
                                        "politicalPower", 10,
                                        "familyLoyalty", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "nobles", 8,
                                        "court", 8
                                ),
                                List.of(),
                                List.of("taught_family_ambition")
                        ),
                        new Choice(
                                "Divide resources carefully",
                                "You focus on practical survival and inheritance.",
                                Map.of(
                                        "wealth", 10,
                                        "stress", 6
                                ),
                                Map.of(
                                        "merchants", 8,
                                        "familyCouncil", 8
                                ),
                                List.of(),
                                List.of("settled_inheritance")
                        )
                )
        ));

        eventPool.add(new GameEvent(
                "The Final Patronage",
                "In your later years, someone asks for your support. Your name still has weight, but every final choice shapes memory.",
                "Legacy",
                List.of(
                        new Choice(
                                "Support a promising scholar",
                                "Your final support strengthens learning beyond your own life.",
                                Map.of(
                                        "education", 5,
                                        "reputation", 8,
                                        "morality", 5
                                ),
                                Map.of(
                                        "scholars", 15
                                ),
                                List.of(),
                                List.of("final_scholar_patronage")
                        ),
                        new Choice(
                                "Support a military figure",
                                "You place your remaining influence behind force and order.",
                                Map.of(
                                        "politicalPower", 8,
                                        "stress", 5
                                ),
                                Map.of(
                                        "military", 15,
                                        "court", 5
                                ),
                                List.of(),
                                List.of("final_military_patronage")
                        ),
                        new Choice(
                                "Support the poor quietly",
                                "Your final years bring relief to those who had little.",
                                Map.of(
                                        "wealth", -10,
                                        "morality", 10,
                                        "reputation", 8
                                ),
                                Map.of(
                                        "commonPeople", 15
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                10,
                                                "Locked: Requires Wealth 10."
                                        )
                                ),
                                List.of("final_charity")
                        )
                )
        ));
    }

    private static void addConsequenceEvents(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "The Collector Returns",
                "The tax collector you once bribed has returned. He remembers your weakness and now demands more, hinting that he could expose your corruption to the court.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Pay him again to stay silent",
                                "You pay the collector once more. The secret remains buried, but corruption now has a price on your future.",
                                Map.of(
                                        "wealth", -15,
                                        "morality", -5,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 3,
                                        "shadowNetwork", 10
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                15,
                                                "Locked: Requires Wealth 15."
                                        )
                                ),
                                List.of("blackmailed_by_collector")
                        ),
                        new Choice(
                                "Refuse and threaten him",
                                "politicalPower",
                                50,
                                "Your threat works. The collector leaves in fear, and the shadow network respects your boldness.",
                                "Your threat fails. The collector laughs and begins preparing a report against you.",
                                Map.of(
                                        "politicalPower", 10,
                                        "stress", 10,
                                        "morality", -5
                                ),
                                Map.of(
                                        "reputation", -10,
                                        "stress", 20
                                ),
                                Map.of(
                                        "shadowNetwork", 15,
                                        "court", -5
                                ),
                                Map.of(
                                        "court", -15,
                                        "shadowNetwork", -5
                                ),
                                List.of(),
                                List.of("collector_silenced"),
                                List.of("collector_reported_you")
                        ),
                        new Choice(
                                "Confess the old bribe before he exposes you",
                                "You confess first. The court dislikes your corruption, but some respect your honesty.",
                                Map.of(
                                        "morality", 10,
                                        "reputation", -5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "court", -5,
                                        "scholars", 5
                                ),
                                List.of(),
                                List.of("confessed_bribery")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("used_bribery"),
                List.of("collector_silenced")
        ));

        eventPool.add(new GameEvent(
                "The Scholar's Complaint",
                "The tutor you mocked has written to officials, describing you as arrogant and unfit for responsibility. The report now moves through the court.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Apologize publicly to the scholars",
                                "Your apology softens the insult, but nobles whisper that you bent your neck too easily.",
                                Map.of(
                                        "morality", 5,
                                        "reputation", -5,
                                        "stress", 5
                                ),
                                Map.of(
                                        "scholars", 15,
                                        "nobles", -5,
                                        "court", 5
                                )
                        ),
                        new Choice(
                                "Use family influence to bury the report",
                                "Your family pressure slows the report, but court officials now watch you carefully.",
                                Map.of(
                                        "politicalPower", 5,
                                        "stress", 10,
                                        "morality", -5
                                ),
                                Map.of(
                                        "court", -5,
                                        "nobles", 10,
                                        "scholars", -10
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "politicalPower",
                                                35,
                                                "Locked: Requires Political Power 35."
                                        )
                                ),
                                List.of("court_suspicion")
                        ),
                        new Choice(
                                "Threaten the scholar through servants",
                                "The scholar falls silent, but the shadow around your name grows darker.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -15,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 20,
                                        "scholars", -20,
                                        "court", -10
                                ),
                                List.of(),
                                List.of("court_suspicion")
                        )
                ),
                List.of(),
                List.of(
                        "Umayyad Noble Heir",
                        "Provincial Governor's Child",
                        "Royal Prince",
                        "Court Servant",
                        "Palace Clerk's Child"
                ),
                List.of(),
                List.of("angered_scholars"),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Secret Finds Its Way Back",
                "The palace secret you sold has reached dangerous ears. Now a court official suspects that someone close to the palace betrayed confidential knowledge.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Frame another servant",
                                "You shift suspicion onto someone weaker. You survive, but your soul grows heavier.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -15,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 5,
                                        "shadowNetwork", 15,
                                        "commonPeople", -5
                                ),
                                List.of(),
                                List.of("framed_innocent")
                        ),
                        new Choice(
                                "Destroy the evidence quietly",
                                "politicalPower",
                                60,
                                "The evidence disappears. Only a few whispers remain.",
                                "You fail to destroy the evidence. The court now suspects you directly.",
                                Map.of(
                                        "stress", 10,
                                        "politicalPower", 5
                                ),
                                Map.of(
                                        "stress", 25,
                                        "reputation", -15,
                                        "politicalPower", -10
                                ),
                                Map.of(
                                        "shadowNetwork", 10,
                                        "court", -5
                                ),
                                Map.of(
                                        "court", -20,
                                        "nobles", -10
                                ),
                                List.of(),
                                List.of("evidence_destroyed"),
                                List.of("court_suspicion")
                        ),
                        new Choice(
                                "Seek protection from shadow contacts",
                                "Your hidden contacts shield you, but now they own part of your future.",
                                Map.of(
                                        "politicalPower", 5,
                                        "morality", -10,
                                        "stress", 15
                                ),
                                Map.of(
                                        "shadowNetwork", 20,
                                        "court", -10
                                ),
                                List.of(
                                        ChoiceRequirement.factionAtLeast(
                                                "shadowNetwork",
                                                45,
                                                "Locked: Requires Shadow Network 45."
                                        )
                                ),
                                List.of("owed_shadow_debt")
                        )
                ),
                List.of(),
                List.of(
                        "Court Servant",
                        "Palace Clerk's Child",
                        "Royal Prince",
                        "Umayyad Noble Heir"
                ),
                List.of(),
                List.of("sold_palace_secret"),
                List.of("evidence_destroyed")
        ));

        eventPool.add(new GameEvent(
                "The Rival's Revenge",
                "The rival you once undermined has returned with allies. What was once a private competition has become a public danger.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Admit what you did and seek peace",
                                "The confession damages your pride, but it prevents a larger conflict.",
                                Map.of(
                                        "morality", 10,
                                        "reputation", -5,
                                        "stress", 8
                                ),
                                Map.of(
                                        "scholars", 5,
                                        "commonPeople", 5
                                ),
                                List.of(),
                                List.of("made_peace_with_rival")
                        ),
                        new Choice(
                                "Strike against the rival first",
                                "You move before they can hurt you. The conflict ends, but your reputation darkens.",
                                Map.of(
                                        "politicalPower", 10,
                                        "morality", -12,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 12,
                                        "commonPeople", -8
                                ),
                                List.of(),
                                List.of("crushed_rival")
                        ),
                        new Choice(
                                "Ask powerful friends for protection",
                                "You survive through connections, but now you owe favors.",
                                Map.of(
                                        "stress", 8,
                                        "politicalPower", 5
                                ),
                                Map.of(
                                        "court", 8,
                                        "nobles", 8
                                ),
                                List.of(
                                        ChoiceRequirement.factionAtLeast(
                                                "nobles",
                                                45,
                                                "Locked: Requires Nobles 45."
                                        )
                                ),
                                List.of("protected_by_powerful_friends")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("undermined_rival"),
                List.of("made_peace_with_rival")
        ));

        eventPool.add(new GameEvent(
                "The Patron Turns Against You",
                "The powerful figure you angered has begun working quietly against your future. Their influence spreads through offices, families, and court whispers.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Offer a careful apology",
                                "Your apology reduces the danger, but it costs pride.",
                                Map.of(
                                        "stress", 5,
                                        "reputation", -3,
                                        "morality", 5
                                ),
                                Map.of(
                                        "court", 5,
                                        "nobles", 3
                                ),
                                List.of(),
                                List.of("appeased_powerful_patron")
                        ),
                        new Choice(
                                "Expose their pressure publicly",
                                "reputation",
                                60,
                                "You turn their pressure into a public issue. People believe you, and the patron's influence weakens.",
                                "You turn their pressure into a public issue, but people doubt you. The patron now has reason to destroy your standing.",
                                Map.of(
                                        "reputation", 12,
                                        "politicalPower", 8,
                                        "stress", 10
                                ),
                                Map.of(
                                        "reputation", -15,
                                        "stress", 18,
                                        "politicalPower", -8
                                ),
                                Map.of(
                                        "commonPeople", 12,
                                        "court", -5
                                ),
                                Map.of(
                                        "court", -15,
                                        "nobles", -10
                                ),
                                List.of(),
                                List.of("exposed_powerful_patron"),
                                List.of("court_suspicion")
                        ),
                        new Choice(
                                "Seek shadow help",
                                "Hidden contacts begin protecting you, but every hidden protection has a price.",
                                Map.of(
                                        "politicalPower", 5,
                                        "morality", -8,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 18,
                                        "court", -8
                                ),
                                List.of(),
                                List.of("owed_shadow_debt")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("angered_powerful_patron"),
                List.of("appeased_powerful_patron")
        ));
    }


    private static void addRoyalSuccessionArc(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "The Father's Weakness",
                "Your father, once feared in the household and respected by the court, is beginning to lose control. Servants whisper. Nobles watch. Your brother watches you.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Serve your father loyally",
                                "You stand beside your father and strengthen the household's public image. Some call you loyal. Others call you passive.",
                                Map.of(
                                        "familyLoyalty", 12,
                                        "morality", 5,
                                        "stress", 6
                                ),
                                Map.of(
                                        "court", 8,
                                        "familyCouncil", 10,
                                        "nobles", 5
                                ),
                                List.of(),
                                List.of("royal_succession_started", "stood_by_father")
                        ),
                        new Choice(
                                "Quietly gather court supporters",
                                "You begin building your own circle. The court notices your ambition before your father fully understands it.",
                                Map.of(
                                        "politicalPower", 14,
                                        "familyLoyalty", -6,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 8,
                                        "nobles", 12,
                                        "shadowNetwork", 5
                                ),
                                List.of(),
                                List.of("royal_succession_started", "succession_faction_built")
                        ),
                        new Choice(
                                "Challenge your father openly",
                                "reputation",
                                55,
                                "Your challenge shocks the household, but your confidence forces the court to take you seriously.",
                                "Your challenge fails. Your father humiliates you before the household, and your brother smiles quietly.",
                                Map.of(
                                        "politicalPower", 16,
                                        "reputation", 8,
                                        "stress", 12,
                                        "familyLoyalty", -10
                                ),
                                Map.of(
                                        "reputation", -12,
                                        "politicalPower", -8,
                                        "stress", 15,
                                        "familyLoyalty", -8
                                ),
                                Map.of(
                                        "court", 10,
                                        "nobles", 8,
                                        "familyCouncil", -8
                                ),
                                Map.of(
                                        "court", -8,
                                        "familyCouncil", -10,
                                        "nobles", -5
                                ),
                                List.of(),
                                List.of("royal_succession_started", "father_challenged"),
                                List.of("royal_succession_started", "humiliated_by_father")
                        )
                ),
                List.of(),
                List.of(
                        "Royal Prince",
                        "Umayyad Noble Heir",
                        "Provincial Governor's Child"
                ),
                List.of()
        ));

        eventPool.add(new GameEvent(
                "The Brother's Claim",
                "Your brother declares that he is the better heir. Some support him out of fear. Some support him out of hope. The family line begins to crack.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Offer your brother an alliance",
                                "You offer him a place beside you. The household calms, but no one knows if his loyalty is real.",
                                Map.of(
                                        "familyLoyalty", 10,
                                        "morality", 6,
                                        "stress", 8
                                ),
                                Map.of(
                                        "familyCouncil", 12,
                                        "nobles", 4
                                ),
                                List.of(),
                                List.of("brother_allied")
                        ),
                        new Choice(
                                "Push your brother into exile",
                                "Your brother leaves the center of power. The household survives, but resentment travels with him.",
                                Map.of(
                                        "politicalPower", 10,
                                        "familyLoyalty", -12,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 8,
                                        "nobles", 6,
                                        "familyCouncil", -10
                                ),
                                List.of(),
                                List.of("brother_exiled")
                        ),
                        new Choice(
                                "Let shadow contacts remove him quietly",
                                "Your brother's claim disappears from public life. You gain space to rise, but the silence around it becomes part of your name.",
                                Map.of(
                                        "politicalPower", 16,
                                        "morality", -20,
                                        "stress", 15,
                                        "familyLoyalty", -15
                                ),
                                Map.of(
                                        "shadowNetwork", 18,
                                        "court", -5,
                                        "familyCouncil", -15
                                ),
                                List.of(
                                        ChoiceRequirement.factionAtLeast(
                                                "shadowNetwork",
                                                45,
                                                "Locked: Requires Shadow Network 45."
                                        )
                                ),
                                List.of("brother_removed")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("royal_succession_started"),
                List.of("brother_allied")
        ));

        eventPool.add(new GameEvent(
                "The Throne Opens",
                "The old ruler weakens, and the court begins looking for the next center of power. Your supporters ask whether you will step forward.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Seize the throne before rivals unite",
                                "politicalPower",
                                65,
                                "You move quickly. The court bends before your claim, and your name enters the line of rulers.",
                                "You move too soon. Rivals unite against you, and your claim collapses.",
                                Map.of(
                                        "politicalPower", 20,
                                        "reputation", 10,
                                        "stress", 15
                                ),
                                Map.of(
                                        "politicalPower", -18,
                                        "reputation", -15,
                                        "stress", 20
                                ),
                                Map.of(
                                        "court", 18,
                                        "nobles", 12,
                                        "military", 8
                                ),
                                Map.of(
                                        "court", -18,
                                        "nobles", -12,
                                        "shadowNetwork", 8
                                ),
                                List.of(),
                                List.of("took_throne"),
                                List.of("failed_claim")
                        ),
                        new Choice(
                                "Become regent instead of ruler",
                                "You do not take the crown directly. Instead, you rule through influence and patience.",
                                Map.of(
                                        "politicalPower", 14,
                                        "reputation", 6,
                                        "stress", 10
                                ),
                                Map.of(
                                        "court", 12,
                                        "nobles", 8,
                                        "familyCouncil", 6
                                ),
                                List.of(),
                                List.of("became_regent")
                        ),
                        new Choice(
                                "Step aside to preserve the family",
                                "You choose family survival over personal rule. Some call it wisdom. Others call it weakness.",
                                Map.of(
                                        "familyLoyalty", 16,
                                        "morality", 10,
                                        "politicalPower", -10,
                                        "stress", -5
                                ),
                                Map.of(
                                        "familyCouncil", 15,
                                        "scholars", 5,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("stepped_aside_for_family")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("royal_succession_started"),
                List.of("took_throne")
        ));

        eventPool.add(new GameEvent(
                "The Price of the Crown",
                "Power is now yours, but the crown does not bring peace. Old allies ask for payment. Old enemies wait for weakness.",
                "Legacy",
                List.of(
                        new Choice(
                                "Rule with mercy",
                                "Your rule becomes less feared but more stable. People begin to believe the crown can protect them.",
                                Map.of(
                                        "morality", 12,
                                        "reputation", 10,
                                        "stress", 8
                                ),
                                Map.of(
                                        "commonPeople", 12,
                                        "scholars", 8,
                                        "court", 5
                                ),
                                List.of(),
                                List.of("merciful_rule")
                        ),
                        new Choice(
                                "Break rival factions",
                                "You weaken those who might threaten you. The court obeys, but fear spreads through the palace.",
                                Map.of(
                                        "politicalPower", 18,
                                        "morality", -14,
                                        "stress", 12
                                ),
                                Map.of(
                                        "court", 12,
                                        "nobles", -10,
                                        "shadowNetwork", 10
                                ),
                                List.of(),
                                List.of("royal_purge")
                        ),
                        new Choice(
                                "Buy peace with wealth",
                                "You spend heavily to hold the realm together. Stability grows, but the treasury suffers.",
                                Map.of(
                                        "wealth", -18,
                                        "reputation", 8,
                                        "stress", 8
                                ),
                                Map.of(
                                        "merchants", 8,
                                        "nobles", 8,
                                        "commonPeople", 5
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                18,
                                                "Locked: Requires Wealth 18."
                                        )
                                ),
                                List.of("bought_royal_peace")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("took_throne"),
                List.of()
        ));

    }

        private static void addDisgracedBloodlineArc(List<GameEvent> eventPool) {
            eventPool.add(new GameEvent(
                    "The Stain on the Family Name",
                    "Your family name carries an old shame. Some people lower their voices when you enter. Others smile politely while remembering what your bloodline lost.",
                    "Youth",
                    List.of(
                            new Choice(
                                    "Work quietly to restore respect",
                                    "You decide that honor must be rebuilt slowly. No speech can repair what years of discipline must prove.",
                                    Map.of(
                                            "reputation", 8,
                                            "morality", 6,
                                            "stress", 8
                                    ),
                                    Map.of(
                                            "scholars", 6,
                                            "familyCouncil", 8,
                                            "commonPeople", 4
                                    ),
                                    List.of(),
                                    List.of("disgraced_bloodline_started", "quiet_honor_restoration")
                            ),
                            new Choice(
                                    "Hide the family shame",
                                    "You avoid speaking of the past. Silence protects you for now, but hidden shame grows heavier.",
                                    Map.of(
                                            "stress", 6,
                                            "reputation", 3,
                                            "morality", -3
                                    ),
                                    Map.of(
                                            "court", 4,
                                            "familyCouncil", -5
                                    ),
                                    List.of(),
                                    List.of("disgraced_bloodline_started", "family_shame_hidden")
                            ),
                            new Choice(
                                    "Use fear to silence gossip",
                                    "People stop speaking openly, but silence created by fear is not the same as respect.",
                                    Map.of(
                                            "politicalPower", 8,
                                            "morality", -10,
                                            "stress", 10
                                    ),
                                    Map.of(
                                            "shadowNetwork", 10,
                                            "commonPeople", -6,
                                            "familyCouncil", -5
                                    ),
                                    List.of(),
                                    List.of("disgraced_bloodline_started", "gossip_silenced_by_fear")
                            )
                    ),
                    List.of(),
                    List.of(),
                    List.of(
                            "Stable Household",
                            "Debt-Burdened Family",
                            "Recently Orphaned",
                            "Family Divided by Rivalry",
                            "Secret Noble Blood",
                            "Political Enemy of the Court",
                            "Exiled Branch",
                            "Favored by Local Scholars",
                            "Watched by Palace Spies"
                    )
            ));

            eventPool.add(new GameEvent(
                    "The Rival Recites the Past",
                    "A rival publicly reminds others of your family's disgrace. The room goes quiet. This is not only an insult; it is a test of whether your name can survive judgment.",
                    "Adulthood",
                    List.of(
                            new Choice(
                                    "Answer with dignity",
                                    "reputation",
                                    55,
                                    "You answer calmly and turn the insult into proof of your discipline. The room remembers your control.",
                                    "Your answer sounds weak. The rival's words damage your standing.",
                                    Map.of(
                                            "reputation", 12,
                                            "morality", 6,
                                            "stress", 8
                                    ),
                                    Map.of(
                                            "reputation", -10,
                                            "stress", 14
                                    ),
                                    Map.of(
                                            "scholars", 8,
                                            "commonPeople", 6,
                                            "familyCouncil", 6
                                    ),
                                    Map.of(
                                            "nobles", -6,
                                            "familyCouncil", -6
                                    ),
                                    List.of(),
                                    List.of("rival_insult_answered"),
                                    List.of("rival_insult_wounded_name")
                            ),
                            new Choice(
                                    "Expose the rival's own weakness",
                                    "You answer shame with shame. The rival is weakened, but the room sees that you can be cruel.",
                                    Map.of(
                                            "politicalPower", 10,
                                            "morality", -8,
                                            "stress", 10
                                    ),
                                    Map.of(
                                            "nobles", 6,
                                            "shadowNetwork", 8,
                                            "scholars", -5
                                    ),
                                    List.of(),
                                    List.of("rival_counter_exposed")
                            ),
                            new Choice(
                                    "Leave without answering",
                                    "You refuse to perform for the room. Some respect your restraint. Others call it cowardice.",
                                    Map.of(
                                            "stress", -4,
                                            "reputation", -6,
                                            "morality", 4
                                    ),
                                    Map.of(
                                            "familyCouncil", -4,
                                            "scholars", 4
                                    ),
                                    List.of(),
                                    List.of("walked_away_from_bloodline_insult")
                            )
                    ),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of("disgraced_bloodline_started"),
                    List.of()
            ));

            eventPool.add(new GameEvent(
                    "The Forgotten Record",
                    "You discover an old record that may explain the truth behind your family's disgrace. It could clear your name, deepen the scandal, or become a weapon.",
                    "Adulthood",
                    List.of(
                            new Choice(
                                    "Reveal the record honestly",
                                    "You bring the truth into daylight. Some old stories change, but truth does not obey anyone's convenience.",
                                    Map.of(
                                            "reputation", 10,
                                            "morality", 10,
                                            "stress", 8
                                    ),
                                    Map.of(
                                            "scholars", 12,
                                            "familyCouncil", 8,
                                            "court", -3
                                    ),
                                    List.of(),
                                    List.of("family_record_revealed")
                            ),
                            new Choice(
                                    "Alter the record to protect the family",
                                    "You reshape history before others can read it. Your family benefits, but truth becomes another sacrifice.",
                                    Map.of(
                                            "reputation", 8,
                                            "politicalPower", 8,
                                            "morality", -14,
                                            "stress", 10
                                    ),
                                    Map.of(
                                            "court", 6,
                                            "shadowNetwork", 10,
                                            "scholars", -8
                                    ),
                                    List.of(),
                                    List.of("family_record_forged")
                            ),
                            new Choice(
                                    "Destroy the record",
                                    "The record disappears. The past remains unclear, but the chance for full restoration disappears with it.",
                                    Map.of(
                                            "stress", -5,
                                            "morality", -8,
                                            "reputation", -5
                                    ),
                                    Map.of(
                                            "shadowNetwork", 5,
                                            "scholars", -10
                                    ),
                                    List.of(),
                                    List.of("family_record_destroyed")
                            )
                    ),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of("disgraced_bloodline_started"),
                    List.of("family_record_revealed", "family_record_forged", "family_record_destroyed")
            ));

            eventPool.add(new GameEvent(
                    "Honor Before the Court",
                    "The court finally faces the question of your bloodline. Your family name may be restored, reshaped, or abandoned forever.",
                    "Political Crisis",
                    List.of(
                            new Choice(
                                    "Ask the court to restore your family name",
                                    "reputation",
                                    60,
                                    "Your appeal succeeds. The family name no longer carries only shame.",
                                    "The court refuses. Your family name remains wounded before everyone.",
                                    Map.of(
                                            "reputation", 16,
                                            "familyLoyalty", 12,
                                            "stress", 10
                                    ),
                                    Map.of(
                                            "reputation", -14,
                                            "familyLoyalty", -8,
                                            "stress", 18
                                    ),
                                    Map.of(
                                            "court", 10,
                                            "familyCouncil", 15,
                                            "scholars", 8
                                    ),
                                    Map.of(
                                            "court", -10,
                                            "nobles", -8,
                                            "familyCouncil", -8
                                    ),
                                    List.of(),
                                    List.of("family_name_restored"),
                                    List.of("family_restoration_failed")
                            ),
                            new Choice(
                                    "Build a new name through power",
                                    "You stop asking for forgiveness. Instead, you make the world respect what you become.",
                                    Map.of(
                                            "politicalPower", 16,
                                            "reputation", 8,
                                            "morality", -5,
                                            "stress", 10
                                    ),
                                    Map.of(
                                            "court", 8,
                                            "nobles", 10,
                                            "military", 5
                                    ),
                                    List.of(),
                                    List.of("new_name_through_power")
                            ),
                            new Choice(
                                    "Abandon the old family name",
                                    "You choose to live beyond the wound of inheritance. The family council sees this as betrayal, but your life becomes your own.",
                                    Map.of(
                                            "stress", -8,
                                            "morality", 8,
                                            "familyLoyalty", -14,
                                            "reputation", 4
                                    ),
                                    Map.of(
                                            "familyCouncil", -15,
                                            "commonPeople", 6,
                                            "scholars", 4
                                    ),
                                    List.of(),
                                    List.of("old_name_abandoned")
                            )
                    ),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of("disgraced_bloodline_started"),
                    List.of()
            ));

            eventPool.add(new GameEvent(
                    "The Name They Remember",
                    "Years later, people no longer speak of your bloodline exactly as they once did. Whether through truth, power, or separation, you changed what the name meant.",
                    "Legacy",
                    List.of(
                            new Choice(
                                    "Teach descendants the restored truth",
                                    "You make sure the next generation knows both the shame and the recovery.",
                                    Map.of(
                                            "familyLoyalty", 12,
                                            "morality", 8,
                                            "reputation", 6
                                    ),
                                    Map.of(
                                            "familyCouncil", 12,
                                            "scholars", 6
                                    ),
                                    List.of(),
                                    List.of("legacy_restored_truth")
                            ),
                            new Choice(
                                    "Teach descendants to seek power first",
                                    "You teach them that respect is rarely given freely. It must be taken, protected, and used.",
                                    Map.of(
                                            "politicalPower", 12,
                                            "morality", -6,
                                            "stress", 6
                                    ),
                                    Map.of(
                                            "court", 8,
                                            "nobles", 8,
                                            "familyCouncil", 4
                                    ),
                                    List.of(),
                                    List.of("legacy_power_over_shame")
                            ),
                            new Choice(
                                    "Let the old story fade",
                                    "You allow silence to bury the past. Not every wound needs to become a monument.",
                                    Map.of(
                                            "stress", -6,
                                            "reputation", 4,
                                            "familyLoyalty", -3
                                    ),
                                    Map.of(
                                            "commonPeople", 4,
                                            "familyCouncil", -3
                                    ),
                                    List.of(),
                                    List.of("legacy_shame_faded")
                            )
                    ),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of("family_name_restored", "new_name_through_power", "old_name_abandoned"),
                    List.of()
            ));
        }


    private static void addExileReturnArc(List<GameEvent> eventPool) {
        eventPool.add(new GameEvent(
                "The Road Back",
                "Your family was pushed away from its old place, but exile does not erase memory. A chance appears to return near the center of power.",
                "Youth",
                List.of(
                        new Choice(
                                "Study the politics of the city before returning",
                                "You learn names, alliances, and old grudges. Return will not be simple, but you are less blind now.",
                                Map.of(
                                        "education", 10,
                                        "politicalPower", 5,
                                        "stress", 6
                                ),
                                Map.of(
                                        "scholars", 6,
                                        "court", 3
                                ),
                                List.of(),
                                List.of("exile_return_started", "studied_city_politics")
                        ),
                        new Choice(
                                "Seek a border patron",
                                "You search for someone with enough influence to open the road. Patronage can help, but it rarely comes free.",
                                Map.of(
                                        "politicalPower", 8,
                                        "stress", 8,
                                        "familyLoyalty", 4
                                ),
                                Map.of(
                                        "nobles", 8,
                                        "court", 5
                                ),
                                List.of(),
                                List.of("exile_return_started", "found_border_patron")
                        ),
                        new Choice(
                                "Wait and strengthen the family first",
                                "You delay the return. The family becomes more stable, but the old place moves further from your hands.",
                                Map.of(
                                        "familyLoyalty", 12,
                                        "wealth", 5,
                                        "politicalPower", -4,
                                        "stress", -4
                                ),
                                Map.of(
                                        "familyCouncil", 12
                                ),
                                List.of(),
                                List.of("exile_return_started", "waited_in_exile")
                        )
                ),
                List.of(),
                List.of(),
                List.of(
                        "Stable Household",
                        "Debt-Burdened Family",
                        "Disgraced Bloodline",
                        "Recently Orphaned",
                        "Family Divided by Rivalry",
                        "Secret Noble Blood",
                        "Political Enemy of the Court",
                        "Favored by Local Scholars",
                        "Watched by Palace Spies"
                )
        ));

        eventPool.add(new GameEvent(
                "The Gatekeeper's Price",
                "At the city gate, the return becomes real. A gate official recognizes your family name and quietly demands payment, proof, or obedience.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Pay the gatekeeper",
                                "The gate opens, but your return begins with humiliation and loss.",
                                Map.of(
                                        "wealth", -10,
                                        "stress", 8,
                                        "reputation", -3
                                ),
                                Map.of(
                                        "court", 3,
                                        "shadowNetwork", 4
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                10,
                                                "Locked: Requires Wealth 10."
                                        )
                                ),
                                List.of("paid_gatekeeper")
                        ),
                        new Choice(
                                "Argue using old family rights",
                                "education",
                                55,
                                "You speak with enough knowledge that the gatekeeper hesitates. Your name still carries legal weight.",
                                "Your argument fails. The gatekeeper mocks your claim and delays your entry.",
                                Map.of(
                                        "reputation", 8,
                                        "politicalPower", 6,
                                        "stress", 6
                                ),
                                Map.of(
                                        "reputation", -8,
                                        "stress", 12,
                                        "politicalPower", -4
                                ),
                                Map.of(
                                        "scholars", 8,
                                        "court", 5
                                ),
                                Map.of(
                                        "court", -6,
                                        "commonPeople", -3
                                ),
                                List.of(),
                                List.of("old_rights_recognized"),
                                List.of("return_delayed_at_gate")
                        ),
                        new Choice(
                                "Enter through hidden contacts",
                                "You avoid the gatekeeper entirely. The city receives you through shadows, not honor.",
                                Map.of(
                                        "politicalPower", 6,
                                        "morality", -6,
                                        "stress", 8
                                ),
                                Map.of(
                                        "shadowNetwork", 12,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("entered_city_through_shadows")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("exile_return_started"),
                List.of("returned_to_court")
        ));

        eventPool.add(new GameEvent(
                "The Old Enemy Remembers",
                "An old enemy of your family recognizes you in the city. They know why your branch was exiled, and they know how to make others afraid of your return.",
                "Adulthood",
                List.of(
                        new Choice(
                                "Confront the old enemy publicly",
                                "reputation",
                                58,
                                "Your public courage weakens the enemy's attack. People begin to wonder if your family was judged too harshly.",
                                "The confrontation goes badly. Your enemy makes your return look dangerous and foolish.",
                                Map.of(
                                        "reputation", 12,
                                        "politicalPower", 8,
                                        "stress", 10
                                ),
                                Map.of(
                                        "reputation", -12,
                                        "politicalPower", -6,
                                        "stress", 14
                                ),
                                Map.of(
                                        "commonPeople", 8,
                                        "nobles", 6
                                ),
                                Map.of(
                                        "nobles", -8,
                                        "court", -6
                                ),
                                List.of(),
                                List.of("old_enemy_confronted"),
                                List.of("old_enemy_emboldened")
                        ),
                        new Choice(
                                "Offer the enemy a private settlement",
                                "You try to bury old conflict quietly. Peace becomes possible, but some will call it weakness.",
                                Map.of(
                                        "wealth", -8,
                                        "stress", 6,
                                        "familyLoyalty", 6
                                ),
                                Map.of(
                                        "nobles", 6,
                                        "familyCouncil", 6
                                ),
                                List.of(
                                        ChoiceRequirement.statAtLeast(
                                                "wealth",
                                                8,
                                                "Locked: Requires Wealth 8."
                                        )
                                ),
                                List.of("old_enemy_settled")
                        ),
                        new Choice(
                                "Gather evidence against the enemy",
                                "You search for their own old crimes. A return to court may require more than innocence; it may require leverage.",
                                Map.of(
                                        "politicalPower", 8,
                                        "morality", -4,
                                        "stress", 10
                                ),
                                Map.of(
                                        "shadowNetwork", 10,
                                        "court", 3
                                ),
                                List.of(),
                                List.of("evidence_against_old_enemy")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("paid_gatekeeper", "old_rights_recognized", "entered_city_through_shadows"),
                List.of("old_enemy_settled")
        ));

        eventPool.add(new GameEvent(
                "The Court Hears the Exile",
                "Your return reaches the court. Some argue that your branch should be restored. Others say exile exists for a reason.",
                "Political Crisis",
                List.of(
                        new Choice(
                                "Ask for official restoration",
                                "politicalPower",
                                60,
                                "The court grants your branch a place again. Your exile does not vanish, but it no longer defines you.",
                                "The court refuses your request. Your return remains unofficial and fragile.",
                                Map.of(
                                        "politicalPower", 16,
                                        "reputation", 12,
                                        "familyLoyalty", 10,
                                        "stress", 10
                                ),
                                Map.of(
                                        "politicalPower", -12,
                                        "reputation", -10,
                                        "stress", 16
                                ),
                                Map.of(
                                        "court", 15,
                                        "nobles", 10,
                                        "familyCouncil", 12
                                ),
                                Map.of(
                                        "court", -12,
                                        "nobles", -8,
                                        "familyCouncil", -6
                                ),
                                List.of(),
                                List.of("exiled_branch_restored"),
                                List.of("restoration_refused")
                        ),
                        new Choice(
                                "Accept a smaller position",
                                "You return without full honor, but your family gains a foothold. Sometimes survival enters before dignity.",
                                Map.of(
                                        "politicalPower", 8,
                                        "familyLoyalty", 8,
                                        "stress", 6
                                ),
                                Map.of(
                                        "court", 8,
                                        "familyCouncil", 8
                                ),
                                List.of(),
                                List.of("minor_return_granted")
                        ),
                        new Choice(
                                "Reject the court and build power outside it",
                                "You stop begging for recognition. If the court will not restore you, you will build your own base.",
                                Map.of(
                                        "politicalPower", 14,
                                        "morality", -4,
                                        "stress", 12
                                ),
                                Map.of(
                                        "commonPeople", 8,
                                        "shadowNetwork", 8,
                                        "court", -12
                                ),
                                List.of(),
                                List.of("outside_power_base")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("exile_return_started"),
                List.of("exiled_branch_restored")
        ));

        eventPool.add(new GameEvent(
                "Return Written in Memory",
                "Years later, the story of exile and return becomes part of your household's identity. The next generation asks what the return truly meant.",
                "Legacy",
                List.of(
                        new Choice(
                                "Teach them patience and restoration",
                                "You teach that exile can be survived through patience, discipline, and careful return.",
                                Map.of(
                                        "familyLoyalty", 12,
                                        "morality", 8,
                                        "reputation", 6
                                ),
                                Map.of(
                                        "familyCouncil", 12,
                                        "scholars", 6
                                ),
                                List.of(),
                                List.of("legacy_of_restoration")
                        ),
                        new Choice(
                                "Teach them never to trust the court",
                                "You teach that power must be protected because courts can exile today and smile tomorrow.",
                                Map.of(
                                        "politicalPower", 10,
                                        "stress", 6,
                                        "morality", -4
                                ),
                                Map.of(
                                        "shadowNetwork", 8,
                                        "familyCouncil", 6,
                                        "court", -5
                                ),
                                List.of(),
                                List.of("legacy_of_distrust")
                        ),
                        new Choice(
                                "Teach them to build beyond old titles",
                                "You teach that a family can outgrow the place that rejected it.",
                                Map.of(
                                        "reputation", 8,
                                        "familyLoyalty", 6,
                                        "stress", -4
                                ),
                                Map.of(
                                        "commonPeople", 8,
                                        "merchants", 6
                                ),
                                List.of(),
                                List.of("legacy_beyond_exile")
                        )
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of("exiled_branch_restored", "minor_return_granted", "outside_power_base"),
                List.of()
        ));
    }

    private static void addPalaceSpyArc(List<GameEvent> eventPool) {

    }

    private static void addMarriageAllianceArc(List<GameEvent> eventPool) {

    }

    private static void addScholarsRiseArc(List<GameEvent> eventPool) {

    }

    private static void addHeresyAccusationArc(List<GameEvent> eventPool) {

    }

    private static void addMerchantCaravanArc(List<GameEvent> eventPool) {

    }

    private static void addGuildWarArc(List<GameEvent> eventPool) {

    }

    private static void addBreadRebellionArc(List<GameEvent> eventPool) {

    }

    private static void addTaxResistanceArc(List<GameEvent> eventPool) {

    }

    private static void addOrphanPatronArc(List<GameEvent> eventPool) {

    }

    private static void addMilitaryBarracksArc(List<GameEvent> eventPool) {

    }

    private static void addFrontierCommanderArc(List<GameEvent> eventPool) {

    }

    private static void addJanissaryLoyaltyArc(List<GameEvent> eventPool) {

    }

    private static void addShadowDebtArc(List<GameEvent> eventPool) {

    }

    private static void addDynastyTransitionArc(List<GameEvent> eventPool) {

    }

    private static void addFamineReliefArc(List<GameEvent> eventPool) {

    }

    private static void addFamilyFeudArc(List<GameEvent> eventPool) {

    }

    private static void addLegacyHeirArc(List<GameEvent> eventPool) {

    }
}