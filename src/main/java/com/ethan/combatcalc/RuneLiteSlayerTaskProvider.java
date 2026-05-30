package com.ethan.combatcalc;

import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RuneLiteSlayerTaskProvider implements SlayerTaskProvider
{
    private static final int BOSS_TASK_ID = 98;

    private final Client client;

    @Inject
    public RuneLiteSlayerTaskProvider(Client client)
    {
        this.client = client;
    }

    @Override
    public String getTaskName()
    {
        if (client == null || client.getVarpValue(VarPlayerID.SLAYER_COUNT) <= 0)
        {
            return null;
        }

        try
        {
            int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
            int taskRow = resolveTaskRow(taskId);
            if (taskRow <= 0)
            {
                return null;
            }

            Object[] taskName = client.getDBTableField(taskRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
            return taskName != null && taskName.length > 0 ? String.valueOf(taskName[0]) : null;
        }
        catch (RuntimeException ex)
        {
            return null;
        }
    }

    private int resolveTaskRow(int taskId)
    {
        if (taskId == BOSS_TASK_ID)
        {
            List<Integer> bossRows = client.getDBRowsByValue(
                    DBTableID.SlayerTaskSublist.ID,
                    DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID,
                    0,
                    client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));
            if (bossRows == null || bossRows.isEmpty())
            {
                return -1;
            }

            Object[] taskRow = client.getDBTableField(bossRows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0);
            return taskRow != null && taskRow.length > 0 && taskRow[0] instanceof Integer ? (Integer) taskRow[0] : -1;
        }

        List<Integer> taskRows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
        return taskRows == null || taskRows.isEmpty() ? -1 : taskRows.get(0);
    }
}
