package com.ethan.combatcalc;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NpcStatsRepositoryTest
{
    @Test
    public void loadsWikiStyleMetadataForCommonTargets()
    {
        NpcStatsRepository repository = new NpcStatsRepository(new Gson());

        NpcCombatProfile gemstoneCrab = repository.getNpcProfile("Gemstone Crab");
        assertEquals(160, gemstoneCrab.getCombatLevel());
        assertEquals(1, gemstoneCrab.getMaxHit());
        assertEquals("osrs-wiki", gemstoneCrab.getDataSource());

        NpcCombatProfile blueDragon = repository.getNpcProfile("Blue dragon");
        assertEquals(111, blueDragon.getCombatLevel());
        assertEquals(10, blueDragon.getMaxHit());
        assertEquals("water", blueDragon.getWikiWeakness());
        assertEquals(50, blueDragon.getElementalWeaknessPercent());

        assertTrue(repository.hasNpcProfile("blue dragon"));
        assertEquals("Blue dragon", repository.getNpcProfile("BLUE DRAGON").getNpcName());
    }
}
