package com.misterpemodder.upgradekit.impl.tool;

import java.util.List;
import java.util.Locale;

import com.misterpemodder.upgradekit.impl.IReplacementConfig;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.impl.behavior.IReplacementBehavior.ReplacementType;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.GTValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
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

public class UpgradeToolBehavior implements IItemBehaviour {
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
      msg(player, "TE data: " + te.writeToNBT(new NBTTagCompound()).toString());
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
    UpgradeConfig config = this.getConfig(stack);

    if (player.isSneaking()) {
      if (!world.isRemote) {
        cycleMode(config);
        player.sendStatusMessage(new TextComponentTranslation(
            config.debug ? "upgradekit.mode.debug.name" : config.mode.getUnlocalizedName()), true);
        this.setConfig(stack, config);
      }
      return EnumActionResult.SUCCESS;
    }

    if (config.debug) {
      this.logMetaTileEntity(player, world, pos, side);
      return EnumActionResult.SUCCESS;
    }

    MetaTileEntity mte = BlockMachine.getMetaTileEntity(world, pos);
    IReplacementBehavior<MetaTileEntity> behavior = UpgradeKit.getReplacementBehaviorForMte(mte);

    if (behavior == null || !behavior.hasReplacements(mte)) {
      if (!world.isRemote)
        statusMsg(player, TextFormatting.RED + "Cannot be replaced");
      return EnumActionResult.SUCCESS;
    }

    Pair<ItemStack, ReplacementType> candidate = findCandidate(player, mte, config, behavior);

    if (candidate == null) {
      if (!world.isRemote)
        statusMsg(player, TextFormatting.RED + "No replacements found in inventory");
      return EnumActionResult.SUCCESS;
    }

    ItemStack upgradeStack = candidate.getLeft();
    ReplacementType type = candidate.getRight();
    TieredMetaTileEntity upgradeMte = (TieredMetaTileEntity) MachineItemBlock.getMetaTileEntity(upgradeStack);
    String line;

    if (type == ReplacementType.UPGRADE)
      line = TextFormatting.GREEN + "Upgraded" + TextFormatting.RESET + " to ";
    else if (type == ReplacementType.DOWNGRADE)
      line = TextFormatting.DARK_RED + "Downgraded" + TextFormatting.RESET + " to ";
    else
      line = TextFormatting.YELLOW + "Replaced" + TextFormatting.RESET + " with ";

    behavior.replace(player, world, pos, side, mte, behavior.getReplacementFromStack(upgradeStack));
    if (!world.isRemote) {
      if (!player.capabilities.isCreativeMode) {
        upgradeStack.shrink(1);
        if (upgradeStack.isEmpty())
          player.inventory.deleteStack(upgradeStack);
      }
      player.sendStatusMessage(
          new TextComponentString(line).appendSibling(new TextComponentTranslation(upgradeMte.getMetaFullName()))
              .appendSibling(new TextComponentString(String.format(" (%s tier)", GTValues.VN[upgradeMte.getTier()]))),
          true);
    }
    return EnumActionResult.SUCCESS;
  }

  @Override
  public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
      EnumFacing side, float hitX, float hitY, float hitZ) {
    return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
  }

  private static <T> Pair<ItemStack, ReplacementType> findCandidate(EntityPlayer player, T toReplace,
      IReplacementConfig config, IReplacementBehavior<T> behavior) {
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
    UpgradeConfig config = this.getConfig(stack);

    if (config.debug)
      lines.add(I18n.format("upgradekit.mode.debug.name"));
    else
      lines.add(I18n.format(this.getConfig(stack).getMode().getUnlocalizedName()));
  }

  private static <T> ReplacementType canReplace(T toReplace, ItemStack replacement, IReplacementBehavior<T> behavior,
      IReplacementConfig config) {
    ReplacementType type = behavior.getReplacementType(toReplace, behavior.getReplacementFromStack(replacement));

    switch (config.getMode()) {
      case UPGRADE_ONLY:
        return type == ReplacementType.UPGRADE ? ReplacementType.UPGRADE : ReplacementType.NONE;
      case DOWNGRADE_ONLY:
        return type == ReplacementType.DOWNGRADE ? ReplacementType.DOWNGRADE : ReplacementType.NONE;
      default:
        return type;
    }
  }

  protected UpgradeConfig getConfig(ItemStack stack) {
    UpgradeConfig config = new UpgradeConfig();
    NBTTagCompound compound = stack.getTagCompound();

    if (compound != null) {
      NBTTagCompound configCompound = compound.getCompoundTag("ReplacementConfig");

      if (configCompound != null) {
        config.debug = compound.getBoolean("Debug");
        try {
          config.mode = IReplacementConfig.Mode.valueOf(compound.getString("Mode").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
        }
      }
    }
    return config;
  }

  protected void setConfig(ItemStack stack, UpgradeConfig config) {
    NBTTagCompound compound = stack.getTagCompound();

    if (compound == null) {
      compound = new NBTTagCompound();
      stack.setTagCompound(compound);
    }
    compound.setBoolean("Debug", config.debug);
    compound.setString("Mode", config.mode.name().toLowerCase(Locale.ROOT));
  }

  private static void cycleMode(UpgradeConfig config) {
    int next = config.mode.ordinal() + 1;

    if (next >= IReplacementConfig.Mode.values().length) {
      if (config.debug)
        config.mode = IReplacementConfig.Mode.REPLACE;
      config.debug = !config.debug;
    } else {
      config.mode = IReplacementConfig.Mode.values()[next];
    }
  }

  protected static final class UpgradeConfig implements IReplacementConfig {
    public boolean debug;
    public IReplacementConfig.Mode mode;

    public UpgradeConfig() {
      this.debug = false;
      this.mode = IReplacementConfig.Mode.REPLACE;
    }

    public void readFromNBT(NBTTagCompound compound) {

    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      compound.setBoolean("Debug", this.debug);
      compound.setString("Mode", mode.name().toLowerCase(Locale.ROOT));
      return compound;
    }

    @Override
    public Mode getMode() {
      return this.mode;
    }
  }
}