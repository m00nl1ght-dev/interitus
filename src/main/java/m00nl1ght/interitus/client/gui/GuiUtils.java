package m00nl1ght.interitus.client.gui;

import org.lwjgl.opengl.GL11;

import m00nl1ght.interitus.util.Toolkit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiUtils {
	
	private GuiUtils() {}
	
	public static boolean drawCheckbox(int x, int y, int w, int h, boolean checked, int mX, int mY, boolean clicked) {
		Gui.drawRect(x - 1, y - 1, x + w + 1, y + h + 1, -6250336);
        Gui.drawRect(x, y, x + w, y + h, -16777216);
        if (checked) Gui.drawRect(x + 3, y + 3, x + w - 3, y + h - 3, -6250336);
        return clicked && (mX >= x && mX < x + w && mY >= y && mY < y + h);
	}
	
	public static boolean drawButton(int x, int y, int w, int h, int mX, int mY, boolean click, FontRenderer renderer) {
		boolean flag = mX >= x && mX < x + w && mY >= y && mY < y + h;
		Minecraft.getMinecraft().getTextureManager().bindTexture(Toolkit.BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		drawTexturedModalRect(x, y, 0, flag?86:66, w / 2, h);
		drawTexturedModalRect(x + w / 2, y, 200 - w / 2, flag?86:66, w / 2, h);
		drawCenteredString(renderer, "...", x + w / 2, y + (h - 8) / 2, flag?16777120:14737632);
		return flag && click;
	}
	
	public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double) (x + 0), (double) (y + height), 0D).tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + height), 0D).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + 0), 0D).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F)).endVertex();
		bufferbuilder.pos((double) (x + 0), (double) (y + 0), 0D).tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F)).endVertex();
		tessellator.draw();
	}

	public static void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, (float) (x - fontRendererIn.getStringWidth(text) / 2), (float) y, color);
	}
	
	public static void drawRect(int x, int y, int w, int h, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x+w, y, 0.0D).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x,  y, 0.0D).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x,  y+h, 0.0D).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x+w, y+h, 0.0D).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

}
