package com.misterpemodder.upgradekit.api.target;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author MisterPeModder
 * @since 1.0.0
 */
public interface IReplacementTarget<T> {
  /**
   * 
   * @param player
   * @param world
   * @param pos
   * @param side
   * @param hitX
   * @param hitY
   * @param hitZ
   * @param hand
   * @return
   * @since 1.0.0
   */
  @Nullable
  T getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
      EnumHand hand);

  /**
   * 
   * @return
   * @since 1.0.0
   */
  Class<T> getTargetClass();
}