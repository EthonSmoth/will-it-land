package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Prayer;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CombatModifierTest
{
    @Test
    public void salveDoesNotApplyToGemstoneCrab()
    {
        CombatModifier modifier = new CombatModifier(clientWithAmulet(ItemID.SALVE_AMULETEI));

        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MELEE, gemstoneCrab()), 0.0001);
        assertEquals(1.0, modifier.getDamageMultiplier(CombatType.MAGIC, gemstoneCrab()), 0.0001);
    }

    @Test
    public void salveEiAppliesToUndeadForAllCombatStyles()
    {
        CombatModifier modifier = new CombatModifier(clientWithAmulet(ItemID.SALVE_AMULETEI));
        NpcCombatProfile skeleton = new NpcCombatProfile("Skeleton");

        assertEquals(1.20, modifier.getOffensiveRollMultiplier(CombatType.MELEE, skeleton), 0.0001);
        assertEquals(1.20, modifier.getOffensiveRollMultiplier(CombatType.RANGED, skeleton), 0.0001);
        assertEquals(1.20, modifier.getOffensiveRollMultiplier(CombatType.MAGIC, skeleton), 0.0001);
        assertEquals(1.20, modifier.getDamageMultiplier(CombatType.MAGIC, skeleton), 0.0001);
    }

    @Test
    public void unimbuedSalveAppliesOnlyToMelee()
    {
        CombatModifier modifier = new CombatModifier(clientWithAmulet(ItemID.SALVE_AMULET));
        NpcCombatProfile skeleton = new NpcCombatProfile("Skeleton");

        assertEquals(1.15, modifier.getOffensiveRollMultiplier(CombatType.MELEE, skeleton), 0.0001);
        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.RANGED, skeleton), 0.0001);
        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MAGIC, skeleton), 0.0001);
    }

    @Test
    public void magicDamageAmuletsDoNotAddMagicAccuracy()
    {
        CombatModifier modifier = new CombatModifier(clientWithAmulet(ItemID.OCCULT_NECKLACE));

        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MAGIC, new NpcCombatProfile("Cow")), 0.0001);
    }

    @Test
    public void missingClientFallsBackToNoModifier()
    {
        CombatModifier modifier = new CombatModifier(null);

        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MELEE, new NpcCombatProfile("Cow")), 0.0001);
        assertEquals(1.0, modifier.getDamageMultiplier(CombatType.MELEE, new NpcCombatProfile("Cow")), 0.0001);
    }

    @Test
    public void offensivePrayersUseStyleSpecificMultipliers()
    {
        CombatModifier piety = new CombatModifier(clientWithPrayers(Prayer.PIETY));
        CombatModifier rigour = new CombatModifier(clientWithPrayers(Prayer.RIGOUR));
        CombatModifier augury = new CombatModifier(clientWithPrayers(Prayer.AUGURY));

        assertEquals(1.20, piety.getAccuracyPrayerMultiplier(CombatType.MELEE), 0.0001);
        assertEquals(1.23, piety.getStrengthPrayerMultiplier(), 0.0001);
        assertEquals(1.20, rigour.getAccuracyPrayerMultiplier(CombatType.RANGED), 0.0001);
        assertEquals(1.23, rigour.getRangedStrengthPrayerMultiplier(), 0.0001);
        assertEquals(1.25, augury.getAccuracyPrayerMultiplier(CombatType.MAGIC), 0.0001);
    }

    @Test
    public void slayerHelmetAppliesOnMatchingTask()
    {
        CombatModifier modifier = new CombatModifier(
                clientWithHead(ItemID.SLAYER_HELMET_I),
                config(true, true, true),
                null,
                () -> "Skeletons");

        assertEquals(1.15, modifier.getOffensiveRollMultiplier(CombatType.MELEE, new NpcCombatProfile("Skeleton")), 0.0001);
        assertEquals(1.15, modifier.getDamageMultiplier(CombatType.MELEE, new NpcCombatProfile("Skeleton")), 0.0001);
    }

    @Test
    public void imbuedSlayerHelmetAppliesToMagicAndRangedOnMatchingTask()
    {
        CombatModifier modifier = new CombatModifier(
                clientWithHead(ItemID.SLAYER_HELMET_I),
                config(true, true, true),
                null,
                () -> "Abyssal demons");

        assertEquals(1.15, modifier.getOffensiveRollMultiplier(CombatType.MAGIC, new NpcCombatProfile("Abyssal demon")), 0.0001);
        assertEquals(1.15, modifier.getDamageMultiplier(CombatType.RANGED, new NpcCombatProfile("Abyssal demon")), 0.0001);
    }

    @Test
    public void slayerHelmetDoesNotApplyOffTask()
    {
        CombatModifier modifier = new CombatModifier(
                clientWithHead(ItemID.SLAYER_HELMET_I),
                config(true, true, true),
                null,
                () -> "Goblins");

        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MELEE, new NpcCombatProfile("Skeleton")), 0.0001);
    }

    @Test
    public void salveAndSlayerHelmetDoNotStack()
    {
        CombatModifier modifier = new CombatModifier(
                itemClient(ItemID.SLAYER_HELMET_I, ItemID.SALVE_AMULETEI),
                config(true, true, true),
                null,
                () -> "Skeletons");

        assertEquals(1.20, modifier.getOffensiveRollMultiplier(CombatType.MELEE, new NpcCombatProfile("Skeleton")), 0.0001);
        assertEquals(1.20, modifier.getDamageMultiplier(CombatType.MAGIC, new NpcCombatProfile("Skeleton")), 0.0001);
    }

    @Test
    public void configCanDisablePrayerBonuses()
    {
        CombatModifier modifier = new CombatModifier(
                clientWithPrayers(Prayer.PIETY),
                config(false, true, true),
                null);

        assertEquals(1.0, modifier.getAccuracyPrayerMultiplier(CombatType.MELEE), 0.0001);
        assertEquals(1.0, modifier.getStrengthPrayerMultiplier(), 0.0001);
    }

    @Test
    public void configCanDisableSpecialModifiers()
    {
        CombatModifier modifier = new CombatModifier(
                clientWithAmulet(ItemID.SALVE_AMULETEI),
                config(true, false, true),
                null);

        assertEquals(1.0, modifier.getOffensiveRollMultiplier(CombatType.MAGIC, new NpcCombatProfile("Skeleton")), 0.0001);
        assertEquals(1.0, modifier.getDamageMultiplier(CombatType.MAGIC, new NpcCombatProfile("Skeleton")), 0.0001);
    }

    private static Client clientWithAmulet(int amuletId)
    {
        ItemContainer equipment = itemContainerWithAmulet(amuletId);
        return client(equipment, EnumSet.noneOf(Prayer.class));
    }

    private static Client clientWithPrayers(Prayer... prayers)
    {
        Set<Prayer> activePrayers = prayers.length == 0
                ? EnumSet.noneOf(Prayer.class)
                : EnumSet.copyOf(java.util.Arrays.asList(prayers));
        return client(itemContainerWithAmulet(-1), activePrayers);
    }

    private static Client clientWithHead(int headId)
    {
        return client(itemContainerWithHead(headId), EnumSet.noneOf(Prayer.class));
    }

    private static Client itemClient(int headId, int amuletId)
    {
        return client(itemContainerWithHeadAndAmulet(headId, amuletId), EnumSet.noneOf(Prayer.class));
    }

    private static Client client(ItemContainer equipment, Set<Prayer> activePrayers)
    {
        return (Client) Proxy.newProxyInstance(
                Client.class.getClassLoader(),
                new Class<?>[]{Client.class},
                (proxy, method, args) ->
                {
                    if ("getItemContainer".equals(method.getName()) && args[0] == InventoryID.EQUIPMENT)
                    {
                        return equipment;
                    }
                    if ("isPrayerActive".equals(method.getName()))
                    {
                        return activePrayers.contains(args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static WillItLandConfig config(boolean prayerBonuses, boolean specialModifiers, boolean equipmentSets)
    {
        return (WillItLandConfig) Proxy.newProxyInstance(
                WillItLandConfig.class.getClassLoader(),
                new Class<?>[]{WillItLandConfig.class},
                (proxy, method, args) ->
                {
                    if ("enablePrayerBonuses".equals(method.getName()))
                    {
                        return prayerBonuses;
                    }
                    if ("enableSpecialModifiers".equals(method.getName()))
                    {
                        return specialModifiers;
                    }
                    if ("enableEquipmentSets".equals(method.getName()))
                    {
                        return equipmentSets;
                    }
                    return method.getDefaultValue() != null ? method.getDefaultValue() : defaultValue(method.getReturnType());
                });
    }

    private static ItemContainer itemContainerWithAmulet(int amuletId)
    {
        return (ItemContainer) Proxy.newProxyInstance(
                ItemContainer.class.getClassLoader(),
                new Class<?>[]{ItemContainer.class},
                (proxy, method, args) ->
                {
                    if ("getItem".equals(method.getName())
                            && ((Integer) args[0]) == EquipmentInventorySlot.AMULET.getSlotIdx()
                            && amuletId != -1)
                    {
                        return new Item(amuletId, 1);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static ItemContainer itemContainerWithHead(int headId)
    {
        return (ItemContainer) Proxy.newProxyInstance(
                ItemContainer.class.getClassLoader(),
                new Class<?>[]{ItemContainer.class},
                (proxy, method, args) ->
                {
                    if ("getItem".equals(method.getName())
                            && ((Integer) args[0]) == EquipmentInventorySlot.HEAD.getSlotIdx()
                            && headId != -1)
                    {
                        return new Item(headId, 1);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static ItemContainer itemContainerWithHeadAndAmulet(int headId, int amuletId)
    {
        return (ItemContainer) Proxy.newProxyInstance(
                ItemContainer.class.getClassLoader(),
                new Class<?>[]{ItemContainer.class},
                (proxy, method, args) ->
                {
                    if ("getItem".equals(method.getName()))
                    {
                        int slot = (Integer) args[0];
                        if (slot == EquipmentInventorySlot.HEAD.getSlotIdx() && headId != -1)
                        {
                            return new Item(headId, 1);
                        }
                        if (slot == EquipmentInventorySlot.AMULET.getSlotIdx() && amuletId != -1)
                        {
                            return new Item(amuletId, 1);
                        }
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType)
    {
        if (!returnType.isPrimitive())
        {
            return null;
        }
        if (returnType == boolean.class)
        {
            return false;
        }
        if (returnType == int.class)
        {
            return 0;
        }
        if (returnType == long.class)
        {
            return 0L;
        }
        if (returnType == double.class)
        {
            return 0.0;
        }
        if (returnType == float.class)
        {
            return 0.0f;
        }
        if (returnType == short.class)
        {
            return (short) 0;
        }
        if (returnType == byte.class)
        {
            return (byte) 0;
        }
        if (returnType == char.class)
        {
            return '\0';
        }
        return null;
    }

    private static NpcCombatProfile gemstoneCrab()
    {
        return new NpcCombatProfile("Gemstone Crab");
    }
}
