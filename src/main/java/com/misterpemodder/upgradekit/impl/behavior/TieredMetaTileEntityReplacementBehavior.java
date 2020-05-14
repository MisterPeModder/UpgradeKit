package com.misterpemodder.upgradekit.impl.behavior;

import java.util.HashSet;
import java.util.Set;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TieredMetaTileEntityReplacementBehavior implements IReplacementBehavior<MetaTileEntity> {
  protected String machineId;

  protected Set<ResourceLocation> candidates = new HashSet<>();

  public TieredMetaTileEntityReplacementBehavior(String machineId) {
    this.machineId = machineId;
  }

  @Override
  public void addReplacementCandidate(MetaTileEntity mte) {
    if (mte instanceof TieredMetaTileEntity)
      this.candidates.add(mte.metaTileEntityId);
  }

  @Override
  public MetaTileEntity getReplacementFromStack(ItemStack stack) {
    MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stack);

    return mte instanceof TieredMetaTileEntity ? (MetaTileEntity) mte : null;
  }

  @Override
  public boolean hasReplacements(MetaTileEntity replaceable) {
    return replaceable != null && this.candidates.contains(replaceable.metaTileEntityId);
  }

  @Override
  public ReplacementType getReplacementType(MetaTileEntity toReplace, MetaTileEntity replacement) {
    if (replacement == null || !this.candidates.contains(replacement.metaTileEntityId)
        || toReplace.metaTileEntityId.equals(replacement.metaTileEntityId))
      return ReplacementType.NONE;

    int tierDifference = ((TieredMetaTileEntity) replacement).getTier() - ((TieredMetaTileEntity) toReplace).getTier();

    if (tierDifference > 0)
      return ReplacementType.UPGRADE;
    else if (tierDifference < 0)
      return ReplacementType.DOWNGRADE;
    else
      return ReplacementType.EQUIVALENT;
  }

  @Override
  public boolean replace(EntityPlayer player, World world, BlockPos pos, EnumFacing side, MetaTileEntity toReplace,
      MetaTileEntity replacement) {
    IBlockState state = world.getBlockState(pos);
    MetaTileEntityHolder mteHolder = toReplace.getHolder();
    NBTTagCompound compound = mteHolder.writeToNBT(new NBTTagCompound());

    if (!world.isRemote) {
      if (!player.capabilities.isCreativeMode) {
        ItemStack oldStack = toReplace.getStackForm();

        if (!player.inventory.addItemStackToInventory(oldStack))
          Block.spawnAsEntity(world, pos, toReplace.getStackForm());
      }

      IItemHandlerModifiable inputs = toReplace.getImportItems();
      int inputSlots = inputs.getSlots();
      int extraInputSlots = inputSlots - replacement.getImportItems().getSlots();

      // give or drop items that wont fit anymore
      for (int s = inputSlots - extraInputSlots; s < inputSlots; ++s) {
        ItemStack slotStack = inputs.extractItem(s, Integer.MAX_VALUE, false);

        if (!player.inventory.addItemStackToInventory(slotStack))
          Block.spawnAsEntity(world, pos, slotStack);
      }

      IItemHandlerModifiable outputs = toReplace.getExportItems();
      int outputSlots = outputs.getSlots();
      int extraOutputSlots = outputSlots - replacement.getExportItems().getSlots();

      for (int s = outputSlots - extraOutputSlots; s < outputSlots; ++s) {
        ItemStack slotStack = outputs.extractItem(s, Integer.MAX_VALUE, false);

        if (!player.inventory.addItemStackToInventory(slotStack))
          Block.spawnAsEntity(world, pos, slotStack);
      }
    }
    compound.setString("MetaId", replacement.metaTileEntityId.toString());
    mteHolder.readFromNBT(compound);
    mteHolder.markDirty();
    world.setBlockState(pos, state, 3);
    world.notifyBlockUpdate(pos, state, state, 3);
    world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.f, 1.f);
    return true;
  }
}