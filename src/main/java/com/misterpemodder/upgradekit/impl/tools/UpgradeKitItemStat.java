package com.misterpemodder.upgradekit.impl.tools;

import java.util.Locale;

import com.misterpemodder.upgradekit.impl.UpgradeKit;

import gregtech.api.GTValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class UpgradeKitItemStat implements IItemBehaviour {
  @Override
  @SuppressWarnings("rawtypes")
  public void onAddedToItem(MetaValueItem metaValueItem) {
    UpgradeKit.logger.info("On added to item: " + metaValueItem.unlocalizedName);
  }

  private static void msg(EntityPlayer player, String text) {
    player.sendMessage(new TextComponentString(text));
  }

  private static void statusMsg(EntityPlayer player, String text) {
    player.sendStatusMessage(new TextComponentString(text), true);
  }

  private void logMetaTileEntity(EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
    if (world != null && !world.isRemote) {
      IBlockState state = world.getBlockState(pos);

      msg(player, "=========");
      msg(player, "block: " + state.getBlock().getRegistryName());
      msg(player, "pos: " + pos);
      msg(player, "side: " + side);

      TileEntity te = world.getTileEntity(pos);

      if (te == null) {
        msg(player, "not a TileEntity");
        return;
      }
      if (!(te instanceof MetaTileEntityHolder)) {
        msg(player, "not a MetaTileEntity holder");
        return;
      }

      MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();

      if (mte == null) {
        msg(player, "no MetaTileEntity within");
      }
      msg(player, "meta name: " + mte.getMetaName());
      msg(player, "meta full name: " + mte.getMetaFullName());
      msg(player, mte.getCoverAtSide(side) != null ? "cover attached" : "no cover");

      if (!(mte instanceof TieredMetaTileEntity)) {
        msg(player, "not a TieredMetaTileEntity");
        return;
      }

      TieredMetaTileEntity tmte = (TieredMetaTileEntity) mte;

      msg(player, "tier: " + tmte.getTier() + " (" + GTValues.VN[tmte.getTier()] + ')');
    }
  }

  @Override
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
      float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = player.getHeldItem(hand);

    if (player.isSneaking()) {
      if (!world.isRemote) {
        statusMsg(player, "Mode set to: " + cycleMode(stack));
      }
      return EnumActionResult.SUCCESS;
    }
    Mode mode = getMode(stack);

    switch (mode) {
      case DEBUG:
        this.logMetaTileEntity(player, world, pos, side);
        return EnumActionResult.SUCCESS;
      default:
        MetaTileEntity mte = BlockMachine.getMetaTileEntity(world, pos);
        ItemStack upgradeStack = findCandidate(player, mte, mode);

        if (upgradeStack == ItemStack.EMPTY) {
          if (!world.isRemote)
            statusMsg(player, TextFormatting.RED + "No replacements found in inventory");
          return EnumActionResult.SUCCESS;
        }
        TieredMetaTileEntity upgradeMte = (TieredMetaTileEntity) MachineItemBlock.getMetaTileEntity(upgradeStack);
        int tierDiff = UpgradeKit.upgradeMap.getTierDifference(upgradeMte, mte);
        String line;

        if (tierDiff > 0)
          line = TextFormatting.GREEN + "Upgraded" + TextFormatting.RESET + " to ";
        else if (tierDiff < 0)
          line = TextFormatting.DARK_RED + "Downgraded" + TextFormatting.RESET + " to ";
        else
          line = TextFormatting.YELLOW + "Replaced" + TextFormatting.RESET + " with ";
        if (!world.isRemote) {
          if (!player.capabilities.isCreativeMode) {
            upgradeStack.shrink(1);
            if (upgradeStack.isEmpty())
              player.inventory.deleteStack(upgradeStack);
          }
          player.sendStatusMessage(new TextComponentString(line)
              .appendSibling(new TextComponentTranslation(upgradeMte.getMetaFullName())).appendSibling(
                  new TextComponentString(String.format(" (%s tier)", GTValues.VN[upgradeMte.getTier()]))),
              true);
        }
        return EnumActionResult.SUCCESS;
    }
  }

  @Override
  public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
      EnumFacing side, float hitX, float hitY, float hitZ) {
    return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
  }

  private static ItemStack findCandidate(EntityPlayer player, MetaTileEntity mte, Mode mode) {
    if (mte == null || !UpgradeKit.upgradeMap.hasUpgrades(mte))
      return ItemStack.EMPTY;
    ItemStack offhandStack;
    ItemStack mainhandStack;

    if (canReplace(mte, (offhandStack = player.getHeldItem(EnumHand.OFF_HAND)), mode)) {
      return offhandStack;
    } else if (canReplace(mte, (mainhandStack = player.getHeldItem(EnumHand.MAIN_HAND)), mode)) {
      return mainhandStack;
    } else {
      for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
        ItemStack itemstack = player.inventory.getStackInSlot(i);

        if (canReplace(mte, itemstack, mode))
          return itemstack;
      }
      return ItemStack.EMPTY;
    }
  }

  private static boolean canReplace(MetaTileEntity mte, ItemStack stack, Mode mode) {
    MetaTileEntity stackMte = MachineItemBlock.getMetaTileEntity(stack);

    if (stackMte != null && UpgradeKit.upgradeMap.canReplace(mte, stackMte)) {
      if (mode == Mode.REPLACE)
        return true;
      int tierDiff = UpgradeKit.upgradeMap.getTierDifference(stackMte, mte);

      return !((mode == Mode.UPGRADE_ONLY && tierDiff <= 0) || (mode == Mode.DOWNGRADE_ONLY && tierDiff >= 0));
    }
    return false;
  }

  private static void setMode(ItemStack stack, Mode mode) {
    NBTTagCompound compound = stack.getTagCompound();

    if (compound == null) {
      compound = new NBTTagCompound();
      stack.setTagCompound(compound);
    }
    compound.setString("Mode", mode.name().toLowerCase(Locale.ROOT));
  }

  private static Mode getMode(ItemStack stack) {
    NBTTagCompound compound = stack.getTagCompound();

    if (compound != null) {
      try {
        return Mode.valueOf(compound.getString("Mode").toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
      }
    }
    return Mode.REPLACE;
  }

  private static Mode cycleMode(ItemStack stack) {
    int ordinal = getMode(stack).ordinal();
    Mode newMode = Mode.values()[ordinal + 1 >= Mode.values().length ? 0 : ordinal + 1];

    setMode(stack, newMode);
    return newMode;
  }

  private static enum Mode {
    REPLACE, UPGRADE_ONLY, DOWNGRADE_ONLY, DEBUG
  }
}