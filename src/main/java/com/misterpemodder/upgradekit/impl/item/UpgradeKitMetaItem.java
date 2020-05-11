package com.misterpemodder.upgradekit.impl.item;

import com.misterpemodder.upgradekit.impl.tools.UpgradeKitTool;

import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

public class UpgradeKitMetaItem extends ToolMetaItem<ToolMetaItem<?>.MetaToolValueItem> {
  @Override
  public void registerSubItems() {
    UKMetaItems.UPGRADE_KIT = (ToolMetaItem<?>.MetaToolValueItem) this.addItem(0, "tool.upgrade_kit")
        .setToolStats(new UpgradeKitTool()).addOreDict("upgradeKit");
  }

  public void registerRecipes() {
    for (Material material : Material.MATERIAL_REGISTRY) {
      if (material instanceof IngotMaterial // Material has an ingot form...
          && !material.hasFlag(DustMaterial.MatFlags.NO_SMASHING) // ...can be bent...
          && ((IngotMaterial) material).toolDurability != 0) { // and has durability.
        IngotMaterial toolMaterial = (IngotMaterial) material;

        ModHandler.addShapedRecipe(String.format("upgrade_kit_%s", material.toString()),
            UKMetaItems.UPGRADE_KIT.getStackForm(toolMaterial, 1), "whf", "PGP", "PPP", 'P',
            new UnificationEntry(OrePrefix.plate, toolMaterial), 'G',
            new UnificationEntry(OrePrefix.gear, toolMaterial));
      }
    }
  }
}
