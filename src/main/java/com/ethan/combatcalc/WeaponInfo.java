package com.ethan.combatcalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeaponInfo
{
    private final String weaponName;
    private final String ammoName;
    private final int attackSpeedTicks;
    private final int rapidAttackSpeedTicks;
    private final int baseRange;
    private final int longRange;
    private final int stabAttackBonus;
    private final int slashAttackBonus;
    private final int crushAttackBonus;
    private final int magicAttackBonus;
    private final int rangedAttackBonus;
    private final int strengthBonus;
    private final int rangedStrengthBonus;
    private final float magicDamageBonus;
    private final int specialEnergyPercent;
    private final AttackSubType activeAttackSubType;
    private final int activeAttackStyleIndex;
    private final WeaponSpecialAttack specialAttack;
    private final List<String> passiveEffects;
    private final List<String> notes;

    private WeaponInfo(Builder builder)
    {
        this.weaponName = builder.weaponName;
        this.ammoName = builder.ammoName;
        this.attackSpeedTicks = builder.attackSpeedTicks;
        this.rapidAttackSpeedTicks = builder.rapidAttackSpeedTicks;
        this.baseRange = builder.baseRange;
        this.longRange = builder.longRange;
        this.stabAttackBonus = builder.stabAttackBonus;
        this.slashAttackBonus = builder.slashAttackBonus;
        this.crushAttackBonus = builder.crushAttackBonus;
        this.magicAttackBonus = builder.magicAttackBonus;
        this.rangedAttackBonus = builder.rangedAttackBonus;
        this.strengthBonus = builder.strengthBonus;
        this.rangedStrengthBonus = builder.rangedStrengthBonus;
        this.magicDamageBonus = builder.magicDamageBonus;
        this.specialEnergyPercent = builder.specialEnergyPercent;
        this.activeAttackSubType = builder.activeAttackSubType;
        this.activeAttackStyleIndex = builder.activeAttackStyleIndex;
        this.specialAttack = builder.specialAttack;
        this.passiveEffects = Collections.unmodifiableList(new ArrayList<>(builder.passiveEffects));
        this.notes = Collections.unmodifiableList(new ArrayList<>(builder.notes));
    }

    public static Builder builder(String weaponName)
    {
        return new Builder(weaponName);
    }

    public static WeaponInfo empty()
    {
        return builder("").build();
    }

    public Builder toBuilder()
    {
        return new Builder(weaponName)
                .ammoName(ammoName)
                .attackSpeedTicks(attackSpeedTicks)
                .rapidAttackSpeedTicks(rapidAttackSpeedTicks)
                .baseRange(baseRange)
                .longRange(longRange)
                .stabAttackBonus(stabAttackBonus)
                .slashAttackBonus(slashAttackBonus)
                .crushAttackBonus(crushAttackBonus)
                .magicAttackBonus(magicAttackBonus)
                .rangedAttackBonus(rangedAttackBonus)
                .strengthBonus(strengthBonus)
                .rangedStrengthBonus(rangedStrengthBonus)
                .magicDamageBonus(magicDamageBonus)
                .specialEnergyPercent(specialEnergyPercent)
                .activeAttackSubType(activeAttackSubType)
                .activeAttackStyleIndex(activeAttackStyleIndex)
                .specialAttack(specialAttack)
                .passiveEffects(passiveEffects)
                .notes(notes);
    }

    public boolean hasWeapon()
    {
        return weaponName != null && !weaponName.isEmpty();
    }

    public boolean hasAmmo()
    {
        return ammoName != null && !ammoName.isEmpty();
    }

    public boolean hasSpecialAttack()
    {
        return specialAttack != null;
    }

    public String formatAttackSpeed()
    {
        int selectedSpeed = getSelectedAttackSpeedTicks();
        if (selectedSpeed <= 0)
        {
            return "";
        }

        if (selectedSpeed != attackSpeedTicks && attackSpeedTicks > 0)
        {
            return formatTicks(selectedSpeed) + " selected, " + attackSpeedTicks + " base";
        }

        String formatted = formatTicks(selectedSpeed);
        if (rapidAttackSpeedTicks > 0 && rapidAttackSpeedTicks != attackSpeedTicks)
        {
            formatted += ", " + rapidAttackSpeedTicks + " rapid";
        }
        return formatted;
    }

    public String formatRange()
    {
        if (baseRange <= 0)
        {
            return "";
        }

        if (longRange <= 0 || longRange == baseRange)
        {
            return baseRange + " tiles";
        }

        return baseRange + " tiles, " + longRange + " long";
    }

    public String formatActiveStyleSummary()
    {
        if (activeAttackSubType != AttackSubType.RANGED)
        {
            return "";
        }

        switch (activeAttackStyleIndex)
        {
            case 0:
                return "Accurate: +3 Ranged";
            case 1:
                return "Rapid: faster attack speed";
            case 2:
                return "Longrange: +3 Defence, extended range";
            default:
                return "";
        }
    }

    public String formatShiftTooltip()
    {
        if (!hasWeapon())
        {
            return "";
        }

        StringBuilder tooltip = new StringBuilder(weaponName);
        appendTooltipLine(tooltip, "Mode", formatActiveStyleSummary());
        appendTooltipLine(tooltip, "Bonuses", formatRelevantBonuses());
        appendTooltipLine(tooltip, "Speed", formatAttackSpeed());
        appendTooltipLine(tooltip, "Range", formatRange());
        if (hasAmmo())
        {
            appendTooltipLine(tooltip, "Ammo", ammoName);
        }
        if (hasSpecialAttack())
        {
            appendTooltipLine(tooltip, "Spec", specialAttack.formatSummary());
            appendTooltipLine(tooltip, "", specialAttack.getDescription());
        }
        for (String passiveEffect : passiveEffects)
        {
            appendTooltipLine(tooltip, "Effect", passiveEffect);
        }
        for (String note : notes)
        {
            appendTooltipLine(tooltip, "Note", note);
        }

        return tooltip.toString();
    }

    private static void appendTooltipLine(StringBuilder tooltip, String label, String value)
    {
        if (value == null || value.isEmpty())
        {
            return;
        }

        tooltip.append("<br>");
        if (label != null && !label.isEmpty())
        {
            tooltip.append(label).append(": ");
        }
        tooltip.append(value);
    }

    public String formatRelevantBonuses()
    {
        if (activeAttackSubType == AttackSubType.MAGIC)
        {
            String bonus = formatSigned(magicAttackBonus) + " Magic";
            if (magicDamageBonus != 0)
            {
                bonus += ", " + formatMagicDamage();
            }
            return bonus;
        }

        if (activeAttackSubType == AttackSubType.RANGED || rangedStrengthBonus != 0)
        {
            if (activeAttackSubType == AttackSubType.RANGED)
            {
                return formatSigned(rangedAttackBonus) + " Ranged, " + formatSigned(rangedStrengthBonus) + " Ranged Str";
            }
            return formatSigned(rangedStrengthBonus) + " Ranged Str";
        }

        switch (activeAttackSubType)
        {
            case STAB:
                return formatSigned(stabAttackBonus) + " Stab, " + formatSigned(strengthBonus) + " Str";
            case SLASH:
                return formatSigned(slashAttackBonus) + " Slash, " + formatSigned(strengthBonus) + " Str";
            case CRUSH:
                return formatSigned(crushAttackBonus) + " Crush, " + formatSigned(strengthBonus) + " Str";
            default:
                return "";
        }
    }

    public String formatAllOffensiveBonuses()
    {
        return "Stab " + formatSigned(stabAttackBonus)
                + " | Slash " + formatSigned(slashAttackBonus)
                + " | Crush " + formatSigned(crushAttackBonus)
                + " | Magic " + formatSigned(magicAttackBonus)
                + " | Ranged " + formatSigned(rangedAttackBonus);
    }

    public String formatMagicDamage()
    {
        if (magicDamageBonus == (int) magicDamageBonus)
        {
            return formatSigned((int) magicDamageBonus) + "% Magic Dmg";
        }
        return (magicDamageBonus > 0 ? "+" : "") + String.format("%.1f", magicDamageBonus) + "% Magic Dmg";
    }

    private static String formatTicks(int ticks)
    {
        return ticks + " ticks (" + String.format("%.1f", ticks * 0.6) + "s)";
    }

    private static String formatSigned(int value)
    {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    public String getWeaponName()
    {
        return weaponName;
    }

    public String getAmmoName()
    {
        return ammoName;
    }

    public int getAttackSpeedTicks()
    {
        return attackSpeedTicks;
    }

    public int getSelectedAttackSpeedTicks()
    {
        if (activeAttackStyleIndex == 1 && rapidAttackSpeedTicks > 0)
        {
            return rapidAttackSpeedTicks;
        }

        return attackSpeedTicks;
    }

    public int getRapidAttackSpeedTicks()
    {
        return rapidAttackSpeedTicks;
    }

    public int getBaseRange()
    {
        return baseRange;
    }

    public int getLongRange()
    {
        return longRange;
    }

    public int getStabAttackBonus()
    {
        return stabAttackBonus;
    }

    public int getSlashAttackBonus()
    {
        return slashAttackBonus;
    }

    public int getCrushAttackBonus()
    {
        return crushAttackBonus;
    }

    public int getMagicAttackBonus()
    {
        return magicAttackBonus;
    }

    public int getRangedAttackBonus()
    {
        return rangedAttackBonus;
    }

    public int getStrengthBonus()
    {
        return strengthBonus;
    }

    public int getRangedStrengthBonus()
    {
        return rangedStrengthBonus;
    }

    public float getMagicDamageBonus()
    {
        return magicDamageBonus;
    }

    public int getSpecialEnergyPercent()
    {
        return specialEnergyPercent;
    }

    public AttackSubType getActiveAttackSubType()
    {
        return activeAttackSubType;
    }

    public int getActiveAttackStyleIndex()
    {
        return activeAttackStyleIndex;
    }

    public WeaponSpecialAttack getSpecialAttack()
    {
        return specialAttack;
    }

    public List<String> getPassiveEffects()
    {
        return passiveEffects;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public static class Builder
    {
        private final String weaponName;
        private String ammoName = "";
        private int attackSpeedTicks;
        private int rapidAttackSpeedTicks;
        private int baseRange;
        private int longRange;
        private int stabAttackBonus;
        private int slashAttackBonus;
        private int crushAttackBonus;
        private int magicAttackBonus;
        private int rangedAttackBonus;
        private int strengthBonus;
        private int rangedStrengthBonus;
        private float magicDamageBonus;
        private int specialEnergyPercent;
        private AttackSubType activeAttackSubType = AttackSubType.UNKNOWN;
        private int activeAttackStyleIndex = -1;
        private WeaponSpecialAttack specialAttack;
        private List<String> passiveEffects = new ArrayList<>();
        private List<String> notes = new ArrayList<>();

        private Builder(String weaponName)
        {
            this.weaponName = weaponName == null ? "" : weaponName;
        }

        public Builder ammoName(String ammoName)
        {
            this.ammoName = ammoName == null ? "" : ammoName;
            return this;
        }

        public Builder attackSpeedTicks(int attackSpeedTicks)
        {
            this.attackSpeedTicks = attackSpeedTicks;
            return this;
        }

        public Builder rapidAttackSpeedTicks(int rapidAttackSpeedTicks)
        {
            this.rapidAttackSpeedTicks = rapidAttackSpeedTicks;
            return this;
        }

        public Builder baseRange(int baseRange)
        {
            this.baseRange = baseRange;
            return this;
        }

        public Builder longRange(int longRange)
        {
            this.longRange = longRange;
            return this;
        }

        public Builder stabAttackBonus(int stabAttackBonus)
        {
            this.stabAttackBonus = stabAttackBonus;
            return this;
        }

        public Builder slashAttackBonus(int slashAttackBonus)
        {
            this.slashAttackBonus = slashAttackBonus;
            return this;
        }

        public Builder crushAttackBonus(int crushAttackBonus)
        {
            this.crushAttackBonus = crushAttackBonus;
            return this;
        }

        public Builder magicAttackBonus(int magicAttackBonus)
        {
            this.magicAttackBonus = magicAttackBonus;
            return this;
        }

        public Builder rangedAttackBonus(int rangedAttackBonus)
        {
            this.rangedAttackBonus = rangedAttackBonus;
            return this;
        }

        public Builder strengthBonus(int strengthBonus)
        {
            this.strengthBonus = strengthBonus;
            return this;
        }

        public Builder rangedStrengthBonus(int rangedStrengthBonus)
        {
            this.rangedStrengthBonus = rangedStrengthBonus;
            return this;
        }

        public Builder magicDamageBonus(float magicDamageBonus)
        {
            this.magicDamageBonus = magicDamageBonus;
            return this;
        }

        public Builder specialEnergyPercent(int specialEnergyPercent)
        {
            this.specialEnergyPercent = specialEnergyPercent;
            return this;
        }

        public Builder activeAttackSubType(AttackSubType activeAttackSubType)
        {
            this.activeAttackSubType = activeAttackSubType == null ? AttackSubType.UNKNOWN : activeAttackSubType;
            return this;
        }

        public Builder activeAttackStyleIndex(int activeAttackStyleIndex)
        {
            this.activeAttackStyleIndex = activeAttackStyleIndex;
            return this;
        }

        public Builder specialAttack(WeaponSpecialAttack specialAttack)
        {
            this.specialAttack = specialAttack;
            return this;
        }

        public Builder passiveEffects(List<String> passiveEffects)
        {
            this.passiveEffects = new ArrayList<>(passiveEffects);
            return this;
        }

        public Builder addPassiveEffect(String passiveEffect)
        {
            if (passiveEffect != null && !passiveEffect.isEmpty())
            {
                this.passiveEffects.add(passiveEffect);
            }
            return this;
        }

        public Builder notes(List<String> notes)
        {
            this.notes = new ArrayList<>(notes);
            return this;
        }

        public Builder addNote(String note)
        {
            if (note != null && !note.isEmpty())
            {
                this.notes.add(note);
            }
            return this;
        }

        public WeaponInfo build()
        {
            return new WeaponInfo(this);
        }
    }
}
