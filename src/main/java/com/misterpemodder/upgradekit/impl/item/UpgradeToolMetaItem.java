package com.misterpemodder.upgradekit.impl.item;

import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolComponent;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolHV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolLV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolMV;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class UpgradeToolMetaItem extends ToolMetaItem<ToolMetaItem<?>.MetaToolValueItem> {
  public static final int DURABILITY_DAMAGE = 50;

  @Override
  @SuppressWarnings("deprecation")
  public void registerSubItems() {
    UKMetaItems.UPGRADE_TOOL_LV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(0, "tool.upgrade_tool.lv")
        .setToolStats(new UpgradeToolLV()).setFullRepairCost(4).addStats(new UpgradeToolComponent(25))
        .addStats(ElectricStats.createElectricItem(100000L, 1L)).addStats(UpgradeToolDurabilityManager.INSTANCE)
        .setModelAmount(2);
    UKMetaItems.UPGRADE_TOOL_MV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(1, "tool.upgrade_tool.mv")
        .setToolStats(new UpgradeToolMV()).setFullRepairCost(4).addStats(new UpgradeToolComponent(15))
        .addStats(ElectricStats.createElectricItem(400000L, 2L)).addStats(UpgradeToolDurabilityManager.INSTANCE)
        .setModelAmount(2);
    UKMetaItems.UPGRADE_TOOL_HV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(2, "tool.upgrade_tool.hv")
        .setToolStats(new UpgradeToolHV()).setFullRepairCost(4).addStats(new UpgradeToolComponent(10))
        .addStats(ElectricStats.createElectricItem(1600000L, 3L)).addStats(UpgradeToolDurabilityManager.INSTANCE)
        .setModelAmount(2);
    ;
  }

  @Override
  protected int getModelIndex(ItemStack stack) {
    return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)
        .getCharge() <= (DURABILITY_DAMAGE * ConfigHolder.energyUsageMultiplier) ? 0 : 1;
  }

  @Override
  public int getRGBDurabilityForDisplay(ItemStack stack) {
    ToolMetaItem<?>.MetaToolValueItem item = this.getItem(stack);
    IItemDurabilityManager manager;

    if (item != null && (manager = item.getDurabilityManager()) != null)
      return manager.getRGBDurabilityForDisplay(stack);
    return super.getRGBDurabilityForDisplay(stack);
  }

  public static class UpgradeToolDurabilityManager implements IItemDurabilityManager {
    public static UpgradeToolDurabilityManager INSTANCE = new UpgradeToolDurabilityManager();

    private UpgradeToolDurabilityManager() {
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
      IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

      return 1.0 - (electricItem.getCharge() / (electricItem.getMaxCharge() * 1.0));
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
      float charge = 1.0F - (float) getDurabilityForDisplay(stack);
      int color = MathHelper.hsvToRGB(Math.max(0.0F, 1.0F / (-5.0F * charge - 2.1F) + 0.47F), 1.0F, 1.0F);

      if (charge < 0.11D)
        return UpgradeKit.proxy.blinkRGBColor(color);
      return color;
    }

    @Override
    public boolean showsDurabilityBar(ItemStack stack) {
      return true;
    }
  }
}
