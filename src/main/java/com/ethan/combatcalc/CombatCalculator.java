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
 *   1. Compute an OFFENSIVE ROLL: effectiveLevel * (equipmentBonus + 64)
 *      where effectiveLevel includes prayer, stance bonus, and the +8 baseline.
 *
 *   2. Compute a DEFENSIVE ROLL for the NPC: (npcDefenceLevel + 9) * (defenceBonus + 64)
 *      For magic, the NPC’s magic level is used instead of its defence level.
 *
 *   3. Apply any special multipliers (prayers, Salve, Slayer helm, etc.) via CombatModifier.
 *
 *   4. Feed both rolls into calculateHitChance() to get a value in [0.0, 1.0].
 *
 * ### Max hit
 *   Melee and ranged max hits are calculated here.
 *   Magic max hit uses a spell or powered-staff base max hit supplied by the caller.
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
     * Offensive roll = effectiveAttackLevel * (attackBonus + 64)
     *   where attackBonus is the equipment bonus for the active sub-type
     *   (stab / slash / crush), chosen by the AttackStyleResolver.
     *
     * Defensive roll = (npcDefenceLevel + 9) * (npcDefenceBonusForSubType + 64)
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

        int effectiveAttackLevel = calculateEffectiveLevel(
                playerProfile.getEffectiveAttackLevel(),
                combatModifier.getAccuracyPrayerMultiplier(CombatType.MELEE),
                playerProfile.getAccuracyStyleBonus());
        int offensiveRoll = effectiveAttackLevel * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.MELEE, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        int defenceBonus = npcProfile.getDefenceBonusForAttackType(playerProfile.getAttackSubType());
        int defensiveRoll = calculateNpcDefensiveRoll(npcProfile.getDefenceLevel(), defenceBonus);
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        // Calculate max hit
        int maxHit = calculateMeleeMaxHit(
                playerProfile.getEffectiveStrengthLevel(),
                playerProfile.getStrengthBonus(),
                playerProfile.getStrengthStyleBonus(),
                combatModifier.getStrengthPrayerMultiplier(),
                combatModifier.getDamageMultiplier(CombatType.MELEE, npcProfile));
        result.setMaxHit(maxHit);

        return result;
    }

    /**
     * Calculates ranged hit chance and max hit.
     *
     * Offensive roll = effectiveRangedLevel * (rangedAttackBonus + 64)
     * Defensive roll = (npcDefenceLevel + 9) * (npcRangedDefence + 64)
     *
     * Max hit uses the ranged strength formula (see calculateRangedMaxHit).
     */
    public CombatResult calculateRangedAccuracy(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        CombatResult result = new CombatResult();
        result.setCombatType(CombatType.RANGED);
        result.setAttackSubType(AttackSubType.RANGED);

        int effectiveRangedLevel = calculateEffectiveLevel(
                playerProfile.getEffectiveAttackLevel(),
                combatModifier.getAccuracyPrayerMultiplier(CombatType.RANGED),
                playerProfile.getAccuracyStyleBonus());
        int offensiveRoll = effectiveRangedLevel * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.RANGED, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        int defensiveRoll = calculateNpcDefensiveRoll(npcProfile.getDefenceLevel(), npcProfile.getRangedDefence());
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        // Calculate max hit
        int maxHit = calculateRangedMaxHit(
            playerProfile.getEffectiveStrengthLevel(),
                playerProfile.getRangedStrengthBonus(),
                playerProfile.getRangedStrengthStyleBonus(),
                combatModifier.getRangedStrengthPrayerMultiplier(),
                combatModifier.getDamageMultiplier(CombatType.RANGED, npcProfile));
        result.setMaxHit(maxHit);

        return result;
    }

    /**
     * Calculates magic hit chance (PvM only).
     *
     * Offensive roll = effectiveMagicLevel * (magicAttackBonus + 64)
     *
     * PvM defensive roll uses the NPC’s MAGIC LEVEL (not defence level) as the
     * base stat, combined with the NPC’s magic defence bonus:
     *   Defensive roll = (npcMagicLevel + 9) * (npcMagicDefence + 64)
     *
     * Magic max hit is spell-dependent and uses the caller-provided base max hit.
     */
    public CombatResult calculateMagicAccuracy(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        CombatResult result = new CombatResult();
        result.setCombatType(CombatType.MAGIC);
        result.setAttackSubType(AttackSubType.MAGIC);

        int effectiveMagicLevel = calculateEffectiveLevel(
                playerProfile.getEffectiveAttackLevel(),
                combatModifier.getAccuracyPrayerMultiplier(CombatType.MAGIC),
                playerProfile.getAccuracyStyleBonus());
        int offensiveRoll = effectiveMagicLevel * (playerProfile.getAttackBonus() + 64);

        // Apply special modifiers
        double modifier = combatModifier.getOffensiveRollMultiplier(CombatType.MAGIC, npcProfile);
        int modifiedOffensiveRoll = (int) (offensiveRoll * modifier);

        result.setOffensiveRoll(modifiedOffensiveRoll);

        int defensiveRoll = calculateNpcDefensiveRoll(npcProfile.getMagicLevel(), npcProfile.getMagicDefence());
        result.setDefensiveRoll(defensiveRoll);

        // Calculate hit chance
        double hitChance = calculateHitChance(modifiedOffensiveRoll, defensiveRoll);
        result.setHitChance(hitChance);

        int maxHit = calculateMagicMaxHit(
                playerProfile.getMaxHitBase(),
                playerProfile.getMagicDamageBonus(),
                combatModifier.getDamageMultiplier(CombatType.MAGIC, npcProfile),
                getElementalWeaknessBonus(playerProfile, npcProfile));
        result.setMaxHit(maxHit);

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
        return calculateMeleeMaxHit(strengthLevel, strengthBonus, 0, 1.0, 1.0);
    }

    public int calculateMeleeMaxHit(int strengthLevel, int strengthBonus, double prayerMultiplier, double damageMultiplier)
    {
        return calculateMeleeMaxHit(strengthLevel, strengthBonus, 0, prayerMultiplier, damageMultiplier);
    }

    public int calculateMeleeMaxHit(int strengthLevel, int strengthBonus, int styleBonus, double prayerMultiplier, double damageMultiplier)
    {
        int effectiveStrength = calculateEffectiveLevel(strengthLevel, prayerMultiplier, styleBonus);
        int baseMaxHit = (int) Math.floor(0.5 + effectiveStrength * (strengthBonus + 64) / 640.0);
        return (int) Math.floor(baseMaxHit * damageMultiplier);
    }

    /**
     * Ranged max hit formula.
     *
    *   maxHit = floor( 0.5 + rangedLevel * (rangedStrengthBonus + 64) / 640 )
     *
     * rangedLevel          — effective ranged level (boosted).
     * rangedStrengthBonus  — the ranged strength bonus from equipped ammunition/weapon.
     */
    public int calculateRangedMaxHit(int rangedLevel, int rangedStrengthBonus)
    {
        return calculateRangedMaxHit(rangedLevel, rangedStrengthBonus, 0, 1.0, 1.0);
    }

    public int calculateRangedMaxHit(int rangedLevel, int rangedStrengthBonus, double prayerMultiplier, double damageMultiplier)
    {
        return calculateRangedMaxHit(rangedLevel, rangedStrengthBonus, 0, prayerMultiplier, damageMultiplier);
    }

    public int calculateRangedMaxHit(int rangedLevel, int rangedStrengthBonus, int styleBonus, double prayerMultiplier, double damageMultiplier)
    {
        int effectiveRangedStrength = calculateEffectiveLevel(rangedLevel, prayerMultiplier, styleBonus);
        int baseMaxHit = (int) Math.floor(0.5 + effectiveRangedStrength * (rangedStrengthBonus + 64) / 640.0);
        return (int) Math.floor(baseMaxHit * damageMultiplier);
    }

    public int calculateMagicMaxHit(int baseMaxHit, int magicDamageBonus, double damageMultiplier)
    {
        return calculateMagicMaxHit(baseMaxHit, magicDamageBonus, damageMultiplier, 0);
    }

    public int calculateMagicMaxHit(int baseMaxHit, int magicDamageBonus, double damageMultiplier, int elementalWeaknessPercent)
    {
        if (baseMaxHit <= 0)
        {
            return 0;
        }

        double gearMultiplier = 1.0 + (magicDamageBonus / 100.0);
        int boostedHit = (int) Math.floor(baseMaxHit * gearMultiplier * damageMultiplier);
        int elementalBonus = (int) Math.floor(baseMaxHit * elementalWeaknessPercent / 100.0);
        return boostedHit + elementalBonus;
    }

    private int getElementalWeaknessBonus(CombatProfile playerProfile, NpcCombatProfile npcProfile)
    {
        if (playerProfile.getSpellElement() == null || npcProfile.getWikiWeakness() == null)
        {
            return 0;
        }

        if (!playerProfile.getSpellElement().equalsIgnoreCase(npcProfile.getWikiWeakness()))
        {
            return 0;
        }

        return Math.max(0, npcProfile.getElementalWeaknessPercent());
    }

    private int calculateEffectiveLevel(int boostedLevel, double prayerMultiplier)
    {
        return calculateEffectiveLevel(boostedLevel, prayerMultiplier, 0);
    }

    private int calculateEffectiveLevel(int boostedLevel, double prayerMultiplier, int styleBonus)
    {
        return (int) Math.floor(boostedLevel * prayerMultiplier) + styleBonus + 8;
    }

    private int calculateNpcDefensiveRoll(int npcLevel, int defenceBonus)
    {
        return (npcLevel + 9) * (defenceBonus + 64);
    }
}
