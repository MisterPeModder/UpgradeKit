package com.misterpemodder.upgradekit.api.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.registry.RegistrySimple;

/**
 * @since 1.0.0
 */
public class FreezableNamespacedRegistry<K, V> extends RegistrySimple<K, V> {
  private boolean frozen = false;
  private Set<V> cachedValueSet;
  protected final IntIdentityHashBiMap<V> underlyingIntegerMap = new IntIdentityHashBiMap<V>(256);
  protected final Map<V, K> inverseObjectRegistry;

  public FreezableNamespacedRegistry() {
    this.inverseObjectRegistry = ((BiMap<K, V>) this.registryObjects).inverse();
  }

  public boolean isFrozen() {
    return this.frozen;
  }

  public void freeze() {
    if (this.frozen)
      throw new IllegalStateException("Registry is already frozen!");
    this.frozen = true;
  }

  public Set<V> getValues() {
    if (!this.frozen)
      return ImmutableSet.copyOf(this.registryObjects.values());
    if (this.cachedValueSet == null)
      this.cachedValueSet = ImmutableSet.copyOf(this.registryObjects.values());
    return this.cachedValueSet;
  }

  public void register(K key, V value) {
    this.underlyingIntegerMap.put(value, this.underlyingIntegerMap.size());
    this.putObject(key, value);
  }

  protected Map<K, V> createUnderlyingMap() {
    return HashBiMap.<K, V>create();
  }

  @Nullable
  public V getObject(@Nullable K name) {
    return (V) super.getObject(name);
  }

  @Nullable
  public K getNameForObject(V value) {
    return this.inverseObjectRegistry.get(value);
  }

  public boolean containsKey(K key) {
    return super.containsKey(key);
  }

  public int getIDForObject(@Nullable V value) {
    return this.underlyingIntegerMap.getId(value);
  }

  @Nullable
  public V getObjectById(int id) {
    return this.underlyingIntegerMap.get(id);
  }

  public Iterator<V> iterator() {
    return this.underlyingIntegerMap.iterator();
  }
}