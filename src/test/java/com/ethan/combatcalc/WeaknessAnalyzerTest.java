package com.ethan.combatcalc;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeaknessAnalyzerTest
{
    private final WeaknessAnalyzer analyzer = new WeaknessAnalyzer();

    @Test
    public void findsOverallAndMeleeWeaponWeakness()
    {
        NpcCombatProfile profile = new NpcCombatProfile("Test");
        profile.setStabDefence(20);
        profile.setSlashDefence(5);
        profile.setCrushDefence(-10);
        profile.setRangedDefence(15);
        profile.setMagicDefence(-25);

        WeaknessSummary summary = analyzer.analyze(profile, Collections.emptyList());

        assertEquals(AttackSubType.MAGIC, summary.getDefensiveWeakness());
        assertEquals(-25, summary.getDefensiveWeaknessValue());
        assertEquals(AttackSubType.CRUSH, summary.getWeaponWeakness());
        assertEquals(-10, summary.getWeaponWeaknessValue());
        assertFalse(summary.hasRecommendation());
    }

    @Test
    public void recommendsInventoryWeaponForDefensiveWeakness()
    {
        NpcCombatProfile profile = new NpcCombatProfile("Test");
        profile.setStabDefence(30);
        profile.setSlashDefence(-10);
        profile.setCrushDefence(10);
        profile.setRangedDefence(20);
        profile.setMagicDefence(25);

        WeaponInfo scimitar = WeaponInfo.builder("Rune scimitar")
                .slashAttackBonus(45)
                .strengthBonus(44)
                .build();
        WeaponInfo dagger = WeaponInfo.builder("Dragon dagger")
                .stabAttackBonus(40)
                .strengthBonus(40)
                .build();

        WeaknessSummary summary = analyzer.analyze(profile, Arrays.asList(scimitar, dagger));

        assertTrue(summary.hasRecommendation());
        assertEquals("Rune scimitar", summary.getRecommendedWeaponName());
        assertEquals(AttackSubType.SLASH, summary.getRecommendedWeaponStyle());
    }

    @Test
    public void fallsBackToBestMeleeWeaponWhenOverallWeaknessHasNoCandidate()
    {
        NpcCombatProfile profile = new NpcCombatProfile("Test");
        profile.setStabDefence(20);
        profile.setSlashDefence(-5);
        profile.setCrushDefence(0);
        profile.setRangedDefence(10);
        profile.setMagicDefence(-30);

        WeaponInfo scimitar = WeaponInfo.builder("Rune scimitar")
                .slashAttackBonus(45)
                .strengthBonus(44)
                .build();

        WeaknessSummary summary = analyzer.analyze(profile, Collections.singletonList(scimitar));

        assertEquals(AttackSubType.MAGIC, summary.getDefensiveWeakness());
        assertEquals("Rune scimitar", summary.getRecommendedWeaponName());
        assertEquals(AttackSubType.SLASH, summary.getRecommendedWeaponStyle());
    }
}
