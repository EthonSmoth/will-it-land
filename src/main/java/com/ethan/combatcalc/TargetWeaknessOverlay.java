package com.ethan.combatcalc;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
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
import java.util.Collections;
import java.util.List;

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
    private final Client client;
    private final NpcStatsRepository npcStatsRepository;
    private final WeaknessAnalyzer weaknessAnalyzer;
    private final InventoryWeaponCollector inventoryWeaponCollector;

    @Inject
    public TargetWeaknessOverlay(WillItLandPlugin plugin,
                                 WillItLandConfig config,
                                 Client client,
                                 NpcStatsRepository npcStatsRepository,
                                 WeaknessAnalyzer weaknessAnalyzer,
                                 InventoryWeaponCollector inventoryWeaponCollector)
    {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.npcStatsRepository = npcStatsRepository;
        this.weaknessAnalyzer = weaknessAnalyzer;
        this.inventoryWeaponCollector = inventoryWeaponCollector;
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

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<WeaponInfo> candidates = inventoryWeaponCollector.collectCandidates();
        if (config.showAllNpcWeaknessOverlays())
        {
            for (NPC npc : client.getNpcs())
            {
                if (npc.getName() == null || !npcStatsRepository.hasNpcProfile(npc.getName()))
                {
                    continue;
                }
                renderNpcOverlay(graphics, npc, candidates);
            }
            return null;
        }

        renderNpcOverlay(graphics, plugin.getCurrentTargetNpc(), candidates);
        return null;
    }

    private void renderNpcOverlay(Graphics2D graphics, NPC target, List<WeaponInfo> candidates)
    {
        if (target == null || target.getName() == null)
        {
            return;
        }

        if (config.onlyShowWeaknessInCombat() && !isInvolvedInCombat(target))
        {
            return;
        }

        NpcCombatProfile profile = npcStatsRepository.getNpcProfile(target.getName());
        int combatLevel = resolveCombatLevel(target, profile);
        if (combatLevel < config.weaknessMinimumCombatLevel())
        {
            return;
        }

        WeaknessSummary summary = target == plugin.getCurrentTargetNpc() && plugin.getLatestWeaknessSummary() != null
                ? plugin.getLatestWeaknessSummary()
                : weaknessAnalyzer.analyze(profile, candidates == null ? Collections.emptyList() : candidates);
        if (summary == null)
        {
            return;
        }

        String threatLine = buildThreatLine(target, profile, combatLevel);
        String defenceLine = "DEF " + formatWeakness(summary) + " (" + summary.getDefensiveWeaknessValue() + ")";
        String weaponLine = "WEAP " + formatStyle(summary.getWeaponWeakness()) + " (" + summary.getWeaponWeaknessValue() + ")";
        String recommendLine = summary.hasRecommendation()
                ? "BEST " + abbreviate(summary.getRecommendedWeaponName(), 24) + " [" + formatStyle(summary.getRecommendedWeaponStyle()) + "]"
                : "BEST none in inventory";

        net.runelite.api.Point location = target.getCanvasTextLocation(
                graphics,
                threatLine,
                target.getLogicalHeight() + 55);
        if (location == null)
        {
            return;
        }

        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int width = Math.max(metrics.stringWidth(threatLine),
                Math.max(metrics.stringWidth(defenceLine),
                        Math.max(metrics.stringWidth(weaponLine), metrics.stringWidth(recommendLine)))) + PADDING_X * 2;
        int height = lineHeight * 4 + LINE_GAP * 3 + PADDING_Y * 2;
        int x = location.getX() - width / 2;
        int y = location.getY() - height;

        graphics.setColor(BACKGROUND);
        graphics.fillRoundRect(x, y, width, height, 8, 8);
        graphics.setColor(BORDER);
        graphics.drawRoundRect(x, y, width, height, 8, 8);

        int textX = x + PADDING_X;
        int baseline = y + PADDING_Y + metrics.getAscent();
        drawLine(graphics, threatLine, textX, baseline, Color.WHITE);
        drawLine(graphics, defenceLine, textX, baseline + lineHeight + LINE_GAP, DEFENCE_COLOR);
        drawLine(graphics, weaponLine, textX, baseline + (lineHeight + LINE_GAP) * 2, WEAPON_COLOR);
        drawLine(graphics, recommendLine, textX, baseline + (lineHeight + LINE_GAP) * 3, RECOMMEND_COLOR);
    }

    private boolean isInvolvedInCombat(NPC npc)
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            return false;
        }

        Actor playerTarget = localPlayer.getInteracting();
        Actor npcTarget = npc.getInteracting();
        return playerTarget == npc || npcTarget == localPlayer;
    }

    private String buildThreatLine(NPC npc, NpcCombatProfile profile, int combatLevel)
    {
        StringBuilder line = new StringBuilder(abbreviate(npc.getName(), 20));
        if (combatLevel > 0)
        {
            line.append(" L").append(combatLevel);
        }
        if (profile.getMaxHit() > 0)
        {
            line.append(" MAX ").append(profile.getMaxHit());
        }
        if (profile.isAggressive())
        {
            line.append(" AGG");
        }
        return line.toString();
    }

    private int resolveCombatLevel(NPC npc, NpcCombatProfile profile)
    {
        if (profile.getCombatLevel() > 0)
        {
            return profile.getCombatLevel();
        }

        return Math.max(0, npc.getCombatLevel());
    }

    private String formatWeakness(WeaknessSummary summary)
    {
        String label = summary.getWeaknessLabel();
        if (label == null || label.isEmpty())
        {
            label = formatStyle(summary.getDefensiveWeakness());
        }

        if ("wiki".equals(summary.getWeaknessSource()))
        {
            return label + "/wiki";
        }

        return label;
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
