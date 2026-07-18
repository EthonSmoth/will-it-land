package com.ethan.combatcalc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class WeaponAttackStyles
{
    private static final Map<String, AttackSubType[]> ATTACK_TYPES;

    static
    {
        Map<String, AttackSubType[]> attackTypes = new HashMap<>();

        put(attackTypes, "2h Sword", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.SLASH);
        put(attackTypes, "Axe", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.SLASH);
        put(attackTypes, "Banner", AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.STAB);
        put(attackTypes, "Bladed Staff", AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.MAGIC, AttackSubType.MAGIC);
        put(attackTypes, "Blaster", AttackSubType.UNKNOWN, AttackSubType.UNKNOWN);
        put(attackTypes, "Bludgeon", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.CRUSH);
        put(attackTypes, "Blunt", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.CRUSH);
        put(attackTypes, "Bow", AttackSubType.RANGED, AttackSubType.RANGED, AttackSubType.RANGED);
        put(attackTypes, "Bulwark", AttackSubType.CRUSH, AttackSubType.UNKNOWN);
        put(attackTypes, "Chinchompas", AttackSubType.RANGED, AttackSubType.RANGED, AttackSubType.RANGED);
        put(attackTypes, "Chinchompa", AttackSubType.RANGED, AttackSubType.RANGED, AttackSubType.RANGED);
        put(attackTypes, "Claw", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.STAB, AttackSubType.SLASH);
        put(attackTypes, "Crossbow", AttackSubType.RANGED, AttackSubType.RANGED, AttackSubType.RANGED);
        put(attackTypes, "Gun", AttackSubType.UNKNOWN, AttackSubType.CRUSH);
        put(attackTypes, "Multi-Style", AttackSubType.STAB, AttackSubType.RANGED, AttackSubType.MAGIC);
        put(attackTypes, "Multi-Melee", AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.SLASH);
        put(attackTypes, "Partisan", AttackSubType.STAB, AttackSubType.STAB, AttackSubType.CRUSH, AttackSubType.STAB);
        put(attackTypes, "Pickaxe", AttackSubType.STAB, AttackSubType.STAB, AttackSubType.CRUSH, AttackSubType.STAB);
        put(attackTypes, "Polearm", AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.STAB);
        put(attackTypes, "Polestaff", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.CRUSH);
        put(attackTypes, "Powered Staff", AttackSubType.MAGIC, AttackSubType.MAGIC, AttackSubType.MAGIC);
        put(attackTypes, "Powered Wand", AttackSubType.MAGIC, AttackSubType.MAGIC, AttackSubType.MAGIC);
        put(attackTypes, "Salamander", AttackSubType.SLASH, AttackSubType.RANGED, AttackSubType.MAGIC);
        put(attackTypes, "Scythe", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.SLASH);
        put(attackTypes, "Slash Sword", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.STAB, AttackSubType.SLASH);
        put(attackTypes, "Spear", AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.CRUSH, AttackSubType.STAB);
        put(attackTypes, "Spiked", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.STAB, AttackSubType.CRUSH);
        put(attackTypes, "Stab Sword", AttackSubType.STAB, AttackSubType.STAB, AttackSubType.SLASH, AttackSubType.STAB);
        put(attackTypes, "Staff", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.MAGIC, AttackSubType.MAGIC);
        put(attackTypes, "Thrown", AttackSubType.RANGED, AttackSubType.RANGED, AttackSubType.RANGED);
        put(attackTypes, "Unarmed", AttackSubType.CRUSH, AttackSubType.CRUSH, AttackSubType.CRUSH);
        put(attackTypes, "Whip", AttackSubType.SLASH, AttackSubType.SLASH, AttackSubType.SLASH);

        ATTACK_TYPES = Collections.unmodifiableMap(attackTypes);
    }

    private WeaponAttackStyles()
    {
    }

    static boolean hasCategory(String category)
    {
        return ATTACK_TYPES.containsKey(normalizeCategory(category));
    }

    static AttackSubType resolveAttackSubType(String category, int styleIndex)
    {
        AttackSubType[] styles = ATTACK_TYPES.get(normalizeCategory(category));
        if (styles == null || styleIndex < 0 || styleIndex >= styles.length)
        {
            return AttackSubType.UNKNOWN;
        }

        return styles[styleIndex];
    }

    static CombatType combatTypeFor(AttackSubType attackSubType)
    {
        switch (attackSubType)
        {
            case RANGED:
                return CombatType.RANGED;
            case MAGIC:
                return CombatType.MAGIC;
            case STAB:
            case SLASH:
            case CRUSH:
                return CombatType.MELEE;
            default:
                return CombatType.UNKNOWN;
        }
    }

    private static void put(Map<String, AttackSubType[]> attackTypes, String category, AttackSubType... styles)
    {
        attackTypes.put(normalizeCategory(category), styles);
    }

    private static String normalizeCategory(String category)
    {
        if (category == null)
        {
            return "";
        }

        return category
                .replaceAll("<[^>]+>", "")
                .replace('\u00a0', ' ')
                .trim()
                .replaceFirst("(?i)^(weapon|category)\\s*:\\s*", "")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }
}
