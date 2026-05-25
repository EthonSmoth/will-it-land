package com.ethan.combatcalc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeaponInfoTest
{
    @Test
    public void formatsSpeedInTicksAndSeconds()
    {
        WeaponInfo info = WeaponInfo.builder("Abyssal whip")
                .attackSpeedTicks(4)
                .build();

        assertEquals("4 ticks (2.4s)", info.formatAttackSpeed());
    }

    @Test
    public void formatsRapidSpeedWhenKnown()
    {
        WeaponInfo info = WeaponInfo.builder("Toxic blowpipe")
                .attackSpeedTicks(3)
                .rapidAttackSpeedTicks(2)
                .build();

        assertEquals("3 ticks (1.8s), 2 rapid", info.formatAttackSpeed());
    }

    @Test
    public void formatsRangeWithLongrange()
    {
        WeaponInfo info = WeaponInfo.builder("Twisted bow")
                .baseRange(10)
                .longRange(10)
                .build();

        assertEquals("10 tiles", info.formatRange());
    }

    @Test
    public void formatsOffensiveBonusesForActiveStyle()
    {
        WeaponInfo info = WeaponInfo.builder("Tumeken's shadow")
                .magicAttackBonus(35)
                .magicDamageBonus(15.0f)
                .activeAttackSubType(AttackSubType.MAGIC)
                .build();

        assertEquals("+35 Magic, +15% Magic Dmg", info.formatRelevantBonuses());
    }

    @Test
    public void detectsEquippedAmmo()
    {
        WeaponInfo info = WeaponInfo.builder("Dragon crossbow")
                .ammoName("Ruby bolts (e)")
                .rangedStrengthBonus(103)
                .build();

        assertTrue(info.hasAmmo());
        assertEquals("Ruby bolts (e)", info.getAmmoName());
        assertEquals("+103 Ranged Str", info.formatRelevantBonuses());
    }

    @Test
    public void formatsAllOffensiveBonuses()
    {
        WeaponInfo info = WeaponInfo.builder("Mixed weapon")
                .stabAttackBonus(10)
                .slashAttackBonus(20)
                .crushAttackBonus(-5)
                .magicAttackBonus(0)
                .rangedAttackBonus(7)
                .build();

        assertEquals("Stab +10 | Slash +20 | Crush -5 | Magic 0 | Ranged +7", info.formatAllOffensiveBonuses());
    }

    @Test
    public void emptyInfoDoesNotRenderWeaponSection()
    {
        WeaponInfo info = WeaponInfo.empty();

        assertFalse(info.hasWeapon());
        assertEquals("", info.formatAttackSpeed());
        assertEquals("", info.formatRange());
    }
}
