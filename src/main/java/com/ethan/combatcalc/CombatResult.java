package com.ethan.combatcalc;

/**
 * Result of a combat accuracy calculation.
 */
public class CombatResult
{
    private CombatType combatType;
    private AttackSubType attackSubType;
    private double hitChance;
    private int maxHit;
    private int offensiveRoll;
    private int defensiveRoll;
    private boolean npcUnknown; // Flag if NPC stats were not found in database
    private boolean debugMode; // Flag for debug display
    private String debugInfo; // Debug information string

    public CombatResult()
    {
        this.hitChance = 0.0;
        this.maxHit = 0;
        this.offensiveRoll = 0;
        this.defensiveRoll = 0;
        this.npcUnknown = false;
        this.debugMode = false;
        this.debugInfo = "";
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

    public double getHitChance()
    {
        return hitChance;
    }

    public void setHitChance(double hitChance)
    {
        this.hitChance = hitChance;
    }

    public int getMaxHit()
    {
        return maxHit;
    }

    public void setMaxHit(int maxHit)
    {
        this.maxHit = maxHit;
    }

    public int getOffensiveRoll()
    {
        return offensiveRoll;
    }

    public void setOffensiveRoll(int offensiveRoll)
    {
        this.offensiveRoll = offensiveRoll;
    }

    public int getDefensiveRoll()
    {
        return defensiveRoll;
    }

    public void setDefensiveRoll(int defensiveRoll)
    {
        this.defensiveRoll = defensiveRoll;
    }

    public boolean isNpcUnknown()
    {
        return npcUnknown;
    }

    public void setNpcUnknown(boolean npcUnknown)
    {
        this.npcUnknown = npcUnknown;
    }

    public boolean isDebugMode()
    {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode)
    {
        this.debugMode = debugMode;
    }

    public String getDebugInfo()
    {
        return debugInfo;
    }

    public void setDebugInfo(String debugInfo)
    {
        this.debugInfo = debugInfo;
    }

    /**
     * Format hit chance as a percentage string.
     */
    public String formatHitChance()
    {
        return String.format("%.1f%%", hitChance * 100.0);
    }
}
