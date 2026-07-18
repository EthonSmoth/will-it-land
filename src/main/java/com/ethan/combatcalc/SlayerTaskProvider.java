package com.ethan.combatcalc;

import com.google.inject.ImplementedBy;

@ImplementedBy(RuneLiteSlayerTaskProvider.class)
public interface SlayerTaskProvider
{
    String getTaskName();
}
