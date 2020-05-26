package com.misterpemodder.upgradekit.api.target;

import java.util.Set;

import javax.annotation.Nullable;

import com.misterpemodder.upgradekit.api.util.FreezableNamespacedRegistry;
import com.misterpemodder.upgradekit.impl.UpgradeKit;
import com.misterpemodder.upgradekit.impl.target.ReplacementTargets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author MisterPeModder
 * @since 1.0.0
 */
public interface IReplacementTarget<T> {
  /**
  * The replacement target registry.
  * Targets must be registered before the post init phase.
  * 
  * @since 1.0.0
  */
  public static final FreezableNamespacedRegistry<ResourceLocation, IReplacementTarget<?>> REGISTRY = new FreezableNamespacedRegistry<>();

  public static final ResourceLocation EMPTY_ID = UpgradeKit.newId("empty");

  /**
   * The empty target.
   * @since 1.0.0
   */
  public static final IReplacementTarget<Void> EMPTY = new EmptyReplacementTarget();

  /**
   * 
   * @param player
   * @param world
   * @param pos
   * @param side
   * @param hitX
   * @param hitY
   * @param hitZ
   * @param hand
   * @return
   * @since 1.0.0
   */
  @Nullable
  T getTarget(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
      EnumHand hand);

  String getUnlocalizedName();

  /**
  * 
  * @param <T>
  * @param id
  * @param target
  * @return
  * @since 1.0.0
  */
  static <T> IReplacementTarget<T> register(ResourceLocation id, IReplacementTarget<T> target) {
    REGISTRY.register(id, target);
    return target;
  }

  /**
   * @return The set of all replacement targets, excluding {@link #EMPTY}.
   * @since 1.0.0
   */
  static Set<IReplacementTarget<?>> getAllTargets() {
    if (REGISTRY.isFrozen()) {
      if (ReplacementTargets.targets == null)
        ReplacementTargets.targets = ReplacementTargets.buildTargetSet();
      return ReplacementTargets.targets;
    }
    return ReplacementTargets.buildTargetSet();
  }
}