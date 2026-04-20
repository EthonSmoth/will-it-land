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
}
