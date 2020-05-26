package com.misterpemodder.upgradekit.api;

import java.util.Locale;

import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

/**
 * @author MisterPeModder
 * @since 1.0.0
 */
public class UpgradeToolConfig {
  protected IReplacementTarget<?> currentTarget;
  protected boolean safeMode;
  protected ReplacementMode replacementMode;

  public UpgradeToolConfig() {
    this.currentTarget = ReplacementTargets.EMPTY;
    this.safeMode = true;
    this.replacementMode = ReplacementMode.REPLACE;
  }

  /**
   * @return The current selected target or {@link ReplacementTargets#EMPTY} if there is none.
   * @since 1.0.0
   */
  public IReplacementTarget<?> getCurrentTarget() {
    return this.currentTarget;
  }

  /**
   * Sets the selected target
   * 
   * @param currentTarget
   * @since 1.0.0
   */
  public void setCurrentTarget(IReplacementTarget<?> currentTarget) {
    this.currentTarget = currentTarget;
  }

  /**
   * @return Is this tool in safe mode?
   * @since 1.0.0
   */
  public boolean isSafeMode() {
    return this.safeMode;
  }

  /**
   * Enables/disables safe mode.
   * 
   * @param safeMode
   * @since 1.0.0
   */
  public void setSafeMode(boolean safeMode) {
    this.safeMode = safeMode;
  }

  /**
   * @return The current replacement mode.
   * @since 1.0.0
   */
  public ReplacementMode getReplacementMode() {
    return this.replacementMode;
  }

  /**
   * Sets the replacement mode.
   * 
   * @param replacementMode The new mode.
   * @since 1.0.0
   */
  public void setReplacementMode(ReplacementMode replacementMode) {
    this.replacementMode = replacementMode;
  }

  public void readFromNbt(NBTTagCompound compound) {
    ResourceLocation targetId = new ResourceLocation(compound.getString("SelectedTarget"));
    IReplacementTarget<?> target = ReplacementTargets.REGISTRY.getObject(targetId);

    this.setCurrentTarget(target != null ? target : ReplacementTargets.EMPTY);
    try {
      this.setReplacementMode(ReplacementMode.valueOf(compound.getString("ReplacementMode").toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      this.setReplacementMode(ReplacementMode.REPLACE);
    }
    this.setSafeMode(compound.getBoolean("SafeMode"));
  }

  public void writeToNbt(NBTTagCompound compound) {
    IReplacementTarget<?> target = this.getCurrentTarget();
    ResourceLocation targetId = ReplacementTargets.REGISTRY.getNameForObject(target);

    if (targetId == null)
      throw new IllegalStateException(
          "Tried to serialize UpgradeToolConfig with an unregistered IReplacementTarget: " + target);
    compound.setString("SelectedTarget", targetId.toString());
    compound.setString("ReplacementMode", this.getReplacementMode().name().toLowerCase(Locale.ROOT));
    compound.setBoolean("SafeMode", this.isSafeMode());
  }

  /**
   * @since 1.0.0
   */
  public static enum ReplacementMode implements IStringSerializable {
    /**
     * Allows any type of replacement.
     * @since 1.0.0
     */
    REPLACE,

    /**
     * Allows only upgrades
     * @since 1.0.0
     */
    UPGRADE_ONLY,

    /**
     * Allows only downgrades
     * @since 1.0.0
     */
    DOWNGRADE_ONLY;

    public String getName() {
      return "upgradekit.mode." + this.name().toLowerCase();
    }
  }
}