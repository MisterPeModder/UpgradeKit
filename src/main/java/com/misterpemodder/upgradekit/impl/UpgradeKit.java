package com.misterpemodder.upgradekit.impl;

import java.util.function.Predicate;

import com.misterpemodder.upgradekit.api.UpgradeKitAPI;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.api.capability.CapabilityUpgradeTool;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.impl.behavior.GenericCoverReplacementBehavior;
import com.misterpemodder.upgradekit.impl.behavior.ReplacementBehaviors;
import com.misterpemodder.upgradekit.impl.behavior.TieredMetaTileEntityReplacementBehavior;
import com.misterpemodder.upgradekit.impl.item.UKMetaItems;
import com.misterpemodder.upgradekit.impl.proxy.CommonProxy;
import com.misterpemodder.upgradekit.impl.target.ReplacementTargets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregtech.api.GTValues;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.MaterialIconType;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = UpgradeKitAPI.MOD_ID, name = UpgradeKit.NAME, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required:forge@[14.23.5.2838,);"
    + "required-after:gregtech", version = UpgradeKit.VERSION)
public class UpgradeKit {
  public static final String NAME = "Upgrade Kit";
  public static final String VERSION = "@VERSION@";

  public static Logger logger = LogManager.getLogger(UpgradeKitAPI.MOD_ID);

  public static MaterialIconType upgradeToolCasingMaterialIconType;
  public static OrePrefix upgradeToolCasingOrePrefix;

  @SidedProxy(modId = UpgradeKitAPI.MOD_ID, clientSide = "com.misterpemodder.upgradekit.impl.proxy.ClientProxy", serverSide = "com.misterpemodder.upgradekit.impl.proxy.ServerProxy")
  public static CommonProxy proxy;

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
    CapabilityUpgradeTool.register();
    UKMetaItems.init();
    ReplacementTargets.init();
    ReplacementBehaviors.init();

    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    IReplacementBehavior.REGISTRY.freeze();
    IReplacementTarget.REGISTRY.freeze();
    TieredMetaTileEntityReplacementBehavior.buildCandidatesMap();
    GenericCoverReplacementBehavior.buildCandidatesMap();
  }

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    RecipeHandler.init();
  }

  public static String getMachineTypeId(MetaTileEntity mte) {
    return mte.metaTileEntityId.getResourcePath().split("\\.")[0];
  }

  public static String getCoverTypeId(CoverDefinition cover) {
    return cover.getCoverId().getResourcePath().split("\\.")[0];
  }

  public static ResourceLocation newId(String name) {
    return new ResourceLocation(UpgradeKitAPI.MOD_ID, name);
  }

  public static void insertOrDropStack(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
    if (!player.inventory.addItemStackToInventory(stack))
      Block.spawnAsEntity(world, pos, stack);
  }
}
