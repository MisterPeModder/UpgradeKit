package com.misterpemodder.upgradekit.impl.target;

import com.misterpemodder.upgradekit.api.target.IReplacementTarget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityReplacementTarget implements IReplacementTarget<TileEntity> {
  @Override
  public TileEntity getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
      float hitZ, EnumHand hand) {
    return world.getTileEntity(pos);
  }

  @Override
  public String getUnlocalizedName() {
    return "upgradekit.target.tile_entity";
  }
}