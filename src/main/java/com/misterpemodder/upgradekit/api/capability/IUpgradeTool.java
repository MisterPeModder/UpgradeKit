package com.misterpemodder.upgradekit.api.capability;

import java.util.Set;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * @since 1.0.0
 */
public interface IUpgradeTool {
  /**
   * @return The item stack this tool is in.
   * @since 1.0.0
   */
  ItemStack getStack();

  /**
   * Applies damages the this tool.
   * 
   * @param damage   The damage amount
   * @param simulate Controls whether the damage should actually be applied
   * @return true if the item can recieve the specified amount of damage, false otherwise.
   * @since 1.0.0
   */
  boolean damage(int damage, boolean simulate);

  /**
   * @param user  The user of this tool
   * @param world The world
   * @return The tool is able to function. (e.g has enough power or is not broken).
   * @since 1.0.0
   */
  boolean canUse(EntityPlayer user, World world);

  /**
   * @return The configuration
   * @since 1.0.0
   */
  default UpgradeToolConfig getConfig() {
    UpgradeToolConfig config = new UpgradeToolConfig();
    NBTTagCompound compound = this.getStack().getSubCompound("UpgradeToolConfig");

    if (compound != null)
      config.readFromNbt(compound);

    Set<IReplacementTarget<?>> possibleTargetIds = this.getAllPossibleTargets();

    if (!possibleTargetIds.contains(config.getCurrentTarget())) {
      if (possibleTargetIds.size() > 0)
        config.setCurrentTarget(possibleTargetIds.toArray(new IReplacementTarget<?>[0])[0]);
      else
        config.setCurrentTarget(IReplacementTarget.EMPTY);
    }
    return config;
  }

  /**
   * Writes the passed configuration to the item stack.
   * 
   * @param config The upgrade tool configuration.
   * @since 1.0.0
   */
  default void setConfig(UpgradeToolConfig config) {
    config.writeToNbt(this.getStack().getOrCreateSubCompound("UpgradeToolConfig"));
  }

  /**
   * @return The set of all targets that this tool can handle.
   * @since 1.0.0
   */
  default Set<IReplacementTarget<?>> getAllPossibleTargets() {
    return IReplacementTarget.getAllTargets();
  }
}