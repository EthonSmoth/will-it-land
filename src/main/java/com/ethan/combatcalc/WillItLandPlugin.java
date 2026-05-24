package com.ethan.combatcalc;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.eventbus.Subscribe;
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
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WillItLandOverlay overlay;

    @Inject
    private WillItLandConfig config;

    @Inject
    private AttackStyleResolver attackStyleResolver;

    @Inject
    private EquipmentStatCollector equipmentCollector;

    @Inject
    private CombatCalculator combatCalculator;

    @Inject
    private NpcStatsRepository npcStatsRepository;

    private CombatResult latestResult = new CombatResult();

    public CombatResult getLatestResult()
    {
        return latestResult;
    }

    public WillItLandConfig getConfig()
    {
        return config;
    }


    @Override
    protected void startUp()
    {
        // Register the overlay so it is drawn on screen.
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        // Remove the overlay and reset state so nothing lingers after the plugin is disabled.
        overlayManager.remove(overlay);
        latestResult = new CombatResult();
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
            return;
        }

        // Resolve combat type and subtype
        CombatType combatType = attackStyleResolver.resolveCombatType(client);
        AttackSubType attackSubType = attackStyleResolver.resolveAttackSubType(client, combatType);

        // Build player combat profile
        CombatProfile playerProfile = buildPlayerProfile(combatType, attackSubType);

        // Build NPC combat profile
        NpcCombatProfile npcProfile = npcStatsRepository.getNpcProfile(targetNpc.getName());

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
        }

        return profile;
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
