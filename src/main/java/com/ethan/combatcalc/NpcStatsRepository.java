
package com.ethan.combatcalc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;

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
    private static final Logger logger = LoggerFactory.getLogger(NpcStatsRepository.class);
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
        InputStream resourceStream = getClass().getResourceAsStream(NPC_STATS_FILE);
        if (resourceStream == null)
        {
            logger.warn("Failed to load NPC stats from jar: resource {} was not found", NPC_STATS_FILE);
            npcStats = new HashMap<>();
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8))
        {
            Type mapType = new TypeToken<Map<String, NpcCombatProfile>>() {}.getType();
            Map<String, NpcCombatProfile> loadedStats = gson.fromJson(reader, mapType);
            if (loadedStats == null)
            {
                npcStats = new HashMap<>();
                return;
            }

            npcStats = new HashMap<>();
            for (Map.Entry<String, NpcCombatProfile> entry : loadedStats.entrySet())
            {
                npcStats.put(normalizeNpcName(entry.getKey()), entry.getValue());
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to load NPC stats from jar", e);
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

        NpcCombatProfile profile = npcStats.get(normalizeNpcName(npcName));
        if (profile != null)
        {
            return profile;
        }

        NpcCombatProfile unknownProfile = new NpcCombatProfile(npcName);
        logger.info("NPC profile not found for: {}. Using default stats.", npcName);
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

        return npcStats.containsKey(normalizeNpcName(npcName));
    }

    /**
     * Get all loaded NPC profiles.
     */
    public Map<String, NpcCombatProfile> getAllNpcProfiles()
    {
        return new HashMap<>(npcStats);
    }

    private String normalizeNpcName(String npcName)
    {
        return npcName.toLowerCase(Locale.ROOT).trim();
    }
}
