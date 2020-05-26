package com.misterpemodder.upgradekit.impl.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.ListMultimap;
import com.misterpemodder.upgradekit.api.behavior.IReplacementBehavior;
import com.misterpemodder.upgradekit.api.target.IReplacementTarget;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.target.ReplacementTargets;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class ReplacementBehaviors {
  public static Map<IReplacementTarget<?>, PriorityQueue<IReplacementBehavior<?, ?>>> behaviorQueuesForTarget = new HashMap<>();
  public static ListMultimap<IReplacementTarget<?>, IReplacementBehavior<?, ?>> behaviorsForTarget;

  public static IReplacementBehavior<TileEntity, TieredMetaTileEntity> TIERED_META_TILE_ENTITY = new TieredMetaTileEntityReplacementBehavior();
  public static IReplacementBehavior<CoverBehavior, CoverDefinition> GENERIC_COVER = new GenericCoverReplacementBehavior();

  public static void init() {
    IReplacementBehavior.register(UpgradeKit.newId("tiered_meta_tile_entity"), TIERED_META_TILE_ENTITY,
        ReplacementTargets.TILE_ENTITY);
    IReplacementBehavior.register(UpgradeKit.newId("generic_cover"), GENERIC_COVER, ReplacementTargets.COVER);
  }
}