package com.misterpemodder.upgradekit.api.behavior;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author MisterPeModder
 * @since 1.0.0
 */
public interface IReplacementBehavior<T> {
  /**
   * Gets a replacement object from an {@link ItemStack}.
   * 
   * @param stack The stack.
   * @return The replacement object, may be null
   * @since 1.0.0
   */
  @Nullable
  T getReplacementFromStack(ItemStack stack);

  /**
   * @param object The object.
   * @return The unlocalized name of this object.
   * @since 1.0.0
   */
  String getUnlocalizedNameForObject(T object);

  /**
   * Queries if the passed object can be replaced by something else.
   * 
   * @param replaceable The object to query, can be null.
   * @return Is there a replacement available? always false if {@code replaceable} is null.
   * @since 1.0.0
   */
  boolean hasReplacements(@Nullable T replaceable);

  /**
   * Can {@code toReplace} be replaced with {@code replacement}?
   * This method assumes that {@code this.hasReplacements(toReplace) == true}.
   * 
   * @param toReplace   The object that may be replaceable.
   * @param replacement The replacement candidate, may be null.
   * @return <ul>
   *  <li>{@link ReplacementType#NONE}: if {@code replacement} cannot replace {@code toReplace}.</li>
   *  <li>{@link ReplacementType#EQUIVALENT}: if {@code replacement} has the same tier as {@code toReplace}.</li>
   *  <li>{@link ReplacementType#UPGRADE}: if {@code replacement} is higher tier than {@code toReplace}.</li>
   *  <li>{@link ReplacementType#DOWNGRADE}: if {@code replacement} is lower tier than {@code toReplace}.</li>
   * </ul>
   * @since 1.0.0
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
   * @since 1.0.0
   */
  boolean replace(EntityPlayer player, World world, BlockPos pos, EnumFacing side, T toReplace, T replacement);

  /**
   * @since 1.0.0
   */
  enum ReplacementType {
    NONE, EQUIVALENT, UPGRADE, DOWNGRADE
  }
}