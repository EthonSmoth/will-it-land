package com.ethan.combatcalc;

public enum AttackSubType
{
    // Melee subtypes
    STAB("Stab"),
    SLASH("Slash"),
    CRUSH("Crush"),
    // Ranged subtypes
    RANGED("Ranged"),
    // Magic subtypes
    MAGIC("Magic"),
    // Fallback
    UNKNOWN("Unknown");

    private final String displayName;

    AttackSubType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
