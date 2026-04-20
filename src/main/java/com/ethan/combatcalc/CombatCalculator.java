package com.ethan.combatcalc;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Core combat accuracy and max hit calculation engine.
 * Implements OSRS standard formulas for melee, ranged, and magic.
 */
@Singleton
public class CombatCalculator
{
    private static final double ACCURACY_THRESHOLD = 2.0;
    private final CombatModifier combatModifier;

    @Inject
    public CombatCalculator(CombatModifier combatModifier)
    {
        this.combatModifier = combatModifier;
    }

    /**
     * Calculates melee accuracy.
     * Attack roll vs Defence roll formula.
     */
    public CombatResult calculateMeleeAccuracy(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        CombatResult result = new CombatResult();
        result.setCombatType(CombatType.MELEE);
        result.setAttackSubType(playerProfile.getAttackSubType());

        // Offensive roll = (Attack level + 64) * (Attack bonus + 64)
        int offensiveRoll = (playerProfile.getEffectiveAttackLevel() + 64) * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.MELEE, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        // Defensive roll = (Defence level + 64) * (Defence bonus + 64)
        int defenceBonus = npcProfile.getDefenceBonusForAttackType(playerProfile.getAttackSubType());
        int defensiveRoll = (npcProfile.getDefenceLevel() + 64) * (defenceBonus + 64);
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        // Calculate max hit
        int maxHit = calculateMeleeMaxHit(playerProfile.getEffectiveStrengthLevel(), playerProfile.getStrengthBonus());
        result.setMaxHit(maxHit);

        return result;
    }

    /**
     * Calculates ranged accuracy.
     * Attack roll vs Defence roll formula.
     */
    public CombatResult calculateRangedAccuracy(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        CombatResult result = new CombatResult();
        result.setCombatType(CombatType.RANGED);
        result.setAttackSubType(AttackSubType.RANGED);

        // Offensive roll = (Ranged level + 64) * (Ranged attack + 64)
        int offensiveRoll = (playerProfile.getEffectiveAttackLevel() + 64) * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.RANGED, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        // Defensive roll = (Defence level + 64) * (Ranged defence + 64)
        int defensiveRoll = (npcProfile.getDefenceLevel() + 64) * (npcProfile.getRangedDefence() + 64);
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        // Calculate max hit
        int maxHit = calculateRangedMaxHit(playerProfile.getEffectiveAttackLevel(), playerProfile.getRangedStrengthBonus());
        result.setMaxHit(maxHit);

        return result;
    }

    /**
     * Calculates magic accuracy (PvM formula).
     * For PvM: defensive roll uses Magic level + Magic defence bonus.
     */
    public CombatResult calculateMagicAccuracy(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        CombatResult result = new CombatResult();
        result.setCombatType(CombatType.MAGIC);
        result.setAttackSubType(AttackSubType.MAGIC);

        // Offensive roll = (Magic level + 64) * (Magic attack + 64)
        int offensiveRoll = (playerProfile.getEffectiveAttackLevel() + 64) * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.MAGIC, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        // Defensive roll (PvM) = (NPC Magic level + 64) * (Magic defence + 64)
        int defensiveRoll = (npcProfile.getMagicLevel() + 64) * (npcProfile.getMagicDefence() + 64);
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        return result;
    }

    /**
     * Standard OSRS hit chance formula.
     * Split based on whether offensive roll exceeds defensive roll.
     * Results are clamped to [0.0, 1.0] range.
     */
    private double calculateHitChance(int offensiveRoll, int defensiveRoll)
    {
        double hitChance;

        if (offensiveRoll > defensiveRoll)
        {
            // If attack roll > defence roll:  1 - (defence roll + 2) / (2 * (attack roll + 1))
            hitChance = 1.0 - (double) (defensiveRoll + 2) / (2.0 * (offensiveRoll + 1));
        }
        else
        {
            // If attack roll <= defence roll:  attack roll / (2 * (defence roll + 1))
            hitChance = (double) offensiveRoll / (2.0 * (defensiveRoll + 1));
        }

        // Clamp to valid hit chance range [0.0, 1.0]
        return Math.max(0.0, Math.min(1.0, hitChance));
    }

    /**
     * Calculates melee max hit.
     * Formula: floor((Strength level + Strength bonus / 8 + 1) * (Strength bonus / 8 + 1))
     * Simplified for pass 1.
     */
    public int calculateMeleeMaxHit(int strengthLevel, int strengthBonus)
    {
        double base = strengthLevel + 1;
        double bonus = (strengthBonus / 8.0) + 1;
        return (int) Math.floor(base * bonus);
    }

    /**
     * Calculates ranged max hit.
     * Formula: floor(1.3 + (Ranged level * (Ranged strength + 64) / 64))
     */
    public int calculateRangedMaxHit(int rangedLevel, int rangedStrengthBonus)
    {
        return (int) Math.floor(1.3 + (rangedLevel * (rangedStrengthBonus + 64) / 64.0));
    }
}
