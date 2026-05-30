package com.ethan.combatcalc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NpcCombatProfileTest
{
    @Test
    public void storesThreatAndWikiWeaknessMetadata()
    {
        NpcCombatProfile profile = new NpcCombatProfile("Blue Dragon");
        profile.setCombatLevel(111);
        profile.setMaxHit(10);
        profile.setAggressive(true);
        profile.setAttackType("dragonfire, melee");
        profile.setWikiWeakness("water");
        profile.setElementalWeaknessPercent(50);
        profile.setDataSource("osrs-wiki");

        assertEquals(111, profile.getCombatLevel());
        assertEquals(10, profile.getMaxHit());
        assertTrue(profile.isAggressive());
        assertEquals("dragonfire, melee", profile.getAttackType());
        assertEquals("water", profile.getWikiWeakness());
        assertEquals(50, profile.getElementalWeaknessPercent());
        assertEquals("osrs-wiki", profile.getDataSource());
        assertTrue(profile.hasWikiWeakness());
        assertTrue(profile.hasThreatData());
    }

    @Test
    public void emptyProfileHasNoThreatData()
    {
        NpcCombatProfile profile = new NpcCombatProfile("Unknown");

        assertFalse(profile.hasWikiWeakness());
        assertFalse(profile.hasThreatData());
    }
}
