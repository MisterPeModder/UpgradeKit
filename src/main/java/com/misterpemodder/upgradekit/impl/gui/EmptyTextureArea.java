package com.misterpemodder.upgradekit.impl.gui;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.PositionedRect;
import gregtech.api.util.Size;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EmptyTextureArea extends TextureArea {
  public static EmptyTextureArea INSTANCE = new EmptyTextureArea();

  private EmptyTextureArea() {
    super(new ResourceLocation(GTValues.MODID, "textures/items/void.png"), 0.0, 0.0, 0.0, 0.0);
  }

  @Override
  public TextureArea getSubArea(double offsetX, double offsetY, double width, double height) {
    return this;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawRotated(int x, int y, Size areaSize, PositionedRect positionedRect, int orientation) {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void draw(double x, double y, int width, int height) {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth,
      double drawnHeight) {
  }
}