package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.WorldState;
import com.example.al_mirath.model.EndingResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;


public class GameEngine {

    private final PlayerCharacter player;
    private final FactionRelations factions;
    private final WorldState worldState;
    private final List<GameEvent> eventPool;
    private final Random random = new Random();
    private final Map<String, Integer> stageEventsPlayed = new HashMap<>();
    private final String birthIntroMessage;

    private final Map<String, Integer> maxEventsByStage = Map.of(
            "Childhood", 3,
            "Youth", 4,
            "Adulthood", 7,
            "Political Crisis", 5,
            "Legacy", 3
    );

    private final List<String> lifeStages = List.of(
            "Childhood",
            "Youth",
            "Adulthood",
            "Political Crisis",
            "Legacy"
    );

    private final Set<String> playedEventTitles = new HashSet<>();

    private int currentStageIndex = 0;
    private GameEvent currentEvent;
    private EndingResult endingResult;
    private String latestLegacyTitleMessage = "";
    private String latestStatusChangeMessage = "";

    public GameEngine() {
        CharacterGenerator generator = new CharacterGenerator();
        this.player = generator.generateCharacter();
        this.factions = new FactionRelations();
        this.worldState = new WorldState();

        this.birthIntroMessage = createBirthIntroMessage();
        this.eventPool = EventLibrary.createEventPool();
    }

    /**
     * Restore constructor used when loading a saved game.
     * Rebuilds the engine's internal state from previously saved values
     * instead of generating a brand-new character.
     */
    private GameEngine(
            PlayerCharacter player,
            FactionRelations factions,
            WorldState worldState,
            int currentStageIndex,
            Map<String, Integer> stageEventsPlayed,
            Set<String> playedEventTitles,
            String currentEventTitle
    ) {
        this.player = player;
        this.factions = factions;
        this.worldState = worldState;

        this.birthIntroMessage = createBirthIntroMessage();
        this.eventPool = EventLibrary.createEventPool();

        this.currentStageIndex = currentStageIndex;
        this.stageEventsPlayed.putAll(stageEventsPlayed);
        this.playedEventTitles.addAll(playedEventTitles);

        if (currentEventTitle != null) {
            for (GameEvent event : eventPool) {
                if (event.getTitle().equals(currentEventTitle)) {
                    this.currentEvent = event;
                    break;
                }
            }
        }
    }

    /**
     * Serializes the full engine state (player, factions, world flags,
     * progression) to a JSON string so it can be persisted and restored later.
     */
    public String toSaveJson() {
        JSONObject root = new JSONObject();

        JSONObject playerJson = new JSONObject();
        playerJson.put("name", player.getName());
        playerJson.put("era", player.getEra());
        playerJson.put("origin", player.getOrigin());
        playerJson.put("familyCondition", player.getFamilyCondition());
        playerJson.put("trait", player.getTrait());
        playerJson.put("age", player.getAge());
        playerJson.put("currentStatus", player.getCurrentStatus());
        playerJson.put("health", player.getHealth());
        playerJson.put("wealth", player.getWealth());
        playerJson.put("education", player.getEducation());
        playerJson.put("reputation", player.getReputation());
        playerJson.put("politicalPower", player.getPoliticalPower());
        playerJson.put("morality", player.getMorality());
        playerJson.put("familyLoyalty", player.getFamilyLoyalty());
        playerJson.put("stress", player.getStress());
        playerJson.put("legacyTitles", new JSONArray(player.getLegacyTitles()));
        root.put("player", playerJson);

        JSONObject factionsJson = new JSONObject();
        factionsJson.put("court", factions.getCourt());
        factionsJson.put("nobles", factions.getNobles());
        factionsJson.put("military", factions.getMilitary());
        factionsJson.put("scholars", factions.getScholars());
        factionsJson.put("merchants", factions.getMerchants());
        factionsJson.put("commonPeople", factions.getCommonPeople());
        factionsJson.put("familyCouncil", factions.getFamilyCouncil());
        factionsJson.put("shadowNetwork", factions.getShadowNetwork());
        root.put("factions", factionsJson);

        root.put("worldFlags", new JSONArray(worldState.getFlags()));
        root.put("currentStageIndex", currentStageIndex);
        root.put("stageEventsPlayed", new JSONObject(stageEventsPlayed));
        root.put("playedEventTitles", new JSONArray(playedEventTitles));
        root.put("currentEventTitle", currentEvent == null ? JSONObject.NULL : currentEvent.getTitle());

        return root.toString();
    }

    /**
     * Rebuilds a GameEngine from a JSON string previously produced by
     * {@link #toSaveJson()}.
     */
    public static GameEngine fromSaveJson(String json) {
        JSONObject root = new JSONObject(json);

        JSONObject playerJson = root.getJSONObject("player");

        PlayerCharacter player = new PlayerCharacter(
                playerJson.getString("name"),
                playerJson.getString("era"),
                playerJson.getString("origin"),
                playerJson.getString("familyCondition"),
                playerJson.getString("trait"),
                playerJson.getInt("age"),
                playerJson.getInt("health"),
                playerJson.getInt("wealth"),
                playerJson.getInt("education"),
                playerJson.getInt("reputation"),
                playerJson.getInt("politicalPower"),
                playerJson.getInt("morality"),
                playerJson.getInt("familyLoyalty"),
                playerJson.getInt("stress")
        );

        player.setCurrentStatus(playerJson.getString("currentStatus"));

        JSONArray legacyTitlesJson = playerJson.getJSONArray("legacyTitles");
        for (int i = 0; i < legacyTitlesJson.length(); i++) {
            player.addLegacyTitle(legacyTitlesJson.getString(i));
        }

        JSONObject factionsJson = root.getJSONObject("factions");
        FactionRelations factions = new FactionRelations(
                factionsJson.getInt("court"),
                factionsJson.getInt("nobles"),
                factionsJson.getInt("military"),
                factionsJson.getInt("scholars"),
                factionsJson.getInt("merchants"),
                factionsJson.getInt("commonPeople"),
                factionsJson.getInt("familyCouncil"),
                factionsJson.getInt("shadowNetwork")
        );

        WorldState worldState = new WorldState();
        JSONArray worldFlagsJson = root.getJSONArray("worldFlags");
        List<String> worldFlags = new ArrayList<>();
        for (int i = 0; i < worldFlagsJson.length(); i++) {
            worldFlags.add(worldFlagsJson.getString(i));
        }
        worldState.addFlags(worldFlags);

        Map<String, Integer> stageEventsPlayed = new HashMap<>();
        JSONObject stageEventsJson = root.getJSONObject("stageEventsPlayed");
        for (String key : stageEventsJson.keySet()) {
            stageEventsPlayed.put(key, stageEventsJson.getInt(key));
        }

        Set<String> playedEventTitles = new HashSet<>();
        JSONArray playedEventTitlesJson = root.getJSONArray("playedEventTitles");
        for (int i = 0; i < playedEventTitlesJson.length(); i++) {
            playedEventTitles.add(playedEventTitlesJson.getString(i));
        }

        String currentEventTitle = root.isNull("currentEventTitle")
                ? null
                : root.getString("currentEventTitle");

        return new GameEngine(
                player,
                factions,
                worldState,
                root.getInt("currentStageIndex"),
                stageEventsPlayed,
                playedEventTitles,
                currentEventTitle
        );
    }

    public String getBirthIntroMessage() {
        return birthIntroMessage;
    }

    public String consumeLatestStatusChangeMessage() {
        String message = latestStatusChangeMessage;
        latestStatusChangeMessage = "";
        return message;
    }

    private String createBirthIntroMessage() {
        return "A new life begins...\n\n"
                + "You are born during the " + player.getEra() + ".\n\n"
                + "Origin: " + player.getOrigin() + "\n"
                + "Family Condition: " + player.getFamilyCondition() + "\n"
                + "Trait: " + player.getTrait() + "\n\n"
                + getFamilyBirthDescription()
                + "\n\nYour story begins at age " + player.getAge()
                + ". The world does not yet know your name, but your choices will decide whether your life becomes forgotten, honored, feared, or remembered.";
    }

    private String getFamilyBirthDescription() {
        return switch (player.getFamilyCondition()) {
            case "Stable Household" ->
                    "You are born into a household with enough order to give you a fair beginning, though no future is guaranteed.";

            case "Debt-Burdened Family" ->
                    "Your family carries debt before you are old enough to understand money. Survival will not be easy.";

            case "Disgraced Bloodline" ->
                    "Your family name carries shame. Some doors are already closed before you even begin your life.";

            case "Recently Orphaned" ->
                    "Loss reaches your life early. Without normal protection, you must grow stronger than other children.";

            case "Family Divided by Rivalry" ->
                    "Your household is divided by suspicion and rivalry. Even family loyalty may become a test.";

            case "Secret Noble Blood" ->
                    "There are whispers that your bloodline is greater than it appears. If true, this secret could become power or danger.";

            case "Political Enemy of the Court" ->
                    "Your family is already disliked by the court. Power watches your household with cold eyes.";

            case "Exiled Branch" ->
                    "You are born into a branch pushed away from its former place. Return, revenge, or survival may define your path.";

            case "Favored by Local Scholars" ->
                    "Local scholars respect your household. Knowledge may open doors that wealth cannot.";

            case "Watched by Palace Spies" ->
                    "From your earliest years, hidden eyes follow your family. One wrong step may be remembered.";

            default ->
                    "Your family background shapes your first steps, but your choices will shape the rest.";
        };
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public FactionRelations getFactions() {
        return factions;
    }

    public GameEvent getCurrentEvent() {
        if (!player.isAlive()) {
            currentEvent = null;

            if (endingResult == null) {
                endingResult = calculateEnding();
            }

            return null;
        }

        if (currentEvent == null) {
            currentEvent = selectEventForCurrentStage();
        }

        if (currentEvent == null && endingResult == null) {
            endingResult = calculateEnding();
        }

        return currentEvent;
    }

    private GameEvent selectEventForCurrentStage() {
        while (currentStageIndex < lifeStages.size()) {
            String stage = lifeStages.get(currentStageIndex);

            int playedInStage = stageEventsPlayed.getOrDefault(stage, 0);
            int maxForStage = maxEventsByStage.getOrDefault(stage, 1);

            if (playedInStage >= maxForStage) {
                currentStageIndex++;
                continue;
            }

            GameEvent consequenceEvent = findEventForStage(stage, true);

            if (consequenceEvent != null) {
                return consequenceEvent;
            }

            GameEvent normalEvent = findEventForStage(stage, false);

            if (normalEvent != null) {
                return normalEvent;
            }

            currentStageIndex++;
        }

        return null;
    }

    private GameEvent findEventForStage(String stage, boolean consequenceOnly) {
        List<GameEvent> eligibleEvents = new ArrayList<>();

        for (GameEvent event : eventPool) {
            if (playedEventTitles.contains(event.getTitle())) {
                continue;
            }

            if (!event.getLifeStage().equals(stage)) {
                continue;
            }

            if (consequenceOnly && !event.hasRequiredFlags()) {
                continue;
            }

            if (!consequenceOnly && event.hasRequiredFlags()) {
                continue;
            }

            if (event.isEligibleFor(player, worldState)) {
                eligibleEvents.add(event);
            }
        }

        if (eligibleEvents.isEmpty()) {
            return null;
        }

        return eligibleEvents.get(random.nextInt(eligibleEvents.size()));
    }

    public String applyChoice(Choice choice) {
        if (!canChoose(choice)) {
            return "This choice is unavailable.\n\n" + getLockedReason(choice);
        }

        boolean success = resolveChoiceSuccess(choice);

        Map<String, Integer> statEffects;
        Map<String, Integer> factionEffects;
        String resultText;
        List<String> flagsToAdd;

        if (success) {
            statEffects = choice.getSuccessStatEffects();
            factionEffects = choice.getSuccessFactionEffects();
            resultText = choice.getSuccessText();
            flagsToAdd = choice.getSuccessFlags();
        } else {
            statEffects = choice.getFailureStatEffects();
            factionEffects = choice.getFailureFactionEffects();
            resultText = choice.getFailureText();
            flagsToAdd = choice.getFailureFlags();
        }

        for (Map.Entry<String, Integer> effect : statEffects.entrySet()) {
            player.applyChange(effect.getKey(), effect.getValue());
        }

        for (Map.Entry<String, Integer> effect : factionEffects.entrySet()) {
            factions.applyChange(effect.getKey(), effect.getValue());
        }

        for (String flag : flagsToAdd) {
            worldState.addFlag(flag);
        }

        advanceAgeAfterEvent();
        updateCurrentStatus();
        checkMortalityAfterChoice(choice, success);

        latestLegacyTitleMessage = checkForNewLegacyTitles();

        if (currentEvent != null) {
            playedEventTitles.add(currentEvent.getTitle());
        }

        if (currentEvent != null) {
            String stage = currentEvent.getLifeStage();
            int playedInStage = stageEventsPlayed.getOrDefault(stage, 0);
            stageEventsPlayed.put(stage, playedInStage + 1);
        }

        currentEvent = null;

        String finalResult;

        if (choice.requiresStatCheck()) {
            finalResult = buildCheckedResultText(success, resultText);
        } else {
            finalResult = resultText;
        }

        return finalResult;
    }

    private boolean resolveChoiceSuccess(Choice choice) {
        if (!choice.requiresStatCheck()) {
            return true;
        }

        int statValue = player.getStatValue(choice.getCheckStat());
        int difficulty = choice.getDifficulty();

        int successChance = 50 + (statValue - difficulty);
        successChance = Math.max(10, Math.min(90, successChance));

        int roll = random.nextInt(100) + 1;

        return roll <= successChance;
    }

    private String buildCheckedResultText(boolean success, String resultText) {
        String outcome = success ? "Outcome: Success" : "Outcome: Failure";
        return outcome + "\n\n" + resultText;
    }

    private void advanceAgeAfterEvent() {
        if (currentEvent == null) {
            return;
        }

        String stage = currentEvent.getLifeStage();
        int yearsPassed;

        switch (stage) {
            case "Childhood" -> yearsPassed = random.nextInt(2) + 1; // 1–2 years
            case "Youth" -> yearsPassed = random.nextInt(2) + 1; // 1–2 years
            case "Adulthood" -> yearsPassed = random.nextInt(3) + 1; // 1–3 years
            case "Political Crisis" -> yearsPassed = random.nextInt(3) + 1; // 1–3 years
            case "Legacy" -> yearsPassed = random.nextInt(5) + 2; // 2–6 years
            default -> yearsPassed = 1;
        }

        player.increaseAge(yearsPassed);
    }

    private void checkMortalityAfterChoice(Choice choice, boolean success) {
        if (!player.isAlive()) {
            return;
        }

        int risk = 0;
        String reason = "";

        // Very low health is dangerous at any age
        if (player.getHealth() <= 10) {
            risk += 45;
            reason = "Your body finally failed after years of hardship.";
        } else if (player.getHealth() <= 20) {
            risk += 25;
            reason = "Your weakened body could not fully recover from the burdens of life.";
        } else if (player.getHealth() <= 35) {
            risk += 10;
            reason = "Poor health made every crisis more dangerous.";
        }

        // Stress makes health risk worse
        if (player.getStress() >= 90) {
            risk += 25;
            reason = "The pressure of your life became too heavy to survive.";
        } else if (player.getStress() >= 75) {
            risk += 12;
            reason = "Years of pressure weakened your chance of survival.";
        }

        // Age risk
        if (player.getAge() >= 90) {
            risk += 55;
            reason = "Old age closed the final chapter of your life.";
        } else if (player.getAge() >= 80) {
            risk += 30;
            reason = "Age made every burden harder to carry.";
        } else if (player.getAge() >= 70) {
            risk += 15;
            reason = "You had entered the dangerous final years of life.";
        } else if (player.getAge() >= 60) {
            risk += 6;
            reason = "Age slowly began to weaken your body.";
        }

        // Dangerous political consequences
        if (worldState.hasFlag("court_suspicion") && factions.getCourt() <= 25) {
            risk += 20;
            reason = "Court suspicion surrounded your final days.";
        }

        if (worldState.hasFlag("collector_reported_you") && factions.getCourt() <= 30) {
            risk += 18;
            reason = "The report against you destroyed your protection.";
        }

        if (worldState.hasFlag("owed_shadow_debt") && factions.getShadowNetwork() >= 70) {
            risk += 18;
            reason = "The shadow network protected you for too long to let you walk away freely.";
        }

        if (worldState.hasFlag("framed_innocent") && factions.getCourt() <= 30) {
            risk += 15;
            reason = "The lie you built began to collapse around you.";
        }

        // Failed stat-check choices are more dangerous
        if (choice.requiresStatCheck() && !success) {
            risk += 10;

            if (choice.getCheckStat().equals("health")) {
                risk += 15;
                reason = "A failed physical challenge left lasting damage.";
            }

            if (choice.getCheckStat().equals("politicalPower")) {
                risk += 10;
                reason = "A failed power move exposed you to dangerous enemies.";
            }
        }

        // Young death should be possible, but only under heavy danger
        if (player.getAge() < 25 && player.getHealth() > 35 && player.getStress() < 80) {
            risk = Math.min(risk, 8);
        }

        // Keep risk controlled
        risk = Math.max(0, Math.min(75, risk));

        int roll = random.nextInt(100) + 1;

        if (roll <= risk) {
            if (reason.isEmpty()) {
                reason = "Your life ended before your ambitions could fully unfold.";
            }

            player.markDead(reason);
        }
    }

    private String checkForNewLegacyTitles() {
        StringBuilder message = new StringBuilder();

        if (worldState.hasFlag("protected_commoners")
                && player.getReputation() >= 55
                && factions.getCommonPeople() >= 65) {
            addTitleMessage(message, "Hero of the People");
        }

        if (worldState.hasFlag("protected_commoners")
                && factions.getCommonPeople() >= 70
                && player.getWealth() <= 20) {
            addTitleMessage(message, "Voice of the Poor");
        }

        if (player.getEducation() >= 80
                && factions.getScholars() >= 70
                && player.getReputation() >= 45) {
            addTitleMessage(message, "Light of the Madrasa");
        }

        if (player.getEducation() >= 75
                && player.getMorality() >= 75
                && factions.getScholars() >= 65) {
            addTitleMessage(message, "The Wise Judge");
        }

        if (worldState.hasFlag("angered_scholars")
                && player.getEducation() >= 65
                && factions.getScholars() <= 20) {
            addTitleMessage(message, "Heretic in the Court");
        }

        if (player.getPoliticalPower() >= 65
                && factions.getMilitary() >= 70
                && player.getReputation() >= 50) {
            addTitleMessage(message, "Sword of the Realm");
        }

        if (player.getHealth() >= 75
                && factions.getMilitary() >= 70
                && player.getReputation() >= 55) {
            addTitleMessage(message, "Frontier Hero");
        }

        if (player.getMorality() <= 20
                && factions.getMilitary() >= 75
                && player.getPoliticalPower() >= 55) {
            addTitleMessage(message, "Blood General");
        }

        if (player.getWealth() >= 75
                && factions.getMerchants() >= 70) {
            addTitleMessage(message, "Golden Hand");
        }

        if (player.getWealth() >= 65
                && factions.getMerchants() >= 80) {
            addTitleMessage(message, "Master of Caravans");
        }

        if (player.getWealth() >= 80
                && player.getMorality() <= 25) {
            addTitleMessage(message, "Coin-Bound Soul");
        }

        if (worldState.hasFlag("declared_loyalty")
                && player.getPoliticalPower() >= 60
                && factions.getCourt() >= 70) {
            addTitleMessage(message, "Dynasty Loyalist");
        }

        if (factions.getCourt() >= 70
                && player.getStress() >= 80
                && player.getPoliticalPower() >= 50) {
            addTitleMessage(message, "Court Survivor");
        }

        if (player.getPoliticalPower() >= 80
                && player.getReputation() >= 60) {
            addTitleMessage(message, "Rising Power");
        }

        if (factions.getCourt() <= 15
                && player.getPoliticalPower() >= 60) {
            addTitleMessage(message, "Enemy of the Court");
        }

        if ((worldState.hasFlag("used_shadow_contacts")
                || worldState.hasFlag("sold_palace_secret")
                || worldState.hasFlag("learned_palace_secrets"))
                && player.getPoliticalPower() >= 55
                && factions.getShadowNetwork() >= 65) {
            addTitleMessage(message, "Knife in the Dark");
        }

        if (factions.getShadowNetwork() >= 80
                && player.getReputation() <= 35) {
            addTitleMessage(message, "Whisper Lord");
        }

        if ((worldState.hasFlag("betrayed_comrade") || worldState.hasFlag("sold_palace_secret"))
                && player.getMorality() <= 25
                && player.getFamilyLoyalty() <= 40) {
            addTitleMessage(message, "The Betrayer");
        }

        if (player.getFamilyLoyalty() >= 85
                && player.getMorality() >= 60) {
            addTitleMessage(message, "Bloodline Protector");
        }

        if (player.getFamilyCondition().equals("Recently Orphaned")
                && player.getReputation() >= 60
                && player.getStress() >= 70) {
            addTitleMessage(message, "Orphan of Iron");
        }

        if (player.getFamilyCondition().equals("Disgraced Bloodline")
                && player.getReputation() >= 65
                && player.getPoliticalPower() >= 45) {
            addTitleMessage(message, "Restorer of Honor");
        }

        if (player.getFamilyCondition().equals("Exiled Branch")
                && player.getPoliticalPower() >= 65
                && factions.getCourt() >= 50) {
            addTitleMessage(message, "Returned from Exile");
        }

        if (player.getStress() >= 90
                && player.getHealth() >= 45
                && player.getReputation() >= 45) {
            addTitleMessage(message, "Burdened Survivor");
        }

        if (player.getHealth() <= 20
                && player.getReputation() >= 60) {
            addTitleMessage(message, "Fading Legend");
        }

        return message.toString();
    }

    private void addTitleMessage(StringBuilder message, String title) {
        boolean added = player.addLegacyTitle(title);

        if (added) {
            if (!message.isEmpty()) {
                message.append("\n");
            }

            message.append("Legacy Title Gained: ").append(title);
        }
    }

    private void updateCurrentStatus() {
        String oldStatus = player.getCurrentStatus();
        String newStatus;

        // Major story-arc statuses first
        if (worldState.hasFlag("took_throne")) {
            newStatus = "Ruler";

        } else if (worldState.hasFlag("became_regent")) {
            newStatus = "Royal Regent";

        } else if (worldState.hasFlag("failed_claim")) {
            newStatus = "Disgraced Claimant";

        } else if (worldState.hasFlag("family_name_restored")) {
            newStatus = "Restorer of Honor";

        } else if (worldState.hasFlag("new_name_through_power")) {
            newStatus = "Power-Made Noble";

        } else if (worldState.hasFlag("old_name_abandoned")) {
            newStatus = "Self-Made Survivor";

        } else if (worldState.hasFlag("family_restoration_failed")) {
            newStatus = "Disgraced Survivor";

        } else if (worldState.hasFlag("exiled_branch_restored")) {
            newStatus = "Returned from Exile";

        } else if (worldState.hasFlag("minor_return_granted")) {
            newStatus = "Restored Minor Noble";

        } else if (worldState.hasFlag("outside_power_base")) {
            newStatus = "Power Outside Court";

        } else if (worldState.hasFlag("restoration_refused")) {
            newStatus = "Unrecognized Returnee";

        } else if (worldState.hasFlag("popular_uprising_leader")) {
            newStatus = "People's Leader";

        } else if (worldState.hasFlag("civic_reformer")) {
            newStatus = "Civic Reformer";

        } else if (worldState.hasFlag("commoner_leader")) {
            newStatus = "Commoner Leader";

            // Normal stat/faction-based statuses
        } else if (player.getPoliticalPower() >= 75 && factions.getCourt() >= 65) {
            newStatus = "Powerful Court Figure";

        } else if (player.getPoliticalPower() >= 65 && factions.getMilitary() >= 70) {
            newStatus = "Military Commander";

        } else if (player.getEducation() >= 75 && factions.getScholars() >= 65) {
            newStatus = "Respected Scholar";

        } else if (player.getWealth() >= 70 && factions.getMerchants() >= 65) {
            newStatus = "Influential Merchant";

        } else if (factions.getShadowNetwork() >= 70 && player.getPoliticalPower() >= 45) {
            newStatus = "Shadow Broker";

        } else if (factions.getCourt() <= 20 && player.getPoliticalPower() >= 40) {
            newStatus = "Enemy of the Court";

        } else if (player.getWealth() <= 15 && player.getStress() >= 70) {
            newStatus = "Desperate Survivor";

        } else if (player.getReputation() >= 55 && factions.getCommonPeople() >= 60) {
            newStatus = "Local Hero";

        } else if (player.getEducation() >= 60) {
            newStatus = "Educated Aspirant";

        } else if (player.getPoliticalPower() >= 45) {
            newStatus = "Rising Political Actor";

        } else if (player.getWealth() >= 45) {
            newStatus = "Stable Householder";

        } else {
            newStatus = player.getOrigin();
        }

        player.setCurrentStatus(newStatus);

        if (!oldStatus.equals(newStatus)) {
            latestStatusChangeMessage =
                    "Your place in society has changed.\n\n"
                            + "From: " + oldStatus + "\n"
                            + "To: " + newStatus + "\n\n"
                            + "This means new events, risks, and opportunities may now appear.";
        }
    }

    public boolean hasMoreEvents() {
        return getCurrentEvent() != null;
    }

    public boolean canChoose(Choice choice) {
        return getLockedReason(choice).isEmpty();
    }

    public String getLockedReason(Choice choice) {
        for (ChoiceRequirement requirement : choice.getRequirements()) {
            if (!requirementMet(requirement)) {
                return requirement.getFailMessage();
            }
        }

        return "";
    }

    public String getLifeSummary() {
        return "\n\n----- Life Summary -----\n"
                + "Age: " + player.getAge() + "\n"
                + "Origin: " + player.getOrigin() + "\n"
                + "Final Status: " + player.getCurrentStatus() + "\n"
                + "Family Condition: " + player.getFamilyCondition() + "\n"
                + "Trait: " + player.getTrait() + "\n"
                + "Legacy Titles: " + player.getLegacyTitlesText() + "\n\n"
                + "Major Memories:\n"
                + getMajorMemoriesText();
    }

    private String getMajorMemoriesText() {
        List<String> memories = new ArrayList<>();

        if (worldState.hasFlag("protected_commoners")) {
            memories.add("You stood with common people during hardship.");
        }

        if (worldState.hasFlag("used_bribery")) {
            memories.add("You once solved a crisis through bribery, and that choice followed you.");
        }

        if (worldState.hasFlag("angered_scholars")) {
            memories.add("Your conflict with scholars damaged your learned reputation.");
        }

        if (worldState.hasFlag("learned_palace_secrets")) {
            memories.add("You learned early that court secrets could become power.");
        }

        if (worldState.hasFlag("betrayed_comrade")) {
            memories.add("You gained attention by betraying someone close to you.");
        }

        if (worldState.hasFlag("used_shadow_contacts")) {
            memories.add("You used hidden contacts to protect your interests.");
        }

        if (worldState.hasFlag("sold_palace_secret")) {
            memories.add("You sold a palace secret and invited danger into your future.");
        }

        if (worldState.hasFlag("declared_loyalty")) {
            memories.add("You publicly supported the ruling dynasty.");
        }

        if (worldState.hasFlag("heard_rebel_voices")) {
            memories.add("You listened to voices of rebellion when the dynasty weakened.");
        }

        if (worldState.hasFlag("fed_people_during_riot")) {
            memories.add("You gave relief to hungry people during unrest.");
        }

        if (worldState.hasFlag("fair_scholar_judgment")) {
            memories.add("You were remembered for a fair judgment.");
        }

        if (worldState.hasFlag("disciplined_soldiers")) {
            memories.add("You commanded soldiers with discipline.");
        }

        if (worldState.hasFlag("protected_merchants")) {
            memories.add("You protected merchants from unfair officials.");
        }

        if (worldState.hasFlag("served_shadow_network")) {
            memories.add("You served the shadow network when they demanded payment.");
        }

        if (!player.getOrigin().equals(player.getCurrentStatus())) {
            memories.add("Your life changed your social position from your original background.");
        }

        if (player.getLegacyTitlesText().equals("None")) {
            memories.add("You gained no famous legacy title, but your household still carried the memory of your choices.");
        }

        if (memories.isEmpty()) {
            memories.add("Your life passed without one famous public turning point, but your choices still shaped your household.");
        }

        return "- " + String.join("\n- ", memories);
    }

    public EndingResult getEndingResult() {
        if (endingResult == null) {
            endingResult = calculateEnding();
        }

        return endingResult;
    }

    public String consumeLatestLegacyTitleMessage() {
        String message = latestLegacyTitleMessage;
        latestLegacyTitleMessage = "";
        return message;
    }

    private boolean requirementMet(ChoiceRequirement requirement) {
        return switch (requirement.getType()) {
            case "statMin" -> player.getStatValue(requirement.getKey()) >= requirement.getMinValue();
            case "factionMin" -> getFactionValue(requirement.getKey()) >= requirement.getMinValue();
            case "familyIs" -> player.getFamilyCondition().equals(requirement.getExpectedValue());
            case "familyNot" -> !player.getFamilyCondition().equals(requirement.getExpectedValue());
            case "originIs" -> player.getOrigin().equals(requirement.getExpectedValue());
            case "title" -> player.hasLegacyTitle(requirement.getExpectedValue());
            default -> true;
        };
    }

    private EndingResult calculateDeathEnding(String titles) {

        String endingInfo =
                "\n\nAge at death: " + player.getAge()
                        + "\nOrigin: " + player.getOrigin()
                        + "\nFinal Status: " + player.getCurrentStatus()
                        + "\n\nLegacy Titles: " + titles;

        if (player.getAge() >= 85) {
            return new EndingResult(
                    "Old Age Final Chapter",
                    "Your life reached a rare old age. By the end, your body grew weak, but your household had years to remember your choices, warnings, victories, and mistakes."
                            + endingInfo
            );
        }

        if (player.getAge() >= 70 && player.getHealth() <= 30) {
            return new EndingResult(
                    "Final Years of Decline",
                    "Age and poor health slowly closed your path. You survived many storms, but your final years were shaped by weakness, memory, and the consequences of earlier decisions."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("court_suspicion") && factions.getCourt() <= 30) {
            return new EndingResult(
                    "Died Under Court Suspicion",
                    "The court never fully trusted you again. Suspicion followed your name until your final days, and your household learned that survival near power always has a price."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("collector_reported_you") && factions.getCourt() <= 35) {
            return new EndingResult(
                    "Destroyed by Corruption",
                    "The old bribe returned at the worst possible time. The report damaged your name, weakened your protection, and left your family carrying the cost of your hidden mistake."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("owed_shadow_debt") && factions.getShadowNetwork() >= 70) {
            return new EndingResult(
                    "Owned by Shadows",
                    "The hidden world protected you, but that protection became a chain. By the end, your fate belonged more to secret allies than to your own household."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("framed_innocent") && player.getMorality() <= 25) {
            return new EndingResult(
                    "Buried by Lies",
                    "You survived by pushing blame onto another person, but lies rarely stay quiet forever. Your final chapter was shaped by fear, suspicion, and the weight of what you chose."
                            + endingInfo
            );
        }

        if (player.getWealth() <= 10 && player.getStress() >= 75) {
            return new EndingResult(
                    "Final Years in Poverty",
                    "Poverty followed you until the end. Your household survived on little, and your final days became a reminder that not every legacy is built from power or wealth."
                            + endingInfo
            );
        }

        if (player.getHealth() <= 15 && player.getStress() >= 80) {
            return new EndingResult(
                    "Broken by Hardship",
                    "Years of pressure, poor health, and difficult choices wore you down. Your life ended before your ambitions could fully settle into legacy."
                            + endingInfo
            );
        }

        if (player.getHealth() <= 20) {
            return new EndingResult(
                    "Weak Body, Heavy Life",
                    "Your body could no longer carry the burden of your choices. Even without glory, your struggle became part of your household's memory."
                            + endingInfo
            );
        }

        if (player.getStress() >= 90) {
            return new EndingResult(
                    "Crushed by Pressure",
                    "The pressure around you never truly lifted. Politics, family, fear, and survival weighed on you until your final chapter closed too soon."
                            + endingInfo
            );
        }

        return new EndingResult(
                "Life Cut Short",
                player.getDeathReason()
                        + endingInfo
        );
    }

    private EndingResult calculateEnding() {

        String titles = player.getLegacyTitlesText();

        if (!player.isAlive()) {
            return calculateDeathEnding(titles);
        }

        if (player.getHealth() <= 15) {
            return new EndingResult(
                    "Fading Legend",
                    "Your body could no longer carry the weight of your choices. Yet your name did not vanish completely. Those who remember you speak of a life burned down by struggle, pressure, and survival.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("collector_reported_you")
                && factions.getCourt() <= 30) {
            return new EndingResult(
                    "Disgraced by Corruption",
                    "The collector's report reached the court before you could silence it. Your old bribe became proof of dishonor. Officials used your corruption to weaken your household, and your bloodline carried the stain for years.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("court_suspicion")
                && factions.getCourt() <= 35) {
            return new EndingResult(
                    "Watched by the Court",
                    "You survived, but not freely. Court officials kept your name in their private records. Every promotion, meeting, and alliance came under quiet suspicion. You were never fully trusted again.\n\nLegacy Titles: " + titles
            );
        }

        if ((worldState.hasFlag("owed_shadow_debt")
                || worldState.hasFlag("sold_palace_secret")
                || worldState.hasFlag("used_shadow_contacts"))
                && factions.getShadowNetwork() >= 70) {
            return new EndingResult(
                    "Owned by Shadows",
                    "The hidden world protected you, but protection became ownership. You gained influence through whispers, secrets, and fear. In the end, your legacy belonged less to your family and more to the people who knew your darkest choices.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("angered_scholars")
                && factions.getScholars() <= 25) {
            return new EndingResult(
                    "Enemy of the Learned",
                    "The scholars never forgot your insult. Their letters, sermons, and records slowly shaped your reputation. Even when you gained power, educated circles remembered you as arrogant and unworthy of trust.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("declared_loyalty")
                && factions.getCourt() >= 65
                && player.getPoliticalPower() >= 55) {
            return new EndingResult(
                    "Pillar of the Dynasty",
                    "You stood with the ruling dynasty when others hesitated. Your loyalty brought you influence, protection, and enemies. To loyalists, you became a pillar of order. To rebels, you became part of the old machine.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("protected_commoners")
                && factions.getCommonPeople() >= 65
                && player.getReputation() >= 55) {
            return new EndingResult(
                    "Voice of the People",
                    "Common people remembered your courage long after officials tried to reduce it to troublemaking. Your name survived in markets, villages, and family stories as someone who stood beside the weak.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getEducation() >= 75
                && factions.getScholars() >= 65) {
            return new EndingResult(
                    "Honored Scholar",
                    "Knowledge became your strongest inheritance. Scholars preserved your judgments, writings, and advice. Your power was not only in wealth or soldiers, but in the respect of those who shaped memory.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getWealth() >= 75
                && factions.getMerchants() >= 65) {
            return new EndingResult(
                    "Wealthy Elder",
                    "You secured property, trade, and comfort for your household. Some praised your discipline. Others wondered what you sacrificed to gather so much. Your descendants inherited stability, but also expectation.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getFamilyLoyalty() >= 80
                && factions.getFamilyCouncil() >= 65) {
            return new EndingResult(
                    "Guardian of the Bloodline",
                    "You placed family above ambition. Your household survived because you protected it from hunger, shame, and political storms. Your legacy lived first in the people who carried your name.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getPoliticalPower() >= 75
                && player.getReputation() >= 55) {
            return new EndingResult(
                    "Rising Power",
                    "By the end of your life, few could ignore you. You had enemies, allies, and a name that moved through halls of influence. Whether loved or feared, you were no longer insignificant.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getMorality() <= 25
                && player.getPoliticalPower() >= 55) {
            return new EndingResult(
                    "Feared Survivor",
                    "You survived by making choices others feared to make. Mercy did not define your path. People obeyed you, avoided you, and remembered you with caution rather than love.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getStress() >= 85) {
            return new EndingResult(
                    "Burdened Survivor",
                    "You reached the end, but peace rarely found you. Your life was shaped by pressure, danger, and difficult compromises. Survival itself became your final achievement.\n\nLegacy Titles: " + titles
            );
        }

        return new EndingResult(
                "Forgotten Survivor",
                "You lived through your age without becoming a hero, villain, ruler, or martyr. History did not carve your name into stone, but your choices still shaped the quiet future of your household.\n\nLegacy Titles: " + titles
        );
    }

    private int getFactionValue(String faction) {
        return switch (faction) {
            case "court" -> factions.getCourt();
            case "nobles" -> factions.getNobles();
            case "military" -> factions.getMilitary();
            case "scholars" -> factions.getScholars();
            case "merchants" -> factions.getMerchants();
            case "commonPeople" -> factions.getCommonPeople();
            case "familyCouncil" -> factions.getFamilyCouncil();
            case "shadowNetwork" -> factions.getShadowNetwork();
            default -> 0;
        };
    }
}