package com.misterpemodder.upgradekit.impl.target;

import com.misterpemodder.upgradekit.api.target.IReplacementTarget;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoverReplacementTarget implements IReplacementTarget<CoverBehavior> {
  @Override
  public CoverBehavior getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
      float hitY, float hitZ, EnumHand hand) {
    TileEntity te = world.getTileEntity(pos);
    ICoverable coverable;

    if (te == null || (coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null)) == null)
      return null;

    EnumFacing coverSide = ICoverable.rayTraceCoverableSide(coverable, player);

    return coverSide == null ? null : coverable.getCoverAtSide(coverSide);
  }

  @Override
  public String getUnlocalizedName() {
    return "upgradekit.target.cover";
  }
}