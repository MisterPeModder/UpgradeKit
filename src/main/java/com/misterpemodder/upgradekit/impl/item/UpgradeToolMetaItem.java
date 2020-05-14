package com.misterpemodder.upgradekit.impl.item;

import com.misterpemodder.upgradekit.impl.tool.UpgradeToolBehavior;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolHV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolLV;
import com.misterpemodder.upgradekit.impl.tool.UpgradeToolMV;

import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.ToolMetaItem;

public class UpgradeToolMetaItem extends ToolMetaItem<ToolMetaItem<?>.MetaToolValueItem> {
  @Override
  public void registerSubItems() {
    UKMetaItems.UPGRADE_TOOL_LV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(0, "tool.upgrade_tool.lv")
        .setToolStats(new UpgradeToolLV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(100000L, 1L));
    UKMetaItems.UPGRADE_TOOL_MV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(1, "tool.upgrade_tool.mv")
        .setToolStats(new UpgradeToolMV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(400000L, 2L));
    UKMetaItems.UPGRADE_TOOL_HV = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(2, "tool.upgrade_tool.hv")
        .setToolStats(new UpgradeToolHV()).setFullRepairCost(4).addComponents(new UpgradeToolBehavior())
        .addComponents(ElectricStats.createElectricItem(1600000L, 3L));
  }
}
