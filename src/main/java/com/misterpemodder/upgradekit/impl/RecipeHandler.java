package com.misterpemodder.upgradekit.impl;

import com.misterpemodder.upgradekit.impl.item.UKMetaItems;

import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.SolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.loaders.oreprocessing.ToolRecipeHandler;
import net.minecraft.item.ItemStack;

public final class RecipeHandler {
  public static void init() {
    UpgradeKit.upgradeToolCasingOrePrefix.addProcessingHandler(SolidMaterial.class,
        RecipeHandler::processUpgradeToolCasing);
  }

  public static void processUpgradeToolCasing(OrePrefix prefix, SolidMaterial material) {
    ToolRecipeHandler.processSimpleElectricToolHead(prefix, material, new ToolMetaItem<?>.MetaToolValueItem[] {
        UKMetaItems.UPGRADE_TOOL_LV, UKMetaItems.UPGRADE_TOOL_MV, UKMetaItems.UPGRADE_TOOL_HV });
    ItemStack result = OreDictUnifier.get(UpgradeKit.upgradeToolCasingOrePrefix, material);

    ModHandler.addShapedRecipe(String.format("upgrade_tool_casing_%s", material.toString()), result, "HWF", "PhP",
        "GRG", 'H', new UnificationEntry(OrePrefix.toolHeadHammer, material), 'W',
        new UnificationEntry(OrePrefix.toolHeadWrench, material), 'F',
        new UnificationEntry(OrePrefix.toolHeadFile, material), 'P', new UnificationEntry(OrePrefix.plate, material),
        'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Steel), 'R',
        new UnificationEntry(OrePrefix.ring, Materials.Steel));
  }
}