# Will It Land?

A RuneLite plugin for Old School RuneScape that calculates real-time combat hit chance against your current target.

It helps you understand your actual accuracy in PvM by displaying a live percentage based on OSRS combat formulas, gear, prayers, and modifiers.

---

## ✨ Features

### 🎯 Real-time Hit Chance
Displays your current chance to hit the targeted NPC based on:
- Attack style (Melee, Ranged, Magic)
- Your combat stats
- NPC defensive stats
- Equipment bonuses

### 📊 Combat Overlay
- Clean in-game overlay
- Colour-coded accuracy indicator:
  - 🟢 High chance
  - 🟡 Moderate chance
  - 🔴 Low chance

### ⚔️ OSRS-Accurate Calculations
Implements official-style combat formulas for:
- Melee accuracy
- Ranged accuracy
- Magic accuracy

### 🧠 Modifier Support
Fully accounts for in-game boosts and effects:

**Equipment**
- Salve Amulet (+ variants) vs undead
- Slayer Helmet (+ imbued) on task
- Void Knight set bonuses
- Obsidian set effects (melee)

**Prayers**
- Melee: Clarity of Thought → Improved Reflexes
- Ranged: Sharp Eye → Hawk Eye → Eagle Eye
- Magic: Mystic Will → Mystic Lore → Mystic Might

**Potions**
- Attack / Strength / Ranging / Magic potions
- Bastion potion
- Imbued Heart magic boost

### 📦 NPC Database
- Built-in NPC stats database
- Warning shown if NPC is missing data

### 📈 Extra Insights
- Max hit estimation
- DPS estimation
- Debug mode (detailed roll breakdowns)

---

## 📥 Installation

### Plugin Hub (Recommended)
1. Open RuneLite
2. Go to **Configuration → Plugin Hub**
3. Search: `will-it-land`
4. Click **Install**

---

### Manual Installation
1. Download the latest release JAR from the Releases page
2. Move it to your RuneLite plugins folder:
   - **Windows:** `%USERPROFILE%\.runelite\plugins\`
   - **macOS:** `~/Library/Application Support/RuneLite/plugins/`
   - **Linux:** `~/.runelite/plugins/`
3. Restart RuneLite

---

## 🎮 Usage

1. Enable **Will It Land** in RuneLite plugins
2. Target an NPC in-game
3. View overlay in the top-left corner showing:
   - Attack style icon
   - Hit chance percentage
   - Colour-coded accuracy indicator

The overlay only appears while actively targeting an NPC.

---

## 🛠 Building from Source

### Requirements
- Java 11+
- Gradle 7+

### Build Steps

```bash
git clone https://github.com/yourusername/will-it-land.git
cd will-it-land
./gradlew build

Run Tests
./gradlew test
Run Locally
./gradlew run
📁 Project Structure
src/main/java/com/ethan/combatcalc/
├── WillItLandPlugin.java        Main plugin entry
├── WillItLandOverlay.java       UI overlay renderer
├── CombatCalculator.java        Hit chance engine
├── NpcStatsRepository.java      NPC stats loader
src/main/resources/
└── npc_stats.json               NPC combat data
🧪 Testing

Current test coverage includes:

Hit chance clamping (0–100%)
Melee / Ranged / Magic accuracy
Equipment bonuses
Prayer modifiers
Edge-case handling

All tests passing ✅

./gradlew test
📊 Accuracy Model

Uses OSRS-style combat rolls:

Attack Roll:
(Attack Level + 64) × (Attack Bonus + 64)
Defence Roll:
(Defence Level + 64) × (Defence Bonus + 64)
Hit chance derived from standard roll comparison formula

All modifiers are applied before final evaluation.

⚠️ Disclaimer

This plugin is not affiliated with Jagex Ltd or RuneLite.

Use at your own risk.

🙌 Credits
Developed by Ethan
Built for the RuneLite plugin framework
OSRS combat formulas based on community-verified mechanics


📬 Support

If something breaks or behaves incorrectly:

Open an issue on GitHub
Include RuneLite version + reproduction steps



