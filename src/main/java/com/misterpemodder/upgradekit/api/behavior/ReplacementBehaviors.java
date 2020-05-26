package com.misterpemodder.upgradekit.api.behavior;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.target.ReplacementTargets;
import com.misterpemodder.upgradekit.api.util.FreezableNamespacedRegistry;
import com.misterpemodder.upgradekit.api.util.PriorityNamespacedRegistry;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.behavior.GenericCoverReplacementBehavior;
import com.misterpemodder.upgradekit.impl.behavior.TieredMetaTileEntityReplacementBehavior;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * @since 1.0.0
 */
public final class ReplacementBehaviors {
  private static final Comparator<IReplacementBehavior<?, ?>> BEHAVIOR_PRIORITY_COMPARATOR = (b1,
      b2) -> b1.getPriority() - b2.getPriority();
  private static Map<IReplacementTarget<?>, PriorityQueue<IReplacementBehavior<?, ?>>> behaviorQueuesForTarget = new HashMap<>();
  private static ListMultimap<IReplacementTarget<?>, IReplacementBehavior<?, ?>> behaviorsForTarget;

  /**
   * The replacement behavior registry.
   * Behaviors must be registered before the post init phase.
   * 
   * @since 1.0.0
   */
  public static final FreezableNamespacedRegistry<ResourceLocation, IReplacementBehavior<?, ?>> REGISTRY = new PriorityNamespacedRegistry<>(
      BEHAVIOR_PRIORITY_COMPARATOR);

  public static final IReplacementBehavior<TileEntity, TieredMetaTileEntity> TIERED_META_TILE_ENTITY = register(
      UpgradeKit.newId("tiered_meta_tile_entity"), new TieredMetaTileEntityReplacementBehavior(),
      ReplacementTargets.TILE_ENTITY);
  public static final IReplacementBehavior<CoverBehavior, CoverDefinition> GENERIC_COVER = register(
      UpgradeKit.newId("generic_cover"), new GenericCoverReplacementBehavior(), ReplacementTargets.COVER);

  /**
   * Registers a replacement behavior.
   * 
   * @param <T>      The type of replaceable object.
   * @param <R>      The type of replacement object.
   * @param id       The id of this behavior.
   * @param behavior The behavior to register.
   * @param targets  The target(s) for which this replacement behavior will apply.
   * @return The passed behavior for chaining.
   * @since 1.0.0
   */
  @SafeVarargs
  public static <T, R> IReplacementBehavior<T, R> register(ResourceLocation id, IReplacementBehavior<T, R> behavior,
      IReplacementTarget<T>... targets) {
    REGISTRY.register(id, behavior);
    for (IReplacementTarget<T> target : targets) {
      PriorityQueue<IReplacementBehavior<?, ?>> queue = behaviorQueuesForTarget.get(target);

      if (queue == null) {
        queue = new PriorityQueue<>(BEHAVIOR_PRIORITY_COMPARATOR);
        behaviorQueuesForTarget.put(target, queue);
      }
      queue.add(behavior);
    }
    return behavior;
  }

  /**
   * @param <T>    The type of replaceable object.
   * @param target The target, must be registered.
   * @return The behaviors associated with this target, sorted by priority.
   * @since 1.0.0
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterable<IReplacementBehavior<T, ?>> getBehaviorsForTarget(IReplacementTarget<T> target) {
    if (REGISTRY.isFrozen()) {
      if (behaviorsForTarget == null) {
        behaviorsForTarget = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Map.Entry<IReplacementTarget<?>, PriorityQueue<IReplacementBehavior<?, ?>>> entry : behaviorQueuesForTarget
            .entrySet())
          behaviorsForTarget.putAll(entry.getKey(), entry.getValue());
        behaviorQueuesForTarget = null;
      }
      return (List<IReplacementBehavior<T, ?>>) (Object) behaviorsForTarget.get(target);
    }
    return (PriorityQueue<IReplacementBehavior<T, ?>>) (Object) behaviorQueuesForTarget.get(target);
  }
}