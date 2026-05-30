# Will It Land?

A RuneLite plugin for Old School RuneScape that displays live PvM hit chance, max hit, and estimated DPS against the NPC you are currently targeting.

The current implementation is focused on transparent, OSRS-style combat rolls for melee, ranged, and magic, with regression tests around the recent max-hit and modifier fixes.

## Current Status

- Main plugin wiring: `WillItLandPlugin` registers the overlay, reads the local player target, resolves combat type/subtype, collects gear stats, loads NPC stats, and stores the latest `CombatResult`.
- Overlay wiring: `WillItLandOverlay` reads `latestResult` and honors display toggles for attack style, hit chance, max hit, DPS, NPC warnings, debug info, and weapon intel.
- Target weakness wiring: `TargetWeaknessOverlay` can render weakness, threat, and best inventory/equipped weapon labels above nearby known NPCs, or only above the current target.
- Target intel wiring: `WillItLandOverlay` can show the current target's known level, max hit, aggression flag, weakness source, melee weapon weakness, and best inventory/equipped weapon.
- Config wiring: prayer bonuses, special modifiers, and equipment set effects are now gated by their feature toggles.
- Slayer wiring: Slayer helmet bonuses use RuneLite's current Slayer task varps/DB rows when a task is active, and do not stack with Salve bonuses.
- Test status: `./gradlew.bat test` passes locally.
- Plugin Hub status: latest update PR is tracked at https://github.com/runelite/plugin-hub/pull/12302.

## Features

### Combat Overlay

- Shows while your player is targeting an NPC.
- Displays combat style, hit chance, max hit, and estimated DPS when enabled.
- Includes optional weapon intel rows for weapon bonuses, speed, range, ammo, special attack notes, passive effects, and raw offensive bonuses.
- Includes an optional above-NPC weakness overlay showing target name/level/max hit/aggression, `DEF`, `WEAP`, and `BEST` recommendation lines.
- Can show debug roll details for troubleshooting.
- Shows a warning when the current target NPC is missing from the local stats database.

### Accuracy Calculations

The calculator uses OSRS-style PvM roll mechanics:

- Offensive roll: `effectiveLevel * (attackBonus + 64)`
- NPC defensive roll: `(npcLevel + 9) * (defenceBonus + 64)`
- Magic defence uses the NPC magic level and magic defence bonus.
- Effective levels include prayer multipliers, attack-style bonuses where known, and the +8 baseline.
- Hit chance uses the standard two-branch roll comparison formula.

### Max Hit and DPS

- Melee max hit uses effective Strength, melee strength bonus, prayer strength modifiers, and damage modifiers.
- Ranged max hit uses effective Ranged strength, ranged strength bonus, ranged prayer strength modifiers, and damage modifiers.
- Magic max hit uses the selected/autocast standard spell base max hit, magic damage gear, and damage modifiers.
- Powered staff base max hits are supported for Trident of the seas, Trident of the swamp, Sanguinesti staff, Warped sceptre, and Tumeken's shadow.
- Elemental spell weaknesses add wiki-style bonus damage when the spell element matches the target's `wikiWeakness`.
- Fire Bolt is covered by regression tests against Gemstone Crab.
- DPS is an estimate using average hit, hit chance, and collected weapon attack speed when available.

### Magic Spell Support

Standard spell max hits currently wired:

- Strike spells
- Bolt spells, including Chaos gauntlets for bolt spell max hits
- Blast spells
- Wave spells
- Surge spells

Powered staves with common built-in spell formulas are modeled separately from standard spellbook casts.

### Modifiers

Implemented modifier support includes:

- Offensive prayers for melee, ranged, and magic.
- Strength prayer scaling for melee max hit.
- Ranged strength prayer scaling for ranged max hit.
- Salve amulet variants against undead only.
- Salve imbued/enhanced behavior for melee, ranged, and magic.
- Slayer helmet and imbued Slayer helmet bonuses on matching Slayer tasks.
- Salve and Slayer helmet target-specific bonuses use the best applicable multiplier instead of stacking.
- Equipment set effect hook for Void and Obsidian via `EquipmentSynergyDetector`.

Known limitation: Slayer task matching is name-based after RuneLite resolves the task name, so unusual boss/subtask aliases may need additional alias coverage.

### Above-NPC Weakness Overlay

When enabled, the plugin renders compact labels above known NPCs near you:

- Threat line: NPC name, combat level, known max hit, and aggression marker when available.
- `DEF`: the target's weakness. Wiki-derived weaknesses override local derived stats when present.
- `WEAP`: the target's lowest melee weapon defence across stab, slash, and crush.
- `BEST`: the best matching weapon found in your equipped weapon slot or inventory.

Config options let you show all known nearby NPCs, limit labels to combat, set a minimum combat level, or hide the overlay entirely.

The recommendation uses RuneLite item stats and scores weapons against the target's weakest style. If the overall weakness is magic or ranged but no matching inventory weapon is found, it falls back to the best melee weapon for the target's melee weapon weakness.

Known limitation: recommendations are stat-based and do not yet account for ammo availability, special attacks, monster-specific passives, or exact attack speed DPS.

### Panel Target Intel

The main overlay panel can show the current target's known metadata:

- NPC name, combat level, max hit, and aggression flag.
- Weakness label and source, such as `water (wiki)` or `Slash (derived)`.
- Melee weapon weakness.
- Best matching weapon currently equipped or in inventory.

### NPC Data

NPC stats are loaded from `src/main/resources/npc_stats.json`.

The model now supports both formula inputs and richer overlay metadata:

- `defenceLevel`, `magicLevel`, and style defence bonuses.
- `combatLevel`, `maxHit`, `aggressive`, and `attackType`.
- `wikiWeakness`, `elementalWeaknessPercent`, and `dataSource` for normalized wiki-style data imports.

Current bundled entries include:

- Gemstone Crab
- Chicken
- Cow
- Goblin
- Giant Spider
- Skeleton
- Blue dragon
- Hill Giant
- Abyssal demon

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
+-- PoweredStaffMaxHitResolver.java Powered staff max-hit resolver
+-- SlayerTaskProvider.java        Slayer task source abstraction
+-- RuneLiteSlayerTaskProvider.java RuneLite varp/DB-backed Slayer task source
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
+-- NpcCombatProfileTest.java      NPC metadata model tests
+-- NpcStatsRepositoryTest.java    Bundled NPC data loading tests
+-- PoweredStaffMaxHitResolverTest.java Powered staff max-hit tests
+-- WeaknessAnalyzerTest.java      Defensive weakness and recommendation tests
+-- WeaponAttackStylesTest.java    Attack-style mapping tests
+-- WeaponInfoTest.java            Weapon info formatting tests
+-- WeaponIntelDatabaseTest.java   Weapon intel tests
+-- WillItLandOverlayTest.java     Overlay DPS/color threshold tests
+-- WillItLandPluginTest.java      Config provider wiring test
```

## Testing

Current test coverage includes:

- Hit chance clamping.
- Melee, ranged, and magic accuracy.
- OSRS-style melee max hit regression for unarmed/kick Gemstone Crab.
- Fire Bolt max hit and accuracy against Gemstone Crab.
- Magic damage bonus application.
- Elemental weakness bonus damage for matching standard elemental spells.
- Powered staff max-hit formulas for common powered staves.
- Salve/Salve(ei) undead-only behavior.
- Slayer helmet on-task behavior and Salve/Slayer non-stacking behavior.
- Prayer multipliers for melee, ranged, and magic.
- Config toggles for prayer and special modifiers.
- Defensive weakness detection and inventory weapon recommendations.
- Wiki weakness override behavior, including elemental weaknesses mapping to magic.
- NPC threat/metadata model behavior.
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

Plugin Hub submission/update PR: https://github.com/runelite/plugin-hub/pull/12302

## Plugin Hub Maintenance

Useful RuneLite links:

- Plugin Hub repo: https://github.com/runelite/plugin-hub
- Plugin Hub user/developer info: https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub
- RuneLite developer guide: https://github.com/runelite/runelite/wiki/Developer-Guide
- RuneLite API Javadocs: https://static.runelite.net/api/runelite-api/
- RuneLite client Javadocs: https://static.runelite.net/api/runelite-client/
- Live Plugin Hub listing: https://runelite.net/plugin-hub

Update flow for this plugin:

1. Make and test changes in this repository.
2. Push the plugin repository commit.
3. In `runelite/plugin-hub`, update `plugins/will-it-land` so `commit=` points at the full 40-character plugin commit hash.
4. Open or update a Plugin Hub PR against `runelite/plugin-hub:master`.
5. Watch both Plugin Hub CI jobs. If Plugin Hub asks for changes, fix the plugin repo, push a new plugin commit, then update `commit=` again in the same Plugin Hub PR.

Plugin Hub notes to keep in mind:

- External plugins are community maintained and reviewed for Jagex rule compliance and safety, but RuneLite does not guarantee they work forever.
- Plugin resources must live in `src/main/resources` and should be loaded with `getResourceAsStream`, because Plugin Hub distributes plugins as jars.
- Avoid new third-party dependencies unless necessary; Plugin Hub requires dependency verification for non-transitive dependencies.
- Keep `runeLiteVersion = 'latest.release'` in `build.gradle` so the plugin tracks current RuneLite API releases.
- Keep README feature notes and limitations current because Plugin Hub recommends a clear README for users.

## Formula And Data References

Wiki is king for combat formulas and NPC data. When changing calculator behavior or `npc_stats.json`, start with these pages and add/update regression tests in the same commit:

- Chance to hit: https://oldschool.runescape.wiki/w/Chance_to_hit
- Magic accuracy: https://oldschool.runescape.wiki/w/Magic_accuracy
- Maximum melee hit: https://oldschool.runescape.wiki/w/Maximum_melee_hit
- Maximum ranged hit: https://oldschool.runescape.wiki/w/Maximum_ranged_hit
- Maximum magic hit: https://oldschool.runescape.wiki/w/Maximum_magic_hit
- Magic damage: https://oldschool.runescape.wiki/w/Magic_damage
- Elemental weakness: https://oldschool.runescape.wiki/w/Elemental_weakness
- Damage per second, melee reference: https://oldschool.runescape.wiki/w/Damage_per_second/Melee
- Salve amulet (ei): https://oldschool.runescape.wiki/w/Salve_amulet%28ei%29
- Slayer helmet: https://oldschool.runescape.wiki/w/Slayer_helmet
- OSRS Wiki DPS calculator: https://tools.runescape.wiki/osrs-dps/

Current NPC data starter pages:

- Gemstone Crab: https://oldschool.runescape.wiki/w/Gemstone_Crab
- Blue dragon: https://oldschool.runescape.wiki/w/Blue_dragon
- Hill Giant: https://oldschool.runescape.wiki/w/Hill_Giant
- Abyssal demon: https://oldschool.runescape.wiki/w/Abyssal_demon

Data maintenance rules:

- Prefer explicit `dataSource` values such as `osrs-wiki`.
- Add `wikiWeakness` and `elementalWeaknessPercent` only when a wiki page supports it.
- Keep formula-sensitive examples covered by tests before changing calculator logic.
- Treat local values as a curated cache, not a replacement for the OSRS Wiki.

## Accuracy Notes

The calculator is intentionally conservative and test-backed for the currently wired cases. Some OSRS combat mechanics are still outside the current model, including:

- Some powered staff special cases outside the common built-in spell formulas.
- Spell-specific special cases beyond standard spell base damage, Chaos gauntlets, and elemental weakness damage.
- Rare Slayer task aliases not yet covered by local task-name matching.
- Recommendation scoring that accounts for ammo, special attacks, monster-specific passives, or exact DPS.
- Large NPC database coverage beyond the bundled sample data.

## Disclaimer

This plugin is not affiliated with Jagex Ltd or RuneLite.

OSRS combat formulas are based on community-verified mechanics from the OSRS Wiki and related tooling.
