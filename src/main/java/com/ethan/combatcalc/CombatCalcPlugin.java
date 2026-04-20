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
        name = "Combat Calc Overlay",
        description = "Shows combat info",
        tags = {"combat", "overlay"}
)
public class CombatCalcPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CombatCalcOverlay overlay;

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
}