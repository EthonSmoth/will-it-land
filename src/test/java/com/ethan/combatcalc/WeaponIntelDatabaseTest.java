package com.ethan.combatcalc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeaponIntelDatabaseTest
{
    @Test
    public void returnsSpecialAttackForKnownWeapon()
    {
        WeaponIntelDatabase database = new WeaponIntelDatabase();

        WeaponInfo info = database.lookup("Dragon warhammer");

        assertTrue(info.hasSpecialAttack());
        assertEquals("Smash", info.getSpecialAttack().getName());
        assertEquals(50, info.getSpecialAttack().getEnergyCostPercent());
        assertEquals("Reduces the target's Defence by 30% if it hits.", info.getSpecialAttack().getDescription());
        assertEquals("Crush", info.getSpecialAttack().getDefenceRoll());
    }

    @Test
    public void returnsPassiveEffectForKnownWeapon()
    {
        WeaponIntelDatabase database = new WeaponIntelDatabase();

        WeaponInfo info = database.lookup("Twisted bow");

        assertFalse(info.hasSpecialAttack());
        assertEquals(1, info.getPassiveEffects().size());
        assertEquals("Accuracy and damage scale with the target's Magic level.", info.getPassiveEffects().get(0));
    }

    @Test
    public void normalizesRuneLiteMarkupAndCase()
    {
        WeaponIntelDatabase database = new WeaponIntelDatabase();

        WeaponInfo info = database.lookup("<col=ff981f>toxic blowpipe</col>");

        assertTrue(info.hasSpecialAttack());
        assertEquals("Toxic Siphon", info.getSpecialAttack().getName());
        assertEquals(5, info.getBaseRange());
        assertEquals(7, info.getLongRange());
    }

    @Test
    public void returnsEmptyInfoForUnknownWeapon()
    {
        WeaponIntelDatabase database = new WeaponIntelDatabase();

        WeaponInfo info = database.lookup("Training sword");

        assertEquals("Training sword", info.getWeaponName());
        assertFalse(info.hasSpecialAttack());
        assertTrue(info.getPassiveEffects().isEmpty());
        assertEquals(0, info.getBaseRange());
    }
}
