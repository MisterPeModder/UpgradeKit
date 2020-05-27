package com.misterpemodder.upgradekit.impl;

import java.util.Locale;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior.ReplacementType;
import com.misterpemodder.upgradekit.api.capability.CapabilityUpgradeTool;
import com.misterpemodder.upgradekit.api.capability.IUpgradeTool;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.impl.item.UpgradeToolMetaItem;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.common.ConfigHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public final class UpgradeLogic {
  public static boolean useTool(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
      float hitZ, EnumHand hand, int cooldown) {
    ItemStack stack = player.getHeldItem(hand);
    IUpgradeTool tool = stack.getCapability(CapabilityUpgradeTool.INSTANCE, null);

    if (tool == null)
      return false;

    UpgradeToolConfig config = tool.getConfig();

    if (world == null)
      return false;
    if (player.isSneaking()) {
      if (!world.isRemote)
        PlayerInventoryHolder.openHandItemUI(player, hand);
      return true;
    }
    if (player.getCooldownTracker().hasCooldown(stack.getItem()))
      return true;

    IElectricItem capability = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

    if (capability != null
        && capability.getCharge() < ConfigHolder.energyUsageMultiplier * UpgradeToolMetaItem.DURABILITY_DAMAGE) {
      if (!world.isRemote)
        player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_power")
            .setStyle(new Style().setColor(TextFormatting.RED)), true);
      return true;
    }

    IReplacementTarget<?> target = config.getCurrentTarget();

    player.getCooldownTracker().setCooldown(stack.getItem(), cooldown);
    tryReplaceTarget(tool, target, stack, config, player, world, pos, side, hitX, hitY, hitZ, hand);
    return true;
  }

  private static <T> void tryReplaceTarget(IUpgradeTool tool, IReplacementTarget<T> target, ItemStack stack,
      UpgradeToolConfig config, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
      float hitZ, EnumHand hand) {
    T toReplace = target.getTarget(player, world, pos, side, hitX, hitY, hitZ, hand);

    if (toReplace == null) {
      if (!world.isRemote)
        player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_target")
            .setStyle(new Style().setColor(TextFormatting.RED)), true);
      return;
    }
    if (toReplace != null) {
      if (!player.canPlayerEdit(pos, side, stack)) {
        if (!world.isRemote)
          player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.cannot_edit")
              .setStyle(new Style().setColor(TextFormatting.RED)), true);
        return;
      }

      for (IReplacementBehavior<T, ?> behavior : IReplacementBehavior.getBehaviorsForTarget(target)) {
        if (!behavior.hasReplacements(toReplace))
          continue;

        Pair<ReplacementType, ItemStack> candidate = findCandidate(player, config, world, pos, side, toReplace,
            behavior);
        ReplacementType expectedType = candidate.getLeft();
        ItemStack replacementStack = candidate.getRight();

        if (expectedType == ReplacementType.NONE) {
          if (!world.isRemote)
            player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_replacements")
                .setStyle(new Style().setColor(TextFormatting.RED)), true);
          return;
        }

        Pair<ReplacementType, String> results = performReplacement(player, world, pos, side, toReplace,
            replacementStack, behavior, expectedType);
        ReplacementType type = results.getLeft();
        String replacementMessage = results.getRight();

        if (!world.isRemote) {
          if (!player.capabilities.isCreativeMode)
            tool.damage(UpgradeToolMetaItem.DURABILITY_DAMAGE, false);
          if (type == ReplacementType.NONE) {
            player.sendStatusMessage(
                new TextComponentTranslation(replacementMessage).setStyle(new Style().setColor(TextFormatting.RED)),
                true);
          } else {
            String translationKey;

            if (type == ReplacementType.UPGRADE)
              translationKey = "upgradekit.replace.upgrade";
            else if (type == ReplacementType.DOWNGRADE)
              translationKey = "upgradekit.replace.downgrade";
            else
              translationKey = "upgradekit.replace.equivalent";
            player.sendStatusMessage(
                new TextComponentTranslation(translationKey, new TextComponentTranslation(replacementMessage)), true);
          }
        }
        return;
      }
    }
    if (!world.isRemote) {
      String errorMsg = "upgradekit.error.cannot_";

      switch (config.getReplacementMode()) {
        case UPGRADE_ONLY:
          errorMsg += "upgrade";
          break;
        case DOWNGRADE_ONLY:
          errorMsg += "downgrade";
          break;
        default:
          errorMsg += "replace";
      }
      player.sendStatusMessage(
          new TextComponentTranslation(errorMsg).setStyle(new Style().setColor(TextFormatting.RED)), true);
    }
  }

  private static <T, R> Pair<ReplacementType, ItemStack> findCandidate(EntityPlayer player, UpgradeToolConfig config,
      World world, BlockPos pos, EnumFacing side, T toReplace, IReplacementBehavior<T, R> behavior) {
    ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
    ReplacementType type = canReplace(player, config, world, pos, side, toReplace, behavior, stack);

    if (type != ReplacementType.NONE)
      return Pair.of(type, stack);
    stack = player.getHeldItem(EnumHand.MAIN_HAND);
    type = canReplace(player, config, world, pos, side, toReplace, behavior, stack);
    if (type != ReplacementType.NONE)
      return Pair.of(type, stack);

    for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
      stack = player.inventory.getStackInSlot(i);
      type = canReplace(player, config, world, pos, side, toReplace, behavior, stack);
      if (type != ReplacementType.NONE)
        return Pair.of(type, stack);
    }
    return Pair.of(ReplacementType.NONE, ItemStack.EMPTY);
  }

  private static <T, R> ReplacementType canReplace(EntityPlayer player, UpgradeToolConfig config, World world,
      BlockPos pos, EnumFacing side, T toReplace, IReplacementBehavior<T, R> behavior, ItemStack replacementStack) {
    R replacement = behavior.getReplacementFromStack(replacementStack);

    if (replacement == null)
      return ReplacementType.NONE;

    ReplacementType type = behavior.replace(player, world, pos, side, toReplace, replacement, replacementStack, true);

    switch (config.getReplacementMode()) {
      case UPGRADE_ONLY:
        return type == ReplacementType.UPGRADE ? ReplacementType.UPGRADE : ReplacementType.NONE;
      case DOWNGRADE_ONLY:
        return type == ReplacementType.DOWNGRADE ? ReplacementType.DOWNGRADE : ReplacementType.NONE;
      default:
        return type;
    }
  }

  private static <T, R> Pair<ReplacementType, String> performReplacement(EntityPlayer player, World world, BlockPos pos,
      EnumFacing side, T toReplace, ItemStack replacementStack, IReplacementBehavior<T, R> behavior,
      ReplacementType expectedType) {
    R replacement = behavior.getReplacementFromStack(replacementStack);
    ReplacementType type = behavior.replace(player, world, pos, side, toReplace, replacement, replacementStack, false);
    String key;

    if (type != ReplacementType.NONE)
      key = behavior.getUnlocalizedNameForReplacement(replacement);
    else
      key = "upgradekit.error.failure." + expectedType.name().toLowerCase(Locale.ROOT);
    return Pair.of(type, key);
  }
}