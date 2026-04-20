package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.VarPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Detects active potion boosts and returns stat modifiers.
 * Handles: Bastion, Attack, Strength, Magic, Ranging, Imbued Heart potions.
 */
@Singleton
public class PotionBoostDetector
{
    private final Client client;

    @Inject
    public PotionBoostDetector(Client client)
    {
        this.client = client;
    }

    /**
     * Gets the attack level boost from active potions.
     */
    public int getAttackBoost()
    {
        // Bastion Potion: +10% all combat stats
        if (hasBastionPotion())
        {
            return 1; // Will be multiplied
        }
        // Attack Potion: +3 attack
        if (hasAttackPotion())
        {
            return 3;
        }
        // Combat Potion: +1 attack, +1 strength
        if (hasCombatPotion())
        {
            return 1;
        }

        return 0;
    }

    /**
     * Gets the ranged level boost from active potions.
     */
    public int getRangedBoost()
    {
        // Bastion Potion: +10% all combat stats
        if (hasBastionPotion())
        {
            return 1; // Will be multiplied
        }
        // Ranging Potion: +4 ranging
        if (hasRangingPotion())
        {
            return 4;
        }

        return 0;
    }

    /**
     * Gets the magic level boost from active potions.
     */
    public int getMagicBoost()
    {
        // Bastion Potion: +10% all combat stats
        if (hasBastionPotion())
        {
            return 1; // Will be multiplied
        }
        // Magic Potion: +4 magic
        if (hasMagicPotion())
        {
            return 4;
        }
        // Imbued Heart: +1 magic
        if (hasImbiedHeart())
        {
            return 1;
        }

        return 0;
    }

    /**
     * Gets the multiplier for bastion potion if active.
     */
    public double getBastionMultiplier()
    {
        if (hasBastionPotion())
        {
            return 1.10; // +10% boost
        }
        return 1.0;
    }

    private boolean hasBastionPotion()
    {
        // Check active potions - VarPlayer 3012 stores potion data
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x8) != 0; // Bastion potion bit
    }

    private boolean hasAttackPotion()
    {
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x1) != 0; // Attack potion bit
    }

    private boolean hasCombatPotion()
    {
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x4) != 0; // Combat potion bit
    }

    private boolean hasRangingPotion()
    {
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x2) != 0; // Ranging potion bit
    }

    private boolean hasMagicPotion()
    {
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x10) != 0; // Magic potion bit
    }

    private boolean hasImbiedHeart()
    {
        int potionEffect = client.getVarpValue(3012);
        return (potionEffect & 0x20) != 0; // Imbued heart bit
    }
}

