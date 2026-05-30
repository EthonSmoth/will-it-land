package com.ethan.combatcalc;

/**
 * Combat profile for an NPC target.
 * Contains all defensive stats relevant to the combat calculation.
 */
public class NpcCombatProfile
{
    private String npcName;
    private int defenceLevel;
    private int magicLevel;
    private int stabDefence;
    private int slashDefence;
    private int crushDefence;
    private int rangedDefence;
    private int magicDefence;
    private int combatLevel;
    private int maxHit;
    private boolean aggressive;
    private String attackType;
    private String wikiWeakness;
    private int elementalWeaknessPercent;
    private String dataSource;

    public NpcCombatProfile()
    {
    }

    public NpcCombatProfile(String npcName)
    {
        this.npcName = npcName;
    }

    // Getters and Setters
    public String getNpcName()
    {
        return npcName;
    }

    public void setNpcName(String npcName)
    {
        this.npcName = npcName;
    }

    public int getDefenceLevel()
    {
        return defenceLevel;
    }

    public void setDefenceLevel(int defenceLevel)
    {
        this.defenceLevel = defenceLevel;
    }

    public int getMagicLevel()
    {
        return magicLevel;
    }

    public void setMagicLevel(int magicLevel)
    {
        this.magicLevel = magicLevel;
    }

    public int getStabDefence()
    {
        return stabDefence;
    }

    public void setStabDefence(int stabDefence)
    {
        this.stabDefence = stabDefence;
    }

    public int getSlashDefence()
    {
        return slashDefence;
    }

    public void setSlashDefence(int slashDefence)
    {
        this.slashDefence = slashDefence;
    }

    public int getCrushDefence()
    {
        return crushDefence;
    }

    public void setCrushDefence(int crushDefence)
    {
        this.crushDefence = crushDefence;
    }

    public int getRangedDefence()
    {
        return rangedDefence;
    }

    public void setRangedDefence(int rangedDefence)
    {
        this.rangedDefence = rangedDefence;
    }

    public int getMagicDefence()
    {
        return magicDefence;
    }

    public void setMagicDefence(int magicDefence)
    {
        this.magicDefence = magicDefence;
    }

    public int getCombatLevel()
    {
        return combatLevel;
    }

    public void setCombatLevel(int combatLevel)
    {
        this.combatLevel = combatLevel;
    }

    public int getMaxHit()
    {
        return maxHit;
    }

    public void setMaxHit(int maxHit)
    {
        this.maxHit = maxHit;
    }

    public boolean isAggressive()
    {
        return aggressive;
    }

    public void setAggressive(boolean aggressive)
    {
        this.aggressive = aggressive;
    }

    public String getAttackType()
    {
        return attackType;
    }

    public void setAttackType(String attackType)
    {
        this.attackType = attackType;
    }

    public String getWikiWeakness()
    {
        return wikiWeakness;
    }

    public void setWikiWeakness(String wikiWeakness)
    {
        this.wikiWeakness = wikiWeakness;
    }

    public int getElementalWeaknessPercent()
    {
        return elementalWeaknessPercent;
    }

    public void setElementalWeaknessPercent(int elementalWeaknessPercent)
    {
        this.elementalWeaknessPercent = elementalWeaknessPercent;
    }

    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
    }

    public boolean hasWikiWeakness()
    {
        return wikiWeakness != null && !wikiWeakness.trim().isEmpty();
    }

    public boolean hasThreatData()
    {
        return combatLevel > 0 || maxHit > 0 || aggressive || hasText(attackType);
    }

    /**
     * Gets the appropriate defence bonus based on attack subtype.
     */
    public int getDefenceBonusForAttackType(AttackSubType attackSubType)
    {
        switch (attackSubType)
        {
            case STAB:
                return stabDefence;
            case SLASH:
                return slashDefence;
            case CRUSH:
                return crushDefence;
            case RANGED:
                return rangedDefence;
            case MAGIC:
                return magicDefence;
            default:
                return 0;
        }
    }

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
