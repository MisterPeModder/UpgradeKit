package com.misterpemodder.upgradekit.impl.target;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.impl.UpgradeKit;

import gregtech.api.cover.CoverBehavior;
import net.minecraft.tileentity.TileEntity;

/**
 * @since 1.0.0
 */
public final class ReplacementTargets {
  public static final IReplacementTarget<TileEntity> TILE_ENTITY = new TileEntityReplacementTarget();
  public static final IReplacementTarget<CoverBehavior> COVER = new CoverReplacementTarget();

  public static Set<IReplacementTarget<?>> targets;

  public static void init() {
    IReplacementTarget.register(IReplacementTarget.EMPTY_ID, IReplacementTarget.EMPTY);

    IReplacementTarget.register(UpgradeKit.newId("tile_entity"), TILE_ENTITY);
    IReplacementTarget.register(UpgradeKit.newId("cover"), COVER);
  }

  public static Set<IReplacementTarget<?>> buildTargetSet() {
    ImmutableSet.Builder<IReplacementTarget<?>> builder = ImmutableSet.builder();

    for (IReplacementTarget<?> target : IReplacementTarget.REGISTRY.getValues()) {
      if (target != IReplacementTarget.EMPTY)
        builder.add(target);
    }
    return builder.build();
  }
}