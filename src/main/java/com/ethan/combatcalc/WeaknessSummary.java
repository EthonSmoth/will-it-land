package com.ethan.combatcalc;

public class WeaknessSummary
{
    private final AttackSubType defensiveWeakness;
    private final int defensiveWeaknessValue;
    private final AttackSubType weaponWeakness;
    private final int weaponWeaknessValue;
    private final String recommendedWeaponName;
    private final AttackSubType recommendedWeaponStyle;
    private final String weaknessLabel;
    private final String weaknessSource;

    public WeaknessSummary(AttackSubType defensiveWeakness,
                           int defensiveWeaknessValue,
                           AttackSubType weaponWeakness,
                           int weaponWeaknessValue,
                           String recommendedWeaponName,
                           AttackSubType recommendedWeaponStyle)
    {
        this(defensiveWeakness,
                defensiveWeaknessValue,
                weaponWeakness,
                weaponWeaknessValue,
                recommendedWeaponName,
                recommendedWeaponStyle,
                formatStyle(defensiveWeakness),
                "derived");
    }

    public WeaknessSummary(AttackSubType defensiveWeakness,
                           int defensiveWeaknessValue,
                           AttackSubType weaponWeakness,
                           int weaponWeaknessValue,
                           String recommendedWeaponName,
                           AttackSubType recommendedWeaponStyle,
                           String weaknessLabel,
                           String weaknessSource)
    {
        this.defensiveWeakness = defensiveWeakness;
        this.defensiveWeaknessValue = defensiveWeaknessValue;
        this.weaponWeakness = weaponWeakness;
        this.weaponWeaknessValue = weaponWeaknessValue;
        this.recommendedWeaponName = recommendedWeaponName;
        this.recommendedWeaponStyle = recommendedWeaponStyle;
        this.weaknessLabel = weaknessLabel;
        this.weaknessSource = weaknessSource;
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

    public String getWeaknessLabel()
    {
        return weaknessLabel;
    }

    public String getWeaknessSource()
    {
        return weaknessSource;
    }

    public boolean hasRecommendation()
    {
        return recommendedWeaponName != null && !recommendedWeaponName.isEmpty()
                && recommendedWeaponStyle != null
                && recommendedWeaponStyle != AttackSubType.UNKNOWN;
    }

    private static String formatStyle(AttackSubType attackSubType)
    {
        return attackSubType == null ? "Unknown" : attackSubType.getDisplayName();
    }
}
