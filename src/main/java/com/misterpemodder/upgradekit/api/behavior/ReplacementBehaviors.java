package com.misterpemodder.upgradekit.api.behavior;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;
import com.misterpemodder.upgradekit.api.util.FreezableNamespacedRegistry;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.behavior.TieredMetaTileEntityReplacementBehavior;

import net.minecraft.util.ResourceLocation;

/**
 * @since 1.0.0
 */
public final class ReplacementBehaviors {
  /**
   * The replacement behavior registry.
   * Behaviors must be registered before the post init phase.
   * 
   * @since 1.0.0
   */
  public static final FreezableNamespacedRegistry<ResourceLocation, IReplacementBehavior<?>> REGISTRY = new FreezableNamespacedRegistry<>();
  private static final ListMultimap<IReplacementTarget<?>, IReplacementBehavior<?>> BEHAVIORS_FOR_TARGET = MultimapBuilder
      .hashKeys().arrayListValues().build();

  public static final IReplacementBehavior<?> TIERED_META_TILE_ENTITY = register(
      UpgradeKit.newId("tiered_meta_tile_entity"), new TieredMetaTileEntityReplacementBehavior(),
      ReplacementTargets.META_TILE_ENTITY);

  /**
   * Registers a replacement behavior.
   * 
   * @param <T>      The type of replaceable object.
   * @param id       The id of this behavior.
   * @param behavior The behavior to register.
   * @param targets  The target(s) for which this replacement behavior will apply.
   * @return The passed behavior for chaining.
   * @since 1.0.0
   */
  @SafeVarargs
  public static <T> IReplacementBehavior<T> register(ResourceLocation id, IReplacementBehavior<T> behavior,
      IReplacementTarget<T>... targets) {
    REGISTRY.register(id, behavior);
    for (IReplacementTarget<T> target : targets) {
      BEHAVIORS_FOR_TARGET.put(target, behavior);
    }
    return behavior;
  }

  /**
   * @param <T>    The type of replaceable object.
   * @param target The target.
   * @return The behaviors associated with this target.
   * @since 1.0.0
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterable<IReplacementBehavior<T>> getBehaviorsForTarget(IReplacementTarget<T> target) {
    return (List<IReplacementBehavior<T>>) (Object) BEHAVIORS_FOR_TARGET.get(target);
  }
}