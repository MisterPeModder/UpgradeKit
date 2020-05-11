package com.misterpemodder.upgradekit.impl;

import com.misterpemodder.upgradekit.impl.item.UKMetaItems;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = UpgradeKit.MODID, name = UpgradeKit.NAME, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required:forge@[14.23.5.2838,);"
    + "required-after:gregtech", version = UpgradeKit.VERSION)
public class UpgradeKit {
  public static final String MODID = "upgradekit";
  public static final String NAME = "Upgrade Kit";
  public static final String VERSION = "@VERSION@";

  public static Logger logger = LogManager.getLogger(MODID);

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    UKMetaItems.init();

    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    UKMetaItems.registerRecipes();
  }
}
