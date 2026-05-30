
package com.ethan.combatcalc;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads and provides NPC defensive combat stats from the bundled JSON database.
 *
 * ### Data source
 *   npc_stats.json is packaged inside the jar under src/main/resources/.
 *   It is loaded once at construction time via getClass().getResourceAsStream().
 *   No file system access is performed — the resource lives entirely inside the jar.
 *
 * ### Lookup
 *   NPCs are keyed by name (exact match first, then case-insensitive fallback).
 *   If a name is not found, an empty NpcCombatProfile is returned so the plugin
 *   degrades gracefully, showing a “⚠ NPC Unknown” warning in the overlay.
 *
 * ### Data model
 *   Each entry deserialises into an NpcCombatProfile which holds:
 *     defenceLevel, magicLevel, stabDefence, slashDefence, crushDefence,
 *     rangedDefence, magicDefence.
 */
@Singleton
public class NpcStatsRepository
{
    private final Gson gson;
    private static final Logger logger = Logger.getLogger(NpcStatsRepository.class.getName());
    private static final String NPC_STATS_FILE = "/npc_stats.json";

    private Map<String, NpcCombatProfile> npcStats = new HashMap<>();

    @Inject
    public NpcStatsRepository(Gson gson)
    {
        this.gson = gson;
        loadNpcStats();
    }

    private void loadNpcStats()
    {
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(NPC_STATS_FILE)))
        {
            Type mapType = new TypeToken<Map<String, NpcCombatProfile>>() {}.getType();
            npcStats = gson.fromJson(reader, mapType);
            if (npcStats == null)
            {
                npcStats = new HashMap<>();
            }
        }
        catch (Exception e)
        {
            logger.warning("Failed to load NPC stats from jar: " + e.getMessage());
            npcStats = new HashMap<>();
        }
    }

    /**
     * Retrieve combat profile for an NPC by name.
     */
    public NpcCombatProfile getNpcProfile(String npcName)
    {
        if (npcName == null || npcName.isEmpty())
        {
            return new NpcCombatProfile();
        }

        // Try exact match first
        NpcCombatProfile profile = npcStats.get(npcName);
        if (profile != null)
        {
            return profile;
        }

        // Try case-insensitive match
        for (Map.Entry<String, NpcCombatProfile> entry : npcStats.entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(npcName))
            {
                return entry.getValue();
            }
        }

        // Return empty profile if not found (will set unknown flag in plugin)
        NpcCombatProfile unknownProfile = new NpcCombatProfile(npcName);
        logger.info("NPC profile not found for: " + npcName + ". Using default stats.");
        return unknownProfile;
    }

    /**
     * Check if NPC profile exists.
     */
    public boolean hasNpcProfile(String npcName)
    {
        if (npcName == null || npcName.isEmpty())
        {
            return false;
        }

        if (npcStats.containsKey(npcName))
        {
            return true;
        }

        for (String name : npcStats.keySet())
        {
            if (name.equalsIgnoreCase(npcName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all loaded NPC profiles.
     */
    public Map<String, NpcCombatProfile> getAllNpcProfiles()
    {
        return new HashMap<>(npcStats);
    }
}
