package com.misterpemodder.upgradekit.impl;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = UpgradeKit.MODID,
  name = UpgradeKit.NAME,
  acceptedMinecraftVersions = "[1.12,1.13)",
  dependencies =
    "required:forge@[14.23.5.2838,);"
  + "after:gregtech",
  version = UpgradeKit.VERSION)
public class UpgradeKit {
  public static final String MODID = "upgradekit";
  public static final String NAME = "Upgrade Kit";
  public static final String VERSION = "@VERSION@";

  private static Logger logger;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    logger.info("Upgrade Kit version: " + UpgradeKit.VERSION);
  }
}
