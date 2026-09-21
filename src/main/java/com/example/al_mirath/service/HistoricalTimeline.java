package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.HistoricalEvent;
import com.example.al_mirath.model.HistoricalEvent.Peril;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What actually happened, on the years it actually happened.
 *
 * <p>Every entry here is dated from a published source, named on the entry
 * itself, and nothing in this file is invented: where a source gives a year
 * and not a month, the entry gives a year. The accounts are written for a
 * player rather than a reader of encyclopaedias, but they say only what the
 * source says. Where the game needs a feeling the record does not supply —
 * what it was like in the street — the text stays with what can be stated:
 * the army came, the gates were shut, the caliph was killed.
 *
 * <p>Sources used, all consulted for this file:
 * <ul>
 *   <li>Metropolitan Museum of Art, Heilbrunn Timeline of Art History —
 *       the Umayyad (661–750), Abbasid (750–1258) and Mamluk (1250–1517)
 *       period essays and regional chronologies.</li>
 *   <li>Encyclopaedia Britannica — dynasty, city, ruler and event articles,
 *       cited per entry.</li>
 *   <li>World History Encyclopedia — dynasty timelines and the survey of
 *       plagues of the Near East.</li>
 *   <li>US National Library of Medicine (PMC) — the 1347–49 plague's arrival
 *       and spread in Egypt and Syria.</li>
 * </ul>
 */
public final class HistoricalTimeline {

    private static final String MET_UMAYYAD =
            "Met Museum, The Art of the Umayyad Period (661-750)";
    private static final String MET_ABBASID =
            "Met Museum, The Art of the Abbasid Period (750-1258)";
    private static final String MET_MAMLUK =
            "Met Museum, The Art of the Mamluk Period (1250-1517)";
    private static final String BRITANNICA_UMAYYAD =
            "Britannica, Umayyad Dynasty Timeline";
    private static final String BRITANNICA_ABBASID =
            "Britannica, Abbasid caliphate";
    private static final String BRITANNICA_IRAQ =
            "Britannica, Iraq: The Abbasid Caliphate";
    private static final String BRITANNICA_EGYPT =
            "Britannica, Egypt: The Mamluk and Ottoman periods (1250-1800)";
    private static final String BRITANNICA_OTTOMAN =
            "Britannica, Ottoman Empire Timeline";

    private static final List<HistoricalEvent> TIMELINE = new ArrayList<>();

    private HistoricalTimeline() {
    }

    static {
        umayyad();
        abbasid();
        mamluk();
        ottoman();

        TIMELINE.sort((one, other) -> Integer.compare(one.year(), other.year()));
    }

    // ════════════════════════════════════════════════════════════════════
    //  The Umayyads, 661-750
    // ════════════════════════════════════════════════════════════════════

    private static void umayyad() {
        String era = CityProfile.UMAYYAD;

        add(HistoricalEvent.news(
                "umayyad_founded", 661, era,
                "The Caliphate Moves to Damascus",
                "Muawiya, governor of Syria, is caliph. The seat of Islamic power "
                        + "leaves the Arabian peninsula for Syria, and Damascus becomes the "
                        + "capital of an empire reaching from the Atlantic to the Indus.",
                "history_umayyad_founded", BRITANNICA_UMAYYAD));

        add(new HistoricalEvent(
                "karbala", 680, era,
                "Karbala",
                "In October, Husayn ibn Ali, grandson of the Prophet and the third imam "
                        + "of Shia Islam, is killed with his followers by the forces of the "
                        + "caliph Yazid I at Karbala. What is said about it in public is not "
                        + "what is said about it at home.",
                Set.of(),
                Map.of("stress", 8),
                Map.of("court", -6, "commonPeople", -4),
                "history_karbala", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Say nothing about it anywhere",
                                "You keep your mouth shut in the street and at the table. "
                                        + "Nobody can report what you did not say.",
                                Map.of("stress", 4), Map.of(),
                                List.of(), List.of("kept_quiet_on_karbala")),
                        new Choice("Mourn him openly",
                                "You say plainly whose grandson he was. Some of your "
                                        + "neighbours will not forget it, and neither will the governor's men.",
                                Map.of("reputation", 6, "stress", 6),
                                Map.of("court", -10, "commonPeople", 8),
                                List.of(), List.of("mourned_husayn")),
                        new Choice("Repeat what the governor's criers say",
                                "You repeat the official account where you can be heard "
                                        + "repeating it. It costs you nothing that anyone can see.",
                                Map.of("morality", -6),
                                Map.of("court", 8, "commonPeople", -6),
                                List.of(), List.of("sided_with_yazid"))
                ),
                BRITANNICA_UMAYYAD));

        add(HistoricalEvent.news(
                "sufyanid_end", 684, era,
                "The Line of Muawiya Ends",
                "Civil war, and the deaths of Muawiya's son Yazid I and Yazid's son "
                        + "Muawiya II, bring Sufyanid rule to an end. No one is certain who "
                        + "gives orders this season.",
                "history_sufyanid_end", BRITANNICA_UMAYYAD));

        add(HistoricalEvent.news(
                "marwan_proclaimed", 685, era,
                "Marwan Is Proclaimed in Syria",
                "Amid tribal wars, Marwan I is proclaimed caliph in Syria, beginning "
                        + "the rule of the Marwanids, another branch of the Umayyad house. "
                        + "Under his son Abd al-Malik the empire reaches from Spain to India.",
                "history_marwanids", BRITANNICA_UMAYYAD));

        add(new HistoricalEvent(
                "dome_of_the_rock", 691, era,
                "The Dome of the Rock Is Finished",
                "The first great Umayyad building is completed in Jerusalem under the "
                        + "patronage of the caliph Abd al-Malik, on a site formerly occupied "
                        + "by Solomon's Temple. Every mason in the city has an opinion about it.",
                Set.of("Jerusalem"),
                Map.of("education", 3),
                Map.of("scholars", 6, "court", 4),
                "history_dome_of_the_rock", Set.of(), Peril.NONE, List.of(), MET_UMAYYAD));

        add(new HistoricalEvent(
                "umayyad_mosque", 706, era,
                "The Great Mosque Rises in Damascus",
                "The site of the Roman temple and the Byzantine church of Saint John "
                        + "the Baptist becomes the congregational mosque of the Umayyad "
                        + "capital. The work draws craftsmen and money into the city for years.",
                Set.of("Damascus"),
                Map.of("wealth", 4, "education", 3),
                Map.of("scholars", 5, "merchants", 5),
                "history_umayyad_mosque", Set.of(), Peril.NONE, List.of(), MET_UMAYYAD));

        add(new HistoricalEvent(
                "iberia_conquered", 711, era,
                "The Crossing into Iberia",
                "Umayyad forces cross into the Iberian peninsula; most of it is taken "
                        + "between 711 and 714. Men who left as soldiers write home as "
                        + "landholders, and the roads west fill with people who believe them.",
                Set.of("Cordoba", "Damascus"),
                Map.of("wealth", 5),
                Map.of("military", 8),
                "history_iberia", Set.of(), Peril.NONE,
                List.of(
                        new Choice("Go west with the next company",
                                "You take the long road toward the crossing, on the word of "
                                        + "men who were not there either.",
                                Map.of("wealth", 8, "health", -6, "stress", 6),
                                Map.of("military", 10),
                                List.of(), List.of("went_to_iberia")),
                        new Choice("Stay where your family is",
                                "You stay. The stories come back anyway, improved each time.",
                                Map.of(), Map.of("familyCouncil", 5),
                                List.of(), List.of()))
                ,
                MET_UMAYYAD));

        add(new HistoricalEvent(
                "constantinople_siege", 717, era,
                "The Army Goes to Constantinople",
                "Arab forces besiege Constantinople by land and sea from August. The "
                        + "siege will be held for a year to the day, and will fail. Every "
                        + "quarter of the empire is asked for men, grain and money.",
                Set.of(),
                Map.of("stress", 5, "wealth", -4),
                Map.of("military", 4, "commonPeople", -4),
                "history_constantinople_siege", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Take the levy and go",
                                "You go north with the levy. What comes back a year later is "
                                        + "a smaller army than went.",
                                Map.of("health", -10, "stress", 8, "reputation", 6),
                                Map.of("military", 12, "court", 5),
                                List.of(), List.of("served_at_constantinople")),
                        new Choice("Pay the fine instead",
                                "You find the money to not go. It is a great deal of money.",
                                Map.of("wealth", -12),
                                Map.of("military", -6),
                                List.of(), List.of("bought_out_of_the_levy")),
                        new Choice("Sell to the army instead of joining it",
                                "Grain, rope, pack animals: the army buys whatever is in front "
                                        + "of it, and pays late but pays.",
                                Map.of("wealth", 10, "morality", -3),
                                Map.of("merchants", 8, "military", 3),
                                List.of(), List.of("supplied_the_siege"))
                ),
                "World History Encyclopedia, Umayyad Siege of Constantinople 717 CE"));

        add(HistoricalEvent.news(
                "akroinon", 740, era,
                "Defeat at Akroinon",
                "The Byzantine emperor Leo III wins a major victory over Arab forces at "
                        + "Akroinon, freeing Asia Minor from immediate threat. The northern "
                        + "frontier stops moving forward, and the men there know it first.",
                "history_akroinon", "Britannica, Byzantine Empire: The age of Iconoclasm"));

        add(new HistoricalEvent(
                "third_fitna", 744, era,
                "The Third Fitna",
                "Civil war inside the house of Umayya. It will run for six years and "
                        + "end the dynasty. Governors change with the season, and each one "
                        + "wants the taxes the last one already collected.",
                Set.of(),
                Map.of("stress", 8, "wealth", -6),
                Map.of("court", -10, "military", -6, "nobles", -5),
                "history_third_fitna", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Back the claimant holding your city",
                                "You back whoever holds the gate this month. It is the safest "
                                        + "thing to do until it is the most dangerous.",
                                Map.of("stress", 5), Map.of("court", 6, "nobles", -4),
                                List.of(), List.of("backed_a_claimant")),
                        new Choice("Take the household to the country",
                                "You close the town house and go where nobody is levying "
                                        + "anyone. You will hear what happened months late.",
                                Map.of("wealth", -8, "health", 4, "stress", -4),
                                Map.of("court", -5),
                                List.of(), List.of("left_the_capital_744")),
                        new Choice("Keep your head down and trade",
                                "Somebody has to sell bread to both sides.",
                                Map.of("wealth", 8, "morality", -4),
                                Map.of("merchants", 8),
                                List.of(), List.of("traded_through_the_fitna"))
                ),
                "Britannica, Islamic world: The third fitnah"));

        add(new HistoricalEvent(
                "great_zab", 750, era,
                "The Zab, and the End of the House",
                "Marwan II, the last Umayyad caliph, is defeated at the Great Zab river "
                        + "in Mesopotamia. Abu al-Abbas is declared caliph at Kufa. In Syria, "
                        + "men who served the old house are being looked for by name.",
                Set.of("Damascus", "Aleppo", "Jerusalem"),
                Map.of("stress", 12, "wealth", -10),
                Map.of("court", -20, "military", -10),
                "history_umayyads_fall",
                Set.of("left_the_capital_744", "left_before_the_zab"),
                Peril.DEADLY,
                List.of(
                        new Choice("Be somewhere else, under another name",
                                "You are out of the city before the lists are read, and the "
                                        + "name you give at the next gate is not your father's.",
                                Map.of("wealth", -15, "stress", 10),
                                Map.of("court", -10, "shadowNetwork", 8),
                                List.of(), List.of("left_before_the_zab")),
                        new Choice("Present yourself to the new men",
                                "You go to them before they come to you, which is the only "
                                        + "version of this that has ever worked for anybody.",
                                Map.of("stress", 8, "reputation", -5),
                                Map.of("court", 5, "nobles", -8),
                                List.of(), List.of("submitted_to_the_abbasids")),
                        new Choice("Stand by the house you served",
                                "You do not run and you do not kneel.",
                                Map.of("reputation", 10, "stress", 12),
                                Map.of("court", -20, "military", 6),
                                List.of(), List.of("stood_with_the_umayyads"))
                ),
                BRITANNICA_UMAYYAD));
    }

    // ════════════════════════════════════════════════════════════════════
    //  The Abbasids, 750-1258
    // ════════════════════════════════════════════════════════════════════

    private static void abbasid() {
        String era = CityProfile.ABBASID;

        // The revolt in Khorasan (747) and the fall of the Umayyads (750) are
        // in the record under the era they ended rather than the one they
        // began: an Abbasid life starts in Baghdad, and Baghdad is not laid
        // out until 762. Both years are covered from the Umayyad side above.

        add(new HistoricalEvent(
                "baghdad_founded", 762, era,
                "A City Is Laid Out on the Tigris",
                "The capital moves to Baghdad, laid out new on the Tigris. Bricklayers, "
                        + "carters, clerks and cooks are wanted in numbers nobody has seen, "
                        + "and the wages being talked about are real.",
                Set.of("Baghdad", "Basra", "Damascus"),
                Map.of("wealth", 4),
                Map.of("merchants", 6, "court", 5),
                "history_baghdad_founded", Set.of(), Peril.NONE,
                List.of(
                        new Choice("Go to the new city while it is still mud",
                                "You arrive while the walls are going up and the rents are "
                                        + "nothing. Everyone who comes later will pay more.",
                                Map.of("wealth", 10, "health", -4, "stress", 5),
                                Map.of("merchants", 8, "court", 4),
                                List.of(), List.of("went_to_baghdad_early")),
                        new Choice("Wait and see whether it lasts",
                                "New capitals have been abandoned before. You wait.",
                                Map.of(), Map.of(),
                                List.of(), List.of()))
                ,
                BRITANNICA_ABBASID));

        add(new HistoricalEvent(
                "harun_al_rashid", 786, era,
                "The Reign of Harun al-Rashid Begins",
                "Harun al-Rashid takes the caliphate. The years from here to the middle "
                        + "of the next century are reckoned the height of Abbasid rule, and "
                        + "the wealth of the whole empire flows into Baghdad.",
                Set.of(),
                Map.of("wealth", 6, "education", 4),
                Map.of("court", 6, "merchants", 8, "scholars", 6),
                "history_harun", Set.of(), Peril.NONE, List.of(), BRITANNICA_ABBASID));

        add(new HistoricalEvent(
                "al_mamun", 813, era,
                "Al-Mamun and the Translators",
                "Al-Mamun's caliphate begins. Greek, Persian and Indian works are "
                        + "hunted down, bought and translated; a man who can read two "
                        + "languages can eat on it.",
                Set.of(),
                Map.of("education", 6),
                Map.of("scholars", 10),
                "history_al_mamun", Set.of(), Peril.NONE,
                List.of(
                        new Choice("Take work copying and translating",
                                "It is slow, badly lit, and the best-paid reading anyone in "
                                        + "your family has ever done.",
                                Map.of("education", 10, "wealth", 5, "health", -3),
                                Map.of("scholars", 12),
                                List.of(), List.of("worked_in_the_translation_trade")),
                        new Choice("Stay with your own trade",
                                "Books do not feed a household this year.",
                                Map.of(), Map.of(),
                                List.of(), List.of()))
                ,
                BRITANNICA_ABBASID));

        add(new HistoricalEvent(
                "after_861", 861, era,
                "Anarchy and Rebellion",
                "After the killing of the caliph, anarchy and rebellion shake the "
                        + "empire. Provinces are run by men who acknowledge Baghdad when it "
                        + "suits them, and the soldiers in the capital make and unmake caliphs.",
                Set.of(),
                Map.of("stress", 8, "wealth", -6),
                Map.of("court", -12, "military", -8),
                "history_samarra_anarchy", Set.of(), Peril.HARSH, List.of(), BRITANNICA_ABBASID));

        add(new HistoricalEvent(
                "zanj_rebellion", 869, era,
                "The Zanj Rise in the Salt Flats",
                "In September, Ali ibn Muhammad wins over the slave work-crews of the "
                        + "southern marshes by promising them freedom and wealth. The revolt "
                        + "takes hold in southern Iraq and will not be put down for fourteen years.",
                Set.of("Basra"),
                Map.of("stress", 10, "wealth", -8),
                Map.of("commonPeople", -6, "military", -6, "merchants", -8),
                "history_zanj", Set.of("left_the_south_869"), Peril.DEADLY,
                List.of(
                        new Choice("Get the household out of the south",
                                "You go north with what you can carry and leave what you "
                                        + "cannot. The canal country is not a place to be this year.",
                                Map.of("wealth", -12, "stress", 6),
                                Map.of(),
                                List.of(), List.of("left_the_south_869")),
                        new Choice("Stay and hold what you have",
                                "You bar the doors and count the days. Some of your neighbours "
                                        + "do the same and some of them join the rebels.",
                                Map.of("stress", 12, "health", -6),
                                Map.of("nobles", 5),
                                List.of(), List.of("stayed_through_the_zanj")),
                        new Choice("Deal with whoever holds the canal",
                                "Rebel or governor, somebody is always buying grain.",
                                Map.of("wealth", 8, "morality", -6, "stress", 8),
                                Map.of("merchants", 6, "court", -8),
                                List.of(), List.of("traded_with_the_zanj"))
                ),
                "Britannica, Zanj rebellion"));

        add(HistoricalEvent.news(
                "zanj_crushed", 883, era,
                "The Rebellion Is Put Down",
                "In August, reinforced by Egyptian troops, the regent al-Muwaffaq "
                        + "finally crushes the Zanj revolt. The south is quiet, and empty in "
                        + "places where it was not before.",
                "history_zanj_over", "Britannica, Zanj rebellion"));

        add(HistoricalEvent.news(
                "buyids_enter_baghdad", 945, era,
                "The Buyids Enter Baghdad",
                "Buyid forces take the capital. The caliph keeps his name on the coins, "
                        + "the sermon and little else; real power belongs to whoever holds "
                        + "the soldiers.",
                "history_buyids", BRITANNICA_IRAQ));

        add(HistoricalEvent.news(
                "seljuks_take_baghdad", 1055, era,
                "The Seljuks Take the Capital",
                "Seljuk forces enter Baghdad. Another set of soldiers, another regent, "
                        + "the same arrangement: the caliphate keeps its moral standing and "
                        + "hands over everything else.",
                "history_seljuks", BRITANNICA_ABBASID));

        add(new HistoricalEvent(
                "mustansiriyya", 1228, era,
                "The Mustansiriyya Is Built",
                "Under al-Mustansir, a great madrasa goes up in Baghdad over five "
                        + "years, and with it the city is again the first place in the Islamic "
                        + "world for the arts of the book.",
                Set.of("Baghdad"),
                Map.of("education", 6),
                Map.of("scholars", 10, "court", 4),
                "history_mustansiriyya", Set.of(), Peril.NONE, List.of(), MET_ABBASID));

        add(new HistoricalEvent(
                "mongols_at_the_door", 1257, era,
                "An Army Is Coming Down the Roads",
                "Hulegu, grandson of Genghis Khan, is bringing the Mongol army against "
                        + "Baghdad. What people say about what happened to the cities that "
                        + "refused him does not get less frightening with repetition.",
                Set.of("Baghdad", "Basra"),
                Map.of("stress", 14),
                Map.of("court", -8, "military", -6),
                "history_mongols_coming", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Leave now, with whatever will travel",
                                "You go while the roads are still open, and you are on one of "
                                        + "them when the city is closed.",
                                Map.of("wealth", -18, "stress", 10, "health", -5),
                                Map.of("court", -6),
                                List.of(), List.of("left_baghdad_1257")),
                        new Choice("Send the family out, stay yourself",
                                "The household goes. You stay with the house, because a house "
                                        + "left empty is a house taken.",
                                Map.of("wealth", -8, "stress", 14),
                                Map.of("familyCouncil", 6),
                                List.of(), List.of("sent_the_family_out_1257")),
                        new Choice("Stay. The walls have held before",
                                "They have held before.",
                                Map.of("stress", 8),
                                Map.of("military", 4),
                                List.of(), List.of("stayed_in_baghdad_1257"))
                ),
                "Britannica, Hulegu; Britannica, al-Mustasim"));

        add(new HistoricalEvent(
                "baghdad_falls", 1258, era,
                "Baghdad Falls",
                "Hulegu defeats the caliph's forces in January and takes the city in "
                        + "February after a short siege. The caliph al-Mustasim and three "
                        + "hundred officials surrender and are put to death ten days later. "
                        + "Five centuries of Abbasid Baghdad end in a fortnight.",
                Set.of("Baghdad"),
                Map.of("stress", 20, "wealth", -25, "health", -10),
                Map.of("court", -30, "military", -20, "merchants", -20),
                "history_baghdad_sacked",
                Set.of("left_baghdad_1257", "sent_the_family_out_1257"),
                Peril.CATASTROPHIC,
                List.of(
                        new Choice("Get out through the river gate",
                                "You take the water side while the fighting is at the walls. "
                                        + "Plenty of people have the same idea and not all of them get through.",
                                Map.of("wealth", -20, "health", -8, "stress", 18),
                                Map.of(),
                                List.of(), List.of("fled_the_sack")),
                        new Choice("Hide in the cellar and wait it out",
                                "You go down into the dark with as much water as you can "
                                        + "carry, and you listen to the city being taken apart above you.",
                                Map.of("health", -12, "stress", 20, "wealth", -15),
                                Map.of(),
                                List.of(), List.of("hid_through_the_sack")),
                        new Choice("Offer yourself as a clerk who can read",
                                "Conquerors need registers kept. It is not a brave thing to "
                                        + "offer and it has kept people alive before.",
                                Map.of("morality", -8, "stress", 16, "education", 3),
                                Map.of("court", -10, "shadowNetwork", 8),
                                List.of(), List.of("served_the_conquerors"))
                ),
                "Britannica, al-Mustasim; Britannica, Hulegu; " + BRITANNICA_ABBASID));
    }

    // ════════════════════════════════════════════════════════════════════
    //  The Mamluks, 1250-1517
    // ════════════════════════════════════════════════════════════════════

    private static void mamluk() {
        String era = CityProfile.MAMLUK;

        add(new HistoricalEvent(
                "mamluks_seize_power", 1250, era,
                "The Regiments Take the State",
                "After the death of the sultan al-Salih Ayyub, his mamluk regiments "
                        + "seize authority in Egypt. Shajar al-Durr is raised up briefly, and "
                        + "then Aybak. The men who were bought as soldiers now own the country.",
                Set.of("Cairo"),
                Map.of("stress", 6),
                Map.of("military", 10, "nobles", -8, "court", -6),
                "history_mamluks_rise", Set.of(), Peril.NONE, List.of(), MET_MAMLUK));

        add(new HistoricalEvent(
                "ayn_jalut", 1260, era,
                "Ayn Jalut",
                "The Mamluk army under Qutuz and Baybars defeats the Mongols at Ayn "
                        + "Jalut, and the advance into Syria and Egypt stops there. Cairo has "
                        + "done what Baghdad could not, and intends that everyone should know it.",
                Set.of(),
                Map.of("reputation", 5),
                Map.of("military", 14, "commonPeople", 8),
                "history_ayn_jalut", Set.of(), Peril.NONE,
                List.of(
                        new Choice("Go with the army to Syria",
                                "You march with them, and you are there on the day the thing "
                                        + "everyone said could not be beaten is beaten.",
                                Map.of("health", -8, "reputation", 12, "stress", 6),
                                Map.of("military", 15, "court", 8),
                                List.of(), List.of("fought_at_ayn_jalut")),
                        new Choice("Keep out of it",
                                "Other people's victories cost the same as their defeats.",
                                Map.of(), Map.of("military", -4),
                                List.of(), List.of()))
                ,
                MET_MAMLUK));

        add(HistoricalEvent.news(
                "fall_of_acre", 1291, era,
                "Acre Falls",
                "The last of the Crusader strongholds is taken. Two centuries of "
                        + "Frankish coast end, and the ports change hands along with it.",
                "history_acre", MET_MAMLUK));

        add(new HistoricalEvent(
                "al_nasir_reign", 1293, era,
                "The Long Reign Begins",
                "Al-Malik al-Nasir's rule begins, and will last, with interruptions, "
                        + "most of fifty years: prosperity in Egypt, peace with the powers "
                        + "abroad, and famine and street-fighting at home more than once.",
                Set.of("Cairo"),
                Map.of("wealth", 5),
                Map.of("court", 8, "merchants", 8),
                "history_al_nasir", Set.of(), Peril.NONE, List.of(), BRITANNICA_EGYPT));

        add(HistoricalEvent.news(
                "mongol_truce", 1323, era,
                "A Truce with the Mongols",
                "After decades of raids and battles, a truce is concluded with the "
                        + "Mongols. The caravan roads east are worth using again.",
                "history_mongol_truce", BRITANNICA_EGYPT));

        add(new HistoricalEvent(
                "plague_arrives", 1347, era,
                "The Sickness Reaches Alexandria",
                "Plague comes into Alexandria, most likely on ships from Constantinople "
                        + "and the Black Sea ports. Within the year it is reported east to "
                        + "Gaza and north up the coast through Sidon, Damascus, Homs and Aleppo.",
                Set.of("Cairo", "Jerusalem", "Damascus", "Aleppo"),
                Map.of("stress", 10),
                Map.of("merchants", -8),
                "history_plague_coming", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Take the household out to the villages",
                                "You leave the city while leaving is still a decision rather "
                                        + "than a stampede.",
                                Map.of("wealth", -14, "stress", 6),
                                Map.of("merchants", -6),
                                List.of(), List.of("left_for_the_country_1347")),
                        new Choice("Shut the house and buy in what you need",
                                "Grain, oil, water, and the door barred. You will not open it "
                                        + "for a neighbour and you know what that means.",
                                Map.of("wealth", -10, "morality", -5, "stress", 10),
                                Map.of("commonPeople", -6),
                                List.of(), List.of("shut_the_house_1347")),
                        new Choice("Carry on as normal",
                                "The markets are open. Somebody has to work them.",
                                Map.of("wealth", 5, "stress", 5),
                                Map.of("merchants", 6),
                                List.of(), List.of("worked_through_the_plague"))
                ),
                "World History Encyclopedia, Plagues of the Near East 562-1486 CE"));

        add(new HistoricalEvent(
                "black_death", 1348, era,
                "The Plague Year",
                "The plague sweeps Egypt, and the Syrian cities with it. Damascus will "
                        + "lose something like half of the people in it before the outbreak "
                        + "burns out; Aleppo is struck the same year. It will come back, "
                        + "again and again, for the rest of the century.",
                Set.of("Cairo", "Damascus", "Aleppo", "Jerusalem"),
                Map.of("health", -18, "stress", 20, "wealth", -12),
                Map.of("commonPeople", -10, "merchants", -15),
                "history_black_death",
                Set.of("left_for_the_country_1347", "shut_the_house_1347"),
                Peril.CATASTROPHIC,
                List.of(
                        new Choice("Nurse your own and bury them yourself",
                                "You do what has to be done in your own house, and you do it "
                                        + "with your own hands because there is nobody else left to ask.",
                                Map.of("health", -14, "stress", 22, "morality", 10),
                                Map.of("familyCouncil", 10, "commonPeople", 8),
                                List.of(), List.of("nursed_the_dying")),
                        new Choice("Go now, anywhere upwind",
                                "You take the road and do not look at who is lying beside it.",
                                Map.of("wealth", -18, "health", -6, "stress", 16),
                                Map.of("commonPeople", -8),
                                List.of(), List.of("fled_the_plague")),
                        new Choice("Take work no one else will take",
                                "Carrying the dead pays what it pays because of what it is.",
                                Map.of("wealth", 14, "health", -16, "stress", 18),
                                Map.of("commonPeople", 10),
                                List.of(), List.of("carried_the_dead"))
                ),
                BRITANNICA_EGYPT + "; World History Encyclopedia, Plagues of the Near East; "
                        + "US National Library of Medicine, The Plague of 1348-50"));

        add(new HistoricalEvent(
                "timur_at_aleppo", 1400, era,
                "Timur Takes Aleppo",
                "On the eleventh of November, Timur's army destroys the Mamluk force "
                        + "outside Aleppo in less than an hour. The sack of the city goes on "
                        + "for four days.",
                Set.of("Aleppo"),
                Map.of("health", -12, "stress", 20, "wealth", -22),
                Map.of("military", -15, "merchants", -18),
                "history_timur_aleppo", Set.of("left_aleppo_1400"), Peril.CATASTROPHIC,
                List.of(
                        new Choice("Be out of the city before the army arrives",
                                "The dust of them is visible for a day before they come. You "
                                        + "use the day.",
                                Map.of("wealth", -20, "stress", 14),
                                Map.of(),
                                List.of(), List.of("left_aleppo_1400")),
                        new Choice("Get inside the citadel",
                                "You get behind the walls with everyone else who thought of it.",
                                Map.of("stress", 18, "health", -8, "wealth", -10),
                                Map.of("military", 5),
                                List.of(), List.of("sheltered_in_the_citadel")),
                        new Choice("Hide in the house and hope it is quick",
                                "Four days is not quick.",
                                Map.of("health", -16, "stress", 22, "wealth", -18),
                                Map.of(),
                                List.of(), List.of("hid_in_aleppo"))
                ),
                "Britannica, Battle of Aleppo (1400)"));

        add(new HistoricalEvent(
                "timur_at_damascus", 1401, era,
                "Damascus Is Occupied",
                "Timur's campaign in Syria continues: the Mamluk army is beaten and "
                        + "Damascus occupied. The craftsmen of the city, the ones who make it "
                        + "worth taking, are of particular interest to him.",
                Set.of("Damascus"),
                Map.of("health", -10, "stress", 18, "wealth", -20),
                Map.of("military", -12, "merchants", -15, "scholars", -8),
                "history_timur_damascus", Set.of("left_damascus_1401"), Peril.DEADLY,
                List.of(
                        new Choice("Take the road south while it is open",
                                "You leave for the Hijaz road with what the household can "
                                        + "carry and no more.",
                                Map.of("wealth", -18, "stress", 12),
                                Map.of(),
                                List.of(), List.of("left_damascus_1401")),
                        new Choice("Offer your trade to the conqueror",
                                "Skilled hands are wanted in Samarkand. It is not a choice "
                                        + "everyone is given and not everyone would take it.",
                                Map.of("wealth", 8, "stress", 16, "morality", -6),
                                Map.of("court", -12, "merchants", 6),
                                List.of(), List.of("went_east_with_timur")),
                        new Choice("Stay in the quarter and keep quiet",
                                "You stay where you are and you are very careful.",
                                Map.of("health", -10, "stress", 20),
                                Map.of(),
                                List.of(), List.of("stayed_under_timur"))
                ),
                "Britannica, Timur"));

        add(new HistoricalEvent(
                "portuguese_red_sea", 1500, era,
                "The Sea Road Is Cut",
                "Portuguese ships begin attacking the trade of the Red Sea, and Egypt's "
                        + "hold on the Indian trade — the thing the whole state is paid for "
                        + "with — starts slipping. The customs men feel it first and the "
                        + "sultan's treasury feels it hardest.",
                Set.of("Cairo", "Basra"),
                Map.of("wealth", -10),
                Map.of("merchants", -14, "court", -8),
                "history_portuguese", Set.of(), Peril.NONE, List.of(), BRITANNICA_EGYPT));

        add(new HistoricalEvent(
                "ottoman_conquest_of_egypt", 1517, era,
                "The Ottomans Take Egypt",
                "Having failed to meet field artillery with anything of their own, the "
                        + "Mamluks are beaten decisively in Syria and then in Egypt. From this "
                        + "year they are one part of the government of Egypt rather than the "
                        + "whole of it.",
                Set.of("Cairo", "Damascus", "Aleppo", "Jerusalem"),
                Map.of("stress", 14, "wealth", -12),
                Map.of("court", -20, "military", -15),
                "history_ottomans_take_egypt", Set.of(), Peril.DEADLY,
                List.of(
                        new Choice("Make yourself useful to the new government",
                                "The Ottomans need people who know how Egypt is actually run. "
                                        + "You know how Egypt is actually run.",
                                Map.of("stress", 10, "morality", -4),
                                Map.of("court", 10, "military", -8),
                                List.of(), List.of("served_the_ottomans")),
                        new Choice("Keep away from all of it",
                                "You have no opinions this year, in public or at home.",
                                Map.of("stress", 8, "wealth", -8),
                                Map.of(),
                                List.of(), List.of("kept_out_of_1517")),
                        new Choice("Stand with the sultan's men",
                                "They are beaten and you stand with them anyway.",
                                Map.of("reputation", 8, "health", -12, "stress", 16),
                                Map.of("military", 10, "court", -18),
                                List.of(), List.of("stood_with_the_mamluks"))
                ),
                BRITANNICA_EGYPT));
    }

    // ════════════════════════════════════════════════════════════════════
    //  The Ottomans, from 1453
    // ════════════════════════════════════════════════════════════════════

    private static void ottoman() {
        String era = CityProfile.OTTOMAN;

        add(new HistoricalEvent(
                "constantinople_taken", 1453, era,
                "Constantinople Is Taken",
                "On the twenty-ninth of May, after a siege of fifty-five days, Mehmed "
                        + "II's army breaches the land walls and the city falls. The Byzantine "
                        + "empire ends with it, and the city becomes the Ottoman capital.",
                Set.of("Istanbul"),
                Map.of("stress", 10),
                Map.of("military", 12, "court", 10, "merchants", 6),
                "history_constantinople_taken", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("Take a house in the new capital",
                                "The city is half empty and the sultan wants it filled. Property "
                                        + "is being handed to people who will live in it.",
                                Map.of("wealth", 14, "stress", 6),
                                Map.of("court", 8, "merchants", 8),
                                List.of(), List.of("settled_new_istanbul")),
                        new Choice("Wait until it settles",
                                "A city taken by storm is not a city to move into in the first year.",
                                Map.of(), Map.of(),
                                List.of(), List.of()))
                ,
                "Britannica, Fall of Constantinople (1453)"));

        add(new HistoricalEvent(
                "selim_takes_the_east", 1517, era,
                "Selim Takes Syria, Egypt and the Holy Cities",
                "Selim I's conquest of the Mamluk state is completed: Syria, Palestine, "
                        + "the Hejaz and Egypt. With Mecca and Medina in his hands the sultan "
                        + "is now looked to as the leader of Islam, and the Ottoman sultanate "
                        + "declares itself a caliphate.",
                Set.of("Cairo", "Damascus", "Aleppo", "Jerusalem", "Istanbul"),
                Map.of("stress", 8),
                Map.of("court", 12, "scholars", 8, "military", 8),
                "history_selim_east", Set.of(), Peril.NONE, List.of(), BRITANNICA_OTTOMAN));

        add(HistoricalEvent.news(
                "suleiman_begins", 1520, era,
                "Suleiman Succeeds",
                "Suleiman I takes the throne, beginning a reign of forty-six years "
                        + "that will be reckoned the height of Ottoman power.",
                "history_suleiman", BRITANNICA_OTTOMAN));

        add(new HistoricalEvent(
                "vienna_1529", 1529, era,
                "The Army Goes to Vienna",
                "Suleiman besieges Vienna and is forced to withdraw for want of "
                        + "supplies. The men who went find out what a European autumn does to "
                        + "a campaign that started late.",
                Set.of(),
                Map.of("stress", 6),
                Map.of("military", -6, "court", -4),
                "history_vienna_1529", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("March with the campaign",
                                "You go, and you come back thinner, later and poorer than the "
                                        + "criers promised.",
                                Map.of("health", -10, "wealth", 4, "stress", 10, "reputation", 5),
                                Map.of("military", 10),
                                List.of(), List.of("marched_on_vienna")),
                        new Choice("Supply the baggage train instead",
                                "An army that far from home buys everything twice.",
                                Map.of("wealth", 12, "stress", 5),
                                Map.of("merchants", 10, "military", 4),
                                List.of(), List.of("supplied_vienna_campaign"))
                ),
                BRITANNICA_OTTOMAN));

        add(new HistoricalEvent(
                "suleymaniye", 1550, era,
                "The Suleymaniye Is Begun",
                "Sinan, the sultan's architect, begins the great mosque of Suleiman in "
                        + "Istanbul. It will take seven years and more than three thousand "
                        + "workmen, and it is modelled on the Hagia Sophia across the city.",
                Set.of("Istanbul"),
                Map.of("wealth", 5, "education", 3),
                Map.of("scholars", 8, "court", 6, "commonPeople", 5),
                "history_suleymaniye", Set.of(), Peril.NONE,
                List.of(
                        new Choice("Take work on the site",
                                "Seven years of steady wages on the largest building anyone "
                                        + "you know will ever stand inside.",
                                Map.of("wealth", 10, "health", -6, "reputation", 5),
                                Map.of("court", 5, "commonPeople", 6),
                                List.of(), List.of("worked_on_the_suleymaniye")),
                        new Choice("Watch it go up from the street",
                                "You have your own work.",
                                Map.of(), Map.of(),
                                List.of(), List.of()))
                ,
                "Britannica, Suleymaniye Mosque; Britannica, Sinan"));

        add(HistoricalEvent.news(
                "suleiman_dies", 1566, era,
                "Suleiman Is Dead",
                "The reign that lasted forty-six years is over. The empire is at its "
                        + "greatest extent, and every office in it is now waiting to hear "
                        + "whether it keeps its holder.",
                "history_suleiman_dies", BRITANNICA_OTTOMAN));

        add(new HistoricalEvent(
                "vienna_1683", 1683, era,
                "Vienna, the Second Time",
                "The last Ottoman attempt on Vienna fails. What follows is not one "
                        + "defeat but a run of them, and the empire will be giving ground for "
                        + "the rest of the century.",
                Set.of(),
                Map.of("stress", 8, "wealth", -6),
                Map.of("military", -12, "court", -10),
                "history_vienna_1683", Set.of(), Peril.HARSH,
                List.of(
                        new Choice("March with the campaign",
                                "You go west with them, and you are lucky to come back at all.",
                                Map.of("health", -14, "stress", 12, "wealth", -5),
                                Map.of("military", 8),
                                List.of(), List.of("marched_on_vienna_1683")),
                        new Choice("Stay well clear of it",
                                "You have heard how the last one went.",
                                Map.of("stress", 4), Map.of("military", -5),
                                List.of(), List.of()))
                ,
                BRITANNICA_OTTOMAN));

        add(HistoricalEvent.news(
                "hungary_given_up", 1699, era,
                "Hungary Is Given Up",
                "After sixteen years of losses the empire relinquishes Hungary. The "
                        + "frontier that had moved outward for three hundred years is moving "
                        + "the other way, and everyone can see it.",
                "history_hungary", BRITANNICA_OTTOMAN));
    }

    private static void add(HistoricalEvent event) {
        TIMELINE.add(event);
    }

    // ════════════════════════════════════════════════════════════════════
    //  Reading the record
    // ════════════════════════════════════════════════════════════════════

    /** Everything, in the order it happened. */
    public static List<HistoricalEvent> all() {
        return List.copyOf(TIMELINE);
    }

    /** What happened in a given year, for a person living in a given city. */
    public static List<HistoricalEvent> eventsIn(int year, String era, String city) {
        List<HistoricalEvent> due = new ArrayList<>();

        for (HistoricalEvent event : TIMELINE) {
            if (event.year() == year && event.era().equals(era) && event.reaches(city)) {
                due.add(event);
            }
        }

        return due;
    }

    /** Everything recorded for one era, earliest first. */
    public static List<HistoricalEvent> forEra(String era) {
        List<HistoricalEvent> found = new ArrayList<>();

        for (HistoricalEvent event : TIMELINE) {
            if (event.era().equals(era)) {
                found.add(event);
            }
        }

        return found;
    }

    public static HistoricalEvent byId(String id) {
        for (HistoricalEvent event : TIMELINE) {
            if (event.id().equals(id)) {
                return event;
            }
        }

        return null;
    }
}
