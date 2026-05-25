package com.ethan.combatcalc;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class WillItLandPluginTest
{
    @Test
    public void providesPluginConfig()
    {
        boolean hasConfigProvider = false;

        for (Method method : WillItLandPlugin.class.getDeclaredMethods())
        {
            boolean providesWillItLandConfig = method.isAnnotationPresent(Provides.class)
                    && method.getReturnType().equals(WillItLandConfig.class);
            boolean acceptsConfigManager = method.getParameterCount() == 1
                    && method.getParameterTypes()[0].equals(ConfigManager.class);

            if (providesWillItLandConfig && acceptsConfigManager)
            {
                hasConfigProvider = true;
                break;
            }
        }

        assertTrue("WillItLandPlugin should provide WillItLandConfig through ConfigManager", hasConfigProvider);
    }
}
