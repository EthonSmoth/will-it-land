package com.ethan.combatcalc;

import com.google.inject.Provides;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "will-it-land",
        description = "Combat accuracy calculator - shows hit chance % vs your target NPC",
        tags = {"combat", "accuracy", "overlay", "pvm"}
)
/**
 * Main plugin class for Will It Land.
 *
 * What this plugin does:
 *   - On every game tick, checks whether the local player is currently interacting with an NPC.
 *   - If so, it reads the player's current skill levels (already boosted by potions via the API),
 *     their equipped gear bonuses, and the target NPC's defensive stats from the bundled JSON database.
 *   - It then runs the standard OSRS accuracy formula to produce a hit-chance percentage,
 *     a max-hit value, and an estimated DPS figure.
 *   - These values are stored in a CombatResult and picked up by WillItLandOverlay for display.
 *
 * No file I/O is performed at runtime.  NPC stats are loaded once at startup from the
 * bundled npc_stats.json resource inside the jar (see NpcStatsRepository).
 *
 * The plugin only reads game state — it never writes to the game or sends any packets.
 */
public class WillItLandPlugin extends Plugin
{
    private static final int SPELL_WIND_STRIKE = 3273;
    private static final int SPELL_WATER_STRIKE = 3275;
    private static final int SPELL_EARTH_STRIKE = 3277;
    private static final int SPELL_FIRE_STRIKE = 3279;
    private static final int SPELL_WIND_BOLT = 3281;
    private static final int SPELL_WATER_BOLT = 3283;
    private static final int SPELL_EARTH_BOLT = 3285;
    private static final int SPELL_FIRE_BOLT = 3291;
    private static final int SPELL_WIND_BLAST = 3293;
    private static final int SPELL_WATER_BLAST = 3295;
    private static final int SPELL_EARTH_BLAST = 3297;
    private static final int SPELL_FIRE_BLAST = 3299;
    private static final int SPELL_WIND_WAVE = 3301;
    private static final int SPELL_WATER_WAVE = 3303;
    private static final int SPELL_EARTH_WAVE = 3305;
    private static final int SPELL_FIRE_WAVE = 3307;
    private static final int SPELL_WIND_SURGE = 3309;
    private static final int SPELL_WATER_SURGE = 3311;
    private static final int SPELL_EARTH_SURGE = 3313;
    private static final int SPELL_FIRE_SURGE = 3315;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WillItLandOverlay overlay;

    @Inject
    private TargetWeaknessOverlay targetWeaknessOverlay;

    @Inject
    private WillItLandConfig config;

    @Inject
    private AttackStyleResolver attackStyleResolver;

    @Inject
    private EquipmentStatCollector equipmentCollector;

    @Inject
    private CombatCalculator combatCalculator;

    @Inject
    private WeaponInfoCollector weaponInfoCollector;

    @Inject
    private NpcStatsRepository npcStatsRepository;

    @Inject
    private InventoryWeaponCollector inventoryWeaponCollector;

    @Inject
    private WeaknessAnalyzer weaknessAnalyzer;

    private CombatResult latestResult = new CombatResult();
    private NPC currentTargetNpc;
    private WeaknessSummary latestWeaknessSummary;

    public CombatResult getLatestResult()
    {
        return latestResult;
    }

    public WillItLandConfig getConfig()
    {
        return config;
    }

    public NPC getCurrentTargetNpc()
    {
        return currentTargetNpc;
    }

    public WeaknessSummary getLatestWeaknessSummary()
    {
        return latestWeaknessSummary;
    }

    @Provides
    WillItLandConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(WillItLandConfig.class);
    }

    @Override
    protected void startUp()
    {
        // Register the overlay so it is drawn on screen.
        overlayManager.add(overlay);
        overlayManager.add(targetWeaknessOverlay);
    }

    @Override
    protected void shutDown()
    {
        // Remove the overlay and reset state so nothing lingers after the plugin is disabled.
        overlayManager.remove(overlay);
        overlayManager.remove(targetWeaknessOverlay);
        latestResult = new CombatResult();
        currentTargetNpc = null;
        latestWeaknessSummary = null;
    }

    /**
     * Runs once per game tick (~600 ms).
     *
     * Flow:
     *   1. Bail out if no local player exists (e.g. loading screen).
     *   2. Check who the player is currently interacting with.  If it is not an NPC
     *      (e.g. another player, or nobody), clear the result and return — the overlay
     *      will show nothing.
     *   3. Determine what combat style is currently selected (melee/ranged/magic) and
     *      which sub-type (stab/slash/crush).
     *   4. Build a CombatProfile for the player using current boosted levels + gear.
     *   5. Look up the target NPC's defensive stats from the JSON database.
     *   6. Run the appropriate accuracy calculation and store the result.
     *   7. Optionally populate debug breakdown lines if debug mode is on in config.
     */
    @Subscribe
    public void onGameTick(GameTick event)
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            return;
        }

        // Get target
        Actor interacting = localPlayer.getInteracting();
        NPC targetNpc = null;
        if (interacting instanceof NPC)
        {
            targetNpc = (NPC) interacting;
        }

        if (targetNpc == null)
        {
            latestResult = new CombatResult();
            currentTargetNpc = null;
            latestWeaknessSummary = null;
            return;
        }

        currentTargetNpc = targetNpc;

        // Resolve combat type and subtype
        CombatType combatType = attackStyleResolver.resolveCombatType(client);
        AttackSubType attackSubType = attackStyleResolver.resolveAttackSubType(client, combatType);

        // Build player combat profile
        CombatProfile playerProfile = buildPlayerProfile(combatType, attackSubType);
        WeaponInfo weaponInfo = weaponInfoCollector.collect(attackSubType);

        // Build NPC combat profile
        NpcCombatProfile npcProfile = npcStatsRepository.getNpcProfile(targetNpc.getName());
        latestWeaknessSummary = weaknessAnalyzer.analyze(npcProfile, inventoryWeaponCollector.collectCandidates());

        // Check if NPC was found in database
        boolean npcFound = npcStatsRepository.hasNpcProfile(targetNpc.getName());

        // Calculate based on combat type
        if (combatType == CombatType.MELEE)
        {
            latestResult = combatCalculator.calculateMeleeAccuracy(playerProfile, npcProfile);
        }
        else if (combatType == CombatType.RANGED)
        {
            latestResult = combatCalculator.calculateRangedAccuracy(playerProfile, npcProfile);
        }
        else if (combatType == CombatType.MAGIC)
        {
            latestResult = combatCalculator.calculateMagicAccuracy(playerProfile, npcProfile);
        }

        latestResult.setWeaponInfo(weaponInfo);

        // Set unknown flag if NPC wasn't in database
        if (!npcFound)
        {
            latestResult.setNpcUnknown(true);
        }

        // Add debug info if enabled
        if (config.debugMode())
        {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("Offensive Roll: ").append(latestResult.getOffensiveRoll()).append("\n");
            debugInfo.append("Defensive Roll: ").append(latestResult.getDefensiveRoll()).append("\n");
            debugInfo.append("Max Hit: ").append(latestResult.getMaxHit()).append("\n");
            debugInfo.append("DPS (est): ").append(calculateDPS(latestResult)).append("\n");

            latestResult.setDebugMode(true);
            latestResult.setDebugInfo(debugInfo.toString());
        }
        else
        {
            latestResult.setDebugMode(false);
        }
    }

    /**
     * Assembles a CombatProfile representing the player's current offensive stats.
     *
     * Skill levels are read via client.getBoostedSkillLevel(), which already returns
     * the post-potion value — no manual potion detection is needed or performed.
     *
     * Equipment bonuses are summed across all worn slots by EquipmentStatCollector.
     * The attack bonus selected depends on the active sub-type (stab/slash/crush for
     * melee, ranged attack for ranged, magic attack for magic).
     */
    private CombatProfile buildPlayerProfile(CombatType combatType, AttackSubType attackSubType)
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(combatType);
        profile.setAttackSubType(attackSubType);

        // getBoostedSkillLevel() already includes active potion effects
        int attackLevel = client.getBoostedSkillLevel(Skill.ATTACK);
        int rangedLevel = client.getBoostedSkillLevel(Skill.RANGED);
        int magicLevel = client.getBoostedSkillLevel(Skill.MAGIC);
        int strengthLevel = client.getBoostedSkillLevel(Skill.STRENGTH);

        // Collect equipment bonuses
        EquipmentStatCollector.EquipmentBonuses equipment = equipmentCollector.collectBonuses();

        // Set profile based on combat type
        if (combatType == CombatType.MELEE)
        {
            profile.setEffectiveAttackLevel(attackLevel);
            profile.setEffectiveStrengthLevel(strengthLevel);

            // Select appropriate attack bonus based on subtype
            int attackBonus = 0;
            switch (attackSubType)
            {
                case STAB:
                    attackBonus = equipment.stabAttack;
                    break;
                case SLASH:
                    attackBonus = equipment.slashAttack;
                    break;
                case CRUSH:
                    attackBonus = equipment.crushAttack;
                    break;
                default:
                    attackBonus = equipment.stabAttack;
            }
            profile.setAttackBonus(attackBonus);
            profile.setStrengthBonus(equipment.strengthBonus);
        }
        else if (combatType == CombatType.RANGED)
        {
            profile.setEffectiveAttackLevel(rangedLevel);
            profile.setEffectiveStrengthLevel(rangedLevel);
            profile.setAttackBonus(equipment.rangedAttack);
            profile.setRangedStrengthBonus(equipment.rangedStrengthBonus);
        }
        else if (combatType == CombatType.MAGIC)
        {
            profile.setEffectiveAttackLevel(magicLevel);
            profile.setEffectiveStrengthLevel(magicLevel);
            profile.setAttackBonus(equipment.magicAttack);
            profile.setMagicDamageBonus(equipment.magicDamageBonus);
            profile.setMaxHitBase(getSelectedSpellMaxHit());
        }

        return profile;
    }

    private int getSelectedSpellMaxHit()
    {
        int maxHit = getSpellMaxHit(client.getVarpValue(VarPlayerID.LASTCASTSPELL));
        if (maxHit == 0)
        {
            maxHit = getSpellMaxHit(client.getVarpValue(VarPlayerID.AUTOCAST_SPELL_OBJ));
        }
        if (maxHit == 0)
        {
            maxHit = getSpellMaxHit(client.getVarbitValue(VarbitID.AUTOCAST_SPELL));
        }

        return maxHit;
    }

    private int getSpellMaxHit(int spellId)
    {
        switch (spellId)
        {
            case SPELL_WIND_STRIKE:
                return 2;
            case SPELL_WATER_STRIKE:
                return 4;
            case SPELL_EARTH_STRIKE:
                return 6;
            case SPELL_FIRE_STRIKE:
                return 8;
            case SPELL_WIND_BOLT:
                return chaosGauntletsEquipped() ? 12 : 9;
            case SPELL_WATER_BOLT:
                return chaosGauntletsEquipped() ? 13 : 10;
            case SPELL_EARTH_BOLT:
                return chaosGauntletsEquipped() ? 14 : 11;
            case SPELL_FIRE_BOLT:
                return chaosGauntletsEquipped() ? 15 : 12;
            case SPELL_WIND_BLAST:
                return 13;
            case SPELL_WATER_BLAST:
                return 14;
            case SPELL_EARTH_BLAST:
                return 15;
            case SPELL_FIRE_BLAST:
                return 16;
            case SPELL_WIND_WAVE:
                return 17;
            case SPELL_WATER_WAVE:
                return 18;
            case SPELL_EARTH_WAVE:
                return 19;
            case SPELL_FIRE_WAVE:
                return 20;
            case SPELL_WIND_SURGE:
                return 21;
            case SPELL_WATER_SURGE:
                return 22;
            case SPELL_EARTH_SURGE:
                return 23;
            case SPELL_FIRE_SURGE:
                return 24;
            default:
                return 0;
        }
    }

    private boolean chaosGauntletsEquipped()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item gloves = equipment.getItem(EquipmentInventorySlot.GLOVES.getSlotIdx());
        return gloves != null && gloves.getId() == ItemID.CHAOS_GAUNTLETS;
    }

    /**
     * Estimates damage per second for display purposes only.
     *
     * Formula: (maxHit / 2) * hitChance / attackSpeed
     *   - maxHit / 2 approximates average damage per hit (uniform distribution 0..maxHit).
     *   - attackSpeed is assumed to be 2.4 seconds (4 ticks), a common weapon speed.
     *
     * This is a simplified estimate; actual DPS varies by weapon speed and
     * special attack usage.  The overlay labels it "DPS (est)" to reflect this.
     */
    private double calculateDPS(CombatResult result)
    {
        if (result.getMaxHit() <= 0 || result.getHitChance() <= 0)
        {
            return 0;
        }

        // Simplified DPS: (Max Hit / 2) * Hit Chance / Attack Speed
        // Average hit = Max Hit / 2 (assuming uniform distribution)
        // Attack speed assumed to be ~2.4 seconds (average)
        double averageHit = result.getMaxHit() / 2.0;
        double dps = (averageHit * result.getHitChance()) / 2.4;

        return Math.round(dps * 100.0) / 100.0; // Round to 2 decimals
    }
}
