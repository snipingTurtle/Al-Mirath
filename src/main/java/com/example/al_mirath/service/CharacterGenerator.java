package com.example.al_mirath.service;

import com.example.al_mirath.model.PlayerCharacter;

import java.util.Random;

public class CharacterGenerator {

    private final Random random = new Random();

    private final String[] names = {
            "Yusuf ibn Salim",
            "Ahmad ibn Harun",
            "Musa al-Basri",
            "Khalid ibn Rashid",
            "Omar al-Dimashqi",
            "Hasan ibn Faraj",
            "Ibrahim al-Katib",
            "Tariq ibn Nuh",
            "Zayd ibn Malik",
            "Rashid al-Qurtubi",
            "Farid ibn Umar",
            "Salim al-Halabi",
            "Nadir ibn Ayyub",
            "Bilal al-Misri",
            "Ismail ibn Saad",
            "Qasim al-Baghdadi"
    };

    private final String[] eras = {
            "Umayyad Era",
            "Abbasid Era",
            "Mamluk Era",
            "Ottoman Era"
    };

    private final String[] familyConditions = {
            "Stable Household",
            "Debt-Burdened Family",
            "Disgraced Bloodline",
            "Recently Orphaned",
            "Family Divided by Rivalry",
            "Secret Noble Blood",
            "Political Enemy of the Court",
            "Exiled Branch",
            "Favored by Local Scholars",
            "Watched by Palace Spies"
    };

    private final String[] traits = {
            "Ambitious",
            "Cautious",
            "Brave",
            "Cunning",
            "Pious",
            "Greedy",
            "Merciful",
            "Ruthless",
            "Charismatic",
            "Scholarly",
            "Suspicious",
            "Pragmatic",
            "Idealist",
            "Patient",
            "Proud",
            "Impulsive"
    };

    public PlayerCharacter generateCharacter() {
        String name = randomFrom(names);
        String era = randomFrom(eras);
        String origin = generateOriginByEra(era);
        String familyCondition = randomFrom(familyConditions);
        String trait = randomFrom(traits);

        int health = 60;
        int wealth = 30;
        int education = 30;
        int reputation = 25;
        int politicalPower = 15;
        int morality = 50;
        int familyLoyalty = 50;
        int stress = 40;

        switch (origin) {
            case "Poor Village Child", "Poor Urban Child", "Village Orphan" -> {
                wealth -= 20;
                reputation -= 10;
                stress += 15;
                familyLoyalty += 5;
            }

            case "Bedouin Nomad" -> {
                health += 10;
                wealth -= 10;
                familyLoyalty += 10;
                politicalPower -= 5;
            }

            case "Mawali Convert's Child" -> {
                education += 5;
                reputation -= 10;
                stress += 10;
            }

            case "Frontier Soldier's Child" -> {
                health += 10;
                politicalPower += 10;
                wealth -= 5;
                stress += 10;
            }

            case "Umayyad Noble Heir", "Provincial Governor's Child", "Royal Prince" -> {
                wealth += 30;
                reputation += 20;
                politicalPower += 30;
                stress += 20;
                familyLoyalty -= 10;
            }

            case "Scholar's Child", "Madrasa Student" -> {
                education += 30;
                wealth -= 5;
                reputation += 5;
            }

            case "Scribe's Child" -> {
                education += 20;
                politicalPower += 5;
                wealth -= 5;
            }

            case "Baghdad Merchant's Child", "Cairo Merchant's Child", "Istanbul Guild Merchant" -> {
                wealth += 25;
                education += 5;
                politicalPower += 5;
            }

            case "Court Servant" -> {
                politicalPower += 10;
                education += 5;
                stress += 15;
            }

            case "Mamluk Trainee", "Janissary Recruit" -> {
                health += 20;
                politicalPower += 15;
                education -= 5;
                stress += 15;
            }

            case "Military Servant" -> {
                health += 10;
                politicalPower += 10;
                stress += 15;
                wealth -= 10;
            }

            case "Palace Clerk's Child" -> {
                education += 15;
                politicalPower += 10;
                stress += 10;
            }
        }

        switch (familyCondition) {
            case "Stable Household" -> {
                familyLoyalty += 10;
                stress -= 5;
            }

            case "Debt-Burdened Family" -> {
                wealth -= 15;
                stress += 10;
            }

            case "Disgraced Bloodline" -> {
                reputation -= 20;
                stress += 10;
            }

            case "Recently Orphaned" -> {
                familyLoyalty -= 20;
                stress += 20;
            }

            case "Family Divided by Rivalry" -> {
                familyLoyalty -= 25;
                stress += 15;
            }

            case "Secret Noble Blood" -> {
                reputation += 5;
                politicalPower += 5;
                stress += 5;
            }

            case "Political Enemy of the Court" -> {
                politicalPower -= 10;
                reputation -= 10;
                stress += 20;
            }

            case "Exiled Branch" -> {
                wealth -= 10;
                reputation -= 5;
                stress += 10;
            }

            case "Favored by Local Scholars" -> {
                education += 10;
                reputation += 5;
                stress -= 5;
            }

            case "Watched by Palace Spies" -> {
                politicalPower -= 5;
                stress += 20;
                reputation -= 5;
            }
        }

        switch (trait) {
            case "Ambitious" -> {
                politicalPower += 10;
                stress += 10;
            }

            case "Cautious" -> {
                stress -= 10;
                politicalPower -= 5;
            }

            case "Brave" -> {
                health += 10;
                reputation += 10;
                stress += 5;
            }

            case "Cunning" -> {
                politicalPower += 10;
                morality -= 10;
            }

            case "Pious" -> {
                morality += 15;
                reputation += 5;
            }

            case "Greedy" -> {
                wealth += 15;
                morality -= 10;
                familyLoyalty -= 5;
            }

            case "Merciful" -> {
                morality += 10;
                reputation += 5;
            }

            case "Ruthless" -> {
                politicalPower += 15;
                morality -= 20;
                familyLoyalty -= 5;
            }

            case "Charismatic" -> {
                reputation += 15;
                politicalPower += 5;
            }

            case "Scholarly" -> {
                education += 20;
                health -= 5;
            }

            case "Suspicious" -> {
                stress += 5;
                familyLoyalty -= 10;
                politicalPower += 5;
            }

            case "Pragmatic" -> {
                morality -= 5;
                stress -= 5;
                politicalPower += 5;
            }

            case "Idealist" -> {
                morality += 15;
                reputation += 5;
                politicalPower -= 5;
            }

            case "Patient" -> {
                stress -= 15;
                education += 5;
            }

            case "Proud" -> {
                reputation += 10;
                stress += 5;
                familyLoyalty -= 5;
            }

            case "Impulsive" -> {
                reputation += 5;
                stress += 15;
                morality -= 5;
            }
        }

        int startingAge = random.nextInt(7) + 8;

        return new PlayerCharacter(
                name,
                era,
                origin,
                familyCondition,
                trait,
                startingAge,
                health,
                wealth,
                education,
                reputation,
                politicalPower,
                morality,
                familyLoyalty,
                stress
        );
    }

    private String generateOriginByEra(String era) {
        return switch (era) {
            case "Umayyad Era" -> randomFrom(new String[]{
                    "Poor Village Child",
                    "Bedouin Nomad",
                    "Mawali Convert's Child",
                    "Umayyad Noble Heir",
                    "Frontier Soldier's Child"
            });

            case "Abbasid Era" -> randomFrom(new String[]{
                    "Poor Urban Child",
                    "Scholar's Child",
                    "Baghdad Merchant's Child",
                    "Court Servant",
                    "Scribe's Child"
            });

            case "Mamluk Era" -> randomFrom(new String[]{
                    "Mamluk Trainee",
                    "Cairo Merchant's Child",
                    "Madrasa Student",
                    "Village Orphan",
                    "Military Servant"
            });

            case "Ottoman Era" -> randomFrom(new String[]{
                    "Janissary Recruit",
                    "Provincial Governor's Child",
                    "Istanbul Guild Merchant",
                    "Royal Prince",
                    "Palace Clerk's Child"
            });

            default -> "Poor Village Child";
        };
    }

    private String randomFrom(String[] array) {
        return array[random.nextInt(array.length)];
    }
}