package com.ethan.combatcalc;

public class WeaknessSummary
{
    private final AttackSubType defensiveWeakness;
    private final int defensiveWeaknessValue;
    private final AttackSubType weaponWeakness;
    private final int weaponWeaknessValue;
    private final String recommendedWeaponName;
    private final AttackSubType recommendedWeaponStyle;

    public WeaknessSummary(AttackSubType defensiveWeakness,
                           int defensiveWeaknessValue,
                           AttackSubType weaponWeakness,
                           int weaponWeaknessValue,
                           String recommendedWeaponName,
                           AttackSubType recommendedWeaponStyle)
    {
        this.defensiveWeakness = defensiveWeakness;
        this.defensiveWeaknessValue = defensiveWeaknessValue;
        this.weaponWeakness = weaponWeakness;
        this.weaponWeaknessValue = weaponWeaknessValue;
        this.recommendedWeaponName = recommendedWeaponName;
        this.recommendedWeaponStyle = recommendedWeaponStyle;
    }

    public AttackSubType getDefensiveWeakness()
    {
        return defensiveWeakness;
    }

    public int getDefensiveWeaknessValue()
    {
        return defensiveWeaknessValue;
    }

    public AttackSubType getWeaponWeakness()
    {
        return weaponWeakness;
    }

    public int getWeaponWeaknessValue()
    {
        return weaponWeaknessValue;
    }

    public String getRecommendedWeaponName()
    {
        return recommendedWeaponName;
    }

    public AttackSubType getRecommendedWeaponStyle()
    {
        return recommendedWeaponStyle;
    }

    public boolean hasRecommendation()
    {
        return recommendedWeaponName != null && !recommendedWeaponName.isEmpty()
                && recommendedWeaponStyle != null
                && recommendedWeaponStyle != AttackSubType.UNKNOWN;
    }
}
