package com.misterpemodder.upgradekit.impl.item;

import com.misterpemodder.upgradekit.impl.UpgradeKit;

import gregtech.api.items.materialitem.MaterialMetaItem;

public class UKMetaItem extends MaterialMetaItem {
  public UKMetaItem() {
    super(UpgradeKit.upgradeToolCasingOrePrefix);
  }
}