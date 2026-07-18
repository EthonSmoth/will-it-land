package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.VarPlayer;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WeaponInfoCollector
{
    private final Client client;
    private final ItemManager itemManager;
    private final WeaponIntelDatabase database;

    @Inject
    public WeaponInfoCollector(Client client, ItemManager itemManager)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.database = new WeaponIntelDatabase();
    }

    public WeaponInfo collect(AttackSubType activeAttackSubType)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return WeaponInfo.empty();
        }

        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weapon == null || weapon.getId() == -1)
        {
            return WeaponInfo.empty();
        }

        String weaponName = getItemName(weapon.getId());
        WeaponInfo curated = database.lookup(weaponName);
        WeaponInfo.Builder builder = curated.toBuilder()
                .activeAttackSubType(activeAttackSubType)
                .activeAttackStyleIndex(client.getVarpValue(VarPlayer.ATTACK_STYLE))
                .specialEnergyPercent(client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10);

        ItemEquipmentStats weaponStats = getEquipmentStats(weapon.getId());
        ItemEquipmentStats ammoStats = getAmmoStats(equipment);

        applyEquipmentStats(builder, weaponStats, ammoStats);
        applyAmmo(builder, equipment);

        return builder.build();
    }

    private String getItemName(int itemId)
    {
        ItemComposition composition = itemManager.getItemComposition(itemId);
        return composition != null ? composition.getName() : "";
    }

    private ItemEquipmentStats getEquipmentStats(int itemId)
    {
        ItemStats itemStats = itemManager.getItemStats(itemId);
        return itemStats != null ? itemStats.getEquipment() : null;
    }

    private ItemEquipmentStats getAmmoStats(ItemContainer equipment)
    {
        Item ammo = equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx());
        if (ammo == null || ammo.getId() == -1)
        {
            return null;
        }

        return getEquipmentStats(ammo.getId());
    }

    private void applyAmmo(WeaponInfo.Builder builder, ItemContainer equipment)
    {
        Item ammo = equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx());
        if (ammo == null || ammo.getId() == -1)
        {
            return;
        }

        String ammoName = getItemName(ammo.getId());
        if (ammoName != null && !ammoName.isEmpty())
        {
            builder.ammoName(ammoName);
        }
    }

    private void applyEquipmentStats(WeaponInfo.Builder builder, ItemEquipmentStats weaponStats, ItemEquipmentStats ammoStats)
    {
        if (weaponStats == null)
        {
            return;
        }

        int ammoRangedAttack = ammoStats != null ? ammoStats.getArange() : 0;
        int ammoRangedStrength = ammoStats != null ? ammoStats.getRstr() : 0;

        builder.attackSpeedTicks(weaponStats.getAspeed())
                .stabAttackBonus(weaponStats.getAstab())
                .slashAttackBonus(weaponStats.getAslash())
                .crushAttackBonus(weaponStats.getAcrush())
                .magicAttackBonus(weaponStats.getAmagic())
                .rangedAttackBonus(weaponStats.getArange() + ammoRangedAttack)
                .strengthBonus(weaponStats.getStr())
                .rangedStrengthBonus(weaponStats.getRstr() + ammoRangedStrength)
                .magicDamageBonus(weaponStats.getMdmg());
    }
}
