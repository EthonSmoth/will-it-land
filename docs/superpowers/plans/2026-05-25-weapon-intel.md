# Weapon Intel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an optional overlay section that tells the player the important facts about the currently equipped weapon while fighting the current NPC.

**Architecture:** Store weapon facts in a small `WeaponInfo` model, enrich it with a curated local `WeaponIntelDatabase`, and attach it to `CombatResult` each game tick. Render each fact behind config toggles so players can keep the overlay compact.

**Tech Stack:** Java 11, RuneLite client APIs, JUnit 4, Gradle.

---

### Task 1: Pure Weapon Intel Model

**Files:**
- Create: `src/main/java/com/ethan/combatcalc/WeaponInfo.java`
- Create: `src/main/java/com/ethan/combatcalc/WeaponSpecialAttack.java`
- Create: `src/main/java/com/ethan/combatcalc/WeaponIntelDatabase.java`
- Test: `src/test/java/com/ethan/combatcalc/WeaponIntelDatabaseTest.java`

- [ ] Write tests that assert known facts for common weapons: toxic blowpipe, dragon warhammer, twisted bow, dragon hunter lance, and a missing weapon.
- [ ] Implement immutable-ish data containers with getters.
- [ ] Implement the curated database with exact-name lookup, normalized case/markup handling, special attack details, passive notes, and range overrides where RuneLite item stats do not expose range.
- [ ] Run `.\gradlew.bat test --tests com.ethan.combatcalc.WeaponIntelDatabaseTest`.

### Task 2: Runtime Collection

**Files:**
- Create: `src/main/java/com/ethan/combatcalc/WeaponInfoCollector.java`
- Modify: `src/main/java/com/ethan/combatcalc/CombatResult.java`
- Modify: `src/main/java/com/ethan/combatcalc/WillItLandPlugin.java`
- Test: `src/test/java/com/ethan/combatcalc/WeaponInfoTest.java`

- [ ] Write tests for `WeaponInfo` formatting: attack speed in ticks/seconds, magic damage percent, and ranged strength.
- [ ] Add `WeaponInfo weaponInfo` to `CombatResult`.
- [ ] Collect equipped weapon and ammo item IDs, item names, equipment stats, attack speed, ranged strength, magic damage, special energy, selected attack subtype, and active target name.
- [ ] Merge collected local stats with curated database notes.

### Task 3: Config And Overlay

**Files:**
- Modify: `src/main/java/com/ethan/combatcalc/WillItLandConfig.java`
- Modify: `src/main/java/com/ethan/combatcalc/WillItLandOverlay.java`
- Test: existing full suite

- [ ] Add config toggles: weapon intel master toggle, speed, range, special attack, passive effects, ammo, and raw bonuses.
- [ ] Render a compact "Weapon" section only when enabled and a weapon is equipped.
- [ ] Preserve the existing hit chance, max hit, DPS, and debug rows.
- [ ] Run `.\gradlew.bat test`.

### Task 4: Verification

**Files:**
- All touched files.

- [ ] Run `.\gradlew.bat test`.
- [ ] Check `git status --short` to report exactly what changed.
- [ ] Mention limits: facts and notes are shown; full special-attack DPS simulation is a later enhancement.
