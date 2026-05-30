package com.ethan.combatcalc;

import net.runelite.api.ItemID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PoweredStaffMaxHitResolver
{
    @Inject
    public PoweredStaffMaxHitResolver()
    {
    }

    public int resolveBaseMaxHit(int itemId, int magicLevel)
    {
        if (magicLevel <= 0)
        {
            return 0;
        }

        if (isTridentOfTheSeas(itemId))
        {
            return tridentBaseMaxHit(magicLevel, 0);
        }
        if (isTridentOfTheSwamp(itemId))
        {
            return tridentBaseMaxHit(magicLevel, 3);
        }
        if (isSanguinestiStaff(itemId))
        {
            return tridentBaseMaxHit(magicLevel, 4);
        }
        if (isWarpedSceptre(itemId))
        {
            return Math.max(0, (int) Math.floor((8 * magicLevel + 96) / 37.0));
        }
        if (isTumekensShadow(itemId))
        {
            return (int) Math.floor(magicLevel / 3.0 + 1);
        }

        return 0;
    }

    public static boolean isPoweredStaff(int itemId)
    {
        return isTridentOfTheSeas(itemId) ||
                isTridentOfTheSwamp(itemId) ||
                isSanguinestiStaff(itemId) ||
                isWarpedSceptre(itemId) ||
                isTumekensShadow(itemId);
    }

    private static int tridentBaseMaxHit(int magicLevel, int offset)
    {
        int effectiveMagic = Math.max(75, Math.min(123, magicLevel));
        return 20 + offset + (effectiveMagic - 75) / 3;
    }

    private static boolean isTridentOfTheSeas(int itemId)
    {
        return itemId == ItemID.TRIDENT_OF_THE_SEAS ||
                itemId == ItemID.TRIDENT_OF_THE_SEAS_FULL ||
                itemId == ItemID.TRIDENT_OF_THE_SEAS_E ||
                itemId == ItemID.TRIDENT_OF_THE_SEAS_O ||
                itemId == ItemID.TRIDENT_OF_THE_SEAS_FULL_O ||
                itemId == ItemID.TRIDENT_OF_THE_SEAS_E_O;
    }

    private static boolean isTridentOfTheSwamp(int itemId)
    {
        return itemId == ItemID.TRIDENT_OF_THE_SWAMP ||
                itemId == ItemID.TRIDENT_OF_THE_SWAMP_E ||
                itemId == ItemID.TRIDENT_OF_THE_SWAMP_O ||
                itemId == ItemID.TRIDENT_OF_THE_SWAMP_E_O;
    }

    private static boolean isSanguinestiStaff(int itemId)
    {
        return itemId == ItemID.SANGUINESTI_STAFF ||
                itemId == ItemID.HOLY_SANGUINESTI_STAFF;
    }

    private static boolean isWarpedSceptre(int itemId)
    {
        return itemId == ItemID.WARPED_SCEPTRE;
    }

    private static boolean isTumekensShadow(int itemId)
    {
        return itemId == ItemID.TUMEKENS_SHADOW ||
                itemId == ItemID.CORRUPTED_TUMEKENS_SHADOW;
    }
}
