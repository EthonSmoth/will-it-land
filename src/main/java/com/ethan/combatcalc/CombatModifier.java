package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Computes special accuracy multipliers that sit on top of the base offensive roll.
 *
 * ### Multipliers applied (stacked multiplicatively)
 *
 *   Salve amulet     — +15% accuracy vs undead NPCs (name-based detection).
 *   Slayer helmet    — +15% accuracy when on a Slayer task (task detection is a TODO;
 *                       currently returns false so no bonus is applied).
 *   Occult amulet    — +10% magic accuracy for listed magic-boosting amulets.
 *   Prayer           — varies by prayer and combat type (see getPrayerAccuracyMultiplier).
 *
 * All equipment checks read the live ItemContainer for the EQUIPMENT inventory —
 * no state is cached between ticks.
 */
@Singleton
public class CombatModifier
{
    private final Client client;
    private final WillItLandConfig config;
    private final EquipmentSynergyDetector equipmentSynergyDetector;

    @Inject
    public CombatModifier(Client client, WillItLandConfig config, EquipmentSynergyDetector equipmentSynergyDetector)
    {
        this.client = client;
        this.config = config;
        this.equipmentSynergyDetector = equipmentSynergyDetector;
    }

    public CombatModifier(Client client)
    {
        this(client, null, null);
    }

    /**
     * Returns the combined accuracy multiplier for all active bonuses.
     *
     * Multipliers are applied in sequence (not averaged) to match how OSRS
     * stacks bonuses in the base engine.
     *
     * @param combatType  The current combat style (used to pick the right prayer tier).
     * @param npcProfile  The target NPC (used for Salve undead check and future Slayer task check).
     * @return            A multiplier ≥ 1.0 to scale the offensive roll by.
     */
    public double getOffensiveRollMultiplier(CombatType combatType, NpcCombatProfile npcProfile)
    {
        double multiplier = 1.0;

        if (specialModifiersEnabled() && isSalveAccuracyApplicable(combatType, npcProfile))
        {
            multiplier *= getSalveMultiplier(combatType);
        }

        // Slayer helmet bonus (15% to melee accuracy on task)
        if (specialModifiersEnabled() && (wearingSlayerHelm() || wearingImbueSlayerHelm()) && isOnSlayerTask(npcProfile))
        {
            multiplier *= 1.15;
        }

        // Magic amulet bonuses (+10% magic accuracy)
        if (specialModifiersEnabled() && combatType == CombatType.MAGIC && wearingMagicAmulet())
        {
            multiplier *= 1.10;
        }

        if (equipmentSetsEnabled() && equipmentSynergyDetector != null)
        {
            multiplier *= equipmentSynergyDetector.getSetEffectAccuracyMultiplier(combatType);
        }

        return multiplier;
    }

    public double getDamageMultiplier(CombatType combatType, NpcCombatProfile npcProfile)
    {
        double multiplier = 1.0;

        if (specialModifiersEnabled() && isSalveAccuracyApplicable(combatType, npcProfile))
        {
            multiplier *= getSalveMultiplier(combatType);
        }

        if (equipmentSetsEnabled() && equipmentSynergyDetector != null)
        {
            multiplier *= equipmentSynergyDetector.getSetEffectDamageMultiplier(combatType);
        }

        return multiplier;
    }

    public double getAccuracyPrayerMultiplier(CombatType combatType)
    {
        if (!prayerBonusesEnabled())
        {
            return 1.0;
        }

        return getPrayerAccuracyMultiplier(combatType);
    }

    public double getStrengthPrayerMultiplier()
    {
        if (client == null || !prayerBonusesEnabled())
        {
            return 1.0;
        }

        if (client.isPrayerActive(Prayer.PIETY))
        {
            return 1.23;
        }
        if (client.isPrayerActive(Prayer.CHIVALRY))
        {
            return 1.18;
        }
        if (client.isPrayerActive(Prayer.ULTIMATE_STRENGTH))
        {
            return 1.15;
        }
        if (client.isPrayerActive(Prayer.SUPERHUMAN_STRENGTH))
        {
            return 1.10;
        }
        if (client.isPrayerActive(Prayer.BURST_OF_STRENGTH))
        {
            return 1.05;
        }

        return 1.0;
    }

    public double getRangedStrengthPrayerMultiplier()
    {
        if (client == null || !prayerBonusesEnabled())
        {
            return 1.0;
        }

        if (client.isPrayerActive(Prayer.RIGOUR))
        {
            return 1.23;
        }
        if (client.isPrayerActive(Prayer.EAGLE_EYE))
        {
            return 1.15;
        }
        if (client.isPrayerActive(Prayer.HAWK_EYE))
        {
            return 1.10;
        }
        if (client.isPrayerActive(Prayer.SHARP_EYE))
        {
            return 1.05;
        }

        return 1.0;
    }

    private boolean prayerBonusesEnabled()
    {
        return config == null || config.enablePrayerBonuses();
    }

    private boolean specialModifiersEnabled()
    {
        return config == null || config.enableSpecialModifiers();
    }

    private boolean equipmentSetsEnabled()
    {
        return config == null || config.enableEquipmentSets();
    }

    /**
     * Returns the prayer accuracy multiplier for the active prayer in the given combat style.
     *
     * Only the highest applicable prayer is returned (the player can only have one
     * accuracy prayer active at a time in OSRS).
     *
     * Melee:  Clarity of Thought +5%, Improved Reflexes +10%,
     *         Incredible Reflexes / Chivalry +15%, Piety +20%.
     * Ranged: Sharp Eye +5%, Hawk Eye +10%, Eagle Eye +15%, Rigour +20%.
     * Magic:  Mystic Will +5%, Mystic Lore +10%, Mystic Might +15%, Augury +25%.
     */
    private double getPrayerAccuracyMultiplier(CombatType combatType)
    {
        if (client == null)
        {
            return 1.0;
        }

        switch (combatType)
        {
            case MELEE:
                if (client.isPrayerActive(Prayer.PIETY))
                {
                    return 1.20; // 20% boost
                }
                if (client.isPrayerActive(Prayer.CHIVALRY))
                {
                    return 1.15; // 15% boost
                }
                if (client.isPrayerActive(Prayer.INCREDIBLE_REFLEXES))
                {
                    return 1.15; // 15% boost
                }
                if (client.isPrayerActive(Prayer.IMPROVED_REFLEXES))
                {
                    return 1.10; // 10% boost
                }
                if (client.isPrayerActive(Prayer.CLARITY_OF_THOUGHT))
                {
                    return 1.05; // 5% boost
                }
                break;

            case RANGED:
                if (client.isPrayerActive(Prayer.RIGOUR))
                {
                    return 1.20; // 20% boost
                }
                if (client.isPrayerActive(Prayer.EAGLE_EYE))
                {
                    return 1.15; // 15% boost
                }
                if (client.isPrayerActive(Prayer.HAWK_EYE))
                {
                    return 1.10; // 10% boost
                }
                if (client.isPrayerActive(Prayer.SHARP_EYE))
                {
                    return 1.05; // 5% boost
                }
                break;

            case MAGIC:
                if (client.isPrayerActive(Prayer.AUGURY))
                {
                    return 1.25; // 25% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_MIGHT))
                {
                    return 1.15; // 15% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_LORE))
                {
                    return 1.10; // 10% boost
                }
                if (client.isPrayerActive(Prayer.MYSTIC_WILL))
                {
                    return 1.05; // 5% boost
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
        int amuletId = getEquippedAmuletId();
        return amuletId == ItemID.SALVE_AMULET ||
                amuletId == ItemID.SALVE_AMULET_E ||
                amuletId == ItemID.SALVE_AMULETI ||
                amuletId == ItemID.SALVE_AMULETEI ||
                amuletId == ItemID.SALVE_AMULETI_25250 ||
                amuletId == ItemID.SALVE_AMULETEI_25278 ||
                amuletId == ItemID.SALVE_AMULETI_26763 ||
                amuletId == ItemID.SALVE_AMULETEI_26782;
    }

    private boolean isSalveAccuracyApplicable(CombatType combatType, NpcCombatProfile npcProfile)
    {
        return wearingSalveAmulet() && isUndeadNPC(npcProfile) && getSalveMultiplier(combatType) > 1.0;
    }

    private double getSalveMultiplier(CombatType combatType)
    {
        int amuletId = getEquippedAmuletId();
        if (amuletId == -1)
        {
            return 1.0;
        }

        boolean enchanted = amuletId == ItemID.SALVE_AMULET_E ||
                amuletId == ItemID.SALVE_AMULETEI ||
                amuletId == ItemID.SALVE_AMULETEI_25278 ||
                amuletId == ItemID.SALVE_AMULETEI_26782;
        boolean imbued = amuletId == ItemID.SALVE_AMULETI ||
                amuletId == ItemID.SALVE_AMULETEI ||
                amuletId == ItemID.SALVE_AMULETI_25250 ||
                amuletId == ItemID.SALVE_AMULETEI_25278 ||
                amuletId == ItemID.SALVE_AMULETI_26763 ||
                amuletId == ItemID.SALVE_AMULETEI_26782;

        if (combatType == CombatType.MELEE)
        {
            return enchanted ? 1.20 : 1.15;
        }

        if (!imbued)
        {
            return 1.0;
        }

        if (combatType == CombatType.MAGIC)
        {
            return enchanted ? 1.20 : 1.15;
        }

        if (combatType == CombatType.RANGED)
        {
            return enchanted ? 1.20 : (7.0 / 6.0);
        }

        return 1.0;
    }

    private int getEquippedAmuletId()
    {
        ItemContainer equipment = client != null ? client.getItemContainer(InventoryID.EQUIPMENT) : null;
        if (equipment == null)
        {
            return -1;
        }

        Item amulet = equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
        return amulet == null ? -1 : amulet.getId();
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
     * Undead detection for the Salve amulet bonus.
     *
     * Uses a simple name-substring match against known undead keywords.
     * This avoids needing an NPC ID list and covers variants (e.g. "Barrows brother").
     * The check is case-insensitive.
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
     * Slayer task detection — not yet implemented.
     *
     * Integrating with the RuneLite Slayer plugin would require a plugin dependency
     * or reading the player’s Slayer task VarBit, which differs by task source.
     * Until implemented, the Slayer helmet bonus is never applied.
     */
    private boolean isOnSlayerTask(NpcCombatProfile npcProfile)
    {
        // TODO: Integrate with Slayer task detection
        // For now, return false as this requires external plugin integration
        return false;
    }
}

