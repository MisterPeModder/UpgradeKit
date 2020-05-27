package com.misterpemodder.upgradekit.impl.render;

import com.misterpemodder.upgradekit.api.capability.CapabilityUpgradeTool;
import com.misterpemodder.upgradekit.api.capability.IUpgradeTool;
import com.misterpemodder.upgradekit.impl.target.ReplacementTargets;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Vector3;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.common.render.WrenchOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Mostly based on WrenchOverlayRenderer from GTCE.
 * https://github.com/GregTechCE
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class UpgradeToolOverlayRenderer {
  @SubscribeEvent
  public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
    EntityPlayer player = event.getPlayer();
    World world = player.world;
    RayTraceResult target = event.getTarget();
    ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
    IUpgradeTool tool = heldItem.getCapability(CapabilityUpgradeTool.INSTANCE, null);

    if (target.typeOfHit != RayTraceResult.Type.BLOCK || tool == null)
      return;

    BlockPos pos = target.getBlockPos();
    IBlockState blockState = world.getBlockState(pos);
    TileEntity tileEntity = world.getTileEntity(pos);

    if (tileEntity != null && shouldDrawOverlay(tool, tileEntity)
        && WrenchOverlayRenderer.useGridForRayTraceResult(target)) {
      EnumFacing facing = target.sideHit;

      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);
      GlStateManager.glLineWidth(2.0F);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);

      if (world.getWorldBorder().contains(pos)) {
        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
        AxisAlignedBB box = blockState.getSelectedBoundingBox(world, pos).grow(0.002D).offset(-d3, -d4, -d5);

        RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 0.0F, 0.0F, 0.4F);
        drawOverlayLines(facing, box);
      }

      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();

      event.setCanceled(true);
    }
  }

  public static boolean shouldDrawOverlay(IUpgradeTool tool, TileEntity tileEntity) {
    if (tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null)) {
      return tool.getConfig().getCurrentTarget() == ReplacementTargets.COVER;
    }
    return false;
  }

  private static void drawOverlayLines(EnumFacing facing, AxisAlignedBB box) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();

    buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

    Vector3 topRight = new Vector3(box.maxX, box.maxY, box.maxZ);
    Vector3 bottomRight = new Vector3(box.maxX, box.minY, box.maxZ);
    Vector3 bottomLeft = new Vector3(box.minX, box.minY, box.maxZ);
    Vector3 topLeft = new Vector3(box.minX, box.maxY, box.maxZ);
    Vector3 shift = new Vector3(0.25, 0, 0);
    Vector3 shiftVert = new Vector3(0, 0.25, 0);
    Vector3 cubeCenter = new Vector3(box.getCenter());

    topRight.subtract(cubeCenter);
    bottomRight.subtract(cubeCenter);
    bottomLeft.subtract(cubeCenter);
    topLeft.subtract(cubeCenter);

    switch (facing) {
      case WEST: {
        topRight.rotate(Math.PI / 2, Vector3.down);
        bottomRight.rotate(Math.PI / 2, Vector3.down);
        bottomLeft.rotate(Math.PI / 2, Vector3.down);
        topLeft.rotate(Math.PI / 2, Vector3.down);
        shift.rotate(Math.PI / 2, Vector3.down);
        shiftVert.rotate(Math.PI / 2, Vector3.down);
        break;
      }
      case EAST: {
        topRight.rotate(-Math.PI / 2, Vector3.down);
        bottomRight.rotate(-Math.PI / 2, Vector3.down);
        bottomLeft.rotate(-Math.PI / 2, Vector3.down);
        topLeft.rotate(-Math.PI / 2, Vector3.down);
        shift.rotate(-Math.PI / 2, Vector3.down);
        shiftVert.rotate(-Math.PI / 2, Vector3.down);
        break;
      }
      case NORTH: {
        topRight.rotate(Math.PI, Vector3.down);
        bottomRight.rotate(Math.PI, Vector3.down);
        bottomLeft.rotate(Math.PI, Vector3.down);
        topLeft.rotate(Math.PI, Vector3.down);
        shift.rotate(Math.PI, Vector3.down);
        shiftVert.rotate(Math.PI, Vector3.down);
        break;
      }
      case UP: {
        Vector3 side = new Vector3(1, 0, 0);

        topRight.rotate(-Math.PI / 2, side);
        bottomRight.rotate(-Math.PI / 2, side);
        bottomLeft.rotate(-Math.PI / 2, side);
        topLeft.rotate(-Math.PI / 2, side);
        shift.rotate(-Math.PI / 2, side);
        shiftVert.rotate(-Math.PI / 2, side);
        break;
      }
      case DOWN: {
        Vector3 side = new Vector3(1, 0, 0);

        topRight.rotate(Math.PI / 2, side);
        bottomRight.rotate(Math.PI / 2, side);
        bottomLeft.rotate(Math.PI / 2, side);
        topLeft.rotate(Math.PI / 2, side);
        shift.rotate(Math.PI / 2, side);
        shiftVert.rotate(Math.PI / 2, side);
        break;
      }
      default:
    }

    topRight.add(cubeCenter);
    bottomRight.add(cubeCenter);
    bottomLeft.add(cubeCenter);
    topLeft.add(cubeCenter);

    Vector3 shiftNegative = shift.copy().negate();
    Vector3 shiftVertNegative = shiftVert.copy().negate();

    // straight top bottom lines
    startLine(buffer, topRight.copy().add(shiftNegative));
    endLine(buffer, bottomRight.copy().add(shiftNegative));

    startLine(buffer, bottomLeft.copy().add(shift));
    endLine(buffer, topLeft.copy().add(shift));

    // straight side to side lines
    startLine(buffer, topLeft.copy().add(shiftVertNegative));
    endLine(buffer, topRight.copy().add(shiftVertNegative));

    startLine(buffer, bottomLeft.copy().add(shiftVert));
    endLine(buffer, bottomRight.copy().add(shiftVert));

    tessellator.draw();
  }

  private static void startLine(BufferBuilder buffer, Vector3 vec) {
    buffer.pos(vec.x, vec.y, vec.z).color(0, 0, 0, 0.0F).endVertex();
  }

  private static void endLine(BufferBuilder buffer, Vector3 vec) {
    buffer.pos(vec.x, vec.y, vec.z).color(0, 0, 0, 0.5F).endVertex();
  }
}