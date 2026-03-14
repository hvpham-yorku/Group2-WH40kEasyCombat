package eecs2311.group2.wh40k_easycombat.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eecs2311.group2.wh40k_easycombat.util.CostTier;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.manager.ArmyBuilderManager;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM.EnhancementEntry;


class ArmyValidationServiceTest {

   @Test
   @DisplayName("Validation should fail when faction is null")
   void testNullFactionValidation() {
       String result = ArmyBuilderManager.validateBeforeSave(
           null,
           "Detachment",
           "MyArmy",
           List.of(createDummyUnit("TestUnit", true)),
           2000
       );

       assertEquals("Please choose one specific faction. \"All\" is not allowed.", result,"Validation should fail when faction is null");
   }

   @Test
   @DisplayName("Validation fails when faction is string 'All'")
   void testAllFactionValidation() {

       String result = ArmyBuilderManager.validateBeforeSave(
           "All",
           "Detachment",
           "MyArmy",
           List.of(createDummyUnit("TestUnit", true)),
           2000
       );

       assertEquals("Please choose one specific faction. \"All\" is not allowed.", result,"Validation should fail when faction is string 'All'");
   }

   @Test
   @DisplayName("Validation fails when detachment is null")
   void testNullDetachmentValidation() {

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           null,
           "MyArmy",
           List.of(createDummyUnit("TestUnit", true)),
           2000
       );

       assertEquals("Please choose one detachment.",result,"Validation should fail when detachment is null");
   }
   
   @Test
   @DisplayName("Validation fails when army name is empty")
   void testEmptyArmyNameValidation() {

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "",
           List.of(createDummyUnit("TestUnit", true)),
           2000
       );

       assertEquals("Please enter an army name.", result, "Validation should fail when army name is empty");
   }
   @Test
   @DisplayName("Validation fails when army list is empty")
   void testEmptyArmyValidation() {

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "MyArmy",
           List.of(),
           2000
       );

       assertEquals("Your army is empty.", result,"Validation should fail when army list is empty");
   }

   @Test
   @DisplayName("Validation fails when more than one warlord is selected")
   void testMultipleWarlordValidation() {

       ArmyUnitVM u1 = createDummyUnit("TestUnit1", true);
       ArmyUnitVM u2 = createDummyUnit("TestUnit2", true);

       u1.warlordProperty().set(true);
       u2.warlordProperty().set(true);

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "MyArmy",
           List.of(u1, u2),
           2000
       );

       assertEquals("Only one CHARACTER unit can be set as warlord.", result,"Validation should fail when multiple warlords are selected");
   }

   @Test
   @DisplayName("Validation fails when duplicate enhancements are used")
   void testDuplicateEnhancementValidation() {

       ArmyUnitVM u1 = createDummyUnit("TestUnit1", true);
       ArmyUnitVM u2 = createDummyUnit("TestUnit2", true);

       EnhancementEntry e = new EnhancementEntry("1", "name", 1);

       u1.setEnhancement(e);
       u2.setEnhancement(e);

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "MyArmy",
           List.of(u1, u2),
           2000
       );

       assertEquals("Each enhancement can only be taken once.",
           result,
           "Validation should fail when duplicate enhancements are used");
   }

   @Test
   @DisplayName("Validation fails when army points exceed limit")
   void testOverPointsLimitArmyValidation() {

       ArmyUnitVM unit1 = createDummyUnit("TestUnit1", true);
       ArmyUnitVM unit2 = createDummyUnit("TestUnit2", true);
       ArmyUnitVM unit3 = createDummyUnit("TestUnit2", true);

       unit1.warlordProperty().set(true);

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "MyArmy",
           List.of(unit1, unit2, unit3),
           250
       );

       assertEquals("Current army is 300 pts, but the selected limit is 250 pts.", result, "Validation should fail when army points exceed limit");
   }

   @Test
   @DisplayName("Validation succeeds for a valid army")
   void testValidArmyValidation() {

       ArmyUnitVM unit = createDummyUnit("TestUnit", true);
       unit.warlordProperty().set(true);

       String result = ArmyBuilderManager.validateBeforeSave(
           "SpaceMarines",
           "Detachment",
           "MyArmy",
           List.of(unit),
           2000
       );

       assertEquals(null, result, "Validation should return null for a valid army");
   }

   // ---- helpers ----
   private ArmyUnitVM createDummyUnit(String name, boolean character) {
   return new ArmyUnitVM(
           name + "id",
           name,
           "statline",
           "role",
           character,
           List.of(new CostTier(1, 100))
       );
   }   
}