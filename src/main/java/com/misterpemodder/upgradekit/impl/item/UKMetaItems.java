package com.misterpemodder.upgradekit.impl.item;

import java.util.List;

import com.misterpemodder.upgradekit.impl.item.UpgradeToolMetaItem.UpgradeToolMetaValueItem;

import gregtech.api.items.metaitem.MetaItem;

public class UKMetaItems {
  public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

  public static UpgradeToolMetaValueItem UPGRADE_TOOL_LV;
  public static UpgradeToolMetaValueItem UPGRADE_TOOL_MV;
  public static UpgradeToolMetaValueItem UPGRADE_TOOL_HV;

  public static void init() {
    UKMetaItem item = new UKMetaItem();
    UpgradeToolMetaItem tool = new UpgradeToolMetaItem();

    item.setRegistryName("meta_item");
    tool.setRegistryName("upgrade_tool");
  }
}
