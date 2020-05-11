package com.misterpemodder.upgradekit.impl.tools;

import gregtech.common.tools.ToolBase;
import net.minecraft.item.ItemStack;

public class UpgradeKitTool extends ToolBase {
  @Override
  public int getToolDamagePerBlockBreak(ItemStack stack) {
    return 2;
  }

  @Override
  public float getBaseDamage(ItemStack stack) {
    return 2;
  }
}
