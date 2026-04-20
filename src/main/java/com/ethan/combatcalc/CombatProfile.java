package com.ethan.combatcalc;

/**
 * Normalized combat profile for the player.
 * Represents all relevant offensive stats for a given combat type.
 */
public class CombatProfile
{
    private CombatType combatType;
    private AttackSubType attackSubType;
    private int effectiveAttackLevel;
    private int effectiveStrengthLevel;
    private int attackBonus;
    private int strengthBonus;
    private int rangedStrengthBonus;
    private int magicDamageBonus;
    private int maxHitBase;

    public CombatProfile()
    {
        this.combatType = CombatType.UNKNOWN;
        this.attackSubType = AttackSubType.UNKNOWN;
    }

    // Getters and Setters
    public CombatType getCombatType()
    {
        return combatType;
    }

    public void setCombatType(CombatType combatType)
    {
        this.combatType = combatType;
    }

    public AttackSubType getAttackSubType()
    {
        return attackSubType;
    }

    public void setAttackSubType(AttackSubType attackSubType)
    {
        this.attackSubType = attackSubType;
    }

    public int getEffectiveAttackLevel()
    {
        return effectiveAttackLevel;
    }

    public void setEffectiveAttackLevel(int effectiveAttackLevel)
    {
        this.effectiveAttackLevel = effectiveAttackLevel;
    }

    public int getEffectiveStrengthLevel()
    {
        return effectiveStrengthLevel;
    }

    public void setEffectiveStrengthLevel(int effectiveStrengthLevel)
    {
        this.effectiveStrengthLevel = effectiveStrengthLevel;
    }

    public int getAttackBonus()
    {
        return attackBonus;
    }

    public void setAttackBonus(int attackBonus)
    {
        this.attackBonus = attackBonus;
    }

    public int getStrengthBonus()
    {
        return strengthBonus;
    }

    public void setStrengthBonus(int strengthBonus)
    {
        this.strengthBonus = strengthBonus;
    }

    public int getRangedStrengthBonus()
    {
        return rangedStrengthBonus;
    }

    public void setRangedStrengthBonus(int rangedStrengthBonus)
    {
        this.rangedStrengthBonus = rangedStrengthBonus;
    }

    public int getMagicDamageBonus()
    {
        return magicDamageBonus;
    }

    public void setMagicDamageBonus(int magicDamageBonus)
    {
        this.magicDamageBonus = magicDamageBonus;
    }

    public int getMaxHitBase()
    {
        return maxHitBase;
    }

    public void setMaxHitBase(int maxHitBase)
    {
        this.maxHitBase = maxHitBase;
    }
}
