package com.ethan.combatcalc;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for combat accuracy calculations.
 */
public class CombatCalculatorTest
{
    private final CombatModifier mockModifier = new MockCombatModifier();
    private final CombatCalculator calculator = new CombatCalculator(mockModifier);

    /**
     * Test that hit chance is properly clamped to 0-100%.
     */
    @Test
    public void testHitChanceClamping()
    {
        CombatProfile profile = new CombatProfile();
        profile.setEffectiveAttackLevel(99);
        profile.setAttackBonus(100);
        profile.setAttackSubType(AttackSubType.STAB);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setDefenceLevel(1);
        npcProfile.setStabDefence(-100);

        CombatResult result = calculator.calculateMeleeAccuracy(profile, npcProfile);

        // Hit chance should never exceed 1.0
        assertTrue("Hit chance should not exceed 100%", result.getHitChance() <= 1.0);
        assertTrue("Hit chance should not be negative", result.getHitChance() >= 0.0);
    }

    /**
     * Test melee accuracy calculation.
     */
    @Test
    public void testMeleeAccuracy()
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(CombatType.MELEE);
        profile.setAttackSubType(AttackSubType.STAB);
        profile.setEffectiveAttackLevel(60);
        profile.setAttackBonus(64);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setDefenceLevel(40);
        npcProfile.setStabDefence(0);

        CombatResult result = calculator.calculateMeleeAccuracy(profile, npcProfile);

        // With level 60 and +64 bonus vs level 40 defence with 0 bonus, should have good accuracy
        assertNotNull("Result should not be null", result);
        assertTrue("Hit chance should be positive", result.getHitChance() > 0.0);
        assertTrue("Hit chance should be reasonable", result.getHitChance() > 0.5);
    }

    /**
     * Test ranged accuracy calculation.
     */
    @Test
    public void testRangedAccuracy()
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(CombatType.RANGED);
        profile.setAttackSubType(AttackSubType.RANGED);
        profile.setEffectiveAttackLevel(60);
        profile.setAttackBonus(50);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setDefenceLevel(40);
        npcProfile.setRangedDefence(0);

        CombatResult result = calculator.calculateRangedAccuracy(profile, npcProfile);

        assertNotNull("Result should not be null", result);
        assertTrue("Hit chance should be positive", result.getHitChance() > 0.0);
        assertEquals("Combat type should be RANGED", CombatType.RANGED, result.getCombatType());
    }

    /**
     * Test magic accuracy calculation.
     */
    @Test
    public void testMagicAccuracy()
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(CombatType.MAGIC);
        profile.setAttackSubType(AttackSubType.MAGIC);
        profile.setEffectiveAttackLevel(60);
        profile.setAttackBonus(40);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setMagicLevel(40);
        npcProfile.setMagicDefence(0);

        CombatResult result = calculator.calculateMagicAccuracy(profile, npcProfile);

        assertNotNull("Result should not be null", result);
        assertTrue("Hit chance should be positive", result.getHitChance() > 0.0);
        assertEquals("Combat type should be MAGIC", CombatType.MAGIC, result.getCombatType());
    }

    /**
     * Test that better equipment gives better accuracy.
     */
    @Test
    public void testEquipmentBoosts()
    {
        CombatProfile baseProfile = new CombatProfile();
        baseProfile.setCombatType(CombatType.MELEE);
        baseProfile.setAttackSubType(AttackSubType.STAB);
        baseProfile.setEffectiveAttackLevel(60);
        baseProfile.setAttackBonus(0);

        CombatProfile boostedProfile = new CombatProfile();
        boostedProfile.setCombatType(CombatType.MELEE);
        boostedProfile.setAttackSubType(AttackSubType.STAB);
        boostedProfile.setEffectiveAttackLevel(60);
        boostedProfile.setAttackBonus(50);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setDefenceLevel(40);
        npcProfile.setStabDefence(0);

        CombatResult baseResult = calculator.calculateMeleeAccuracy(baseProfile, npcProfile);
        CombatResult boostedResult = calculator.calculateMeleeAccuracy(boostedProfile, npcProfile);

        // Better equipment should give better accuracy
        assertTrue("Better equipment should increase hit chance",
                boostedResult.getHitChance() > baseResult.getHitChance());
    }

    /**
     * Test attack style variations.
     */
    @Test
    public void testAttackStyleVariations()
    {
        CombatProfile stabProfile = new CombatProfile();
        stabProfile.setCombatType(CombatType.MELEE);
        stabProfile.setAttackSubType(AttackSubType.STAB);
        stabProfile.setEffectiveAttackLevel(60);
        stabProfile.setAttackBonus(50);

        CombatProfile slashProfile = new CombatProfile();
        slashProfile.setCombatType(CombatType.MELEE);
        slashProfile.setAttackSubType(AttackSubType.SLASH);
        slashProfile.setEffectiveAttackLevel(60);
        slashProfile.setAttackBonus(50);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Test NPC");
        npcProfile.setDefenceLevel(40);
        npcProfile.setStabDefence(10);
        npcProfile.setSlashDefence(0);

        CombatResult stabResult = calculator.calculateMeleeAccuracy(stabProfile, npcProfile);
        CombatResult slashResult = calculator.calculateMeleeAccuracy(slashProfile, npcProfile);

        // Attack style with matching defence bonus should give better accuracy
        assertTrue("Slash should be more accurate vs lower slash defence",
                slashResult.getHitChance() > stabResult.getHitChance());
    }

    /**
     * Test edge case: very high level vs very low defence.
     */
    @Test
    public void testHighLevelVsLowDefence()
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(CombatType.MELEE);
        profile.setAttackSubType(AttackSubType.STAB);
        profile.setEffectiveAttackLevel(99);
        profile.setAttackBonus(200);

        NpcCombatProfile npcProfile = new NpcCombatProfile("Weak NPC");
        npcProfile.setDefenceLevel(1);
        npcProfile.setStabDefence(-200);

        CombatResult result = calculator.calculateMeleeAccuracy(profile, npcProfile);

        // Should have very high accuracy but still clamped to 1.0
        assertTrue("Hit chance should be very high", result.getHitChance() > 0.99);
        assertTrue("Hit chance should not exceed 100%", result.getHitChance() <= 1.0);
    }

    /**
     * Mock implementation of CombatModifier for testing.
     */
    public static class MockCombatModifier extends CombatModifier
    {
        public MockCombatModifier()
        {
            super(null);
        }

        @Override
        public double getOffensiveRollMultiplier(CombatType combatType, NpcCombatProfile npcProfile)
        {
            return 1.0; // No modifiers for base tests
        }
    }
}

