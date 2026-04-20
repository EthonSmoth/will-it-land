package com.ethan.combatcalc;

public enum CombatType
{
    MELEE("Melee"),
    RANGED("Ranged"),
    MAGIC("Magic"),
    UNKNOWN("Unknown");

    private final String displayName;

    CombatType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
