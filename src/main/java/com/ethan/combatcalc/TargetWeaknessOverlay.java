package com.ethan.combatcalc;

import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class TargetWeaknessOverlay extends Overlay
{
    private static final Color BACKGROUND = new Color(0, 0, 0, 170);
    private static final Color BORDER = new Color(255, 220, 120, 210);
    private static final Color DEFENCE_COLOR = new Color(255, 220, 120);
    private static final Color WEAPON_COLOR = new Color(180, 220, 255);
    private static final Color RECOMMEND_COLOR = new Color(180, 255, 180);
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final int LINE_GAP = 2;

    private final WillItLandPlugin plugin;
    private final WillItLandConfig config;

    @Inject
    public TargetWeaknessOverlay(WillItLandPlugin plugin, WillItLandConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showTargetWeaknessOverlay())
        {
            return null;
        }

        NPC target = plugin.getCurrentTargetNpc();
        WeaknessSummary summary = plugin.getLatestWeaknessSummary();
        if (target == null || summary == null)
        {
            return null;
        }

        String defenceLine = "DEF " + formatStyle(summary.getDefensiveWeakness()) + " (" + summary.getDefensiveWeaknessValue() + ")";
        String weaponLine = "WEAP " + formatStyle(summary.getWeaponWeakness()) + " (" + summary.getWeaponWeaknessValue() + ")";
        String recommendLine = summary.hasRecommendation()
                ? "BEST " + abbreviate(summary.getRecommendedWeaponName(), 24) + " [" + formatStyle(summary.getRecommendedWeaponStyle()) + "]"
                : "BEST none in inventory";

        net.runelite.api.Point location = target.getCanvasTextLocation(
                graphics,
                defenceLine,
                target.getLogicalHeight() + 55);
        if (location == null)
        {
            return null;
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int width = Math.max(metrics.stringWidth(defenceLine),
                Math.max(metrics.stringWidth(weaponLine), metrics.stringWidth(recommendLine))) + PADDING_X * 2;
        int height = lineHeight * 3 + LINE_GAP * 2 + PADDING_Y * 2;
        int x = location.getX() - width / 2;
        int y = location.getY() - height;

        graphics.setColor(BACKGROUND);
        graphics.fillRoundRect(x, y, width, height, 8, 8);
        graphics.setColor(BORDER);
        graphics.drawRoundRect(x, y, width, height, 8, 8);

        int textX = x + PADDING_X;
        int baseline = y + PADDING_Y + metrics.getAscent();
        drawLine(graphics, defenceLine, textX, baseline, DEFENCE_COLOR);
        drawLine(graphics, weaponLine, textX, baseline + lineHeight + LINE_GAP, WEAPON_COLOR);
        drawLine(graphics, recommendLine, textX, baseline + (lineHeight + LINE_GAP) * 2, RECOMMEND_COLOR);

        return null;
    }

    private void drawLine(Graphics2D graphics, String text, int x, int y, Color color)
    {
        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    private String formatStyle(AttackSubType attackSubType)
    {
        if (attackSubType == null)
        {
            return "Unknown";
        }

        return attackSubType.getDisplayName();
    }

    private String abbreviate(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}
