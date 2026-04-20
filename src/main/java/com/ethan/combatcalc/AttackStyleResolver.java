package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.VarPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Resolves combat type (MELEE/RANGED/MAGIC) and attack subtype (STAB/SLASH/CRUSH/RANGED/MAGIC).
 * Based on equipped weapon and active attack style.
 */
@Singleton
public class AttackStyleResolver
{
    @Inject
    public AttackStyleResolver()
    {
    }

    public AttackStyleType resolve(Client client)
    {
        int style = client.getVarpValue(VarPlayer.ATTACK_STYLE);

        switch (style)
        {
            case 0:
                return AttackStyleType.STAB;
            case 1:
                return AttackStyleType.SLASH;
            case 2:
                return AttackStyleType.CRUSH;
            case 3:
                return AttackStyleType.CONTROLLED;
            default:
                return AttackStyleType.UNKNOWN;
        }
    }

    /**
     * Determines combat type based on equipped weapon and active style.
     */
    public CombatType resolveCombatType(Client client)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return CombatType.MELEE;
        }

        // Check for magic spell cast
        int spell = client.getVarpValue(108);
        if (spell > 0)
        {
            return CombatType.MAGIC;
        }

        // Check attack style - ranged style is 3
        int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
        if (attackStyle == 3)
        {
            // This could be ranged defensive stance or controlled
            Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
            if (weapon != null && isRangedWeapon(weapon.getId()))
            {
                return CombatType.RANGED;
            }
        }

        // Check if weapon is ranged (bow, blowpipe, etc)
        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weapon != null && weapon.getId() != -1 && isRangedWeapon(weapon.getId()))
        {
            return CombatType.RANGED;
        }

        return CombatType.MELEE;
    }

    /**
     * Checks if a weapon ID is a ranged weapon.
     * This is a basic implementation - you may need to expand based on your needs.
     */
    private boolean isRangedWeapon(int weaponId)
    {
        // Bows
        if ((weaponId >= 4212 && weaponId <= 4223) || // Shortbow, longbow, oak, etc.
                (weaponId >= 845 && weaponId <= 855) || // Magic shortbow, etc.
                weaponId == 20997 || // Twisted bow
                weaponId == 12765 || weaponId == 12766 || weaponId == 12767 || weaponId == 12768) // Crystal bow
        {
            return true;
        }

        // Crossbows
        if ((weaponId >= 837 && weaponId <= 843) || // Bronze crossbow, etc.
                weaponId == 11212 || // Dragon hunter crossbow
                weaponId == 21902 || // Dragon crossbow
                weaponId == 27655) // Armadyl crossbow
        {
            return true;
        }

        // Blowpipe
        if (weaponId >= 12924 && weaponId <= 12926) // Toxic blowpipe
        {
            return true;
        }

        // Other ranged weapons
        return weaponId == 10034 || weaponId == 10033 || // Chinchompas
                weaponId == 11959 || // Black chinchompa
                weaponId == 10156; // Red chinchompa
    }

    /**
     * Checks if a weapon ID is a staff (uses crush attack style).
     */
    private boolean isStaff(int weaponId)
    {
        // Common staff IDs - this list can be expanded
        return weaponId == 1379 || // Staff
                (weaponId >= 1381 && weaponId <= 1389) || // Basic elemental staves
                weaponId == 1391 || weaponId == 1393 || // Mystic staves
                weaponId == 1395 || weaponId == 1397 || weaponId == 1399 || weaponId == 1401 || // Mystic staves
                weaponId == 1403 || weaponId == 1405 || weaponId == 1407 || weaponId == 1409 || // Mystic staves
                weaponId == 3053 || weaponId == 3054 || // Lava battlestaff
                weaponId == 4675 || // Ancient staff
                weaponId == 6914 || // Master wand
                weaponId == 11787 || weaponId == 11789 || // Staff of the dead
                weaponId == 11998 || // Trident of the seas
                weaponId == 12899 || // Trident of the swamp
                weaponId == 21006 || // Kodai wand
                weaponId == 22296 || // Staff of light
                weaponId == 24423 || // Staff of balance
                weaponId == 24424 || // Staff of balance (charged)
                weaponId == 25513 || // Sanguinesti staff
                weaponId == 25514 || // Sanguinesti staff (uncharged)
                weaponId == 25731 || // Toxic staff of the dead
                weaponId == 25733 || // Toxic staff of the dead (uncharged)
                weaponId == 25849 || // Eldritch nightmare staff
                weaponId == 25851 || // Volatile nightmare staff
                weaponId == 25853 || // Harmonised nightmare staff
                weaponId == 25855; // Eldritch nightmare staff (uncharged)
    }

    /**
     * Resolves attack subtype based on combat type and style.
     */
    public AttackSubType resolveAttackSubType(Client client, CombatType combatType)
    {
        switch (combatType)
        {
            case MELEE:
                return resolveMeleeSubType(client);
            case RANGED:
                return AttackSubType.RANGED;
            case MAGIC:
                return AttackSubType.MAGIC;
            default:
                return AttackSubType.UNKNOWN;
        }
    }

    private AttackSubType resolveMeleeSubType(Client client)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        Item weapon = equipment != null ? equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) : null;

        // Unarmed combat always uses crush
        if (weapon == null || weapon.getId() == -1)
        {
            return AttackSubType.CRUSH;
        }

        // Staves always use crush attack style
        if (isStaff(weapon.getId()))
        {
            return AttackSubType.CRUSH;
        }

        int style = client.getVarpValue(VarPlayer.ATTACK_STYLE);

        switch (style)
        {
            case 0:
                return AttackSubType.STAB;
            case 1:
                return AttackSubType.SLASH;
            case 2:
                return AttackSubType.CRUSH;
            case 3:
                return AttackSubType.STAB;
            default:
                return AttackSubType.UNKNOWN;
        }
    }
}
