package com.misterpemodder.upgradekit.impl.item;

import java.util.List;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolMetaItem;

public class UKMetaItems {
  public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

  public static ToolMetaItem<?>.MetaToolValueItem UPGRADE_TOOL_LV;
  public static ToolMetaItem<?>.MetaToolValueItem UPGRADE_TOOL_MV;
  public static ToolMetaItem<?>.MetaToolValueItem UPGRADE_TOOL_HV;

  public static void init() {
    UKMetaItem item = new UKMetaItem();
    UpgradeToolMetaItem tool = new UpgradeToolMetaItem();

    item.setRegistryName("meta_item");
    tool.setRegistryName("upgrade_tool");
  }
}
