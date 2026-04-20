# Will It Land - Improvement Audit Report

## Executive Summary
The plugin is well-structured with good separation of concerns. However, there are several areas where accuracy can be improved, edge cases handled, and new features added to increase reliability and functionality.

---

## Critical Issues to Address

### 1. **Hit Chance Clamping** ⚠️ HIGH PRIORITY
**Issue**: Hit chance can exceed 100% or go below 0% in edge cases.
**Current**: No validation in `calculateHitChance()` method.
**Impact**: Misleading accuracy display.
**Solution**: Clamp hit chance to 0.0 - 1.0 range.
```java
double hitChance = calculateHitChance(offensiveRoll, defensiveRoll);
hitChance = Math.max(0.0, Math.min(1.0, hitChance)); // Clamp to 0-100%
```

### 2. **Magic Amulet Support Missing** ⚠️ MEDIUM PRIORITY
**Issue**: Magic-specific amulets don't boost accuracy.
**Missing**: 
- Occult Amulet (+10% magic accuracy)
- Arcane Pulse Necklace (+10% magic accuracy)
- Amulet of Damned (+10% magic accuracy in PvP)

**Solution**: Add magic amulet detection to `CombatModifier`.

### 3. **Skill Buff/Spell Boosters Not Accounted For** ⚠️ MEDIUM PRIORITY
**Missing Modifiers**:
- Bastion Potion (+10% all combat stats for 6 minutes)
- Combat Potion (+3 attack, +1 strength for 6 minutes)
- Attack Potion (+3 attack for 6 minutes)
- Ranging Potion (+4 ranging for 6 minutes)
- Magic Potion (+4 magic for 6 minutes)
- Imbued Heart (+1 magic for 6 minutes)

**Impact**: Calculations don't reflect potion boosts.
**Solution**: Detect active potions and add to effective level calculations.

### 4. **Slayer Task Detection Not Implemented** ⚠️ MEDIUM PRIORITY
**Current Status**: Returns false always (see TODO in code).
**Impact**: Slayer Helm bonus never applies (+15% melee accuracy).
**Solution**: Integrate with RuneLite's Slayer plugin or parse client state.

### 5. **Defensive Equipment Modifiers Not Implemented** ⚠️ LOW PRIORITY
**Missing NPC Defenses**:
- Ancient Wyvern Defensive scaling
- Boss-specific defensive mechanics
- Prayer-based defense boosts on NPCs

---

## Code Quality Issues

### 6. **Null Safety** 🔴 CRITICAL
**Issue**: Multiple places where null checks could be improved.
**Files Affected**:
- `CombatModifier.java`: `wearingSalveAmulet()` assumes equipment exists
- `NpcStatsRepository.java`: Could handle null NPC names better
- `WillItLandPlugin.java`: No defensive checks on NPC targets

**Example Issue**:
```java
// Currently risky
Item amulet = equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
if (amulet == null || amulet.getId() == -1) // Good, but could be more defensive
```

### 7. **Logging & Debugging** 📊 MEDIUM
**Current**: Uses `Logger` only in `NpcStatsRepository`.
**Issue**: No logging in main calculation engine makes debugging hard.
**Solution**: Add optional debug logging to track modifier applications.

### 8. **Magic Defense Calculation** ⚠️ MEDIUM
**Issue**: Using NPC Magic level as defense roll base may be inaccurate.
**OSRS Formula**: Should be `(Magic level + 64) * (Magic defence + 64)`
**Current**: Same formula is used ✓ (Actually correct!)

---

## Missing Features

### 9. **Weapon-Specific Attack Speed** ❌
**Feature**: Different weapons have different attack speeds affecting DPS.
**Examples**:
- Whip (2.4s) vs Sword (2.25s)
- Dagger (1.75s) vs Polearm (3.0s)
**Impact**: Users can't fully optimize DPS.
**Note**: Out of scope for accuracy calculator but good to document.

### 10. **Equipment Synergies** ❌
**Missing Calculations**:
- Obsidian set effect (+10% melee accuracy when full set worn)
- Void Knight equipment (+10% accuracy, +40% damage)
- God Wars Dungeon set effects
**Impact**: Incomplete accuracy for specific gear combinations.

### 11. **Defensive Calculations** ❌
**Current**: Only offensive calculations shown.
**Missing**: 
- Defensive roll display
- Expected incoming damage
- Defense-based strategies
**Complexity**: Would require damage calculation engine.

### 12. **NPC Weakness System** ❌
**Current**: Doesn't account for NPC weakness bonuses.
**Examples**:
- Slayer monsters with weapon weaknesses
- Boss-specific attack type preferences
**Impact**: Some NPCs show lower accuracy than they should.

### 13. **Player Level Variance** ⚠️
**Issue**: Using boosted levels only.
**Missing**: Base level visibility.
**Impact**: Users don't see effect of level boosts at a glance.

---

## Performance Considerations

### 14. **Equipment Collection Efficiency** 📈
**Current**: Iterates through all equipment slots every tick.
**Optimization**: Cache equipment stats, only refresh on equipment change event.
**Impact**: Reduced CPU usage during combat.

### 15. **NPC Stats Lookup** 📈
**Current**: Linear search for case-insensitive NPC names.
**Optimization**: Consider HashMap with lowercase keys for O(1) lookup.
**Impact**: Better performance with many NPCs.

---

## UI/UX Improvements

### 16. **Visual Indicators for Modifiers** 🎨
**Idea**: Show what modifiers are active.
**Example**: 
```
⚔ Will It Land?
Style: Crush
Hit Chance: 75.0%
(+15% Salve, +10% Prayer)
```

### 17. **Accuracy Range** 📊
**Idea**: Show likely range of accuracy over time.
**Example**: "75% ±5%" accounting for variance.

### 18. **NPC Not Found Warning** ⚠️
**Current**: Silently uses default stats if NPC not in database.
**Issue**: No visual indicator that calculation may be inaccurate.
**Solution**: Show warning indicator or different overlay color.

---

## Configuration/Settings

### 19. **Plugin Configuration Options** ⚙️
**Missing Options**:
- Toggle between hit chance % and raw rolls
- Show/hide modifier breakdown
- Decimal precision (1 or 2 places)
- Overlay position customization
- Color theme customization

### 20. **Debug Mode** 🐛
**Idea**: Add debug mode showing:
- Offensive roll value
- Defensive roll value
- All applied modifiers
- Calculation breakdown

---

## Testing & Validation

### 21. **Unit Tests** ❌
**Current Status**: No unit tests in project.
**Missing Tests**:
- `CombatCalculator` accuracy formulas
- `AttackStyleResolver` weapon detection
- `CombatModifier` modifier calculations
- Edge cases (level 0, negative bonuses, etc.)

### 22. **Manual Validation** ⚠️
**Issue**: No documented way to verify calculations against OSRS wiki values.
**Solution**: Create test cases with known NPC/player combos and expected results.

---

## Data Quality

### 23. **NPC Stats Database** 📦
**Issue**: npc_stats.json may be outdated or incomplete.
**Missing**:
- New boss NPCs
- Quest bosses
- Event bosses
- Seasonal content

**Solution**: Establish update protocol for NPC database.

### 24. **Staff Weapon IDs** 🔧
**Current**: Hardcoded staff detection with specific IDs.
**Risk**: New staves will be detected as melee weapons initially.
**Solution**: Add logging when unknown staves detected to improve detection.

---

## Documentation

### 25. **Code Documentation** 📝
**Missing**:
- Detailed combat formula explanation
- Modifier application order documentation
- Known limitations of accuracy (variance, tick timing, etc.)
- Contribution guidelines for adding new modifiers

---

## Priority Roadmap

### Immediate (Next Release)
1. ✅ Hit chance clamping (Issue #1)
2. ✅ Slayer task detection integration (Issue #4)
3. ✅ Magic amulet support (Issue #2)
4. ✅ Better null safety (Issue #6)
5. ✅ NPC not found warning (Issue #18)

### Short Term (Next 2 Releases)
6. Potion boost detection (Issue #3)
7. Unit test suite (Issue #21)
8. Debug mode (Issue #20)
9. Configuration options (Issue #19)
10. Defensive equipment modifiers (Issue #5)

### Long Term (Future Versions)
11. Equipment synergies (Issue #10)
12. NPC weakness system (Issue #12)
13. Visual modifier indicators (Issue #16)
14. Performance optimizations (Issues #14, #15)

---

## Recommendations

### Best Next Steps
1. **Add hit chance clamping immediately** - This is a correctness issue
2. **Implement unit tests** - Catch bugs before release
3. **Add magic amulet support** - Quick win for completeness
4. **Integrate slayer task detection** - Adds real value to calculations
5. **Create debug mode** - Helps verify calculations and troubleshoot

### Long-term Architecture
- Consider splitting calculation logic from RuneLite-specific code for better testability
- Add event-based modifier tracking (listen for equipment changes, prayer changes)
- Create abstract modifier system for easier addition of new modifiers

---

## Known Limitations

### By Design
- Only shows hit chance, not damage/DPS
- Doesn't account for game tick timing variance
- Simplified undead detection based on NPC name
- No PvP-specific calculations (different formulas vs NPCs)

### Fixable Issues
- Potion effects not detected
- Slayer task not detected
- Some equipment modifiers missing
- Magic amulets not boosted

---

## Conclusion

The plugin has a solid foundation with correct core formulas and good code structure. The main improvements needed are:
1. Edge case handling (hit chance clamping)
2. Missing modifier implementations (potions, magic amulets)
3. External system integration (slayer tasks)
4. Test coverage and validation
5. User feedback mechanisms

Estimated effort: 8-12 hours to implement all high-priority items.

