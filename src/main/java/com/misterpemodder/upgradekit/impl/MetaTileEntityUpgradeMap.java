package com.misterpemodder.upgradekit.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;

public class MetaTileEntityUpgradeMap implements IUpgradeMap<MetaTileEntity> {
  private HashMap<String, List<TieredMetaTileEntity>[]> map;

  @SuppressWarnings("unchecked")
  public MetaTileEntityUpgradeMap() {
    this.map = new HashMap<>();

    for (MetaTileEntity mte : GregTechAPI.META_TILE_ENTITY_REGISTRY) {
      if (mte instanceof TieredMetaTileEntity) {
        TieredMetaTileEntity tmte = (TieredMetaTileEntity) mte;
        String id = UpgradeKit.getMachineId(mte);
        List<TieredMetaTileEntity>[] tiers = this.map.get(id);

        if (tiers == null) {
          tiers = new ArrayList[GTValues.V.length];
          for (int i = 0, l = GTValues.V.length; i < l; ++i) {
            tiers[i] = new ArrayList<>();
          }
          this.map.put(id, tiers);
        }

        tiers[tmte.getTier()].add(tmte);
      }
    }
  }

  @Override
  public boolean hasUpgrades(MetaTileEntity object) {
    if (!(object instanceof TieredMetaTileEntity))
      return false;
    TieredMetaTileEntity mte = (TieredMetaTileEntity) object;
    String id = UpgradeKit.getMachineId(mte);
    List<TieredMetaTileEntity>[] tiers = this.map.get(id);

    if (tiers == null)
      return false;
    int candidatesCount = 0;

    for (int i = 0, l = GTValues.V.length; i < l; ++i)
      candidatesCount += tiers[i].size();
    return candidatesCount > 1;
  }

  @Override
  public boolean canReplace(MetaTileEntity first, MetaTileEntity second) {
    return !first.metaTileEntityId.equals(second.metaTileEntityId)
        && UpgradeKit.getMachineId(first).equals(UpgradeKit.getMachineId(second));
  }

  @Override
  public int getTierDifference(MetaTileEntity first, MetaTileEntity second) {
    return (first instanceof TieredMetaTileEntity ? ((TieredMetaTileEntity) first).getTier() : 0)
        - (second instanceof TieredMetaTileEntity ? ((TieredMetaTileEntity) second).getTier() : 0);
  }
}