package com.misterpemodder.upgradekit.api.tool;

import javax.annotation.Nullable;

import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @since 1.0.0
 */
public interface IUpgradeToolProvider {
  /**
   * @return The upgrade tool, may be null
   * @since 1.0.0
   */
  @Nullable
  IUpgradeTool getUpgradeTool();

  /**
   * Utility method for retrieving an upgrade tool from an item stack.
   * 
   * @param stack The item stack.
   * @return The upgrade tool, may be null.
   * @since 1.0.0
   */
  @Nullable
  static IUpgradeTool getUpgradeToolForStack(ItemStack stack) {
    Item item = stack.getItem();

    if (item instanceof IUpgradeToolProvider)
      return ((IUpgradeToolProvider) item).getUpgradeTool();
    if (item instanceof MetaItem<?>) {
      MetaItem<?>.MetaValueItem metaValueItem = ((MetaItem<?>) item).getItem(stack);

      if (metaValueItem instanceof IUpgradeToolProvider)
        return ((IUpgradeToolProvider) metaValueItem).getUpgradeTool();
    }
    return null;
  }
}