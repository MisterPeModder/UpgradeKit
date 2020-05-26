package com.misterpemodder.upgradekit.api.util;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @since 1.0.0
 */
public class PriorityNamespacedRegistry<K, V> extends FreezableNamespacedRegistry<K, V> {
  private PriorityQueue<V> priorityQueue;
  private List<V> valuesByPriority;

  public PriorityNamespacedRegistry(@Nullable Comparator<V> comparator) {
    super();
    this.priorityQueue = new PriorityQueue<>(comparator);
  }

  /**
   * @return The list of values in this registry ordered by priority.
   * @since 1.0.0
   */
  public List<V> getValuesByPriority() {
    if (!this.isFrozen())
      return ImmutableList.copyOf(this.priorityQueue);
    return this.valuesByPriority;
  }

  @Override
  public void freeze() {
    this.valuesByPriority = this.getValuesByPriority();
    this.priorityQueue = null;
    super.freeze();
  }

  @Override
  public void putObject(K key, V value) {
    super.putObject(key, value);
    this.priorityQueue.add(value);
  }
}