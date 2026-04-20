package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Detects equipment set effects and special synergies.
 * Includes: Void Knight, Obsidian set, GWD sets, etc.
 */
@Singleton
public class EquipmentSynergyDetector
{
    private final Client client;

    @Inject
    public EquipmentSynergyDetector(Client client)
    {
        this.client = client;
    }

    /**
     * Gets accuracy multiplier from equipment set effects.
     */
    public double getSetEffectAccuracyMultiplier(CombatType combatType)
    {
        double multiplier = 1.0;

        // Void Knight set: +10% accuracy (requires full set)
        if (isWearingVoidSet(combatType))
        {
            multiplier *= 1.10;
        }

        // Obsidian set: +10% melee accuracy (requires full set)
        if (combatType == CombatType.MELEE && isWearingObsidianSet())
        {
            multiplier *= 1.10;
        }

        return multiplier;
    }

    /**
     * Gets damage multiplier from equipment set effects.
     */
    public double getSetEffectDamageMultiplier(CombatType combatType)
    {
        double multiplier = 1.0;

        // Void Knight set: +40% damage (requires full set)
        if (isWearingVoidSet(combatType))
        {
            multiplier *= 1.40;
        }

        // Obsidian set: +10% damage (requires full set)
        if (combatType == CombatType.MELEE && isWearingObsidianSet())
        {
            multiplier *= 1.10;
        }

        return multiplier;
    }

    /**
     * Checks if wearing full Void Knight set for a specific combat type.
     */
    private boolean isWearingVoidSet(CombatType combatType)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item helmet = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        Item top = equipment.getItem(EquipmentInventorySlot.BODY.getSlotIdx());
        Item legs = equipment.getItem(EquipmentInventorySlot.LEGS.getSlotIdx());
        Item hands = equipment.getItem(EquipmentInventorySlot.GLOVES.getSlotIdx());

        // Check for Void helmet (varies by type)
        if (helmet == null || helmet.getId() == -1)
        {
            return false;
        }

        int helmetId = helmet.getId();
        boolean validHelmet = false;

        switch (combatType)
        {
            case MELEE:
                validHelmet = helmetId == 11664; // Void melee helm
                break;
            case RANGED:
                validHelmet = helmetId == 11665; // Void ranger helm
                break;
            case MAGIC:
                validHelmet = helmetId == 11663; // Void mage helm
                break;
        }

        if (!validHelmet)
        {
            return false;
        }

        // Check for Void robe top (8840), Void robe legs (8842), Void gloves (8844)
        return top != null && top.getId() == 8840 &&
                legs != null && legs.getId() == 8842 &&
                hands != null && hands.getId() == 8844;
    }

    /**
     * Checks if wearing full Obsidian set (melee only).
     */
    private boolean isWearingObsidianSet()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item helmet = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        Item top = equipment.getItem(EquipmentInventorySlot.BODY.getSlotIdx());
        Item legs = equipment.getItem(EquipmentInventorySlot.LEGS.getSlotIdx());
        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());

        // Obsidian helmet (11665), Obsidian platebody (11663), Obsidian platelegs (11661), Obsidian maul (6718)
        return helmet != null && helmet.getId() == 11665 &&
                top != null && top.getId() == 11663 &&
                legs != null && legs.getId() == 11661 &&
                weapon != null && weapon.getId() == 6718;
    }

    /**
     * Gets set effect name if applicable.
     */
    public String getActiveSetEffect()
    {
        // Check all possible set effects and return active one
        if (isWearingObsidianSet())
        {
            return "Obsidian Set (+10% Acc/Dmg)";
        }

        // Would check Void here but depends on combat type

        return null;
    }
}

