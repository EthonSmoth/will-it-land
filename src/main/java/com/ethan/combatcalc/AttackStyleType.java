package com.ethan.combatcalc;

public enum AttackStyleType
{
    STAB("Stab"),
    SLASH("Slash"),
    CRUSH("Crush"),
    CONTROLLED("Controlled"),
    UNKNOWN("Unknown");

    private final String displayName;

    AttackStyleType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}