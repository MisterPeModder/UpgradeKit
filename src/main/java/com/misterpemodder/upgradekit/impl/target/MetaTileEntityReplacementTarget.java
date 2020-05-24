package com.misterpemodder.upgradekit.impl.target;

import com.misterpemodder.upgradekit.api.target.IReplacementTarget;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MetaTileEntityReplacementTarget implements IReplacementTarget<MetaTileEntity> {
  @Override
  public MetaTileEntity getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
      float hitY, float hitZ, EnumHand hand) {
    return world != null ? BlockMachine.getMetaTileEntity(world, pos) : null;
  }

  @Override
  public Class<MetaTileEntity> getTargetClass() {
    return MetaTileEntity.class;
  }
}