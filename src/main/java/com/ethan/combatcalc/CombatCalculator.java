package com.ethan.combatcalc;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Core combat accuracy and max-hit calculation engine.
 *
 * All formulas implement the standard OSRS mechanics documented on the OSRS wiki:
 *   https://oldschool.runescape.wiki/w/Combat
 *
 * ### How OSRS accuracy works (all three styles)
 *
 *   1. Compute an OFFENSIVE ROLL:  (effectiveLevel + 64) * (equipmentBonus + 64)
 *      The +64 offset is baked into the OSRS engine to prevent the roll from
 *      reaching zero when a stat is 0.
 *
 *   2. Compute a DEFENSIVE ROLL for the NPC:  (npcDefenceLevel + 64) * (defenceBonus + 64)
 *      For magic, the NPC’s magic level is used instead of its defence level.
 *
 *   3. Apply any special multipliers (prayers, Salve, Slayer helm, etc.) via CombatModifier.
 *
 *   4. Feed both rolls into calculateHitChance() to get a value in [0.0, 1.0].
 *
 * ### Max hit
 *   Melee and ranged max hits are calculated here.
 *   Magic max hit is spell-dependent and not yet implemented — it requires knowing
 *   which spell or powered-staff the player is using.
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
     * Calculates melee hit chance and max hit.
     *
     * Offensive roll = (attackLevel + 64) * (attackBonus + 64)
     *   where attackBonus is the equipment bonus for the active sub-type
     *   (stab / slash / crush), chosen by the AttackStyleResolver.
     *
     * Defensive roll = (npcDefenceLevel + 64) * (npcDefenceBonusForSubType + 64)
     *   The NPC defence bonus used is the one matching the player’s attack sub-type,
     *   e.g. stab attack reads the NPC’s stab defence stat.
     *
     * The offensive roll is multiplied by any active modifiers (prayers, Salve, etc.)
     * before being compared to the defensive roll.
     *
     * Max hit uses the melee strength formula (see calculateMeleeMaxHit).
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
     * Calculates ranged hit chance and max hit.
     *
     * Offensive roll = (rangedLevel + 64) * (rangedAttackBonus + 64)
     * Defensive roll = (npcDefenceLevel + 64) * (npcRangedDefence + 64)
     *
     * Max hit uses the ranged strength formula (see calculateRangedMaxHit).
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
     * Calculates magic hit chance (PvM only).
     *
     * Offensive roll = (magicLevel + 64) * (magicAttackBonus + 64)
     *
     * PvM defensive roll uses the NPC’s MAGIC LEVEL (not defence level) as the
     * base stat, combined with the NPC’s magic defence bonus:
     *   Defensive roll = (npcMagicLevel + 64) * (npcMagicDefence + 64)
     *
     * Magic max hit is spell-dependent and is not set here — it requires knowing
     * the active spell or powered staff, which varies too much to generalise.
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
     * The standard OSRS two-branch hit-chance formula.
     *
     * When attack > defence (strong attacker):
     *   hitChance = 1 - (defenceRoll + 2) / (2 * (attackRoll + 1))
     *
     * When attack <= defence (weak attacker or equal):
     *   hitChance = attackRoll / (2 * (defenceRoll + 1))
     *
     * The result is clamped to [0.0, 1.0] as a safety measure, though the
     * formulas naturally stay within range for non-negative inputs.
     *
     * Source: https://oldschool.runescape.wiki/w/Accuracy
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
     * Melee max hit formula.
     *
     * Simplified form of the OSRS wiki formula:
     *   maxHit = floor( (strengthLevel + 1) * ((strengthBonus / 8) + 1) )
     *
     * strengthLevel  — the player’s effective strength level (boosted).
     * strengthBonus  — the total melee strength bonus summed from all equipped gear.
     */
    public int calculateMeleeMaxHit(int strengthLevel, int strengthBonus)
    {
        double base = strengthLevel + 1;
        double bonus = (strengthBonus / 8.0) + 1;
        return (int) Math.floor(base * bonus);
    }

    /**
     * Ranged max hit formula.
     *
     *   maxHit = floor( 1.3 + rangedLevel * (rangedStrengthBonus + 64) / 64 )
     *
     * rangedLevel          — effective ranged level (boosted).
     * rangedStrengthBonus  — the ranged strength bonus from equipped ammunition/weapon.
     */
    public int calculateRangedMaxHit(int rangedLevel, int rangedStrengthBonus)
    {
        return (int) Math.floor(1.3 + (rangedLevel * (rangedStrengthBonus + 64) / 64.0));
    }
}
