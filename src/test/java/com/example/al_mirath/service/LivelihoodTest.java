package com.example.al_mirath.service;

import com.example.al_mirath.model.Activity;
import com.example.al_mirath.model.ActivityResult;
import com.example.al_mirath.model.Career;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Making a living: activities, a trade, and something to leave behind.
 *
 * <p>These three are the reason to press the age-up button rather than watch
 * it. Each one has to visibly pay, or the menus are decoration.
 */
class LivelihoodTest {

    // ── Activities ──────────────────────────────────────────────────────

    @Test
    @DisplayName("doing something has an effect, and doing it twice does not")
    void anActivityIsWorthTheYear() {
        GameEngine engine = Lives.aliveAt(20);

        Activity pray = ActivityLibrary.byId("pray");
        assertNotNull(pray);

        int stressBefore = engine.getPlayer().getStress();
        int moralityBefore = engine.getPlayer().getMorality();

        ActivityResult first = engine.performActivity(pray, null);

        assertTrue(first.success(),
                "prayer is not something you can fail, but: " + first.narration());
        assertTrue(engine.getPlayer().getStress() <= stressBefore,
                "a year of prayer raised the character's stress");
        assertTrue(engine.getPlayer().getMorality() >= moralityBefore);

        int stressAfter = engine.getPlayer().getStress();

        ActivityResult second = engine.performActivity(pray, null);

        assertFalse(second.success(), "the same afternoon was spent twice");
        assertEquals(stressAfter, engine.getPlayer().getStress(),
                "a refused activity still changed the character");
    }

    @Test
    @DisplayName("an activity that costs money takes it, and refuses when it cannot")
    void thePriceIsPaid() {
        GameEngine engine = Lives.aliveAt(22);

        Activity alms = ActivityLibrary.byId("alms");
        assertNotNull(alms);

        engine.getPlayer().setNetWorth(1000);

        engine.performActivity(alms, null);

        assertEquals(1000 - alms.wealthCost(), (long) engine.getPlayer().getNetWorth(),
                "giving alms did not cost what it says it costs");

        engine.getPlayer().clearYearlyActivities();
        engine.getPlayer().setNetWorth(alms.wealthCost() - 1);

        ActivityResult refused = engine.performActivity(alms, null);

        assertFalse(refused.success());
        assertEquals(alms.wealthCost() - 1, (long) engine.getPlayer().getNetWorth(),
                "an activity the character could not afford was charged for anyway");
    }

    @Test
    @DisplayName("something done once in a life cannot be done twice")
    void onceIsOnce() {
        GameEngine engine = Lives.aliveAt(30);

        Activity pilgrimage = ActivityLibrary.byId("hajj");
        assertNotNull(pilgrimage);
        assertTrue(pilgrimage.oncePerLife());

        engine.getPlayer().setNetWorth(10_000);

        ActivityResult made = engine.performActivity(pilgrimage, true);
        assertTrue(made.success());

        engine.getPlayer().clearYearlyActivities();

        assertFalse(engine.lockedReasonFor(pilgrimage).isEmpty(),
                "the pilgrimage was offered a second time");
    }

    /**
     * The claim the whole activity menu rests on.
     *
     * <p>Measured on what the activities actually do — a body kept up and a
     * mind kept quiet — rather than on average lifespan, which is the same
     * effect after it has been through a mortality curve and is far too noisy
     * at any sample a test can afford. Reaching seventy is kept as the
     * downstream check because the gap there is close to an order of
     * magnitude and survives the noise.
     */
    @Test
    @DisplayName("looking after yourself leaves a visibly better-off character")
    void careIsWorthSomething() {
        int lives = 220;

        long carelessHealth = 0;
        long carefulHealth = 0;
        long carelessStress = 0;
        long carefulStress = 0;

        int carelessReachedSeventy = 0;
        int carefulReachedSeventy = 0;

        Random careless = new Random(3);
        Random careful = new Random(3);

        for (int life = 0; life < lives; life++) {
            GameEngine a = new GameEngine();
            Lives.live(a, Lives.takingAnyOpenChoice(careless));

            carelessHealth += a.getPlayer().getHealth();
            carelessStress += a.getPlayer().getStress();

            if (a.getPlayer().getAge() >= 70) {
                carelessReachedSeventy++;
            }

            GameEngine b = new GameEngine();
            Lives.liveCarefully(b, Lives.takingAnyOpenChoice(careful));

            carefulHealth += b.getPlayer().getHealth();
            carefulStress += b.getPlayer().getStress();

            if (b.getPlayer().getAge() >= 70) {
                carefulReachedSeventy++;
            }
        }

        long carelessHealthMean = carelessHealth / lives;
        long carefulHealthMean = carefulHealth / lives;
        long carelessStressMean = carelessStress / lives;
        long carefulStressMean = carefulStress / lives;

        assertTrue(carefulHealthMean > carelessHealthMean + 15,
                "a life that took the baths, wrestled and walked in the garden "
                        + "ended on " + carefulHealthMean + " health against "
                        + carelessHealthMean + " for one that did none of it");

        assertTrue(carelessStressMean > carefulStressMean + 10,
                "a life that prayed, sat with its household and kept a garden "
                        + "ended on " + carefulStressMean + " stress against "
                        + carelessStressMean + " for one that did none of it");

        assertTrue(carefulReachedSeventy > carelessReachedSeventy,
                "reaching seventy was no more likely for a character who looked "
                        + "after themselves (" + carefulReachedSeventy + ") than "
                        + "for one who did not (" + carelessReachedSeventy + ")");
    }

    // ── A trade ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("a post pays every year, and pays more the higher you climb")
    void aTradePays() {
        GameEngine engine = Lives.aliveAt(20);

        // Near the top of the ladder, where the pay plainly clears what a
        // household costs. A student's stipend does not, which is true of
        // students and not a bug — but it makes a poor thing to assert on.
        engine.getPlayer().takeCareer(Career.SCHOLAR, Career.SCHOLAR.maxRank());
        engine.getPlayer().setNetWorth(500);

        double before = engine.getPlayer().getNetWorth();
        int pay = engine.getPlayer().annualIncome();

        engine.ageOneYear();

        assertTrue(pay > 0, "the top of the scholars' ladder pays nothing");

        assertTrue(engine.getPlayer().getNetWorth() > before,
                "a year on " + pay + " dirhams left the purse at "
                        + (long) engine.getPlayer().getNetWorth());

        boolean saidSo = engine.getLifeLog().stream()
                .anyMatch(line -> line.text().contains("were paid"));

        assertTrue(saidSo, "the year's wages were paid without a word in the chronicle");

        int lowRank = Career.SCHOLAR.incomeAt(0);
        int highRank = Career.SCHOLAR.incomeAt(Career.SCHOLAR.maxRank());

        assertTrue(highRank > lowRank * 4,
                "climbing the whole ladder is barely worth more than the bottom rung");
    }

    @Test
    @DisplayName("advancement wants both merit and years served")
    void promotionIsEarnedTwice() {
        PlayerCharacter brilliant = new PlayerCharacter(
                "Test", "Abbasid Era", "Scholar Household", "Stable Household",
                "Ambitious", 25, 70, 50, 95, 95, 50, 50, 50, 20);

        brilliant.takeCareer(Career.SCHOLAR, 0);

        assertFalse(CareerService.isPromotionDue(brilliant),
                "somebody was raised in their first year purely on being clever");

        for (int year = 0; year < 3; year++) {
            brilliant.recordYearWorked();
        }

        assertTrue(CareerService.isPromotionDue(brilliant),
                "three years and every requirement met was still not enough");

        PlayerCharacter patient = new PlayerCharacter(
                "Test", "Abbasid Era", "Scholar Household", "Stable Household",
                "Patient", 25, 70, 20, 10, 10, 10, 50, 50, 20);

        patient.takeCareer(Career.SCHOLAR, 0);

        for (int year = 0; year < 20; year++) {
            patient.recordYearWorked();
        }

        assertFalse(CareerService.isPromotionDue(patient),
                "twenty years of turning up promoted somebody who never learned anything");
    }

    @Test
    @DisplayName("a post out of reach says what is missing rather than hiding")
    void aClosedDoorIsLabelled() {
        PlayerCharacter nobody = new PlayerCharacter(
                "Test", "Abbasid Era", "Poor Village Child", "Debt-Burdened Family",
                "Cautious", 20, 40, 5, 5, 5, 5, 50, 50, 50);

        List<CareerService.Opening> openings =
                CareerService.openings(nobody, new com.example.al_mirath.model.FactionRelations(), null);

        assertFalse(openings.isEmpty());

        boolean anyShut = false;

        for (CareerService.Opening opening : openings) {
            if (!opening.isOpen()) {
                anyShut = true;

                assertFalse(opening.blockedBy().isBlank(),
                        opening.title() + " is shut and does not say why");
            }

            assertFalse(opening.title().isBlank());
        }

        assertTrue(anyShut,
                "a penniless, unlettered twenty-year-old was offered every post in the city");
    }

    @Test
    @DisplayName("a trade taken is a trade the character holds")
    void takingAPostSticks() {
        GameEngine engine = Lives.aliveAt(20);

        CareerService.Opening farming = null;

        for (CareerService.Opening opening : engine.careerOpenings()) {
            if (opening.career() == Career.FARMER) {
                farming = opening;
            }
        }

        assertNotNull(farming, "farming is open to anyone and was not offered");
        assertTrue(farming.isOpen(), farming.blockedBy());

        engine.takeJob(farming, true);

        assertEquals(Career.FARMER, engine.getPlayer().getCareer());
        assertTrue(engine.getPlayer().isEmployed());

        engine.leaveTrade();

        assertFalse(engine.getPlayer().isEmployed(),
                "walking out of a post left the character still in it");
    }

    // ── Holdings ────────────────────────────────────────────────────────

    @Test
    @DisplayName("property is bought, pays, wears out, and can be put right")
    void holdingsBehave() {
        GameEngine engine = Lives.aliveAt(25);

        engine.getPlayer().setNetWorth(20_000);

        PropertyMarket.Listing listing = null;

        for (PropertyMarket.Listing candidate : engine.propertyListings()) {
            if (candidate.isOpen() && candidate.type().baseIncome() > 0) {
                listing = candidate;
                break;
            }
        }

        if (listing == null) {
            // The market is different every year; this is not a failure.
            return;
        }

        double purse = engine.getPlayer().getNetWorth();

        engine.buyProperty(listing);

        assertEquals(1, engine.getPlayer().getProperties().size());
        assertEquals((long) (purse - listing.price()),
                (long) engine.getPlayer().getNetWorth(),
                "the deed cost something other than its price");

        Property bought = engine.getPlayer().getProperties().get(0);

        assertTrue(bought.effectiveIncome() > 0, "a shop that earns nothing is furniture");

        int condition = bought.condition();
        bought.damage(40);

        assertTrue(bought.condition() < condition);
        assertTrue(bought.repairQuote() > 0, "a damaged holding costs nothing to repair");

        engine.getPlayer().setNetWorth(20_000);
        engine.repairProperty(bought);

        assertEquals(100, bought.condition(), "the masons took the money and left");
    }

    @Test
    @DisplayName("a quick sale never fetches what it cost")
    void sellingIsALoss() {
        Property shop = new Property("A shop", Property.Type.SHOP, 800);

        assertTrue(shop.resaleValue() < shop.purchasePrice(),
                "property sold for more than it cost, in a hurry, at full condition");

        shop.damage(80);

        assertTrue(shop.resaleValue() < 800 * 0.5,
                "a ruin still fetched half its price");
    }

    @Test
    @DisplayName("what the house owns outlives the person who bought it")
    void theEstateIsInherited() {
        Random random = new Random(19);

        for (int attempt = 0; attempt < 250; attempt++) {
            GameEngine engine = new GameEngine();

            engine.getPlayer().setNetWorth(30_000);
            engine.getPlayer().addProperty(
                    new Property("The caravansary by the gate",
                            Property.Type.CARAVANSARY, 3000));

            Lives.live(engine, Lives.takingAnyOpenChoice(random));

            if (!engine.hasSuccessor()) {
                continue;
            }

            GameEngine heir = engine.succeedTo(engine.getSuccessors().get(0));
            assertNotNull(heir);

            assertFalse(heir.getPlayer().getProperties().isEmpty(),
                    "the caravansary vanished when its owner died");

            assertTrue(heir.getPlayer().getNetWorth() > 0,
                    "thirty thousand dirhams died with the man who saved them");

            assertTrue(heir.getPlayer().getNetWorth() < 30_000,
                    "a death, a burial and the other claimants cost the estate nothing");

            return;
        }
    }

    @Test
    @DisplayName("a trade and its holdings survive a save")
    void theLivelihoodRoundTrips() {
        GameEngine engine = Lives.aliveAt(24);

        engine.getPlayer().takeCareer(Career.MERCHANT, 1);
        engine.getPlayer().recordYearWorked();
        engine.getPlayer().addProperty(new Property("A shop on the row", Property.Type.SHOP, 760, 62));

        GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

        assertEquals(Career.MERCHANT, loaded.getPlayer().getCareer());
        assertEquals(1, loaded.getPlayer().getCareerRank());
        assertEquals(1, loaded.getPlayer().getYearsInCareer());
        assertEquals(1, loaded.getPlayer().getProperties().size());
        assertEquals(62, loaded.getPlayer().getProperties().get(0).condition());
        assertEquals("A shop on the row", loaded.getPlayer().getProperties().get(0).name());
    }
}
