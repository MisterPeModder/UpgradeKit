package com.misterpemodder.upgradekit.api.behavior;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author MisterPeModder
 * @param <T> The type of object to replace.
 * @param <R> The type of replacement object.
 * @since 1.0.0
 */
public interface IReplacementBehavior<T, R> {
  /**
   * Gets a replacement object from an {@link ItemStack}.
   * 
   * @param stack The stack.
   * @return The replacement object, may be null
   * @since 1.0.0
   */
  @Nullable
  R getReplacementFromStack(ItemStack stack);

  /**
   * @param object The replacement object.
   * @return The unlocalized name of this replecement object.
   * @since 1.0.0
   */
  String getUnlocalizedNameForReplacement(R object);

  /**
   * Queries if the passed object can be replaced by something else.
   * 
   * @param target The object to query, can be null.
   * @return Is there a replacement available? always false if {@code target} is null.
   * @since 1.0.0
   */
  boolean hasReplacements(@Nullable T target);

  /**
   * Replaces {@code toReplace} by  {@code replacement}.
   * This method assumes that {@code this.hasReplacements(toReplace) == true}.
   * 
   * @param config           The replacement configuration.
   * @param player           The player, may be null.
   * @param world            The world the player is in.
   * @param pos              The position of the target block.
   * @param side             The target side.
   * @param toReplace        The old object to be replaced.
   * @param replacement      The new object.
   * @param replacementStack The item stack the replacement came from.
   * @return <ul>
   *  <li>{@link ReplacementType#NONE}: if {@code replacement} cannot replace {@code toReplace}.</li>
   *  <li>{@link ReplacementType#EQUIVALENT}: if {@code replacement} has the same tier as {@code toReplace}.</li>
   *  <li>{@link ReplacementType#UPGRADE}: if {@code replacement} is higher tier than {@code toReplace}.</li>
   *  <li>{@link ReplacementType#DOWNGRADE}: if {@code replacement} is lower tier than {@code toReplace}.</li>
   * </ul>
   * @since 1.0.0
   */
  ReplacementType replace(@Nullable EntityPlayer player, World world, BlockPos pos, EnumFacing side, T toReplace,
      R replacement, ItemStack replacementStack, boolean simulate);

  /**
   * @return The priority of this behavior
   * @since 1.0.0
   */
  default int getPriority() {
    return 1000;
  }

  /**
   * @since 1.0.0
   */
  enum ReplacementType {
    NONE, EQUIVALENT, UPGRADE, DOWNGRADE
  }
}