package com.ethan.combatcalc;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeaponAttackStylesTest
{
    @Test
    public void supportsAllCombatOptionCategoriesFromWeaponsCategoriesWikiPage()
    {
        List<String> categories = Arrays.asList(
                "2h Sword",
                "Axe",
                "Banner",
                "Bladed Staff",
                "Blaster",
                "Bludgeon",
                "Blunt",
                "Bow",
                "Bulwark",
                "Chinchompas",
                "Claw",
                "Crossbow",
                "Gun",
                "Multi-Style",
                "Multi-Melee",
                "Partisan",
                "Pickaxe",
                "Polearm",
                "Polestaff",
                "Powered Staff",
                "Powered Wand",
                "Salamander",
                "Scythe",
                "Slash Sword",
                "Spear",
                "Spiked",
                "Stab Sword",
                "Staff",
                "Thrown",
                "Unarmed",
                "Whip"
        );

        for (String category : categories)
        {
            assertTrue("Missing category: " + category, WeaponAttackStyles.hasCategory(category));
        }
    }

    @Test
    public void resolvesMeleeCategoriesWithMixedAttackTypes()
    {
        assertEquals(AttackSubType.SLASH, WeaponAttackStyles.resolveAttackSubType("Slash Sword", 0));
        assertEquals(AttackSubType.STAB, WeaponAttackStyles.resolveAttackSubType("Slash Sword", 2));
        assertEquals(AttackSubType.CRUSH, WeaponAttackStyles.resolveAttackSubType("Spear", 2));
        assertEquals(AttackSubType.SLASH, WeaponAttackStyles.resolveAttackSubType("Whip", 1));
    }

    @Test
    public void resolvesRangedAndMagicCategories()
    {
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Bow", 0));
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Crossbow", 2));
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Thrown", 1));
        assertEquals(AttackSubType.MAGIC, WeaponAttackStyles.resolveAttackSubType("Powered Staff", 2));
        assertEquals(AttackSubType.MAGIC, WeaponAttackStyles.resolveAttackSubType("Staff", 4));
    }

    @Test
    public void resolvesOtherMultiStyleCategories()
    {
        assertEquals(AttackSubType.SLASH, WeaponAttackStyles.resolveAttackSubType("Salamander", 0));
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Salamander", 1));
        assertEquals(AttackSubType.MAGIC, WeaponAttackStyles.resolveAttackSubType("Salamander", 2));
        assertEquals(AttackSubType.STAB, WeaponAttackStyles.resolveAttackSubType("Multi-Style", 0));
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Multi-Style", 1));
        assertEquals(AttackSubType.MAGIC, WeaponAttackStyles.resolveAttackSubType("Multi-Style", 2));
        assertEquals(AttackSubType.CRUSH, WeaponAttackStyles.resolveAttackSubType("Multi-Melee", 2));
    }

    @Test
    public void normalizesCombatTabCategoryText()
    {
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("Weapon: Chinchompa", 1));
        assertEquals(AttackSubType.RANGED, WeaponAttackStyles.resolveAttackSubType("weapon: bow", 2));
        assertEquals(AttackSubType.MAGIC, WeaponAttackStyles.resolveAttackSubType("<col=ff981f>Powered staff</col>", 0));
        assertEquals(AttackSubType.UNKNOWN, WeaponAttackStyles.resolveAttackSubType("Bulwark", 1));
        assertEquals(AttackSubType.UNKNOWN, WeaponAttackStyles.resolveAttackSubType("Not real", 0));
        assertEquals(AttackSubType.UNKNOWN, WeaponAttackStyles.resolveAttackSubType("Bow", 9));
    }
}
