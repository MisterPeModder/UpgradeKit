package com.misterpemodder.upgradekit.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.misterpemodder.upgradekit.impl.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.impl.behavior.TieredMetaTileEntityReplacementBehavior;
import com.misterpemodder.upgradekit.impl.item.UKMetaItems;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.unification.material.MaterialIconType;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
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

  private static Map<String, IReplacementBehavior<MetaTileEntity>> mteReplacementBehaviors;

  public static MaterialIconType upgradeToolCasingMaterialIconType;
  public static OrePrefix upgradeToolCasingOrePrefix;

  public UpgradeKit() {
    EnumHelper.addEnum(MaterialIconType.class, "toolCasingUpgradeTool", new Class[0]);
    upgradeToolCasingMaterialIconType = MaterialIconType.valueOf("toolCasingUpgradeTool");
    EnumHelper.addEnum(OrePrefix.class, "toolCasingUpgradeTool",
        new Class[] { String.class, long.class, Material.class, MaterialIconType.class, long.class, Predicate.class },
        "Upgrade Kit Casing", GTValues.M, null, upgradeToolCasingMaterialIconType, OrePrefix.Flags.ENABLE_UNIFICATION,
        OrePrefix.Conditions.isToolMaterial);
    upgradeToolCasingOrePrefix = OrePrefix.valueOf("toolCasingUpgradeTool");
    upgradeToolCasingOrePrefix.maxStackSize = 16;
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    UKMetaItems.init();

    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    mteReplacementBehaviors = buildMteReplacementMap();
  }

  private static Map<String, IReplacementBehavior<MetaTileEntity>> buildMteReplacementMap() {
    Map<String, IReplacementBehavior<MetaTileEntity>> map = new HashMap<>();
    long startTime = System.currentTimeMillis();

    logger.info("Building upgrade maps...");
    for (MetaTileEntity mte : GregTechAPI.META_TILE_ENTITY_REGISTRY) {
      if (mte instanceof TieredMetaTileEntity) {
        String id = UpgradeKit.getMachineId(mte);
        IReplacementBehavior<MetaTileEntity> behavior = map.get(id);

        if (behavior == null) {
          behavior = new TieredMetaTileEntityReplacementBehavior(id);
          map.put(id, behavior);
        }
        behavior.addReplacementCandidate(mte);
      }
    }
    logger.info("Built upgrade maps in " + (System.currentTimeMillis() - startTime) + "ms");
    return map;
  }

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    RecipeHandler.init();
  }

  public static String getMachineId(MetaTileEntity mte) {
    return mte.metaTileEntityId.getResourcePath().split("\\.")[0];
  }

  @Nullable
  public static IReplacementBehavior<MetaTileEntity> getReplacementBehaviorForMte(@Nullable MetaTileEntity mte) {
    return mte == null ? null : mteReplacementBehaviors.get(getMachineId(mte));
  }
}
