package com.misterpemodder.upgradekit.impl;

public interface IUpgradeMap<T> {
  /**
   * Checks if the passed object can be upgraded/downgraded.
   * 
   * @param object The object to check.
   * @return true if there is at least one object that can replace the passed one, false otherwise.
   */
  boolean hasUpgrades(T object);

  /**
   * @param first
   * @param second
   * @return Can the two objects be replaced by each other?
   */
  boolean canReplace(T first, T second);

  /**
   * Gets the tier difference between the two objects.
   * NOTE: this method does not check if the two objects are interchangable,
   * use {@link IUpgradeMap#canReplace(Object, Object)} before calling this.
   * 
   * @param first
   * @param second
   * @return The tier difference, positive if first is higher tier than second,
   *         negative if lower tier or zero if on the same tier.
   */
  int getTierDifference(T first, T second);
}