package com.misterpemodder.upgradekit.impl.tool;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

public class UpgradeToolMV extends UpgradeToolLV {
  @Override
  public int getToolDamagePerBlockBreak(ItemStack stack) {
    return 8;
  }

  @Override
  public float getBaseDamage(ItemStack stack) {
    return 1.5F;
  }

  @Override
  public float getMaxDurabilityMultiplier(ItemStack stack) {
    return 8.0F;
  }

  @Override
  public int getToolDamagePerEntityAttack(ItemStack stack) {
    return 8;
  }

  @Override
  public ItemStack getBrokenStack(ItemStack stack) {
    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    return MetaItems.POWER_UNIT_MV.getChargedStackWithOverride(electricItem);
  }
}
