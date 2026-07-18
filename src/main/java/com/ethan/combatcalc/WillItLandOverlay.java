package com.ethan.combatcalc;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
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
    private final TooltipManager tooltipManager;

    @Inject
    public WillItLandOverlay(WillItLandPlugin plugin, WillItLandConfig config, TooltipManager tooltipManager)
    {
        this.plugin = plugin;
        this.config = config;
        this.tooltipManager = tooltipManager;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
    }

    public WillItLandOverlay(WillItLandPlugin plugin, WillItLandConfig config)
    {
        this(plugin, config, null);
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
            double dps = result.getEstimatedDps();
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("DPS (est)")
                    .right(String.format("%.2f", dps))
                    .rightColor(new Color(150, 200, 255)) // Light blue
                    .build());
        }

        if (config.showWeaponIntel())
        {
            renderWeaponIntel(result.getWeaponInfo());
        }

        if (config.showNpcThreatIntel())
        {
            renderNpcThreatIntel(plugin.getLatestNpcProfile(), plugin.getLatestWeaknessSummary());
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

    private void renderWeaponIntel(WeaponInfo weaponInfo)
    {
        if (weaponInfo == null || !weaponInfo.hasWeapon())
        {
            return;
        }

        if (plugin != null && plugin.isShiftDown() && tooltipManager != null)
        {
            String tooltip = weaponInfo.formatShiftTooltip();
            if (!tooltip.isEmpty())
            {
                tooltipManager.add(new Tooltip(tooltip));
            }
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Weapon")
                .right(weaponInfo.getWeaponName())
                .rightColor(new Color(255, 220, 120))
                .build());

        String relevantBonuses = weaponInfo.formatRelevantBonuses();
        if (!relevantBonuses.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bonuses")
                    .right(relevantBonuses)
                    .rightColor(new Color(220, 220, 220))
                    .build());
        }

        String activeStyle = weaponInfo.formatActiveStyleSummary();
        if (!activeStyle.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Mode")
                    .right(activeStyle)
                    .rightColor(new Color(180, 220, 255))
                    .build());
        }

        if (config.showWeaponSpeed())
        {
            String speed = weaponInfo.formatAttackSpeed();
            if (!speed.isEmpty())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Speed")
                        .right(speed)
                        .rightColor(new Color(180, 220, 255))
                        .build());
            }
        }

        if (config.showWeaponRange())
        {
            String range = weaponInfo.formatRange();
            if (!range.isEmpty())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Range")
                        .right(range)
                        .rightColor(new Color(180, 255, 180))
                        .build());
            }
        }

        if (config.showWeaponAmmo() && weaponInfo.hasAmmo())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Ammo")
                    .right(weaponInfo.getAmmoName())
                    .rightColor(new Color(200, 255, 200))
                    .build());
        }

        if (config.showWeaponSpecialAttack() && weaponInfo.hasSpecialAttack())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Spec")
                    .right(weaponInfo.getSpecialAttack().formatSummary())
                    .rightColor(new Color(255, 180, 180))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Energy")
                    .right(weaponInfo.getSpecialEnergyPercent() + "%")
                    .rightColor(new Color(255, 220, 160))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Spec note")
                    .right(weaponInfo.getSpecialAttack().getDescription())
                    .rightColor(new Color(220, 220, 220))
                    .build());
        }

        if (config.showWeaponPassiveEffects())
        {
            for (String passiveEffect : weaponInfo.getPassiveEffects())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Effect")
                        .right(passiveEffect)
                        .rightColor(new Color(220, 220, 220))
                        .build());
            }
            for (String note : weaponInfo.getNotes())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Note")
                        .right(note)
                        .rightColor(new Color(220, 220, 220))
                        .build());
            }
        }

        if (config.showWeaponRawBonuses())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Raw atk")
                    .right(weaponInfo.formatAllOffensiveBonuses())
                    .rightColor(new Color(190, 190, 190))
                    .build());
        }
    }

    private void renderNpcThreatIntel(NpcCombatProfile profile, WeaknessSummary summary)
    {
        if (profile == null && summary == null)
        {
            return;
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("")
                .build());

        if (profile != null)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("NPC")
                    .right(profile.getNpcName() == null ? "Unknown" : profile.getNpcName())
                    .rightColor(new Color(255, 220, 120))
                    .build());

            if (profile.getCombatLevel() > 0)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("NPC level")
                        .right(String.valueOf(profile.getCombatLevel()))
                        .rightColor(new Color(220, 220, 220))
                        .build());
            }

            if (profile.getMaxHit() > 0)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("NPC max")
                        .right(String.valueOf(profile.getMaxHit()))
                        .rightColor(new Color(255, 180, 180))
                        .build());
            }

            if (profile.isAggressive())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Aggressive")
                        .right("yes")
                        .rightColor(new Color(255, 180, 120))
                        .build());
            }
        }

        if (summary != null)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Weakness")
                    .right(formatWeakness(summary))
                    .rightColor(new Color(255, 220, 120))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Weapon weak")
                    .right(summary.getWeaponWeakness().getDisplayName())
                    .rightColor(new Color(180, 220, 255))
                    .build());

            if (summary.hasRecommendation())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Best inv")
                        .right(summary.getRecommendedWeaponName())
                        .rightColor(new Color(180, 255, 180))
                        .build());
            }
        }
    }

    private String formatWeakness(WeaknessSummary summary)
    {
        String label = summary.getWeaknessLabel();
        if (label == null || label.isEmpty())
        {
            label = summary.getDefensiveWeakness().getDisplayName();
        }
        return label + " (" + summary.getWeaknessSource() + ")";
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
     * Get color for hit chance based on the percentage.
     * Green (high chance) -> Yellow (medium) -> Red (low chance)
     */
    private Color getHitChanceColor(double hitChance)
    {
        double highThreshold = config.colorHighAccuracy() / 100.0;
        double mediumThreshold = config.colorMediumAccuracy() / 100.0;
        double lowThreshold = config.colorLowAccuracy() / 100.0;

        if (hitChance >= highThreshold)
        {
            return new Color(0, 255, 0); // Green
        }
        else if (hitChance >= mediumThreshold)
        {
            return new Color(255, 255, 0); // Yellow
        }
        else if (hitChance >= lowThreshold)
        {
            return new Color(255, 165, 0); // Orange
        }
        else
        {
            return new Color(255, 0, 0); // Red
        }
    }
}
