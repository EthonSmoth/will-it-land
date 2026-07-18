package com.ethan.combatcalc;

import net.runelite.api.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PoweredStaffMaxHitResolverTest
{
    @Test
    public void resolvesTridentFamilyBaseMaxHits()
    {
        PoweredStaffMaxHitResolver resolver = new PoweredStaffMaxHitResolver();

        assertEquals(20, resolver.resolveBaseMaxHit(ItemID.TRIDENT_OF_THE_SEAS, 75));
        assertEquals(28, resolver.resolveBaseMaxHit(ItemID.TRIDENT_OF_THE_SEAS, 99));
        assertEquals(31, resolver.resolveBaseMaxHit(ItemID.TRIDENT_OF_THE_SWAMP, 99));
        assertEquals(32, resolver.resolveBaseMaxHit(ItemID.SANGUINESTI_STAFF, 99));
    }

    @Test
    public void resolvesWarpedAndShadowBaseMaxHits()
    {
        PoweredStaffMaxHitResolver resolver = new PoweredStaffMaxHitResolver();

        assertEquals(16, resolver.resolveBaseMaxHit(ItemID.WARPED_SCEPTRE, 62));
        assertEquals(24, resolver.resolveBaseMaxHit(ItemID.WARPED_SCEPTRE, 99));
        assertEquals(34, resolver.resolveBaseMaxHit(ItemID.TUMEKENS_SHADOW, 99));
    }

    @Test
    public void identifiesSupportedPoweredStaves()
    {
        assertTrue(PoweredStaffMaxHitResolver.isPoweredStaff(ItemID.TRIDENT_OF_THE_SEAS));
        assertTrue(PoweredStaffMaxHitResolver.isPoweredStaff(ItemID.SANGUINESTI_STAFF));
        assertTrue(PoweredStaffMaxHitResolver.isPoweredStaff(ItemID.TUMEKENS_SHADOW));
    }
}
