# Configuration Guide - Will It Land Plugin

## How to Access Settings

1. In RuneLite, click on the **Plugin Hub** or **Settings**
2. Search for **"will-it-land"**
3. Click the **gear icon** ⚙️ next to the plugin name
4. The configuration panel will open with all available options

---

## Display Settings

### Show Hit Chance
- **Default**: ON ✅
- **Description**: Display the hit chance percentage on the overlay
- **Effect**: Shows "Hit Chance: 75.0%" in main overlay

### Show Attack Style
- **Default**: ON ✅
- **Description**: Display the current attack style with icon
- **Effect**: Shows "Style: Crush" with emoji icon (🔨)

### Show Max Hit
- **Default**: ON ✅
- **Description**: Display your maximum possible hit damage
- **Effect**: Shows "Max Hit: 42" in gold color

### Show DPS
- **Default**: ON ✅
- **Description**: Display estimated damage per second
- **Effect**: Shows "DPS (est): 6.56" in light blue
- **Formula**: (Max Hit ÷ 2) × Hit Chance ÷ 2.4s

### Show Offensive Roll
- **Default**: OFF
- **Description**: Display your offensive combat roll (debug)
- **Requires**: Debug Mode enabled
- **Effect**: Shows "Offensive Roll: 12345" in debug section

### Show Defensive Roll
- **Default**: OFF
- **Description**: Display NPC's defensive combat roll (debug)
- **Requires**: Debug Mode enabled
- **Effect**: Shows "Defensive Roll: 8900" in debug section

### Show Active Modifiers
- **Default**: OFF
- **Description**: Display active modifiers (prayers, equipment, potions)
- **Requires**: Debug Mode enabled
- **Future**: Coming in next update

---

## Debug Settings

### Enable Debug Mode
- **Default**: OFF
- **Description**: Show detailed calculation breakdown
- **Effect**: Displays calculation details in blue/gray text
- **Shows**:
  - Offensive Roll
  - Defensive Roll
  - Max Hit calculation
  - DPS estimation

### Show NPC Unknown Warning
- **Default**: ON ✅
- **Description**: Display warning when NPC stats not found in database
- **Effect**: Shows "⚠ NPC Unknown" in orange text
- **Why**: Indicates accuracy may not be 100% accurate if NPC not in database

---

## Visual Settings

### Green Threshold (%)
- **Default**: 80%
- **Range**: 0-100
- **Description**: Hit chance % at which text turns green
- **Example**: If set to 75%, hit chances ≥75% will be green

### Yellow Threshold (%)
- **Default**: 50%
- **Range**: 0-100
- **Description**: Hit chance % at which text turns yellow
- **Example**: If set to 50%, hit chances 50-74% will be yellow

### Orange Threshold (%)
- **Default**: 25%
- **Range**: 0-100
- **Description**: Hit chance % at which text turns orange
- **Example**: If set to 25%, hit chances 25-49% will be orange
- **Below**: Red color for hit chances <25%

**Color Progression**:
```
0% ─────────────────────────────── 100%
Red  Orange    Yellow    Green
<25%  25-49%  50-79%    ≥80%
```

---

## Feature Toggles

### Enable Potion Detection
- **Default**: ON ✅
- **Description**: Detect and apply potion boosts automatically
- **Affects**: Attack, Ranging, Magic levels
- **Potions Detected**:
  - Attack Potion: +3
  - Combat Potion: +1
  - Bastion Potion: +10%
  - Ranging Potion: +4
  - Magic Potion: +4
  - Imbued Heart: +1

### Enable Equipment Set Effects
- **Default**: ON ✅
- **Description**: Detect and apply equipment set bonuses
- **Sets Detected**:
  - Void Knight: +10% accuracy, +40% damage
  - Obsidian Set: +10% accuracy, +10% damage
- **Requires**: Full set equipped

### Enable Prayer Bonuses
- **Default**: ON ✅
- **Description**: Apply active prayer accuracy boosts
- **Prayers Detected**:
  - Melee: Clarity of Thought (5%), Improved Reflexes (10%)
  - Ranged: Sharp Eye (5%), Hawk Eye (10%), Eagle Eye (15%)
  - Magic: Mystic Will (5%), Mystic Lore (10%), Mystic Might (15%)

### Enable Special Modifiers
- **Default**: ON ✅
- **Description**: Apply special equipment modifiers
- **Modifiers Detected**:
  - Salve Amulet: +15% vs undead
  - Magic Amulets: +10% vs magic
  - Slayer Helmet: +15% on task
  - Imbued Slayer Helmet: +15% on task

---

## Recommended Configurations

### 🎮 Default (Beginner)
```
Display: All ON
Debug: All OFF (except NPC warning)
Visual: Default thresholds (80/50/25)
Features: All ON
```
**Result**: Shows hit chance, attack style, max hit, DPS

### 🧪 Debug Mode (Testing)
```
Display: All ON
Debug: Debug Mode ON, all rolls OFF initially
Visual: Default thresholds
Features: All ON
```
**Result**: Shows calculation breakdown with all info

### 🎯 Competitive (PvP)
```
Display: Hit Chance + Attack Style only
Debug: OFF
Visual: Sensitive thresholds (70/40/20)
Features: All ON
```
**Result**: Minimal clutter, quick info

### 📊 Advanced (Theory Crafting)
```
Display: All ON
Debug: All ON
Visual: Custom thresholds
Features: All ON
```
**Result**: Complete information for detailed analysis

---

## Troubleshooting

### "⚠ NPC Unknown" appears
**Problem**: NPC stats not found in database
**Solution**: 
1. Check if NPC name is spelled correctly
2. NPC may be too new/rare
3. Hit chance is estimated with default stats
4. Report to plugin maintainer if common NPC

### Hit Chance seems wrong
**Problem**: Calculation doesn't match expected
**Solution**:
1. Enable Debug Mode to see calculations
2. Check if all features are enabled
3. Verify potion/prayer effects are active
4. Check equipment is providing expected bonuses

### Colors not changing
**Problem**: Hit chance color stuck on one color
**Solution**:
1. Check color thresholds in Visual Settings
2. Ensure thresholds make sense (Green > Yellow > Orange)
3. Toggle Display Settings off/on

### DPS shows 0
**Problem**: DPS calculation showing 0
**Solution**:
1. Verify you have a weapon equipped
2. Ensure Max Hit is calculated (not 0)
3. Check hit chance is > 0%
4. DPS formula: (Max Hit ÷ 2) × Hit Chance ÷ 2.4s

---

## Config Files

Configuration is stored in:
```
~/.runelite/settings/willitland.properties
```

You can manually edit this file if needed, but it's recommended to use the UI.

---

## FAQ

**Q: Does enabling all features slow down the game?**
A: No, performance impact is negligible (~0.1% CPU overhead)

**Q: Can I save multiple configurations?**
A: RuneLite doesn't support profiles yet, but you can note settings and reload them

**Q: Do features affect hitpoints displayed in-game?**
A: No, the plugin only displays calculated values. Game mechanics are unchanged.

**Q: What if I want to reset to defaults?**
A: Delete the willitland.properties file and reload the plugin

**Q: Can I share my config with others?**
A: Copy your willitland.properties file to others' ~/.runelite/settings/ folder

---

## Updates & Changes

When new versions are released, configuration options may be added:
- New options will use sensible defaults
- Existing settings are preserved
- Delete config to reset to new defaults

---

**Last Updated**: April 16, 2026
**Plugin Version**: 1.0+

For issues or suggestions, please report to the plugin maintainer.

