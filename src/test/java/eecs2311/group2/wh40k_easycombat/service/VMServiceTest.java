package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.service.vm.DicePool;
import eecs2311.group2.wh40k_easycombat.service.vm.RuleContext;
import eecs2311.group2.wh40k_easycombat.service.vm.RuleResult;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full JUnit 5 test suite for VMService API.
 * Highlighting success/failure handling and variable retrieval from RuleResult.
 */
public class VMServiceTest {

    private RuleContext ctx;

    @BeforeEach
    void setUp() {
        // Reset context before each test for isolation
        ctx = new RuleContext();
    }

    @Test
    @DisplayName("Complex Workflow: Reroll, Filter, and Count")
    void testRerollFilterFlow() {
        String script = """
        fixed 1 1 3 6 -> raw_hits
        reroll raw_hits (@ == 1) -> rerolled_pool
        filter rerolled_pool (@ >= 4) -> successes
        count successes -> total
        """;

        VMService.loadRule("ComplexFlow", script);
        RuleResult result = VMService.run("ComplexFlow", ctx);

        // Assert execution was successful
        assertTrue(result.isSuccess(), "Execution should succeed. Error: " + result.getError());

        // Retrieve values from the result's context
        DicePool rerolled = (DicePool) result.getValue("rerolled_pool");
        Number total = (Number) result.getValue("total");

        assertNotNull(rerolled);
        assertEquals(((DicePool)result.getValue("successes")).getDice().size(), total.intValue());
    }

    @Test
    @DisplayName("Sorting: Keep High/Low Logic")
    void testKeepLogic() {
        String script = """
        fixed 1 5 2 6 4 -> p
        keep_high p 2 -> high
        keep_low p 2 -> low
        """;

        VMService.loadRule("KeepTest", script);
        RuleResult result = VMService.run("KeepTest", ctx);

        assertTrue(result.isSuccess());

        List<Integer> highDice = ((DicePool) result.getValue("high")).getDice();
        List<Integer> lowDice = ((DicePool) result.getValue("low")).getDice();

        assertTrue(highDice.contains(6) && highDice.contains(5));
        assertTrue(lowDice.contains(1) && lowDice.contains(2));
    }

    @Test
    @DisplayName("Arithmetic: Variable Access and Math Operations")
    void testMathAndContextAccess() {
        // Inject external variable into context
        ctx.set("attacker_strength", 4.0);

        String script = """
        attacker_strength + 2 -> modified_s
        modified_s * 2 -> final_s
        """;

        VMService.loadRule("MathTest", script);
        RuleResult result = VMService.run("MathTest", ctx);

        assertTrue(result.isSuccess());
        assertEquals(6.0, ((Number) result.getValue("modified_s")).doubleValue());
        assertEquals(12.0, ((Number) result.getValue("final_s")).doubleValue());
    }

    @Test
    @DisplayName("Control Flow: While Loop Integrity")
    void testWhileLoop() {
        String script = """
        5 -> counter
        0 -> steps
        while (counter > 0)
            counter -= 1
            steps++
        endwhile
        """;

        VMService.loadRule("LoopTest", script);
        RuleResult result = VMService.run("LoopTest", ctx);

        assertTrue(result.isSuccess());
        assertEquals(0.0, ((Number) result.getValue("counter")).doubleValue());
        assertEquals(5, ((Number) result.getValue("steps")).intValue());
    }

    @Test
    @DisplayName("Error Handling: Catching Syntax Errors")
    void testSyntaxError() {
        // Missing the closing parenthesis ')' in while condition
        String badScript = """
        while (hp > 0
            hp -= 1
        endwhile
        """;

        // Depending on your implementation, loadRule might throw DSLException
        // or VMService.run might return a failure result.
        try {
            VMService.loadRule("BadSyntax", badScript);
            RuleResult result = VMService.run("BadSyntax", ctx);

            assertFalse(result.isSuccess(), "Result should report failure for bad syntax");
            assertNotNull(result.getError());
            System.out.println("Captured Expected Error: " + result.getError());
            assertTrue(result.getError().contains("Line 1"), "Error should point to Line 1");

        } catch (Exception e) {
            // If the compiler throws immediately during loadRule
            assertTrue(e.getMessage().contains("Line 1"));
        }
    }

    @Test
    @DisplayName("Error Handling: Undefined Variable in Expression")
    void testRuntimeError() {
        // 'undefined_var' is never initialized
        String script = "undefined_var + 10 -> result";

        VMService.loadRule("RuntimeError", script);
        RuleResult result = VMService.run("RuntimeError", ctx);

        assertFalse(result.isSuccess(), "Should fail due to undefined variable");
        System.out.println("Captured Runtime Error: " + result.getError());
    }

    @Test
    @DisplayName("Utility: Print and Store Flow")
    void testPrintAndStore() {
        String script = """
        roll 3 -> p
        print p
        100 -> score
        print score
        """;

        VMService.loadRule("PrintTest", script);
        RuleResult result = VMService.run("PrintTest", ctx);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue("p"));
        assertEquals(100, ((Number)result.getValue("score")).intValue());
    }

    @Test
    @DisplayName("External Parameters: Single Numbers and Map Data")
    void testExternalParameters() {
        // 1. Passing individual numbers (Double and Integer)
        ctx.set("base_damage", 10.0);
        ctx.set("modifier", 2);

        // 2. Passing a Map as a parameter (e.g., weapon stats)
        // Note: In your expression system, you might access map values via specific logic,
        // but here we treat the Map as a single variable.
        java.util.Map<String, Object> weaponStats = new java.util.HashMap<>();
        weaponStats.put("strength", 4.0);
        weaponStats.put("ap", -1.0);
        ctx.set("weapon", weaponStats);

        String script = """
        # Test basic numeric arithmetic from external context
        base_damage + modifier -> total_raw
        
        # If your Expression system supports map access (e.g., weapon.strength), 
        # you would test it here. If not, we just verify the variable exists.
        print weapon
        """;

        VMService.loadRule("ParamTest", script);
        RuleResult result = VMService.run("ParamTest", ctx);

        assertTrue(result.isSuccess(), "Should handle external numeric parameters");
        assertEquals(12.0, ((Number) result.getValue("total_raw")).doubleValue());
        assertNotNull(result.getValue("weapon"), "External Map should be accessible in context");
    }

    @Test
    @DisplayName("Error: Accessing Non-existent External Parameter")
    void testMissingExternalParameter() {
        // We do NOT set "external_var" in the context
        String script = "external_var + 5 -> result";

        VMService.loadRule("MissingParamTest", script);
        RuleResult result = VMService.run("MissingParamTest", ctx);

        // This should fail because VarExpr now throws an exception if the key is missing
        assertFalse(result.isSuccess(), "Should fail when variable is missing from context");
        assertTrue(result.getError().contains("external_var"), "Error message should mention the missing variable");
        System.out.println("Expected Error: " + result.getError());
    }

    @Test
    @DisplayName("Data Persistence: Sequential Script Execution")
    void testContextPersistence() {
        // First run: Define a variable
        VMService.loadRule("Step1", "100 -> shared_val");
        VMService.run("Step1", ctx);

        // Second run: Use the variable defined in Step 1 (using the SAME ctx)
        VMService.loadRule("Step2", "shared_val + 50 -> final_val");
        RuleResult result = VMService.run("Step2", ctx);

        assertTrue(result.isSuccess());
        assertEquals(150.0, ((Number) result.getValue("final_val")).doubleValue());
    }

    @Test
    @DisplayName("Member Access: Accessing Map properties using weapon.stats")
    void testMemberAccess() {
        // Setup a nested Map structure
        java.util.Map<String, Object> weapon = new java.util.HashMap<>();
        weapon.put("name", "Bolter");
        weapon.put("S", 4);
        weapon.put("AP", -1);

        // Put the map into the context
        ctx.set("weapon", weapon);

        // Script using the dot notation
        // Note: Your ExpressionParser must be updated to produce MemberExpr for the '.' token
        String script = """
        weapon.S -> weapon_strength
        weapon.AP -> weapon_ap
        weapon_strength + 2 -> modified_s
        """;

        VMService.loadRule("MemberTest", script);
        RuleResult result = VMService.run("MemberTest", ctx);

        assertTrue(result.isSuccess(), "Should handle weapon.S access. Error: " + result.getError());

        assertEquals(4, ((Number) result.getValue("weapon_strength")).intValue());
        assertEquals(-1, ((Number) result.getValue("weapon_ap")).intValue());
        assertEquals(6, ((Number) result.getValue("modified_s")).intValue());
    }

    @Test
    @DisplayName("Error: Accessing non-existent member")
    void testMissingMemberError() {
        java.util.Map<String, Object> weapon = new java.util.HashMap<>();
        weapon.put("S", 4);
        ctx.set("weapon", weapon);

        // 'A' does not exist in the map
        String script = "weapon.A -> result";

        VMService.loadRule("MissingMemberTest", script);
        RuleResult result = VMService.run("MissingMemberTest", ctx);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Property 'A' not found"),
                "Error should specify which property was missing");
        System.out.println("Captured Expected Error: " + result.getError());
    }

    @Test
    @DisplayName("Management: Get Loaded Rules List")
    void testGetLoadedRules() {
        // 1. Initially, it might have rules from other tests, or be empty
        // We load two specific new rules
        String r1 = "1 -> a";
        String r2 = "2 -> b";

        VMService.loadRule("AlphaRule", r1);
        VMService.loadRule("BetaRule", r2);

        // 2. Get the set of names
        java.util.Set<String> loadedRules = VMService.getLoadedRules();

        // 3. Verify they exist in the set
        assertTrue(loadedRules.contains("AlphaRule"), "AlphaRule should be in the list");
        assertTrue(loadedRules.contains("BetaRule"), "BetaRule should be in the list");

        System.out.println("Current Loaded Rules: " + loadedRules);
    }

    @Test
    @DisplayName("Management: Remove a Loaded Rule")
    void testRemoveRule() {
        String script = "10 -> val";
        String name = "TemporaryRule";
        VMService.loadRule(name, script);

        assertTrue(VMService.getLoadedRules().contains(name));

        VMService.removeLoadedRule(name);

        assertFalse(VMService.getLoadedRules().contains(name), "Rule should have been removed from the set");

        RuleResult result = VMService.run(name, ctx);

        assertFalse(result.isSuccess(), "Running a removed rule should return a failure result");
        assertTrue(result.getError().contains("doesn't exist") || result.getError().contains("not found"),
                "Error message should indicate the rule is missing");

        System.out.println("Successfully blocked execution: " + result.getError());
    }

    @Test
    @DisplayName("Management: Overwriting an existing rule")
    void testOverwriteRule() {
        VMService.loadRule("VersionRule", "1 -> v");
        RuleResult res1 = VMService.run("VersionRule", ctx);

        assertTrue(res1.isSuccess());
        assertEquals(1, ((Number)res1.getValue("v")).intValue());

        VMService.loadRule("VersionRule", "2 -> v");

        ctx = new RuleContext();
        RuleResult res2 = VMService.run("VersionRule", ctx);

        assertTrue(res2.isSuccess(), "Overwritten rule should still run successfully");
        assertEquals(2.0, ((Number)res2.getValue("v")).doubleValue(), "Rule logic should be updated to Version 2");
    }
}