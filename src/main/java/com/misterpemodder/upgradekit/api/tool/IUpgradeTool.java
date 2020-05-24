package com.misterpemodder.upgradekit.api.tool;

import java.util.Set;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;

import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.item.ItemStack;

/**
 * @since 1.0.0
 */
public interface IUpgradeTool extends IItemComponent {
  /**
   * @param stack The item stack.
   * @return The configuration
   * @since 1.0.0
   */
  UpgradeToolConfig getConfig(ItemStack stack);

  /**
   * Writes the passed configuration to the item stack.
   * 
   * @param stack  The item stack.
   * @param config The upgrade tool configuration.
   * @since 1.0.0
   */
  void setConfig(ItemStack stack, UpgradeToolConfig config);

  /**
   * @return the set of all targets that this tool can handle.
   * @since 1.0.0
   */
  default Set<IReplacementTarget<?>> getAllPossibleTargets() {
    return ReplacementTargets.getAllTargets();
  }
}