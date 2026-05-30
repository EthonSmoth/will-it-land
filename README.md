# Will It Land?

A RuneLite plugin for Old School RuneScape that displays live PvM hit chance, max hit, and estimated DPS against the NPC you are currently targeting.

The current implementation is focused on transparent, OSRS-style combat rolls for melee, ranged, and magic, with regression tests around the recent max-hit and modifier fixes.

## Current Status

- Main plugin wiring: `WillItLandPlugin` registers the overlay, reads the local player target, resolves combat type/subtype, collects gear stats, loads NPC stats, and stores the latest `CombatResult`.
- Overlay wiring: `WillItLandOverlay` reads `latestResult` and honors display toggles for attack style, hit chance, max hit, DPS, NPC warnings, debug info, and weapon intel.
- Target weakness wiring: `TargetWeaknessOverlay` renders the current NPC's defensive weakness, melee weapon weakness, and best matching inventory/equipped weapon above the target.
- Config wiring: prayer bonuses, special modifiers, and equipment set effects are now gated by their feature toggles.
- Test status: `./gradlew.bat test` passes locally.
- Git status at this README update: target weakness overlay, inventory weapon recommendation, and documentation changes are ready for the next commit.

## Features

### Combat Overlay

- Shows while your player is targeting an NPC.
- Displays combat style, hit chance, max hit, and estimated DPS when enabled.
- Includes optional weapon intel rows for weapon bonuses, speed, range, ammo, special attack notes, passive effects, and raw offensive bonuses.
- Includes an optional above-target weakness overlay showing `DEF`, `WEAP`, and `BEST` recommendation lines.
- Can show debug roll details for troubleshooting.
- Shows a warning when the target NPC is missing from the local stats database.

### Accuracy Calculations

The calculator uses OSRS-style PvM roll mechanics:

- Offensive roll: `effectiveLevel * (attackBonus + 64)`
- NPC defensive roll: `(npcLevel + 9) * (defenceBonus + 64)`
- Magic defence uses the NPC magic level and magic defence bonus.
- Hit chance uses the standard two-branch roll comparison formula.

### Max Hit and DPS

- Melee max hit uses effective Strength, melee strength bonus, prayer strength modifiers, and damage modifiers.
- Ranged max hit uses effective Ranged strength, ranged strength bonus, ranged prayer strength modifiers, and damage modifiers.
- Magic max hit uses the selected/autocast standard spell base max hit, magic damage gear, and damage modifiers.
- Fire Bolt is covered by regression tests against Gemstone Crab.
- DPS is an estimate using average hit and a fixed 2.4 second attack speed assumption.

### Magic Spell Support

Standard spell max hits currently wired:

- Strike spells
- Bolt spells, including Chaos gauntlets for bolt spell max hits
- Blast spells
- Wave spells
- Surge spells

Powered staves and special-case spell systems are not fully modeled yet.

### Modifiers

Implemented modifier support includes:

- Offensive prayers for melee, ranged, and magic.
- Strength prayer scaling for melee max hit.
- Ranged strength prayer scaling for ranged max hit.
- Salve amulet variants against undead only.
- Salve imbued/enhanced behavior for melee, ranged, and magic.
- Magic amulet accuracy bonus for configured magic amulets.
- Equipment set effect hook for Void and Obsidian via `EquipmentSynergyDetector`.

Known limitation: Slayer helmet task detection is still a placeholder and does not apply until task detection is implemented.

### Target Weakness Overlay

When enabled, the plugin renders a compact label above the NPC you are fighting:

- `DEF`: the target's lowest overall defensive stat across stab, slash, crush, ranged, and magic.
- `WEAP`: the target's lowest melee weapon defence across stab, slash, and crush.
- `BEST`: the best matching weapon found in your equipped weapon slot or inventory.

The recommendation uses RuneLite item stats and scores weapons against the target's weakest style. If the overall weakness is magic or ranged but no matching inventory weapon is found, it falls back to the best melee weapon for the target's melee weapon weakness.

Known limitation: recommendations are stat-based and do not yet account for ammo availability, special attacks, monster-specific passives, or exact attack speed DPS.

### NPC Data

NPC stats are loaded from `src/main/resources/npc_stats.json`.

Current bundled entries include:

- Gemstone Crab
- Chicken
- Cow
- Goblin
- Giant Spider
- Skeleton

Unknown NPCs use fallback/default stats and can show an overlay warning.

## Project Structure

```text
src/main/java/com/ethan/combatcalc/
+-- WillItLandPlugin.java          Main plugin entry and tick pipeline
+-- WillItLandOverlay.java         Overlay renderer
+-- TargetWeaknessOverlay.java     Above-target weakness/recommendation renderer
+-- WillItLandConfig.java          RuneLite config toggles
+-- AttackStyleResolver.java       Combat type/subtype resolver
+-- EquipmentStatCollector.java    Equipment bonus collector
+-- CombatCalculator.java          Hit chance and max-hit engine
+-- CombatModifier.java            Prayer, Salve, special, and set multipliers
+-- EquipmentSynergyDetector.java  Void/Obsidian set detection
+-- NpcStatsRepository.java        NPC stats loader
+-- NpcCombatProfile.java          NPC combat stat model
+-- CombatProfile.java             Player combat stat model
+-- CombatResult.java              Calculation result model
+-- WeaponInfoCollector.java       Weapon stat/intel collector
+-- InventoryWeaponCollector.java  Inventory/equipment weapon candidates
+-- WeaknessAnalyzer.java          NPC weakness and weapon recommendation logic
+-- WeaknessSummary.java           Weakness overlay data model
+-- WeaponInfo.java                Weapon info model
+-- WeaponIntelDatabase.java       Known weapon notes and effects

src/main/resources/
+-- npc_stats.json                 Bundled NPC combat data

src/test/java/com/ethan/combatcalc/
+-- CombatCalculatorTest.java      Formula and crab regression tests
+-- CombatModifierTest.java        Prayer/Salve/config toggle tests
+-- WeaknessAnalyzerTest.java      Defensive weakness and recommendation tests
+-- WeaponAttackStylesTest.java    Attack-style mapping tests
+-- WeaponInfoTest.java            Weapon info formatting tests
+-- WeaponIntelDatabaseTest.java   Weapon intel tests
+-- WillItLandPluginTest.java      Config provider wiring test
```

## Testing

Current test coverage includes:

- Hit chance clamping.
- Melee, ranged, and magic accuracy.
- OSRS-style melee max hit regression for unarmed/kick Gemstone Crab.
- Fire Bolt max hit and accuracy against Gemstone Crab.
- Magic damage bonus application.
- Salve/Salve(ei) undead-only behavior.
- Prayer multipliers for melee, ranged, and magic.
- Config toggles for prayer and special modifiers.
- Defensive weakness detection and inventory weapon recommendations.
- Weapon info and attack-style mapping.

Run all tests:

```bash
./gradlew.bat test
```

On macOS/Linux:

```bash
./gradlew test
```

## Building and Running

Requirements:

- Java 11+
- Gradle wrapper included in the repo

Build:

```bash
./gradlew.bat build
```

Run in RuneLite developer mode:

```bash
./gradlew.bat run
```

The Gradle `run` task uses `com.ethan.combatcalc.CombatCalcPluginTest` as the local RuneLite plugin test entry point.

## Installation

Manual local testing:

1. Build the plugin.
2. Run through the Gradle `run` task for developer-mode testing.
3. For normal RuneLite plugin distribution, package/release according to RuneLite external plugin requirements.

This repository is not currently documented as published on Plugin Hub.

## Accuracy Notes

The calculator is intentionally conservative and test-backed for the currently wired cases. Some OSRS combat mechanics are still outside the current model, including:

- Powered staff formulas.
- Spell-specific special cases beyond standard spell base damage and Chaos gauntlets.
- Slayer task detection for Slayer helmet bonuses.
- Exact weapon attack speeds for DPS.
- Recommendation scoring that accounts for ammo, special attacks, monster-specific passives, or exact DPS.
- Large NPC database coverage beyond the bundled sample data.

## Disclaimer

This plugin is not affiliated with Jagex Ltd or RuneLite.

OSRS combat formulas are based on community-verified mechanics from the OSRS Wiki and related tooling.
