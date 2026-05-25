package com.ethan.combatcalc;

import java.util.HashMap;
import java.util.Map;

public class WeaponIntelDatabase
{
    private final Map<String, WeaponInfo> weapons = new HashMap<>();

    public WeaponIntelDatabase()
    {
        add(WeaponInfo.builder("Toxic blowpipe")
                .baseRange(5)
                .longRange(7)
                .rapidAttackSpeedTicks(2)
                .specialAttack(new WeaponSpecialAttack(
                        "Toxic Siphon",
                        50,
                        "Deals damage with increased accuracy and heals for half the damage dealt.",
                        "Ranged",
                        "+100%",
                        "+50%"))
                .addPassiveEffect("Can inflict venom when charged with scales and darts.")
                .build());

        add(WeaponInfo.builder("Dragon warhammer")
                .baseRange(1)
                .specialAttack(new WeaponSpecialAttack(
                        "Smash",
                        50,
                        "Reduces the target's Defence by 30% if it hits.",
                        "Crush",
                        "None",
                        "None"))
                .build());

        add(WeaponInfo.builder("Twisted bow")
                .baseRange(10)
                .longRange(10)
                .addPassiveEffect("Accuracy and damage scale with the target's Magic level.")
                .build());

        add(WeaponInfo.builder("Dragon hunter lance")
                .baseRange(1)
                .addPassiveEffect("20% accuracy and damage bonus against draconic creatures.")
                .build());

        add(WeaponInfo.builder("Dragon hunter crossbow")
                .baseRange(7)
                .longRange(9)
                .specialAttack(new WeaponSpecialAttack(
                        "Puncture",
                        65,
                        "Increases accuracy and damage against dragons.",
                        "Ranged",
                        "+30%",
                        "+25%"))
                .addPassiveEffect("30% accuracy and 25% damage bonus against draconic creatures.")
                .build());

        add(WeaponInfo.builder("Arclight")
                .baseRange(1)
                .specialAttack(new WeaponSpecialAttack(
                        "Weaken",
                        50,
                        "Lowers a demon's Attack, Strength, and Defence if it hits.",
                        "Slash",
                        "None",
                        "None"))
                .addPassiveEffect("70% accuracy and damage bonus against demons.")
                .build());

        add(WeaponInfo.builder("Scorching bow")
                .baseRange(10)
                .longRange(10)
                .addPassiveEffect("Strong against demons and can burn demonic targets.")
                .build());

        add(WeaponInfo.builder("Osmumten's fang")
                .baseRange(1)
                .specialAttack(new WeaponSpecialAttack(
                        "Aimed Jab",
                        25,
                        "Rolls accuracy twice and uses the better roll.",
                        "Stab",
                        "Special",
                        "None"))
                .addPassiveEffect("Stable damage and rerolled accuracy on normal attacks.")
                .build());

        add(WeaponInfo.builder("Tumeken's shadow")
                .baseRange(10)
                .longRange(10)
                .addPassiveEffect("Multiplies Magic accuracy and Magic damage from gear.")
                .build());

        add(WeaponInfo.builder("Abyssal whip")
                .baseRange(1)
                .specialAttack(new WeaponSpecialAttack(
                        "Energy Drain",
                        50,
                        "Transfers 10% of the target's run energy to you.",
                        "Slash",
                        "+25%",
                        "None"))
                .build());
    }

    public WeaponInfo lookup(String weaponName)
    {
        String normalized = normalize(weaponName);
        WeaponInfo info = weapons.get(normalized);
        if (info == null)
        {
            return WeaponInfo.builder(cleanName(weaponName)).build();
        }

        return info;
    }

    private void add(WeaponInfo info)
    {
        weapons.put(normalize(info.getWeaponName()), info);
    }

    static String normalize(String name)
    {
        return cleanName(name).toLowerCase();
    }

    private static String cleanName(String name)
    {
        if (name == null)
        {
            return "";
        }

        return name
                .replaceAll("<[^>]+>", "")
                .replace('\u00a0', ' ')
                .trim()
                .replaceAll("\\s+", " ");
    }
}
