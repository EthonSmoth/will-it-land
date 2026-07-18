package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.VarPlayer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;

/**
 * Determines what combat style the player is currently using.
 *
 * ### CombatType (MELEE / RANGED / MAGIC)
 *   Resolved by inspecting the equipped weapon and VarPlayer values:
 *     - If a selected spell is active (varp 108 > 0) → MAGIC.
 *     - If the equipped weapon is in the hardcoded ranged-weapon list → RANGED.
 *     - Otherwise → MELEE.
 *
     *   Powered staves (Trident, Sanguinesti, Tumeken's shadow, etc.) are treated
     *   as MAGIC even when no standard spell is selected.
 *
 * ### AttackSubType (STAB / SLASH / CRUSH / RANGED / MAGIC)
 *   For MELEE, the active attack style VarPlayer is mapped to a sub-type.
 *   Staves always use CRUSH regardless of the style selector.
 *   Unarmed combat (no weapon) defaults to CRUSH.
 *   For RANGED and MAGIC the sub-type is fixed to RANGED or MAGIC respectively.
 */
@Singleton
public class AttackStyleResolver
{
    private final ItemManager itemManager;

    @Inject
    public AttackStyleResolver(ItemManager itemManager)
    {
        this.itemManager = itemManager;
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
     * Determines the high-level combat type (MELEE / RANGED / MAGIC).
     *
     * Detection order:
     *   1. If VarPlayer 108 (selected spell) is non-zero → MAGIC.
     *      This covers all standard and ancient spellbook spells.
     *   2. If the equipped weapon is in the ranged-weapon list → RANGED.
     *      Checked regardless of attack style, because some ranged weapons
     *      have a style-3 slot that could be confused with melee controlled stance.
     *   3. Default → MELEE.
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

        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weapon != null && weapon.getId() != -1 && PoweredStaffMaxHitResolver.isPoweredStaff(weapon.getId()))
        {
            return CombatType.MAGIC;
        }

        AttackSubType widgetSubType = resolveSubTypeFromCombatOptions(client);
        CombatType widgetCombatType = WeaponAttackStyles.combatTypeFor(widgetSubType);
        if (widgetCombatType != CombatType.UNKNOWN)
        {
            return widgetCombatType;
        }

        // Check attack style - ranged style is 3
        int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
        if (attackStyle == 3)
        {
            // This could be ranged defensive stance or controlled
            if (weapon != null && isRangedWeapon(weapon))
            {
                return CombatType.RANGED;
            }
        }

        // Check if weapon is ranged (bow, blowpipe, etc)
        if (weapon != null && weapon.getId() != -1 && isRangedWeapon(weapon))
        {
            return CombatType.RANGED;
        }

        return CombatType.MELEE;
    }

    /**
     * Hardcoded list of ranged weapon item IDs.
     *
     * Used by resolveCombatType() to distinguish ranged weapons from melee weapons.
     * Covers bows, crossbows, the toxic blowpipe, and chinchompas.
     * This list is not exhaustive — newer or uncommon ranged weapons may need to be added.
     */
    private boolean isRangedWeapon(Item weapon)
    {
        String weaponName = getItemName(weapon);
        return weaponName.contains(" bow") ||
                weaponName.endsWith("bow") ||
                weaponName.contains("crossbow") ||
                weaponName.contains("blowpipe") ||
                weaponName.contains("chinchompa") ||
                weaponName.contains(" knife") ||
                weaponName.endsWith("knife") ||
                weaponName.contains(" dart") ||
                weaponName.endsWith("dart") ||
                weaponName.contains("javelin") ||
                weaponName.contains("thrownaxe") ||
                weaponName.contains("thrown axe") ||
                weaponName.contains("ballista");
    }

    /**
     * Hardcoded list of staff/wand item IDs.
     *
     * Staves use the CRUSH attack sub-type in the melee accuracy formula
     * (the weapon uses the crush attack bonus column in the equipment table).
     * This list covers common staves, battlestaves, and magic wands/staffs.
     * It is also used to prevent staves from being misidentified as ranged weapons.
     */
    private boolean isStaff(Item weapon)
    {
        String weaponName = getItemName(weapon);
        return weaponName.contains("staff") ||
                weaponName.contains("battlestaff") ||
                weaponName.contains("trident") ||
                weaponName.contains("wand") ||
                weaponName.contains("sceptre") ||
                weaponName.contains("scepter") ||
                weaponName.contains("crozier");
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

    /**
     * Maps the active VarPlayer attack-style index to an AttackSubType for melee.
     *
     * VarPlayer.ATTACK_STYLE values for a typical weapon:
     *   0 → Stab (Accurate)
     *   1 → Slash (Aggressive)
     *   2 → Crush (Defensive)
     *   3 → Stab  (Controlled — maps to stab as the closest equivalent)
     *
     * The exact mapping varies by weapon type; this covers the common sword/dagger layout.
     * Staves always return CRUSH before this switch is reached.
     */
    private AttackSubType resolveMeleeSubType(Client client)
    {
        AttackSubType widgetSubType = resolveSubTypeFromCombatOptions(client);
        if (widgetSubType != AttackSubType.UNKNOWN)
        {
            return widgetSubType;
        }

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        Item weapon = equipment != null ? equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) : null;

        // Unarmed combat always uses crush
        if (weapon == null || weapon.getId() == -1)
        {
            return AttackSubType.CRUSH;
        }

        // Staves always use crush attack style
        if (isStaff(weapon))
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

    private AttackSubType resolveSubTypeFromCombatOptions(Client client)
    {
        Widget categoryWidget = client.getWidget(InterfaceID.CombatInterface.CATEGORY);
        if (categoryWidget == null)
        {
            return AttackSubType.UNKNOWN;
        }

        return WeaponAttackStyles.resolveAttackSubType(categoryWidget.getText(), client.getVarpValue(VarPlayer.ATTACK_STYLE));
    }

    private String getItemName(Item weapon)
    {
        if (weapon == null || weapon.getId() == -1 || itemManager == null)
        {
            return "";
        }

        String name = itemManager.getItemComposition(weapon.getId()).getName();
        if (name == null)
        {
            return "";
        }

        return name.toLowerCase(Locale.ROOT);
    }
}
