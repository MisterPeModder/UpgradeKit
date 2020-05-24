package com.misterpemodder.upgradekit.api.target;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.misterpemodder.upgradekit.api.util.FreezableNamespacedRegistry;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.target.MetaTileEntityReplacementTarget;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * @since 1.0.0
 */
public final class ReplacementTargets {
  /**
  * The replacement target registry.
  * Targets must be registered before the post init phase.
  * 
  * @since 1.0.0
  */
  public static final FreezableNamespacedRegistry<ResourceLocation, IReplacementTarget<?>> REGISTRY = new FreezableNamespacedRegistry<>();

  public static final ResourceLocation EMPTY_ID = UpgradeKit.newId("empty");

  public static final IReplacementTarget<MetaTileEntity> META_TILE_ENTITY = register(
      UpgradeKit.newId("meta_tile_entity"), new MetaTileEntityReplacementTarget());
  public static final IReplacementTarget<Void> EMPTY = register(EMPTY_ID, new EmptyReplacementTarget());

  private static Set<IReplacementTarget<?>> targets;

  /**
   * 
   * @param <T>
   * @param id
   * @param target
   * @return
   * @since 1.0.0
   */
  public static <T> IReplacementTarget<T> register(ResourceLocation id, IReplacementTarget<T> target) {
    REGISTRY.register(id, target);
    return target;
  }

  /**
   * @return The set of all replacement targets, excluding {@link #EMPTY}.
   * @since 1.0.0
   */
  public static Set<IReplacementTarget<?>> getAllTargets() {
    if (REGISTRY.isFrozen()) {
      if (targets == null)
        targets = buildTargetSet();
      return targets;
    }
    return buildTargetSet();
  }

  private static Set<IReplacementTarget<?>> buildTargetSet() {
    ImmutableSet.Builder<IReplacementTarget<?>> builder = ImmutableSet.builder();

    for (IReplacementTarget<?> target : REGISTRY.getValues()) {
      if (target != EMPTY)
        builder.add(target);
    }
    return builder.build();
  }
}