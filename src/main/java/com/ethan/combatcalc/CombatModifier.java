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
import java.util.Locale;

/**
 * Computes special accuracy multipliers that sit on top of the base offensive roll.
 *
 * ### Multipliers applied (stacked multiplicatively)
 *
 *   Salve amulet     — +15% accuracy vs undead NPCs (name-based detection).
 *   Slayer helmet    — +15% accuracy when on a Slayer task (task detection is a TODO;
 *                       currently returns false so no bonus is applied).
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
    private final SlayerTaskProvider slayerTaskProvider;

    @Inject
    public CombatModifier(Client client,
                          WillItLandConfig config,
                          EquipmentSynergyDetector equipmentSynergyDetector,
                          SlayerTaskProvider slayerTaskProvider)
    {
        this.client = client;
        this.config = config;
        this.equipmentSynergyDetector = equipmentSynergyDetector;
        this.slayerTaskProvider = slayerTaskProvider;
    }

    public CombatModifier(Client client, WillItLandConfig config, EquipmentSynergyDetector equipmentSynergyDetector)
    {
        this(client, config, equipmentSynergyDetector, new RuneLiteSlayerTaskProvider(client));
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

        if (specialModifiersEnabled())
        {
            multiplier *= getBestTargetSpecificMultiplier(combatType, npcProfile);
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

        if (specialModifiersEnabled())
        {
            multiplier *= getBestTargetSpecificMultiplier(combatType, npcProfile);
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

    private double getBestTargetSpecificMultiplier(CombatType combatType, NpcCombatProfile npcProfile)
    {
        double salve = isSalveAccuracyApplicable(combatType, npcProfile) ? getSalveMultiplier(combatType) : 1.0;
        double slayer = slayerHelmetApplies(combatType, npcProfile) ? 1.15 : 1.0;
        return Math.max(salve, slayer);
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
        if (client == null)
        {
            return false;
        }

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
        return headId == ItemID.SLAYER_HELMET ||
                headId == ItemID.BLACK_SLAYER_HELMET ||
                headId == ItemID.GREEN_SLAYER_HELMET ||
                headId == ItemID.RED_SLAYER_HELMET ||
                headId == ItemID.PURPLE_SLAYER_HELMET ||
                headId == ItemID.TURQUOISE_SLAYER_HELMET ||
                headId == ItemID.HYDRA_SLAYER_HELMET ||
                headId == ItemID.TWISTED_SLAYER_HELMET ||
                headId == ItemID.TZTOK_SLAYER_HELMET ||
                headId == ItemID.TZKAL_SLAYER_HELMET ||
                headId == ItemID.VAMPYRIC_SLAYER_HELMET ||
                headId == ItemID.ARAXYTE_SLAYER_HELMET ||
                headId == ItemID.HOODED_SLAYER_HELMET ||
                wearingImbueSlayerHelm();
    }

    /**
     * Checks if the player is wearing an Imbued Slayer helmet.
     */
    private boolean wearingImbueSlayerHelm()
    {
        if (client == null)
        {
            return false;
        }

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
        return headId == ItemID.SLAYER_HELMET_I ||
                headId == ItemID.BLACK_SLAYER_HELMET_I ||
                headId == ItemID.GREEN_SLAYER_HELMET_I ||
                headId == ItemID.RED_SLAYER_HELMET_I ||
                headId == ItemID.PURPLE_SLAYER_HELMET_I ||
                headId == ItemID.TURQUOISE_SLAYER_HELMET_I ||
                headId == ItemID.HYDRA_SLAYER_HELMET_I ||
                headId == ItemID.TWISTED_SLAYER_HELMET_I ||
                headId == ItemID.TZTOK_SLAYER_HELMET_I ||
                headId == ItemID.TZKAL_SLAYER_HELMET_I ||
                headId == ItemID.VAMPYRIC_SLAYER_HELMET_I ||
                headId == ItemID.ARAXYTE_SLAYER_HELMET_I ||
                headId == ItemID.HOODED_SLAYER_HELMET_I;
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
     * Slayer task detection uses RuneLite's slayer varps/DB rows via SlayerTaskProvider.
     */
    private boolean isOnSlayerTask(NpcCombatProfile npcProfile)
    {
        if (npcProfile == null || npcProfile.getNpcName() == null || slayerTaskProvider == null)
        {
            return false;
        }

        return taskMatchesNpc(slayerTaskProvider.getTaskName(), npcProfile.getNpcName());
    }

    private boolean slayerHelmetApplies(CombatType combatType, NpcCombatProfile npcProfile)
    {
        if (!isOnSlayerTask(npcProfile))
        {
            return false;
        }

        if (combatType == CombatType.MELEE)
        {
            return wearingSlayerHelm();
        }

        return wearingImbueSlayerHelm() && (combatType == CombatType.RANGED || combatType == CombatType.MAGIC);
    }

    private boolean taskMatchesNpc(String taskName, String npcName)
    {
        String task = normalizeTaskName(taskName);
        String npc = normalizeName(npcName);
        if (task.isEmpty() || npc.isEmpty())
        {
            return false;
        }

        return task.contains(npc) || npc.contains(task);
    }

    private String normalizeTaskName(String taskName)
    {
        String task = normalizeName(taskName);
        if (task.endsWith("ies"))
        {
            return task.substring(0, task.length() - 3) + "y";
        }
        if (task.endsWith("s"))
        {
            return task.substring(0, task.length() - 1);
        }
        return task;
    }

    private String normalizeName(String name)
    {
        if (name == null)
        {
            return "";
        }

        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

