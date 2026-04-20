package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles special accuracy modifiers for equipment and buffs.
 * Includes Salve amulet, Slayer helm, Imbued Slayer helm, Prayer bonuses, etc.
 */
@Singleton
public class CombatModifier
{
    private final Client client;

    @Inject
    public CombatModifier(Client client)
    {
        this.client = client;
    }

    /**
     * Applies special accuracy modifiers to the offensive roll.
     * Returns a multiplier to be applied to the offensive roll.
     */
    public double getOffensiveRollMultiplier(CombatType combatType, NpcCombatProfile npcProfile)
    {
        double multiplier = 1.0;

        // Salve amulet bonus (15% to melee accuracy and damage vs undead)
        if (wearingSalveAmulet() && isUndeadNPC(npcProfile))
        {
            multiplier *= 1.15;
        }

        // Slayer helmet bonus (15% to melee accuracy on task)
        if ((wearingSlayerHelm() || wearingImbueSlayerHelm()) && isOnSlayerTask(npcProfile))
        {
            multiplier *= 1.15;
        }

        // Magic amulet bonuses (+10% magic accuracy)
        if (combatType == CombatType.MAGIC && wearingMagicAmulet())
        {
            multiplier *= 1.10;
        }

        // Prayer bonuses
        multiplier *= getPrayerAccuracyMultiplier(combatType);

        return multiplier;
    }

    /**
     * Gets the prayer accuracy multiplier based on active prayers.
     */
    private double getPrayerAccuracyMultiplier(CombatType combatType)
    {
        switch (combatType)
        {
            case MELEE:
                // Precise Shot, Improved Juju (not applicable to melee)
                if (client.isPrayerActive(Prayer.CLARITY_OF_THOUGHT))
                {
                    return 1.05; // 5% boost
                }
                if (client.isPrayerActive(Prayer.IMPROVED_REFLEXES))
                {
                    return 1.1; // 10% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_WILL))
                {
                    return 1.0; // No melee bonus (magic prayer)
                }
                break;

            case RANGED:
                if (client.isPrayerActive(Prayer.SHARP_EYE))
                {
                    return 1.05; // 5% boost
                }
                if (client.isPrayerActive(Prayer.HAWK_EYE))
                {
                    return 1.1; // 10% boost
                }
                if (client.isPrayerActive(Prayer.EAGLE_EYE))
                {
                    return 1.15; // 15% boost
                }
                break;

            case MAGIC:
                if (client.isPrayerActive(Prayer.MYSTIC_WILL))
                {
                    return 1.05; // 5% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_LORE))
                {
                    return 1.1; // 10% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_MIGHT))
                {
                    return 1.15; // 15% boost
                }
                break;
        }

        return 1.0;
    }

    /**
     * Checks if the player is wearing a Salve amulet.
     */
    private boolean wearingSalveAmulet()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item amulet = equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
        if (amulet == null || amulet.getId() == -1)
        {
            return false;
        }

        // Salve amulet IDs
        int amuletId = amulet.getId();
        return amuletId == 4081 || // Salve amulet
                amuletId == 4082 || // Salve amulet (e)
                amuletId == 18783 || // Salve amulet (ei)
                amuletId == 18784; // Salve amulet (imbued)
    }

    /**
     * Checks if the player is wearing a Slayer helmet.
     */
    private boolean wearingSlayerHelm()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item head = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        if (head == null || head.getId() == -1)
        {
            return false;
        }

        int headId = head.getId();
        return headId == 4378 || // Slayer helmet
                headId == 8921 || // Slayer helmet (i)
                headId == 4379; // Black slayer helmet
    }

    /**
     * Checks if the player is wearing an Imbued Slayer helmet.
     */
    private boolean wearingImbueSlayerHelm()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item head = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        if (head == null || head.getId() == -1)
        {
            return false;
        }

        int headId = head.getId();
        return headId == 8921 || // Slayer helmet (i)
                headId == 19639 || // Red slayer helmet (i)
                headId == 19640 || // Blue slayer helmet (i)
                headId == 19641 || // Green slayer helmet (i)
                headId == 19642 || // Black slayer helmet (i)
                headId == 24059; // Shadow slayer helmet (i)
    }

    /**
     * Checks if the player is wearing a magic-boosting amulet.
     * +10% magic accuracy amulets.
     */
    private boolean wearingMagicAmulet()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item amulet = equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
        if (amulet == null || amulet.getId() == -1)
        {
            return false;
        }

        // Magic accuracy boosting amulets
        int amuletId = amulet.getId();
        return amuletId == 4699 || // Occult amulet
                amuletId == 25202 || // Arcane pulse necklace
                amuletId == 25203 || // Arcane pulse necklace (charged)
                amuletId == 22011 || // Amulet of damned
                amuletId == 22012; // Amulet of damned (shadow)
    }

    /**
     * Checks if the NPC is undead (for Salve amulet bonus).
     */
    private boolean isUndeadNPC(NpcCombatProfile npcProfile)
    {
        if (npcProfile == null || npcProfile.getNpcName() == null)
        {
            return false;
        }

        String name = npcProfile.getNpcName().toLowerCase();
        return name.contains("zombie") ||
                name.contains("skeleton") ||
                name.contains("ghost") ||
                name.contains("spectre") ||
                name.contains("vampire") ||
                name.contains("shade") ||
                name.contains("ghoul") ||
                name.contains("mummy") ||
                name.contains("barrows") ||
                name.contains("undead") ||
                name.contains("wight");
    }

    /**
     * Placeholder for checking if player is on a Slayer task.
     * This would require integration with RuneLite's Slayer plugin or similar.
     */
    private boolean isOnSlayerTask(NpcCombatProfile npcProfile)
    {
        // TODO: Integrate with Slayer task detection
        // For now, return false as this requires external plugin integration
        return false;
    }
}

