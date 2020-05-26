package com.misterpemodder.upgradekit.impl.tool;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.items.MetaItems;
import gregtech.common.tools.ToolBase;
import net.minecraft.item.ItemStack;

public class UpgradeToolLV extends ToolBase {
  @Override
  public int getToolDamagePerBlockBreak(ItemStack stack) {
    return 4;
  }

  @Override
  public float getMaxDurabilityMultiplier(ItemStack stack) {
    return 12.0F;
  }

  @Override
  public ItemStack getBrokenStack(ItemStack stack) {
    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    return MetaItems.POWER_UNIT_LV.getChargedStackWithOverride(electricItem);
  }
}
