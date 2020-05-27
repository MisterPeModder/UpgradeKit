package com.misterpemodder.upgradekit.api.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class CapabilityUpgradeTool {
  @CapabilityInject(IUpgradeTool.class)
  public static Capability<IUpgradeTool> INSTANCE = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(IUpgradeTool.class, new Capability.IStorage<IUpgradeTool>() {
      @Override
      public NBTBase writeNBT(Capability<IUpgradeTool> capability, IUpgradeTool instance, EnumFacing side) {
        throw new UnsupportedOperationException("Not supported");
      }

      @Override
      public void readNBT(Capability<IUpgradeTool> capability, IUpgradeTool instance, EnumFacing side, NBTBase base) {
        throw new UnsupportedOperationException("Not supported");
      }
    }, () -> {
      throw new UnsupportedOperationException("IUpgradeTool does not have a default implementation");
    });
  }
}