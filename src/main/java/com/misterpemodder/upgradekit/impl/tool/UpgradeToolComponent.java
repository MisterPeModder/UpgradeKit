package com.misterpemodder.upgradekit.impl.tool;

import java.util.List;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.UpgradeToolConfig.ReplacementMode;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior.ReplacementType;
import com.misterpemodder.upgradekit.api.behavior.ReplacementBehaviors;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.tool.IUpgradeTool;
import com.misterpemodder.upgradekit.impl.gui.UpgradeToolUI;
import com.misterpemodder.upgradekit.impl.item.UpgradeToolMetaItem;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class UpgradeToolComponent implements IItemBehaviour, ItemUIFactory, IUpgradeTool {
  protected final int cooldown;

  public UpgradeToolComponent(int cooldown) {
    this.cooldown = cooldown;
  }

  @Override
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
      float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = player.getHeldItem(hand);
    UpgradeToolConfig config = this.getConfig(stack);

    if (player.isSneaking()) {
      if (!world.isRemote)
        PlayerInventoryHolder.openHandItemUI(player, hand);
      return EnumActionResult.SUCCESS;
    }
    if (player.getCooldownTracker().hasCooldown(stack.getItem()))
      return EnumActionResult.SUCCESS;

    IElectricItem capability = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

    if (capability != null
        && capability.getCharge() < ConfigHolder.energyUsageMultiplier * UpgradeToolMetaItem.DURABILITY_DAMAGE) {
      if (!world.isRemote)
        player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_power")
            .setStyle(new Style().setColor(TextFormatting.RED)), true);
      return EnumActionResult.SUCCESS;
    }

    IReplacementTarget<?> target = config.getCurrentTarget();

    this.tryReplaceTarget(target, stack, config, player, world, pos, side, hitX, hitY, hitZ, hand);
    return EnumActionResult.SUCCESS;
  }

  private <T> void tryReplaceTarget(IReplacementTarget<T> target, ItemStack stack, UpgradeToolConfig config,
      EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
      EnumHand hand) {
    T toReplace = target.getTarget(player, world, pos, side, hitX, hitY, hitZ, hand);

    if (toReplace != null) {
      if (!player.canPlayerEdit(pos, side, stack)) {
        if (!world.isRemote)
          player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.cannot_edit")
              .setStyle(new Style().setColor(TextFormatting.RED)), true);
        return;
      }

      for (IReplacementBehavior<T> behavior : ReplacementBehaviors.getBehaviorsForTarget(target)) {
        if (!behavior.hasReplacements(toReplace))
          continue;

        Pair<ItemStack, ReplacementType> candidate = findCandidate(player, toReplace, config, behavior);

        if (candidate == null) {
          if (!world.isRemote)
            player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_replacements")
                .setStyle(new Style().setColor(TextFormatting.RED)), true);
          return;
        }

        ItemStack upgradeStack = candidate.getLeft();
        ReplacementType type = candidate.getRight();
        T replacement = behavior.getReplacementFromStack(upgradeStack);
        String translationKey;

        if (type == ReplacementType.UPGRADE)
          translationKey = "upgradekit.replace.upgrade";
        else if (type == ReplacementType.DOWNGRADE)
          translationKey = "upgradekit.replace.downgrade";
        else
          translationKey = "upgradekit.replace.equivalent";

        behavior.replace(player, world, pos, side, toReplace, replacement);
        if (!world.isRemote) {
          if (!player.capabilities.isCreativeMode) {
            upgradeStack.shrink(1);
            if (upgradeStack.isEmpty())
              player.inventory.deleteStack(upgradeStack);
            GTUtility.doDamageItem(stack, UpgradeToolMetaItem.DURABILITY_DAMAGE, false);
          }
          player.sendStatusMessage(new TextComponentTranslation(translationKey,
              new TextComponentTranslation(behavior.getUnlocalizedNameForObject(replacement))), true);
        }
        player.getCooldownTracker().setCooldown(stack.getItem(), cooldown);
        return;
      }
    }
    if (!world.isRemote)
      player.sendStatusMessage(new TextComponentTranslation("upgradekit.error.cannot_replace")
          .setStyle(new Style().setColor(TextFormatting.RED)), true);
  }

  @Override
  public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
      EnumFacing side, float hitX, float hitY, float hitZ) {
    return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
  }

  private static <T> Pair<ItemStack, ReplacementType> findCandidate(EntityPlayer player, T toReplace,
      UpgradeToolConfig config, IReplacementBehavior<T> behavior) {
    ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
    ReplacementType type = canReplace(toReplace, stack, behavior, config);

    if (type != ReplacementType.NONE)
      return Pair.of(stack, type);
    stack = player.getHeldItem(EnumHand.MAIN_HAND);
    type = canReplace(toReplace, stack, behavior, config);
    if (type != ReplacementType.NONE)
      return Pair.of(stack, type);

    for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
      stack = player.inventory.getStackInSlot(i);
      type = canReplace(toReplace, stack, behavior, config);
      if (type != ReplacementType.NONE)
        return Pair.of(stack, type);
    }
    return null;
  }

  @Override
  public void addInformation(ItemStack stack, List<String> lines) {
    IItemBehaviour.super.addInformation(stack, lines);

    UpgradeToolConfig config = this.getConfig(stack);
    ReplacementMode mode = config.getReplacementMode();
    TextFormatting modeColor;

    switch (mode) {
      case UPGRADE_ONLY:
        modeColor = TextFormatting.GREEN;
        break;
      case DOWNGRADE_ONLY:
        modeColor = TextFormatting.DARK_RED;
        break;
      case REPLACE:
        modeColor = TextFormatting.YELLOW;
        break;
      default:
        modeColor = TextFormatting.WHITE;
    }

    lines.add("");
    lines.add(I18n.format("upgradekit.tooltip.replacement_target",
        TextFormatting.GOLD + I18n.format(config.getCurrentTarget().getUnlocalizedName())));
    lines.add(I18n.format("upgradekit.tooltip.replacement_mode", modeColor + I18n.format(mode.getName())));
    lines.add(I18n.format("upgradekit.tooltip.safe_mode." + (config.isSafeMode() ? "enabled" : "disabled")));
  }

  private static <T> ReplacementType canReplace(T toReplace, ItemStack replacement, IReplacementBehavior<T> behavior,
      UpgradeToolConfig config) {
    ReplacementType type = behavior.getReplacementType(toReplace, behavior.getReplacementFromStack(replacement));

    switch (config.getReplacementMode()) {
      case UPGRADE_ONLY:
        return type == ReplacementType.UPGRADE ? ReplacementType.UPGRADE : ReplacementType.NONE;
      case DOWNGRADE_ONLY:
        return type == ReplacementType.DOWNGRADE ? ReplacementType.DOWNGRADE : ReplacementType.NONE;
      default:
        return type;
    }
  }

  @Override
  public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
    return UpgradeToolUI.createUI(holder, player);
  }
}