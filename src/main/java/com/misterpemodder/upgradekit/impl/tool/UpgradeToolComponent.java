package com.misterpemodder.upgradekit.impl.tool;

import java.util.List;
import java.util.Set;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior.ReplacementType;
import com.misterpemodder.upgradekit.api.behavior.ReplacementBehaviors;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;
import com.misterpemodder.upgradekit.api.tool.IUpgradeTool;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

    if (player.getCooldownTracker().hasCooldown(stack.getItem()))
      return EnumActionResult.SUCCESS;
    //if (player.isSneaking()) {
    //  if (!world.isRemote)
    //    PlayerInventoryHolder.openHandItemUI(player, hand);
    //  return EnumActionResult.SUCCESS;
    //}

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
    lines.add(I18n.format(this.getConfig(stack).getReplacementMode().getUnlocalizedName()));
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
  public UpgradeToolConfig getConfig(ItemStack stack) {
    UpgradeToolConfig config = new UpgradeToolConfig();
    NBTTagCompound compound = stack.getSubCompound("UpgradeToolConfig");

    if (compound != null)
      config.readFromNbt(compound);

    Set<IReplacementTarget<?>> possibleTargetIds = this.getAllPossibleTargets();

    if (!possibleTargetIds.contains(config.getCurrentTarget())) {
      if (possibleTargetIds.size() > 0)
        config.setCurrentTarget(possibleTargetIds.toArray(new IReplacementTarget<?>[0])[0]);
      else
        config.setCurrentTarget(ReplacementTargets.EMPTY);
    }
    return config;
  }

  @Override
  public void setConfig(ItemStack stack, UpgradeToolConfig config) {
    config.writeToNbt(stack.getOrCreateSubCompound("UpgradeToolConfig"));
  }

  @Override
  public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
    // TODO: Replace texture by EmptyTextureArea.INSTANCE
    return ModularUI.builder(GuiTextures.BACKGROUND, 176, 60).label(9, 8, "upgradekit.gui.title")
        .label(0, 18, "upgradekit.gui.category.selection_mode").label(0, 28, "upgradekit.gui.category.replacement_type")
        .label(0, 38, "upgradekit.gui.category.safe_mode").build(holder, player);
  }

}