package com.misterpemodder.upgradekit.impl.behavior;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IReplacementBehavior<T> {
  void addReplacementCandidate(T candidate);

  /**
   * Gets a replacment object from an {@link ItemStack}.
   * 
   * @param stack The stack.
   * @return The replacement object, may be null
   */
  @Nullable
  T getReplacementFromStack(ItemStack stack);

  /**
   * Queries if the passed object can be replaced by something else.
   * 
   * @param replaceable The object to query, can be null.
   * @return Is there a replacement available? always false if {@code replaceable} is null.
   */
  boolean hasReplacements(@Nullable T replaceable);

  /**
   * Can {@code toReplace} be replaced with {@code replacement}?
   * This method assumes that {@code this.hasRepalcements(toReplace) == true}.
   * 
   * @param toReplace   The object that may be replaceable.
   * @param replacement The replacement candidate, may be null.
   * @return <ul>
   *  <li>{@link ReplacementType#NONE}: if {@code replacement} cannot replace {@code toReplace}.</li>
   *  <li>{@link ReplacementType#EQUIVALENT}: if {@code replacement} has the same tier as {@code toReplace}.</li>
   *  <li>{@link ReplacementType#UPGRADE}: if {@code replacement} is higher tier than {@code toReplace}.</li>
   *  <li>{@link ReplacementType#DOWNGRADE}: if {@code replacement} is lower tier than {@code toReplace}.</li>
   * </ul>
   */
  ReplacementType getReplacementType(T toReplace, @Nullable T replacement);

  /**
   * Replaces {@code toReplace} by  {@code replacement}.
   * This method assumes that {@code this.canReplaceWith(config, toReplace, replacement) == true}.
   * 
   * @param config      The replacement configuration.
   * @param player      The player.
   * @param world       The world the player is in.
   * @param pos         The position of the target block.
   * @param side        The target side.
   * @param toReplace   The old object to be replaced.
   * @param replacement The new object.
   * @return true is operation succeded, false otherwise.
   */
  boolean replace(EntityPlayer player, World world, BlockPos pos, EnumFacing side, T toReplace, T replacement);

  enum ReplacementType {
    NONE, EQUIVALENT, UPGRADE, DOWNGRADE
  }
}