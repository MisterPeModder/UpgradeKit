package com.misterpemodder.upgradekit.impl.tool;

import java.util.List;

import com.misterpemodder.upgradekit.api.UpgradeToolConfig;
import com.misterpemodder.upgradekit.api.UpgradeToolConfig.ReplacementMode;
import com.misterpemodder.upgradekit.api.capability.CapabilityUpgradeTool;
import com.misterpemodder.upgradekit.api.capability.IUpgradeTool;
import com.misterpemodder.upgradekit.impl.UpgradeLogic;
import com.misterpemodder.upgradekit.impl.gui.UpgradeToolUI;
import com.misterpemodder.upgradekit.impl.item.UpgradeToolMetaItem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class UpgradeToolComponent implements IItemBehaviour, ItemUIFactory, IItemCapabilityProvider {
  protected final int cooldown;

  public UpgradeToolComponent(int cooldown) {
    this.cooldown = cooldown;
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = player.getHeldItem(hand);

    if (player.isSneaking()) {
      if (!world.isRemote)
        PlayerInventoryHolder.openHandItemUI(player, hand);
      return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
    return ActionResult.newResult(EnumActionResult.PASS, stack);
  }

  @Override
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
      float hitY, float hitZ, EnumHand hand) {
    return UpgradeLogic.useTool(player, world, pos, side, hitX, hitY, hitZ, hand, this.cooldown)
        ? EnumActionResult.SUCCESS
        : EnumActionResult.PASS;
  }

  @Override
  public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
      EnumFacing side, float hitX, float hitY, float hitZ) {
    return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
  }

  @Override
  public void addInformation(ItemStack stack, List<String> lines) {
    IItemBehaviour.super.addInformation(stack, lines);

    IUpgradeTool tool = stack.getCapability(CapabilityUpgradeTool.INSTANCE, null);

    if (tool == null)
      return;

    UpgradeToolConfig config = tool.getConfig();
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

  @Override
  public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
    return UpgradeToolUI.createUI(holder, player);
  }

  @Override
  public ICapabilityProvider createProvider(ItemStack stack) {
    return new CapabilityProvider(stack);
  }

  private static class CapabilityProvider implements ICapabilityProvider, IUpgradeTool {
    private final ItemStack stack;

    public CapabilityProvider(ItemStack stack) {
      this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
      return this.stack;
    }

    @Override
    public boolean damage(int damage, boolean simulate) {
      return GTUtility.doDamageItem(this.stack, damage, simulate);
    }

    @Override
    public boolean canUse(EntityPlayer user, World world) {
      IElectricItem capability = this.stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

      if (capability != null
          && capability.getCharge() < ConfigHolder.energyUsageMultiplier * UpgradeToolMetaItem.DURABILITY_DAMAGE) {
        if (!world.isRemote)
          user.sendStatusMessage(new TextComponentTranslation("upgradekit.error.no_power")
              .setStyle(new Style().setColor(TextFormatting.RED)), true);
        return false;
      }
      return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
      return capability == CapabilityUpgradeTool.INSTANCE;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
      if (capability == CapabilityUpgradeTool.INSTANCE)
        return CapabilityUpgradeTool.INSTANCE.cast(this);
      return null;
    }
  }
}