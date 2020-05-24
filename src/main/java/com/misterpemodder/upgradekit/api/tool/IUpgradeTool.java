package com.misterpemodder.upgradekit.api.tool;

import java.util.Set;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;

import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @since 1.0.0
 */
public interface IUpgradeTool extends IItemComponent {
  /**
   * @param stack The item stack.
   * @return The configuration
   * @since 1.0.0
   */
  default UpgradeToolConfig getConfig(ItemStack stack) {
    UpgradeToolConfig config = new UpgradeToolConfig();
    NBTTagCompound compound = stack.getSubCompound("UpgradeToolConfig");

    if (compound != null)
      config.readFromNbt(compound);

    Set<IReplacementTarget<?>> possibleTargetIds = this.getAllPossibleTargets();

    if (!possibleTargetIds.contains(config.getCurrentTarget())) {
      if (possibleTargetIds.size() > 0)
        config.setCurrentTarget(possibleTargetIds.toArray(new IReplacementTarget<?>[0])[0]);
      else
        config.setCurrentTarget(ReplacementTargets.EMPTY);
    }
    return config;
  }

  /**
   * Writes the passed configuration to the item stack.
   * 
   * @param stack  The item stack.
   * @param config The upgrade tool configuration.
   * @since 1.0.0
   */
  default void setConfig(ItemStack stack, UpgradeToolConfig config) {
    config.writeToNbt(stack.getOrCreateSubCompound("UpgradeToolConfig"));
  }

  /**
   * @return the set of all targets that this tool can handle.
   * @since 1.0.0
   */
  default Set<IReplacementTarget<?>> getAllPossibleTargets() {
    return ReplacementTargets.getAllTargets();
  }
}