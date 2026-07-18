package com.ethan.combatcalc;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Singleton
public class WeaknessAnalyzer
{
    private static final List<AttackSubType> ALL_DEFENCE_TYPES = Arrays.asList(
            AttackSubType.STAB,
            AttackSubType.SLASH,
            AttackSubType.CRUSH,
            AttackSubType.RANGED,
            AttackSubType.MAGIC);
    private static final List<AttackSubType> MELEE_WEAPON_TYPES = Arrays.asList(
            AttackSubType.STAB,
            AttackSubType.SLASH,
            AttackSubType.CRUSH);

    @Inject
    public WeaknessAnalyzer()
    {
    }

    public WeaknessSummary analyze(NpcCombatProfile npcProfile, List<WeaponInfo> candidateWeapons)
    {
        if (npcProfile == null)
        {
            return null;
        }

        List<WeaponInfo> candidates = candidateWeapons == null ? Collections.emptyList() : candidateWeapons;
        AttackSubType derivedDefensiveWeakness = weakestDefence(npcProfile, ALL_DEFENCE_TYPES);
        AttackSubType wikiWeakness = mapWikiWeakness(npcProfile.getWikiWeakness());
        AttackSubType defensiveWeakness = wikiWeakness != AttackSubType.UNKNOWN ? wikiWeakness : derivedDefensiveWeakness;
        AttackSubType weaponWeakness = weakestDefence(npcProfile, MELEE_WEAPON_TYPES);
        WeaponChoice recommendation = recommendWeapon(candidates, defensiveWeakness);

        if (recommendation == null && defensiveWeakness != weaponWeakness)
        {
            recommendation = recommendWeapon(candidates, weaponWeakness);
        }

        return new WeaknessSummary(
                defensiveWeakness,
                npcProfile.getDefenceBonusForAttackType(defensiveWeakness),
                weaponWeakness,
                npcProfile.getDefenceBonusForAttackType(weaponWeakness),
                recommendation != null ? recommendation.weapon.getWeaponName() : null,
                recommendation != null ? recommendation.style : AttackSubType.UNKNOWN,
                wikiWeakness != AttackSubType.UNKNOWN ? normalizeWeaknessLabel(npcProfile.getWikiWeakness()) : formatStyle(defensiveWeakness),
                wikiWeakness != AttackSubType.UNKNOWN ? "wiki" : "derived");
    }

    private AttackSubType weakestDefence(NpcCombatProfile npcProfile, List<AttackSubType> types)
    {
        AttackSubType weakest = AttackSubType.UNKNOWN;
        int lowest = Integer.MAX_VALUE;

        for (AttackSubType type : types)
        {
            int defence = npcProfile.getDefenceBonusForAttackType(type);
            if (defence < lowest)
            {
                lowest = defence;
                weakest = type;
            }
        }

        return weakest;
    }

    private AttackSubType mapWikiWeakness(String wikiWeakness)
    {
        if (wikiWeakness == null)
        {
            return AttackSubType.UNKNOWN;
        }

        String normalized = normalizeWeaknessLabel(wikiWeakness);
        switch (normalized)
        {
            case "stab":
                return AttackSubType.STAB;
            case "slash":
                return AttackSubType.SLASH;
            case "crush":
                return AttackSubType.CRUSH;
            case "range":
            case "ranged":
                return AttackSubType.RANGED;
            case "magic":
            case "air":
            case "earth":
            case "fire":
            case "water":
                return AttackSubType.MAGIC;
            default:
                return AttackSubType.UNKNOWN;
        }
    }

    private String normalizeWeaknessLabel(String weakness)
    {
        return weakness == null ? "" : weakness.trim().toLowerCase(Locale.ROOT);
    }

    private String formatStyle(AttackSubType attackSubType)
    {
        return attackSubType == null ? "Unknown" : attackSubType.getDisplayName();
    }

    private WeaponChoice recommendWeapon(List<WeaponInfo> candidates, AttackSubType desiredStyle)
    {
        WeaponChoice best = null;

        for (WeaponInfo weapon : candidates)
        {
            if (weapon == null || !weapon.hasWeapon())
            {
                continue;
            }

            int styleScore = scoreForStyle(weapon, desiredStyle);
            if (styleScore <= 0)
            {
                continue;
            }

            WeaponChoice choice = new WeaponChoice(weapon, desiredStyle, styleScore);
            if (best == null || choice.score > best.score)
            {
                best = choice;
            }
        }

        return best;
    }

    private int scoreForStyle(WeaponInfo weapon, AttackSubType style)
    {
        switch (style)
        {
            case STAB:
                return weapon.getStabAttackBonus() + weapon.getStrengthBonus() / 10;
            case SLASH:
                return weapon.getSlashAttackBonus() + weapon.getStrengthBonus() / 10;
            case CRUSH:
                return weapon.getCrushAttackBonus() + weapon.getStrengthBonus() / 10;
            case RANGED:
                return weapon.getRangedAttackBonus() + weapon.getRangedStrengthBonus() / 10;
            case MAGIC:
                return weapon.getMagicAttackBonus() + Math.round(weapon.getMagicDamageBonus() / 2.0f);
            default:
                return 0;
        }
    }

    private static class WeaponChoice
    {
        private final WeaponInfo weapon;
        private final AttackSubType style;
        private final int score;

        private WeaponChoice(WeaponInfo weapon, AttackSubType style, int score)
        {
            this.weapon = weapon;
            this.style = style;
            this.score = score;
        }
    }
}
