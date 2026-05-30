package com.ethan.combatcalc;

import org.junit.Test;

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

public class WillItLandOverlayTest
{
    @Test
    public void dpsUsesCollectedWeaponAttackSpeed()
            throws Exception
    {
        WillItLandOverlay overlay = new WillItLandOverlay(null, config(80, 50, 25));
        CombatResult result = new CombatResult();
        result.setHitChance(1.0);
        result.setMaxHit(10);
        result.setWeaponInfo(WeaponInfo.builder("Test weapon")
                .attackSpeedTicks(5)
                .build());

        assertEquals(1.67, invokeCalculateDps(overlay, result), 0.001);
    }

    @Test
    public void hitChanceColorsUseConfiguredThresholds()
            throws Exception
    {
        WillItLandOverlay overlay = new WillItLandOverlay(null, config(70, 40, 20));

        assertEquals(new Color(0, 255, 0), invokeHitChanceColor(overlay, 0.75));
        assertEquals(new Color(255, 255, 0), invokeHitChanceColor(overlay, 0.45));
        assertEquals(new Color(255, 165, 0), invokeHitChanceColor(overlay, 0.25));
        assertEquals(new Color(255, 0, 0), invokeHitChanceColor(overlay, 0.10));
    }

    private static double invokeCalculateDps(WillItLandOverlay overlay, CombatResult result)
            throws Exception
    {
        Method method = WillItLandOverlay.class.getDeclaredMethod("calculateDPS", CombatResult.class);
        method.setAccessible(true);
        return (double) method.invoke(overlay, result);
    }

    private static Color invokeHitChanceColor(WillItLandOverlay overlay, double hitChance)
            throws Exception
    {
        Method method = WillItLandOverlay.class.getDeclaredMethod("getHitChanceColor", double.class);
        method.setAccessible(true);
        return (Color) method.invoke(overlay, hitChance);
    }

    private static WillItLandConfig config(int high, int medium, int low)
    {
        return (WillItLandConfig) Proxy.newProxyInstance(
                WillItLandConfig.class.getClassLoader(),
                new Class<?>[]{WillItLandConfig.class},
                (proxy, method, args) ->
                {
                    if ("colorHighAccuracy".equals(method.getName()))
                    {
                        return high;
                    }
                    if ("colorMediumAccuracy".equals(method.getName()))
                    {
                        return medium;
                    }
                    if ("colorLowAccuracy".equals(method.getName()))
                    {
                        return low;
                    }
                    return method.getDefaultValue() != null ? method.getDefaultValue() : defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType)
    {
        if (!returnType.isPrimitive())
        {
            return null;
        }
        if (returnType == boolean.class)
        {
            return false;
        }
        if (returnType == int.class)
        {
            return 0;
        }
        if (returnType == double.class)
        {
            return 0.0;
        }
        return 0;
    }
}
