package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import m00nl1ght.interitus.util.Toolkit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public abstract class GuiList extends Gui {
	
	private final Minecraft client;
    protected final int w;
    protected final int h;
    protected final int y;
    protected final int x;
    protected final int slotHeight;
    protected int mouseX;
    protected int mouseY;
    protected boolean mDown, mDownPre;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    protected float scrollDistance;
    protected int selectedIndex = -1;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;

	public GuiList(Minecraft client, int x, int y, int w, int h, int entryHeight) {
		this.client = client;
		this.w = w;
		this.h = h;
		this.y = y;
		this.x = x;
		this.slotHeight = entryHeight;
	}

    public void setHighlightSelection(boolean flag) {
        this.highlightSelected = flag;
    }

    protected void setHeaderInfo(boolean hasHeader, int headerHeight) {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader) this.headerHeight = 0;
    }

    protected abstract int getElementCount();
    
	public boolean isSelectionValid() {
		return this.selectedIndex>=0 && this.selectedIndex<this.getElementCount(); // untested
	}

    protected int getContentHeight() {
        return this.getElementCount() * this.slotHeight + this.headerHeight;
    }

    protected void drawBackground() {}

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     * 
     * Return true if the entry was removed in the process
     */
    protected abstract boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {}

    protected void clickHeader(int x, int y) {}

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawOverlay(int mouseX, int mouseY) {}

	private void applyScrollLimits() {
		int listHeight = this.getContentHeight() - this.h;
		if (this.scrollDistance < 0.0F) {
			this.scrollDistance = 0.0F;
		}
		if (this.scrollDistance > listHeight) {
			if (listHeight<0) {
				this.scrollDistance = 0;
			} else {
				this.scrollDistance = listHeight;
			}
		}
	}

	public void handleMouseInput(int mouseX, int mouseY) throws IOException {
		boolean isHovering = mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y && mouseY <= this.y + this.h;
		if (!isHovering)
			return;

		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			this.scrollDistance += (-1 * scroll / 120.0F) * this.slotHeight / 2;
		}
	}
	
	public int getEntryY(int index) {
		return this.y + 4 - (int) this.scrollDistance + index * this.slotHeight + this.headerHeight;
	}
	
	protected void elementClicked(int index, boolean doubleClick) {}

	protected boolean isSelected(int index) {
		return this.selectedIndex == index;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawBackground();

		boolean isHovering = mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y && mouseY <= this.y + this.h;
		int listLength = this.getElementCount();
		int scrollBarWidth = 6;
		int scrollBarRight = this.x + this.w;
		int scrollBarLeft = scrollBarRight - scrollBarWidth;
		int entryLeft = this.x;
		int entryRight = scrollBarLeft - 1;
		int viewHeight = this.h;
		int border = 4;
		
		this.mDown = Mouse.isButtonDown(0);

		if (mDown) {
			if (this.initialMouseClickY == -1.0F) {
				if (isHovering) {
					int mouseListY = mouseY - this.y - this.headerHeight + (int) this.scrollDistance - border;
					int slotIndex = mouseListY / this.slotHeight;

					if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength) {
						this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
						this.selectedIndex = slotIndex;
						this.lastClickTime = System.currentTimeMillis();
					} else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0) {
						this.clickHeader(mouseX - entryLeft, mouseY - this.y + (int) this.scrollDistance - border);
					}

					if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
						this.scrollFactor = -1.0F;
						int scrollHeight = this.getContentHeight() - viewHeight - border;
						if (scrollHeight < 1)
							scrollHeight = 1;

						int var13 = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());

						if (var13 < 32)
							var13 = 32;
						if (var13 > viewHeight - border * 2)
							var13 = viewHeight - border * 2;

						this.scrollFactor /= (float) (viewHeight - var13) / (float) scrollHeight;
					} else {
						this.scrollFactor = 1.0F;
					}

					this.initialMouseClickY = mouseY;
				} else {
					this.initialMouseClickY = -2.0F;
				}
			} else if (this.initialMouseClickY >= 0.0F) {
				this.scrollDistance -= (mouseY - this.initialMouseClickY) * this.scrollFactor;
				this.initialMouseClickY = mouseY;
			}
		} else {
			this.initialMouseClickY = -1.0F;
		}

		this.applyScrollLimits();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder worldr = tess.getBuffer();

		ScaledResolution res = new ScaledResolution(client);
		double scaleW = client.displayWidth / res.getScaledWidth_double();
		double scaleH = client.displayHeight / res.getScaledHeight_double();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) (x * scaleW), (int) (client.displayHeight - ((this.y+this.h) * scaleH)), (int) (w * scaleW), (int) (viewHeight * scaleH));

		this.drawGradientRect(this.x, this.y, this.w + this.x, this.y + this.h, 0xC0101010, 0xD0101010);

		int baseY = this.y + border - (int) this.scrollDistance;

		if (this.hasHeader) {
			this.drawHeader(entryRight, baseY, tess);
		}

		for (int slotIdx = 0; slotIdx < listLength; ++slotIdx) {
			int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
			int slotBuffer = this.slotHeight - border;
			
			if (slotTop <= (this.y + this.h) && slotTop + slotBuffer >= this.y) {
				if (this.highlightSelected && this.isSelected(slotIdx)) {
					int min = this.x;
					int max = entryRight;
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.disableTexture2D();
					worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
					worldr.pos(min, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
					worldr.pos(max, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
					worldr.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
					worldr.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
					worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
					worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
					worldr.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
					worldr.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
					tess.draw();
					GlStateManager.enableTexture2D();
				}
			}
			
			if (this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess)) {slotIdx--; listLength--;}
		}

		GlStateManager.disableDepth();

		int extraHeight = (this.getContentHeight() + border) - viewHeight;
		if (extraHeight > 0) {
			int height = (viewHeight * viewHeight) / this.getContentHeight();

			if (height < 32)
				height = 32;

			if (height > viewHeight - border * 2)
				height = viewHeight - border * 2;

			int barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + this.y;
			if (barTop < this.y) {
				barTop = this.y;
			}

			GlStateManager.disableTexture2D();
			worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(scrollBarLeft, this.y+this.h, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarRight, this.y+this.h, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarRight, this.y, 0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarLeft, this.y, 0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			tess.draw();
			worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(scrollBarLeft, barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(scrollBarRight, barTop, 0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			tess.draw();
			worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(scrollBarLeft, barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(scrollBarRight - 1, barTop, 0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			tess.draw();
		}

		this.drawOverlay(mouseX, mouseY);
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		this.drawExtra();
		
		this.mDownPre = this.mDown;
	}

    protected void drawExtra() {
		
	}

	@Override
	protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
        float a1 = (color1 >> 24 & 255) / 255.0F;
        float r1 = (color1 >> 16 & 255) / 255.0F;
        float g1 = (color1 >>  8 & 255) / 255.0F;
        float b1 = (color1       & 255) / 255.0F;
        float a2 = (color2 >> 24 & 255) / 255.0F;
        float r2 = (color2 >> 16 & 255) / 255.0F;
        float g2 = (color2 >>  8 & 255) / 255.0F;
        float b2 = (color2       & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, 0.0D).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left,  top, 0.0D).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left,  bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        buffer.pos(right, bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    
	protected void drawRect(int x, int y, int w, int h, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
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
    
	public boolean drawButton(Minecraft mc, int x, int y, int w, int h, boolean enabled, String string) {
		mc.getTextureManager().bindTexture(Toolkit.BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
		int flag = 1;
        if (!enabled) {flag = 0;} else if (hovered) {flag = 2;}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect(x, y, 0, 46 + flag * 20, w / 2, h);
		this.drawTexturedModalRect(x + w / 2, y, 200 - w / 2, 46 + flag * 20, w / 2, h);
		int j = 14737632;
		if (!enabled) {j = 10526880;} else if (hovered) {j = 16777120;}
		this.drawCenteredString(mc.fontRenderer, string, x + w / 2, y + (h - 8) / 2, j);
		if (enabled && hovered && !mDownPre && mDown) {
			mDownPre = true;
			return true;
		}
		return false;
	}

}
