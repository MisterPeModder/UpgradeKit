package com.misterpemodder.upgradekit.impl.behavior;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.impl.UpgradeKit;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GenericCoverReplacementBehavior implements IReplacementBehavior<CoverBehavior, CoverDefinition> {
  private static final Map<ResourceLocation, String> CANDIDATES = new HashMap<>();
  private static final Multiset<String> CANDIDATES_COUNT = HashMultiset.create();

  protected static Map<ResourceLocation, CoverBehavior> dummyCovers;

  private static final Method CAPTURE_DROPS_METHOD;

  public static void buildCandidatesMap() {
    ImmutableMap.Builder<ResourceLocation, CoverBehavior> dummyCoversBuilder = ImmutableMap.builder();

    for (CoverDefinition cover : CoverDefinition.registry) {
      ResourceLocation coverId = cover.getCoverId();
      String coverTypeId = UpgradeKit.getCoverTypeId(cover);

      CANDIDATES.put(coverId, coverTypeId);
      CANDIDATES_COUNT.add(coverTypeId);
      dummyCoversBuilder.put(coverId, cover.createCoverBehavior(DummyCoverable.INSTANCE, EnumFacing.DOWN));
    }
    dummyCovers = dummyCoversBuilder.build();
  }

  @Override
  public CoverDefinition getReplacementFromStack(ItemStack stack) {
    Item item = stack.getItem();

    if (item instanceof MetaItem) {
      MetaItem<?>.MetaValueItem placerItem = ((MetaItem<?>) item).getItem(stack);
      CoverPlaceBehavior behavior = getCoverPlaceBehavior(placerItem);

      if (behavior != null)
        return behavior.coverDefinition;
    }
    return null;
  }

  @Nullable
  private static CoverPlaceBehavior getCoverPlaceBehavior(@Nullable MetaItem<?>.MetaValueItem placerItem) {
    if (placerItem == null)
      return null;
    for (IItemBehaviour behavior : placerItem.getBehaviours())
      if (behavior instanceof CoverPlaceBehavior)
        return (CoverPlaceBehavior) behavior;
    return null;
  }

  @Override
  public String getUnlocalizedNameForReplacement(CoverDefinition object) {
    return object.getDropItemStack().getDisplayName();
  }

  @Override
  public boolean hasReplacements(CoverBehavior target) {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ReplacementType replace(@Nullable EntityPlayer player, World world, BlockPos pos, EnumFacing side,
      CoverBehavior toReplace, CoverDefinition replacement, ItemStack replacementStack, boolean simulate) {
    ReplacementType type = getReplacementType(toReplace, replacement);
    ICoverable coverHolder = toReplace.coverHolder;
    EnumFacing coverSide = player == null ? side : ICoverable.rayTraceCoverableSide(coverHolder, player);
    CoverBehavior replacementCover = replacement.createCoverBehavior(coverHolder, coverSide);

    if (type == ReplacementType.NONE || coverSide == null || !coverHolder.canPlaceCoverOnSide(coverSide)
        || !replacementCover.canAttach())
      return ReplacementType.NONE;
    if (!simulate) {
      if (CAPTURE_DROPS_METHOD == null) {
        coverHolder.placeCoverOnSide(coverSide, replacementStack, replacement);
      } else {
        try {
          // Using Block#captureDrops to capture the items dropped by placeCoverOnSide
          CAPTURE_DROPS_METHOD.invoke(Blocks.AIR, true);
          coverHolder.placeCoverOnSide(coverSide, replacementStack, replacement);

          List<ItemStack> drops = (NonNullList<ItemStack>) CAPTURE_DROPS_METHOD.invoke(Blocks.AIR, false);

          if (!player.capabilities.isCreativeMode)
            for (ItemStack stack : drops)
              UpgradeKit.insertOrDropStack(world, pos, player, stack);
        } catch (Exception e) {
          throw new RuntimeException("Failed to invoke Block#captureDrops", e);
        }
      }
      if (!world.isRemote) {
        if (!player.capabilities.isCreativeMode) {
          replacementStack.shrink(1);
          if (replacementStack.isEmpty())
            player.inventory.deleteStack(replacementStack);
        }
      }
      world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.f, 1.25f);
    }
    return type;
  }

  protected ReplacementType getReplacementType(CoverBehavior toReplace, CoverDefinition replacement) {
    if (toReplace.getCoverDefinition().getCoverId().equals(replacement.getCoverId()))
      return ReplacementType.NONE;
    return ReplacementType.EQUIVALENT;
  }

  @Override
  public int getPriority() {
    return 500;
  }

  static {
    Method captureDropsMethod;
    try {
      captureDropsMethod = ObfuscationReflectionHelper.findMethod(Block.class, "captureDrops", NonNullList.class,
          boolean.class);
    } catch (Exception e) {
      UpgradeKit.logger.error("Failed to find Block#captureDrops method", e);
      captureDropsMethod = null;
    }
    CAPTURE_DROPS_METHOD = captureDropsMethod;
  }

  private static class DummyCoverable implements ICoverable {
    public static final DummyCoverable INSTANCE = new DummyCoverable();

    private DummyCoverable() {
    }

    @Override
    public World getWorld() {
      return null;
    }

    @Override
    public BlockPos getPos() {
      return BlockPos.ORIGIN;
    }

    @Override
    public long getTimer() {
      return 0L;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
      return null;
    }

    @Override
    public boolean placeCoverOnSide(EnumFacing side, ItemStack itemStack, CoverDefinition definition) {
      return false;
    }

    @Override
    public boolean removeCover(EnumFacing side) {
      return false;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
      return false;
    }

    @Override
    public CoverBehavior getCoverAtSide(EnumFacing side) {
      return null;
    }

    @Override
    public void writeCoverData(CoverBehavior behavior, int id, Consumer<PacketBuffer> writer) {
    }

    @Override
    public int getInputRedstoneSignal(EnumFacing side, boolean ignoreCover) {
      return 0;
    }

    @Override
    public ItemStack getStackForm() {
      return ItemStack.EMPTY;
    }

    @Override
    public double getCoverPlateThickness() {
      return 0.0D;
    }

    @Override
    public int getPaintingColor() {
      return 0;
    }

    @Override
    public boolean shouldRenderBackSide() {
      return false;
    }

    @Override
    public void notifyBlockUpdate() {
    }

    @Override
    public void scheduleRenderUpdate() {
    }
  }
}