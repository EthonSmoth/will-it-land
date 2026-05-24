package com.ethan.combatcalc;
import net.runelite.api.InventoryID;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Sums all offensive and defensive equipment bonuses from the player’s worn gear.
 *
 * ### How it works
 *   On each call to collectBonuses(), the EQUIPMENT ItemContainer is read and every
 *   occupied slot is iterated.  For each item, ItemManager.getItemStats() is called to
 *   retrieve the cached ItemEquipmentStats, which exposes the same bonus columns shown
 *   on the in-game equipment screen (stab, slash, crush, magic, ranged attack;
 *   strength, ranged strength; all defence bonuses).
 *
 *   All values are summed into a plain EquipmentBonuses data object and returned.
 *
 * ### No caching
 *   Stats are re-read fresh on every call.  This keeps the code simple and ensures
 *   the values are always up to date without needing to listen to inventory change events.
 *
 * ### No file I/O or network calls
 *   ItemManager serves stats from RuneLite’s local item cache — no external requests.
 */
@Singleton
public class EquipmentStatCollector
{
    private final Client client;
    private final ItemManager itemManager;

    @Inject
    public EquipmentStatCollector(Client client, ItemManager itemManager)
    {
        this.client = client;
        this.itemManager = itemManager;
    }

    /**
     * Collects all equipment bonuses from worn gear.
     */
    public EquipmentBonuses collectBonuses()
    {
        EquipmentBonuses bonuses = new EquipmentBonuses();

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return bonuses;
        }

        // Iterate through all equipment slots
        for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
        {
            Item item = equipment.getItem(slot.getSlotIdx());
            if (item == null || item.getId() == -1)
            {
                continue;
            }

            ItemStats itemStats = itemManager.getItemStats(item.getId());
            if (itemStats == null)
            {
                continue;
            }

            ItemEquipmentStats equipStats = itemStats.getEquipment();
            if (equipStats == null)
            {
                continue;
            }

            // Sum up attack bonuses
            bonuses.stabAttack += equipStats.getAstab();
            bonuses.slashAttack += equipStats.getAslash();
            bonuses.crushAttack += equipStats.getAcrush();
            bonuses.magicAttack += equipStats.getAmagic();
            bonuses.rangedAttack += equipStats.getArange();

            // Sum up strength bonuses
            bonuses.strengthBonus += equipStats.getStr();
            bonuses.rangedStrengthBonus += equipStats.getRstr();

            // Defence bonuses
            bonuses.stabDefence += equipStats.getDstab();
            bonuses.slashDefence += equipStats.getDslash();
            bonuses.crushDefence += equipStats.getDcrush();
            bonuses.rangedDefence += equipStats.getDrange();
            bonuses.magicDefence += equipStats.getDmagic();
        }

        return bonuses;
    }

    /**
     * Container class for equipment bonuses.
     */
    public static class EquipmentBonuses
    {
        public int stabAttack = 0;
        public int slashAttack = 0;
        public int crushAttack = 0;
        public int magicAttack = 0;
        public int rangedAttack = 0;
        public int strengthBonus = 0;
        public int rangedStrengthBonus = 0;
        public int magicDamageBonus = 0;
        public int stabDefence = 0;
        public int slashDefence = 0;
        public int crushDefence = 0;
        public int rangedDefence = 0;
        public int magicDefence = 0;
    }
}
