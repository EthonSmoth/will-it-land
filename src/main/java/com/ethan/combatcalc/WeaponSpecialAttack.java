package com.ethan.combatcalc;

public class WeaponSpecialAttack
{
    private final String name;
    private final int energyCostPercent;
    private final String description;
    private final String defenceRoll;
    private final String attackRollModifier;
    private final String damageRollModifier;

    public WeaponSpecialAttack(String name, int energyCostPercent, String description,
                               String defenceRoll, String attackRollModifier, String damageRollModifier)
    {
        this.name = name;
        this.energyCostPercent = energyCostPercent;
        this.description = description;
        this.defenceRoll = defenceRoll;
        this.attackRollModifier = attackRollModifier;
        this.damageRollModifier = damageRollModifier;
    }

    public String getName()
    {
        return name;
    }

    public int getEnergyCostPercent()
    {
        return energyCostPercent;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDefenceRoll()
    {
        return defenceRoll;
    }

    public String getAttackRollModifier()
    {
        return attackRollModifier;
    }

    public String getDamageRollModifier()
    {
        return damageRollModifier;
    }

    public String formatSummary()
    {
        StringBuilder summary = new StringBuilder();
        summary.append(name).append(" (").append(energyCostPercent).append("%)");
        if (attackRollModifier != null && !attackRollModifier.isEmpty() && !"None".equals(attackRollModifier))
        {
            summary.append(", Acc ").append(attackRollModifier);
        }
        if (damageRollModifier != null && !damageRollModifier.isEmpty() && !"None".equals(damageRollModifier))
        {
            summary.append(", Dmg ").append(damageRollModifier);
        }
        return summary.toString();
    }
}
