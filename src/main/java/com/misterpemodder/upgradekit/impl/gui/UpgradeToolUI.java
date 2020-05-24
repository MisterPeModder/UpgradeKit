package com.misterpemodder.upgradekit.impl.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.UpgradeToolConfig.ReplacementMode;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.tool.IUpgradeTool;
import com.misterpemodder.upgradekit.api.tool.IUpgradeToolProvider;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.gui.PlayerInventoryHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class UpgradeToolUI {
  public static ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
    ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 230, 75).label(50, 8, "upgradekit.gui.title")
        .label(5, 22, "upgradekit.gui.category.replacement_target")
        .label(5, 39, "upgradekit.gui.category.replacement_mode")
        .widget(new CycleButtonWidget(100, 36, 114, 15, ReplacementMode.class, () -> getReplacementMode(holder),
            mode -> setReplacementMode(holder, mode)))
        .label(5, 56, "upgradekit.gui.category.safe_mode").widget(new ToggleButtonWidget(100, 53, 15, 15,
            () -> getSafeMode(holder), pressed -> setSafeMode(holder, pressed)));

    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool != null) {
      List<IReplacementTarget<?>> possibleTargets = ImmutableList.copyOf(tool.getAllPossibleTargets());
      String[] targetNames = new String[possibleTargets.size()];

      for (int i = 0, l = possibleTargets.size(); i < l; ++i)
        targetNames[i] = possibleTargets.get(i).getUnlocalizedName();
      builder.widget(new CycleButtonWidget(100, 19, 114, 15, targetNames, () -> getTargetIndex(holder, possibleTargets),
          index -> setTargetIndex(holder, possibleTargets, index)));
    }
    return builder.build(holder, player);
  }

  private static int getTargetIndex(PlayerInventoryHolder holder, List<IReplacementTarget<?>> possibleTargets) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return 0;
    return Math.max(0, possibleTargets.indexOf(tool.getConfig(stack).getCurrentTarget()));
  }

  private static void setTargetIndex(PlayerInventoryHolder holder, List<IReplacementTarget<?>> possibleTargets,
      int index) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return;

    UpgradeToolConfig config = tool.getConfig(stack);

    if (!possibleTargets.isEmpty() && index < possibleTargets.size())
      config.setCurrentTarget(possibleTargets.get(index));
    tool.setConfig(stack, config);
  }

  private static ReplacementMode getReplacementMode(PlayerInventoryHolder holder) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return ReplacementMode.REPLACE;
    return tool.getConfig(stack).getReplacementMode();
  }

  private static void setReplacementMode(PlayerInventoryHolder holder, ReplacementMode mode) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return;

    UpgradeToolConfig config = tool.getConfig(stack);

    config.setReplacementMode(mode);
    tool.setConfig(stack, config);
  }

  private static boolean getSafeMode(PlayerInventoryHolder holder) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return false;
    return tool.getConfig(stack).isSafeMode();
  }

  private static void setSafeMode(PlayerInventoryHolder holder, boolean safeMode) {
    ItemStack stack = holder.getCurrentItem();
    IUpgradeTool tool = IUpgradeToolProvider.getUpgradeToolForStack(stack);

    if (tool == null)
      return;

    UpgradeToolConfig config = tool.getConfig(stack);

    config.setSafeMode(safeMode);
    tool.setConfig(stack, config);
  }
}