package com.ethan.combatcalc;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import java.awt.Color;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Draws the in-game overlay panel.
 *
 * The overlay is registered/deregistered by WillItLandPlugin on startup/shutdown.
 * On every frame, render() pulls the latest CombatResult from the plugin and builds
 * a list of text rows to display.
 *
 * Each row is guarded by a config toggle so the player can choose exactly what
 * information they want to see (attack style, hit chance, max hit, DPS, NPC warning,
 * and a debug breakdown).
 *
 * No game state is read here — all data comes from the pre-computed CombatResult.
 */
public class WillItLandOverlay extends OverlayPanel
{
    private final WillItLandPlugin plugin;
    private final WillItLandConfig config;

    @Inject
    public WillItLandOverlay(WillItLandPlugin plugin, WillItLandConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        CombatResult result = plugin.getLatestResult();

        // If no valid combat type is set the player has no active NPC target — render nothing.
        if (result.getCombatType() == null || result.getCombatType() == CombatType.UNKNOWN)
        {
            return super.render(graphics);
        }

        // Set a semi-transparent background
        panelComponent.setBackgroundColor(new Color(0, 0, 0, 150)); // Semi-transparent black

        // Header with emoji/icon
        String headerIcon = getAttackStyleIcon(result.getAttackSubType());
        panelComponent.getChildren().add(LineComponent.builder()
                .left(headerIcon + " Will It Land?")
                .leftColor(Color.CYAN)
                .build());

        // Attack style line
        if (config.showAttackStyle())
        {
            String attackStyle = result.getAttackSubType() != null ? result.getAttackSubType().getDisplayName() : "Unknown";
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Style")
                    .right(attackStyle)
                    .rightColor(getStyleColor(result.getAttackSubType()))
                    .build());
        }

        // Hit chance with color coding
        if (config.showHitChance())
        {
            double hitChance = result.getHitChance();
            String hitChanceFormatted = result.formatHitChance();
            Color hitChanceColor = getHitChanceColor(hitChance);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Hit Chance")
                    .right(hitChanceFormatted)
                    .rightColor(hitChanceColor)
                    .build());
        }

        // Max hit
        if (config.showMaxHit() && result.getMaxHit() > 0)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Max Hit")
                    .right(String.valueOf(result.getMaxHit()))
                    .rightColor(new Color(255, 200, 0)) // Gold
                    .build());
        }

        // DPS
        if (config.showDPS() && result.getMaxHit() > 0)
        {
            double dps = calculateDPS(result);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("DPS (est)")
                    .right(String.format("%.2f", dps))
                    .rightColor(new Color(150, 200, 255)) // Light blue
                    .build());
        }

        // Show warning if NPC stats were not found
        if (config.showNpcWarning() && result.isNpcUnknown())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("⚠ NPC Unknown")
                    .leftColor(new Color(255, 165, 0)) // Orange
                    .build());
        }

        // Show debug info if enabled
        if (result.isDebugMode() && result.getDebugInfo() != null && !result.getDebugInfo().isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("")
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("--- Debug Info ---")
                    .leftColor(new Color(100, 100, 255)) // Light blue
                    .build());

            String[] debugLines = result.getDebugInfo().split("\n");
            for (String line : debugLines)
            {
                if (!line.isEmpty())
                {

                    panelComponent.getChildren().add(LineComponent.builder()
                            .left(line)
                            .leftColor(new Color(200, 200, 200)) // Gray
                            .build());
                }
            }
        }

        return super.render(graphics);
    }



    /**
     * Get a display icon for the attack style.
     */
    private String getAttackStyleIcon(AttackSubType attackSubType)
    {
        if (attackSubType == null)
        {
            return "⚔";
        }
        switch (attackSubType)
        {
            case STAB:
                return "🗡";
            case SLASH:
                return "⚔";
            case CRUSH:
                return "🔨";
            case RANGED:
                return "🏹";
            case MAGIC:
                return "✨";
            default:
                return "⚔";
        }
    }

    /**
     * Get color for attack style text.
     */
    private Color getStyleColor(AttackSubType attackSubType)
    {
        if (attackSubType == null)
        {
            return Color.WHITE;
        }
        switch (attackSubType)
        {
            case STAB:
                return new Color(200, 200, 255); // Light blue
            case SLASH:
                return new Color(255, 200, 100); // Orange
            case CRUSH:
                return new Color(255, 150, 150); // Light red
            case RANGED:
                return new Color(150, 255, 150); // Light green
            case MAGIC:
                return new Color(255, 150, 255); // Light magenta
            default:
                return Color.WHITE;
        }
    }

    /**
     * Calculate estimated DPS.
     */
    private double calculateDPS(CombatResult result)
    {
        if (result.getMaxHit() <= 0 || result.getHitChance() <= 0)
        {
            return 0;
        }

        double averageHit = result.getMaxHit() / 2.0;
        double dps = (averageHit * result.getHitChance()) / 2.4;
        return Math.round(dps * 100.0) / 100.0;
    }

    /**
     * Get color for hit chance based on the percentage.
     * Green (high chance) -> Yellow (medium) -> Red (low chance)
     */
    private Color getHitChanceColor(double hitChance)
    {
        if (hitChance >= 0.80)
        {
            return new Color(0, 255, 0); // Green
        }
        else if (hitChance >= 0.50)
        {
            return new Color(255, 255, 0); // Yellow
        }
        else if (hitChance >= 0.25)
        {
            return new Color(255, 165, 0); // Orange
        }
        else
        {
            return new Color(255, 0, 0); // Red
        }
    }
}
