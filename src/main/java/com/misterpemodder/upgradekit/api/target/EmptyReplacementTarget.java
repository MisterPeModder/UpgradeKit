package com.misterpemodder.upgradekit.api.target;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The empty replacement target.
 * @since 1.0.0
 */
public final class EmptyReplacementTarget implements IReplacementTarget<Void> {
  protected EmptyReplacementTarget() {
  }

  @Override
  public Void getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
      float hitZ, EnumHand hand) {
    return null;
  }

  @Override
  public String getUnlocalizedName() {
    return "upgradekit.target.empty";
  }
}