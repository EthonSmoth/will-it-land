package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class InventoryWeaponCollector
{
    private final Client client;
    private final ItemManager itemManager;

    @Inject
    public InventoryWeaponCollector(Client client, ItemManager itemManager)
    {
        this.client = client;
        this.itemManager = itemManager;
    }

    public List<WeaponInfo> collectCandidates()
    {
        List<WeaponInfo> candidates = new ArrayList<>();
        addEquippedWeapon(candidates);
        addInventoryWeapons(candidates);
        return candidates;
    }

    private void addEquippedWeapon(List<WeaponInfo> candidates)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return;
        }

        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        WeaponInfo info = buildWeaponInfo(weapon);
        if (info.hasWeapon())
        {
            candidates.add(info);
        }
    }

    private void addInventoryWeapons(List<WeaponInfo> candidates)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return;
        }

        for (Item item : inventory.getItems())
        {
            WeaponInfo info = buildWeaponInfo(item);
            if (info.hasWeapon())
            {
                candidates.add(info);
            }
        }
    }

    private WeaponInfo buildWeaponInfo(Item item)
    {
        if (item == null || item.getId() == -1)
        {
            return WeaponInfo.empty();
        }

        ItemStats itemStats = itemManager.getItemStats(item.getId());
        if (itemStats == null || !itemStats.isEquipable() || itemStats.getEquipment() == null)
        {
            return WeaponInfo.empty();
        }

        ItemEquipmentStats stats = itemStats.getEquipment();
        if (!hasOffensiveStats(stats))
        {
            return WeaponInfo.empty();
        }

        ItemComposition composition = itemManager.getItemComposition(item.getId());
        String name = composition != null ? composition.getName() : "";
        if (name == null || name.isEmpty())
        {
            return WeaponInfo.empty();
        }

        return WeaponInfo.builder(name)
                .attackSpeedTicks(stats.getAspeed())
                .stabAttackBonus(stats.getAstab())
                .slashAttackBonus(stats.getAslash())
                .crushAttackBonus(stats.getAcrush())
                .magicAttackBonus(stats.getAmagic())
                .rangedAttackBonus(stats.getArange())
                .strengthBonus(stats.getStr())
                .rangedStrengthBonus(stats.getRstr())
                .magicDamageBonus(stats.getMdmg())
                .build();
    }

    private boolean hasOffensiveStats(ItemEquipmentStats stats)
    {
        return stats.getAstab() != 0 ||
                stats.getAslash() != 0 ||
                stats.getAcrush() != 0 ||
                stats.getAmagic() != 0 ||
                stats.getArange() != 0 ||
                stats.getStr() != 0 ||
                stats.getRstr() != 0 ||
                stats.getMdmg() != 0 ||
                stats.getAspeed() > 0;
    }
}
