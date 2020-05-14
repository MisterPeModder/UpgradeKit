package com.misterpemodder.upgradekit.impl;

public interface IReplacementConfig {
  Mode getMode();

  static enum Mode {
    REPLACE, UPGRADE_ONLY, DOWNGRADE_ONLY;

    public String getUnlocalizedName() {
      return "upgradekit.mode." + this.name().toLowerCase() + ".name";
    }
  }
}