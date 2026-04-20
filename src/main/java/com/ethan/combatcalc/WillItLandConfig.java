package com.ethan.combatcalc;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

/**
 * Configuration for Will It Land plugin.
 * Allows players to customize what information is displayed.
 */
@ConfigGroup("willitland")
public interface WillItLandConfig extends Config
{
    @ConfigSection(
            name = "Display Settings",
            description = "Configure what information to display",
            position = 0
    )
    String displaySection = "display";

    @ConfigItem(
            keyName = "showHitChance",
            name = "Show Hit Chance",
            description = "Display the hit chance percentage",
            section = displaySection,
            position = 0
    )
    default boolean showHitChance()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showAttackStyle",
            name = "Show Attack Style",
            description = "Display the current attack style with icon",
            section = displaySection,
            position = 1
    )
    default boolean showAttackStyle()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showMaxHit",
            name = "Show Max Hit",
            description = "Display your maximum possible hit damage",
            section = displaySection,
            position = 2
    )
    default boolean showMaxHit()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showDPS",
            name = "Show DPS",
            description = "Display estimated damage per second",
            section = displaySection,
            position = 3
    )
    default boolean showDPS()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showOffensiveRoll",
            name = "Show Offensive Roll",
            description = "Display your offensive roll in debug mode",
            section = displaySection,
            position = 4
    )
    default boolean showOffensiveRoll()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showDefensiveRoll",
            name = "Show Defensive Roll",
            description = "Display NPC's defensive roll in debug mode",
            section = displaySection,
            position = 5
    )
    default boolean showDefensiveRoll()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showModifiers",
            name = "Show Active Modifiers",
            description = "Display active modifiers (prayers, equipment, potions)",
            section = displaySection,
            position = 6
    )
    default boolean showModifiers()
    {
        return false;
    }

    @ConfigSection(
            name = "Debug Settings",
            description = "Advanced debugging options",
            position = 1
    )
    String debugSection = "debug";

    @ConfigItem(
            keyName = "debugMode",
            name = "Enable Debug Mode",
            description = "Show detailed calculation breakdown",
            section = debugSection,
            position = 0
    )
    default boolean debugMode()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showNpcWarning",
            name = "Show NPC Unknown Warning",
            description = "Display warning when NPC stats not found",
            section = debugSection,
            position = 1
    )
    default boolean showNpcWarning()
    {
        return true;
    }

    @ConfigSection(
            name = "Visual Settings",
            description = "Customize appearance",
            position = 2
    )
    String visualSection = "visual";

    @ConfigItem(
            keyName = "colorHighAccuracy",
            name = "Green Threshold (%)",
            description = "Hit chance threshold for green color (0-100)",
            section = visualSection,
            position = 0
    )
    default int colorHighAccuracy()
    {
        return 80;
    }

    @ConfigItem(
            keyName = "colorMediumAccuracy",
            name = "Yellow Threshold (%)",
            description = "Hit chance threshold for yellow color (0-100)",
            section = visualSection,
            position = 1
    )
    default int colorMediumAccuracy()
    {
        return 50;
    }

    @ConfigItem(
            keyName = "colorLowAccuracy",
            name = "Orange Threshold (%)",
            description = "Hit chance threshold for orange color (0-100)",
            section = visualSection,
            position = 2
    )
    default int colorLowAccuracy()
    {
        return 25;
    }

    @ConfigSection(
            name = "Feature Toggles",
            description = "Enable/disable specific features",
            position = 3
    )
    String featureSection = "features";

    @ConfigItem(
            keyName = "enablePotionDetection",
            name = "Enable Potion Detection",
            description = "Detect and apply potion boosts",
            section = featureSection,
            position = 0
    )
    default boolean enablePotionDetection()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enableEquipmentSets",
            name = "Enable Equipment Set Effects",
            description = "Detect and apply equipment set bonuses",
            section = featureSection,
            position = 1
    )
    default boolean enableEquipmentSets()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enablePrayerBonuses",
            name = "Enable Prayer Bonuses",
            description = "Apply active prayer accuracy boosts",
            section = featureSection,
            position = 2
    )
    default boolean enablePrayerBonuses()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enableSpecialModifiers",
            name = "Enable Special Modifiers",
            description = "Apply Salve amulet, Slayer helm, etc.",
            section = featureSection,
            position = 3
    )
    default boolean enableSpecialModifiers()
    {
        return true;
    }
}

