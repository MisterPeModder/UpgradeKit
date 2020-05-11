package com.misterpemodder.upgradekit.impl.item;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolMetaItem;

import java.util.List;

public class UKMetaItems {
  public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

  public static ToolMetaItem<?>.MetaToolValueItem UPGRADE_KIT;

  public static void init() {
    UpgradeKitMetaItem kit = new UpgradeKitMetaItem();
    kit.setRegistryName("upgrade_kit");
  }

  /*
  public static void registerOreDict() {
    for (MetaItem<?> item : ITEMS)
      if (item instanceof UpgradeKitMetaItem)
        ((UpgradeKitMetaItem) item).registerOreDict();
  }*/

  public static void registerRecipes() {
    for (MetaItem<?> item : ITEMS)
      if (item instanceof UpgradeKitMetaItem)
        ((UpgradeKitMetaItem) item).registerRecipes();
  }
}
