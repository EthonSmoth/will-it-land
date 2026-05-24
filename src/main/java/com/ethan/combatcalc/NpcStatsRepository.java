
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
 * Repository for NPC combat stats loaded from JSON.
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
        return npcStats.containsKey(npcName);
    }

    /**
     * Get all loaded NPC profiles.
     */
    public Map<String, NpcCombatProfile> getAllNpcProfiles()
    {
        return new HashMap<>(npcStats);
    }
}
