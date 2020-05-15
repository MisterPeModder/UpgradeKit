package com.misterpemodder.upgradekit.impl.proxy;

import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {
  @Override
  public int blinkRGBColor(int color) {
    if (Minecraft.getMinecraft().world.getTotalWorldTime() % 40 < 20)
      return color;
    return 0;
  }
}