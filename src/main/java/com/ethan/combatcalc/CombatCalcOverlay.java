package com.ethan.combatcalc;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class CombatCalcOverlay extends OverlayPanel
{
    private final CombatCalcPlugin plugin;

    @Inject
    public CombatCalcOverlay(CombatCalcPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        CombatResult result = plugin.getLatestResult();

        // Header
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Combat Calc")
                .build());

        // Combat type and subtype
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Type")
                .right(result.getCombatType() != null ? result.getCombatType().getDisplayName() : "None")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Style")
                .right(result.getAttackSubType() != null ? result.getAttackSubType().getDisplayName() : "Unknown")
                .build());

        // Attack and Defence bonuses
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Attack Bonus")
                .right(String.valueOf(result.getOffensiveRoll()))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Defence Bonus")
                .right(String.valueOf(result.getDefensiveRoll()))
                .build());

        // Hit chance
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Hit Chance")
                .right(result.formatHitChance())
                .build());

        return super.render(graphics);
    }
}