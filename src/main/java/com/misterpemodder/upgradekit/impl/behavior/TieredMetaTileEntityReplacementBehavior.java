package com.misterpemodder.upgradekit.impl.behavior;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.impl.UpgradeKit;

import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TieredMetaTileEntityReplacementBehavior implements IReplacementBehavior<TileEntity, TieredMetaTileEntity> {
  private static final Map<ResourceLocation, String> CANDIDATES = new HashMap<>();

  public static void buildCandidatesMap() {
    for (MetaTileEntity mte : GregTechAPI.META_TILE_ENTITY_REGISTRY)
      if (mte instanceof TieredMetaTileEntity)
        CANDIDATES.put(mte.metaTileEntityId, UpgradeKit.getMachineTypeId(mte));
  }

  @Override
  public TieredMetaTileEntity getReplacementFromStack(ItemStack stack) {
    MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stack);

    return mte instanceof TieredMetaTileEntity ? (TieredMetaTileEntity) mte : null;
  }

  @Override
  public String getUnlocalizedNameForReplacement(TieredMetaTileEntity object) {
    return object.getMetaFullName();
  }

  @Override
  public boolean hasReplacements(TileEntity target) {
    if (target instanceof MetaTileEntityHolder) {
      MetaTileEntity mte = ((MetaTileEntityHolder) target).getMetaTileEntity();

      return mte != null && CANDIDATES.containsKey(mte.metaTileEntityId);
    }
    return false;
  }

  @Override
  public ReplacementType replace(@Nullable EntityPlayer player, World world, BlockPos pos, EnumFacing side,
      TileEntity toReplace, TieredMetaTileEntity replacement, ItemStack replacementStack, boolean simulate) {
    ReplacementType type = getReplacementType(toReplace, replacement);

    if (type == ReplacementType.NONE || simulate)
      return type;

    TieredMetaTileEntity toReplaceMte = (TieredMetaTileEntity) ((MetaTileEntityHolder) toReplace).getMetaTileEntity();
    IBlockState state = world.getBlockState(pos);
    MetaTileEntityHolder mteHolder = toReplaceMte.getHolder();

    if (!world.isRemote) {
      if (!player.capabilities.isCreativeMode) {
        UpgradeKit.insertOrDropStack(world, pos, player, toReplaceMte.getStackForm());
        replacementStack.shrink(1);
        if (replacementStack.isEmpty())
          player.inventory.deleteStack(replacementStack);
      }

      IItemHandlerModifiable inputs = toReplaceMte.getImportItems();
      int inputSlots = inputs.getSlots();
      int extraInputSlots = inputSlots - replacement.getImportItems().getSlots();

      // give or drop items that wont fit anymore
      for (int s = inputSlots - extraInputSlots; s < inputSlots; ++s) {
        ItemStack slotStack = inputs.extractItem(s, Integer.MAX_VALUE, false);

        if (!player.inventory.addItemStackToInventory(slotStack))
          Block.spawnAsEntity(world, pos, slotStack);
      }

      IItemHandlerModifiable outputs = toReplaceMte.getExportItems();
      int outputSlots = outputs.getSlots();
      int extraOutputSlots = outputSlots - replacement.getExportItems().getSlots();

      for (int s = outputSlots - extraOutputSlots; s < outputSlots; ++s) {
        ItemStack slotStack = outputs.extractItem(s, Integer.MAX_VALUE, false);

        if (!player.inventory.addItemStackToInventory(slotStack))
          Block.spawnAsEntity(world, pos, slotStack);
      }

      IEnergyContainer toReplaceEnergy = toReplace.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
          null);
      IEnergyContainer replacementEnergy = replacement.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
          null);

      if (toReplaceEnergy != null && replacementEnergy != null) {
        long energyStored = toReplaceEnergy.getEnergyStored();
        long newCapacity = replacementEnergy.getEnergyCapacity();

        if (energyStored > newCapacity)
          toReplaceEnergy.removeEnergy(energyStored - newCapacity);
      }
    }

    NBTTagCompound compound = mteHolder.writeToNBT(new NBTTagCompound());

    compound.setString("MetaId", replacement.metaTileEntityId.toString());
    mteHolder.readFromNBT(compound);
    mteHolder.markDirty();
    world.setBlockState(pos, state, 3);
    world.notifyBlockUpdate(pos, state, state, 3);
    world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.f, 1.f);
    return type;
  }

  protected ReplacementType getReplacementType(TileEntity toReplace, TieredMetaTileEntity replacement) {
    TieredMetaTileEntity toReplaceMte = (TieredMetaTileEntity) ((MetaTileEntityHolder) toReplace).getMetaTileEntity();

    if (toReplaceMte.metaTileEntityId.equals(replacement.metaTileEntityId)
        || !CANDIDATES.get(toReplaceMte.metaTileEntityId).equals(CANDIDATES.get(replacement.metaTileEntityId)))
      return ReplacementType.NONE;

    int tierDifference = replacement.getTier() - toReplaceMte.getTier();

    if (tierDifference > 0)
      return ReplacementType.UPGRADE;
    else if (tierDifference < 0)
      return ReplacementType.DOWNGRADE;
    else
      return ReplacementType.EQUIVALENT;
  }
}