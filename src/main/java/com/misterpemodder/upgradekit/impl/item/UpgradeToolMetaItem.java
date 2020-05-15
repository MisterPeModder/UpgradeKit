package com.misterpemodder.upgradekit.impl.item;

import com.misterpemodder.upgradekit.impl.item.UpgradeToolMetaItem.UpgradeToolMetaValueItem;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolBehavior;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolHV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolLV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolMV;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.item.ItemStack;

public class UpgradeToolMetaItem extends ToolMetaItem<UpgradeToolMetaValueItem> {
  @Override
  public void registerSubItems() {
    UKMetaItems.UPGRADE_TOOL_LV = (UpgradeToolMetaValueItem) this.addItem(0, "tool.upgrade_tool.lv")
        .setToolStats(new UpgradeToolLV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(100000L, 1L));
    UKMetaItems.UPGRADE_TOOL_MV = (UpgradeToolMetaValueItem) this.addItem(1, "tool.upgrade_tool.mv")
        .setToolStats(new UpgradeToolMV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(400000L, 2L));
    UKMetaItems.UPGRADE_TOOL_HV = (UpgradeToolMetaValueItem) this.addItem(2, "tool.upgrade_tool.hv")
        .setToolStats(new UpgradeToolHV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(1600000L, 3L));
  }

  @Override
  protected UpgradeToolMetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName) {
    return new UpgradeToolMetaValueItem(metaValue, unlocalizedName);
  }

  @Override
  protected int getModelIndex(ItemStack stack) {
    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

    return electricItem == null || electricItem.getCharge() <= 0 ? 0 : 1;
  }

  public class UpgradeToolMetaValueItem extends ToolMetaItem<?>.MetaToolValueItem {
    protected UpgradeToolMetaValueItem(int metaValue, String unlocalizedName) {
      super(metaValue, unlocalizedName);
    }

    @Override
    public int getModelAmount() {
      return 2;
    }
  }
}
