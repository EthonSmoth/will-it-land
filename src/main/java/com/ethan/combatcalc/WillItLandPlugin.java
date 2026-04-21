package com.ethan.combatcalc;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "will-it-land",
        description = "Combat accuracy calculator - shows hit chance % vs your target NPC",
        tags = {"combat", "accuracy", "overlay", "pvm"}
)
public class WillItLandPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WillItLandOverlay overlay;

    @Inject
    private ConfigManager configManager;

    @Inject
    private AttackStyleResolver attackStyleResolver;

    @Inject
    private EquipmentStatCollector equipmentCollector;

    @Inject
    private CombatCalculator combatCalculator;

    @Inject
    private NpcStatsRepository npcStatsRepository;

    @Inject
    private PotionBoostDetector potionBoostDetector;

    @Inject
    private EquipmentSynergyDetector equipmentSynergyDetector;

    private CombatResult latestResult = new CombatResult();

    public CombatResult getLatestResult()
    {
        return latestResult;
    }

    public ConfigManager getConfigManager()
    {
        return configManager;
    }


    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        latestResult = new CombatResult();
    }

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
        if (configManager.getConfiguration("willitland", "debugMode") != null &&
            Boolean.parseBoolean(configManager.getConfiguration("willitland", "debugMode")))
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

    private CombatProfile buildPlayerProfile(CombatType combatType, AttackSubType attackSubType)
    {
        CombatProfile profile = new CombatProfile();
        profile.setCombatType(combatType);
        profile.setAttackSubType(attackSubType);

        // Get effective levels
        int attackLevel = client.getBoostedSkillLevel(Skill.ATTACK);
        int rangedLevel = client.getBoostedSkillLevel(Skill.RANGED);
        int magicLevel = client.getBoostedSkillLevel(Skill.MAGIC);
        int strengthLevel = client.getBoostedSkillLevel(Skill.STRENGTH);

        // Apply potion boosts if enabled
        String potionDetectionConfig = configManager.getConfiguration("willitland", "enablePotionDetection");
        boolean enablePotions = potionDetectionConfig == null || Boolean.parseBoolean(potionDetectionConfig);

        if (enablePotions)
        {
            attackLevel += potionBoostDetector.getAttackBoost();
            rangedLevel += potionBoostDetector.getRangedBoost();
            magicLevel += potionBoostDetector.getMagicBoost();
        }

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
