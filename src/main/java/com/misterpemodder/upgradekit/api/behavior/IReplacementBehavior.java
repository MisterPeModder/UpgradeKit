package com.misterpemodder.upgradekit.api.behavior;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.annotation.Nullable;

import com.google.common.collect.MultimapBuilder;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.api.util.FreezableNamespacedRegistry;
import com.misterpemodder.upgradekit.api.util.PriorityNamespacedRegistry;
import com.misterpemodder.upgradekit.impl.behavior.ReplacementBehaviors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author MisterPeModder
 * @param <T> The type of object to replace.
 * @param <R> The type of replacement object.
 * @since 1.0.0
 */
public interface IReplacementBehavior<T, R> {
  static final Comparator<IReplacementBehavior<?, ?>> BEHAVIOR_PRIORITY_COMPARATOR = (b1, b2) -> b1.getPriority()
      - b2.getPriority();

  /**
  * The replacement behavior registry.
  * Behaviors must be registered before the post init phase.
  * 
  * @since 1.0.0
  */
  static final FreezableNamespacedRegistry<ResourceLocation, IReplacementBehavior<?, ?>> REGISTRY = new PriorityNamespacedRegistry<>(
      BEHAVIOR_PRIORITY_COMPARATOR);

  /**
   * Gets a replacement object from an {@link ItemStack}.
   * 
   * @param stack The stack.
   * @return The replacement object, may be null
   * @since 1.0.0
   */
  @Nullable
  R getReplacementFromStack(ItemStack stack);

  /**
   * @param object The replacement object.
   * @return The unlocalized name of this replecement object.
   * @since 1.0.0
   */
  String getUnlocalizedNameForReplacement(R object);

  /**
   * Queries if the passed object can be replaced by something else.
   * 
   * @param target The object to query, can be null.
   * @return Is there a replacement available? always false if {@code target} is null.
   * @since 1.0.0
   */
  boolean hasReplacements(@Nullable T target);

  /**
   * Replaces {@code toReplace} by  {@code replacement}.
   * This method assumes that {@code this.hasReplacements(toReplace) == true}.
   * 
   * @param config           The replacement configuration.
   * @param player           The player, may be null.
   * @param world            The world the player is in.
   * @param pos              The position of the target block.
   * @param side             The target side.
   * @param toReplace        The old object to be replaced.
   * @param replacement      The new object.
   * @param replacementStack The item stack the replacement came from.
   * @return <ul>
   *  <li>{@link ReplacementType#NONE}: if {@code replacement} cannot replace {@code toReplace}.</li>
   *  <li>{@link ReplacementType#EQUIVALENT}: if {@code replacement} has the same tier as {@code toReplace}.</li>
   *  <li>{@link ReplacementType#UPGRADE}: if {@code replacement} is higher tier than {@code toReplace}.</li>
   *  <li>{@link ReplacementType#DOWNGRADE}: if {@code replacement} is lower tier than {@code toReplace}.</li>
   * </ul>
   * @since 1.0.0
   */
  ReplacementType replace(@Nullable EntityPlayer player, World world, BlockPos pos, EnumFacing side, T toReplace,
      R replacement, ItemStack replacementStack, boolean simulate);

  /**
   * @return The priority of this behavior
   * @since 1.0.0
   */
  default int getPriority() {
    return 1000;
  }

  /**
   * @since 1.0.0
   */
  enum ReplacementType {
    NONE, EQUIVALENT, UPGRADE, DOWNGRADE
  }

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
  static <T, R> IReplacementBehavior<T, R> register(ResourceLocation id, IReplacementBehavior<T, R> behavior,
      IReplacementTarget<T>... targets) {
    REGISTRY.register(id, behavior);
    for (IReplacementTarget<T> target : targets) {
      PriorityQueue<IReplacementBehavior<?, ?>> queue = ReplacementBehaviors.behaviorQueuesForTarget.get(target);

      if (queue == null) {
        queue = new PriorityQueue<>(BEHAVIOR_PRIORITY_COMPARATOR);
        ReplacementBehaviors.behaviorQueuesForTarget.put(target, queue);
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
  static <T> Iterable<IReplacementBehavior<T, ?>> getBehaviorsForTarget(IReplacementTarget<T> target) {
    if (REGISTRY.isFrozen()) {
      if (ReplacementBehaviors.behaviorsForTarget == null) {
        ReplacementBehaviors.behaviorsForTarget = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Map.Entry<IReplacementTarget<?>, PriorityQueue<IReplacementBehavior<?, ?>>> entry : ReplacementBehaviors.behaviorQueuesForTarget
            .entrySet())
          ReplacementBehaviors.behaviorsForTarget.putAll(entry.getKey(), entry.getValue());
        ReplacementBehaviors.behaviorQueuesForTarget = null;
      }
      return (List<IReplacementBehavior<T, ?>>) (Object) ReplacementBehaviors.behaviorsForTarget.get(target);
    }
    return (PriorityQueue<IReplacementBehavior<T, ?>>) (Object) ReplacementBehaviors.behaviorQueuesForTarget
        .get(target);
  }
}