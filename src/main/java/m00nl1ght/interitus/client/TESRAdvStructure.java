package m00nl1ght.interitus.client;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.Mode;
import m00nl1ght.interitus.util.VarBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRAdvStructure extends TileEntitySpecialRenderer<TileEntityAdvStructure> {
	
	private VarBlockPos pos, size, start = new VarBlockPos(), end = new VarBlockPos();
	
	public TESRAdvStructure(TileEntityRendererDispatcher instance) {
		this.rendererDispatcher=instance;
	}

	@Override
	public void render(TileEntityAdvStructure te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (Minecraft.getMinecraft().player.canUseCommandBlock() || Minecraft.getMinecraft().player.isSpectator()) {
			super.render(te, x, y, z, partialTicks, destroyStage, alpha);
			pos = te.getPosition();
			size = te.getStructureSize();

			if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
				if (te.getMode() == Mode.SAVE || te.getMode() == Mode.LOAD) {
					double d1 = pos.getX();
					double d2 = pos.getZ();
					double d6 = y + pos.getY() - 0.01D;
					double d9 = d6 + size.getY() + 0.02D;
					double d3 = size.getX() + 0.02D;
					double d4 = size.getZ() + 0.02D;
					double d5 = x + (d3 < 0.0D ? d1 + 1.0D + 0.01D : d1 - 0.01D);
					double d7 = z + (d4 < 0.0D ? d2 + 1.0D + 0.01D : d2 - 0.01D);
					double d8 = d5 + d3;
					double d10 = d7 + d4;
					
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					GlStateManager.disableFog();
					GlStateManager.disableLighting();
					GlStateManager.disableTexture2D();
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					this.setLightmapDisabled(true);

					if (te.getMode() == Mode.SAVE || te.showsBoundingBox()) {
						this.renderBox(tessellator, bufferbuilder, d5, d6, d7, d8, d9, d10, 255, 223, 127);
					}

					if (te.getMode() == Mode.SAVE && te.showsAir()) {
						this.renderInvisibleBlocks(te, x, y, z, tessellator, bufferbuilder, true);
						this.renderInvisibleBlocks(te, x, y, z, tessellator, bufferbuilder, false);
					}

					this.setLightmapDisabled(false);
					GlStateManager.glLineWidth(1.0F);
					GlStateManager.enableLighting();
					GlStateManager.enableTexture2D();
					GlStateManager.enableDepth();
					GlStateManager.depthMask(true);
					GlStateManager.enableFog();
				}
			}
		}
	}

	private void renderInvisibleBlocks(TileEntityAdvStructure te, double x, double y, double z, Tessellator tess, BufferBuilder buffer, boolean opt) {
		GlStateManager.glLineWidth(opt ? 3.0F : 1.0F);
		buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
		World world = te.getWorld();
		BlockPos te_pos = te.getPos();
		start.setAdd(te_pos, pos);
		end.setAdd(start, size, -1, -1, -1);

		for (VarBlockPos pointer : VarBlockPos.getBoxIterator(start, end)) {
			IBlockState iblockstate = world.getBlockState(pointer);
			boolean air = iblockstate == Blocks.AIR.getDefaultState();
			boolean voiD = iblockstate == Blocks.STRUCTURE_VOID.getDefaultState();

			if (air || voiD) {
				float f = air ? 0.05F : 0.0F;
				double d0 = pointer.getX() - te_pos.getX() + 0.45F + x - f;
				double d1 = pointer.getY() - te_pos.getY() + 0.45F + y - f;
				double d2 = pointer.getZ() - te_pos.getZ() + 0.45F + z - f;
				double d3 = pointer.getX() - te_pos.getX() + 0.55F + x + f;
				double d4 = pointer.getY() - te_pos.getY() + 0.55F + y + f;
				double d5 = pointer.getZ() - te_pos.getZ() + 0.55F + z + f;

				if (opt) {
					RenderGlobal.drawBoundingBox(buffer, d0, d1, d2, d3, d4, d5, 0.0F, 0.0F, 0.0F, 1.0F);
				} else if (air) {
					RenderGlobal.drawBoundingBox(buffer, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F);
				} else {
					RenderGlobal.drawBoundingBox(buffer, d0, d1, d2, d3, d4, d5, 1.0F, 0.25F, 0.25F, 1.0F);
				}
			}
		}

		tess.draw();
	}

	private void renderBox(Tessellator p_190055_1_, BufferBuilder p_190055_2_, double a, double b, double c, double d, double e, double f, int g, int h, int i) {
		GlStateManager.glLineWidth(2.0F);
		p_190055_2_.begin(3, DefaultVertexFormats.POSITION_COLOR);
		p_190055_2_.pos(a, b, c).color(h, h, h, 0.0F).endVertex();
		p_190055_2_.pos(a, b, c).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, b, c).color(h, i, i, g).endVertex();
		p_190055_2_.pos(d, b, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, b, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, b, c).color(i, i, h, g).endVertex();
		p_190055_2_.pos(a, e, c).color(i, h, i, g).endVertex();
		p_190055_2_.pos(d, e, c).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, e, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, e, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, e, c).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, e, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(a, b, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, b, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, e, f).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, e, c).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, b, c).color(h, h, h, g).endVertex();
		p_190055_2_.pos(d, b, c).color(h, h, h, 0.0F).endVertex();
		p_190055_1_.draw();
		GlStateManager.glLineWidth(1.0F);
	}

	@Override
	public boolean isGlobalRenderer(TileEntityAdvStructure te) {
		return true;
	}

}
