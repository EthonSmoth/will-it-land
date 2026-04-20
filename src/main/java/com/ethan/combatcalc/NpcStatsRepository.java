
package com.ethan.combatcalc;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.RuneLite;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    @Inject
    private Gson gson;
    private static final Logger logger = Logger.getLogger(NpcStatsRepository.class.getName());
    private static final String NPC_STATS_FILE = "/npc_stats.json";

    private Map<String, NpcCombatProfile> npcStats = new HashMap<>();

    public NpcStatsRepository()
    {
        loadNpcStats();
    }

    private void loadNpcStats()
    {
        try {
            // Try to load from jar resources first
            try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(NPC_STATS_FILE))) {
                Type mapType = new TypeToken<Map<String, NpcCombatProfile>>() {}.getType();
                npcStats = gson.fromJson(reader, mapType);
                if (npcStats == null) {
                    npcStats = new HashMap<>();
                }
                logger.info("Loaded " + npcStats.size() + " NPC combat profiles from jar");
                return;
            } catch (Exception e) {
                logger.warning("Failed to load NPC stats from jar: " + e.getMessage());
            }
            // Fallback to user directory
            File configDir = RuneLite.RUNELITE_DIR;
            File statsFile = new File(configDir, "npc_stats.json");
            if (!statsFile.exists()) {
                logger.warning("NPC stats file not found: " + statsFile.getAbsolutePath());
                return;
            }
            Type mapType = new TypeToken<Map<String, NpcCombatProfile>>() {}.getType();
            npcStats = gson.fromJson(new FileReader(statsFile), mapType);
            if (npcStats == null) {
                npcStats = new HashMap<>();
            }
            logger.info("Loaded " + npcStats.size() + " NPC combat profiles from user directory");
        } catch (IOException e) {
            logger.warning("Failed to load NPC stats: " + e.getMessage());
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
