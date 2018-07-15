package m00nl1ght.interitus.client.gui;

import org.lwjgl.opengl.GL11;

import m00nl1ght.interitus.util.Toolkit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

public abstract class GuiDropdown extends Gui {

	private static GuiDropdown open;
	private static int openX, openY;

	protected int w, h, listH;
	protected String text = "";
	protected final FontRenderer renderer;
    protected boolean isEnabled = true, isOpenable = false;
    protected int enabledColor = 14737632;
    protected int disabledColor = 7368816;
    protected static boolean mState;
    
    protected int wBtn = 0;
    protected final int slotHeight = 15;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    protected float scrollDistance;
    private final double scaleW, scaleH;
    private final Minecraft client;

	public GuiDropdown(FontRenderer renderer, int w, int h, int listH) {
		this.w = w;
		this.h = h;
		this.listH = listH;
		this.renderer = renderer;
		client = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(client);
		scaleW = client.displayWidth / res.getScaledWidth_double();
		scaleH = client.displayHeight / res.getScaledHeight_double();
		this.setOpenable(true);
	}
	
	public boolean draw(int x, int y, int mX, int mY, boolean clicked) {
		this.drawTextBox(x, y, mX, mY, clicked && !mState);
		if (this==open) {
			this.drawButton(x+w-wBtn+1, y, wBtn, 20, mX, mY, clicked && !mState);
			return true;
		} else {
			if (this.isOpenable && this.drawButton(x+w-wBtn+1, y, wBtn, 20, mX, mY, clicked && !mState)) {
				this.open(x, y+h);
				mState=true;
			}
			return false;
		}
	}
	
	public static boolean drawDropdown(int mX, int mY, boolean clicked) {
		if (open==null) {mState = clicked; return false;}
		if (!open.drawList(openX, openY, mX, mY, clicked) && clicked && !mState) {
			open.close();
			mState = clicked;
			return true;
		}
		mState = clicked;
		return false;
	}
	
	public boolean drawButton(int x, int y, int w, int h, int mX, int mY, boolean click) {
		boolean flag = mX >= x && mX < x + w && mY >= y && mY < y + h;
		Minecraft.getMinecraft().getTextureManager().bindTexture(Toolkit.BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect(x, y, 0, flag?86:66, w / 2, h);
		this.drawTexturedModalRect(x + w / 2, y, 200 - w / 2, flag?86:66, w / 2, h);
		this.drawCenteredString(renderer, "...", x + w / 2, y + (h - 8) / 2, flag?16777120:14737632);
		return flag && click;
	}
	
	public void drawTextBox(int x, int y, int mx, int mY, boolean click) {
		drawRect(x - 1, y - 1, x + w + 1, y + h + 1, -6250336);
        drawRect(x, y, x + w, y + h, -16777216);
        renderer.drawStringWithShadow(text, x+4, y + (h - 8) / 2, this.isEnabled ? this.enabledColor : this.disabledColor);
	}
	
	public static void drawCheckbox(int x, int y, int w, int h, boolean checked) {
		drawRect(x - 1, y - 1, x + w + 1, y + h + 1, -6250336);
        drawRect(x, y, x + w, y + h, -16777216);
        if (checked) drawRect(x + 3, y + 3, x + w - 3, y + h - 3, -6250336);
	}
	
	public boolean drawList(int x, int y, int mX, int mY, boolean clicked) {
		boolean flag = mX >= x && mX < x + w && mY >= y && mY < y + listH;
		int listLength = this.getElementCount();
		int scrollBarWidth = 6;
		int scrollBarRight = x + w;
		int scrollBarLeft = scrollBarRight - scrollBarWidth;
		int entryLeft = x;
		int entryRight = scrollBarLeft - 1;
		int viewHeight = listH;
		int border = 4;
		int mouseListY = mY - y + (int) this.scrollDistance - border;
		int hoverIdx = mouseListY / this.slotHeight;

		if (clicked) {
			if (this.initialMouseClickY == -1.0F) {
				if (flag) {
					if (mX >= entryLeft && mX <= entryRight && hoverIdx >= 0 && mouseListY >= 0 && hoverIdx < listLength) {
						this.slotClicked(hoverIdx);
					}

					if (mX >= scrollBarLeft && mX <= scrollBarRight) {
						this.scrollFactor = -1.0F;
						int scrollHeight = this.slotHeight*listLength - viewHeight - border;
						if (scrollHeight < 1) scrollHeight = 1;

						int var13 = (int) ((float) (viewHeight * viewHeight) / (float) (listLength*this.slotHeight));

						if (var13 < 32) var13 = 32;
						if (var13 > viewHeight - border * 2) var13 = viewHeight - border * 2;

						this.scrollFactor /= (float) (viewHeight - var13) / (float) scrollHeight;
					} else {
						this.scrollFactor = 1.0F;
					}

					this.initialMouseClickY = mY;
				} else {
					this.initialMouseClickY = -2.0F;
				}
			} else if (this.initialMouseClickY >= 0.0F) {
				this.scrollDistance -= (mY - this.initialMouseClickY) * this.scrollFactor;
				this.initialMouseClickY = mY;
			}
		} else {
			this.initialMouseClickY = -1.0F;
		}

		this.applyScrollLimits();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder worldr = tess.getBuffer();

		drawRect(x - 1, y, x + w + 1, y + listH + 1, -6250336);
        drawRect(x, y + 1, x + w, y + listH, -16777216);
        
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) (x * scaleW), (int) (client.displayHeight - ((y - 1 + listH) * scaleH)), (int) (w * scaleW), (int) ((viewHeight - 3) * scaleH));

		int baseY = y + border - (int) this.scrollDistance;

		for (int slotIdx = 0; slotIdx < listLength; ++slotIdx) {
			int slotTop = baseY + slotIdx * this.slotHeight;
			int slotBuffer = this.slotHeight - border;
			if (slotTop <= (y + listH) && slotTop + slotBuffer >= y && mX <= entryRight) {
				if (flag && hoverIdx==slotIdx) {
					int min = x;
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

			if (this.drawSlot(slotIdx, entryLeft, slotTop, slotBuffer, tess)) {
				slotIdx--;
				listLength--;
			}
		}

		GlStateManager.disableDepth();

		int extraHeight = (this.slotHeight*listLength + border) - viewHeight;
		if (extraHeight > 0) {
			int height = (viewHeight * viewHeight) / (this.slotHeight*listLength);

			if (height < 32) height = 32;

			if (height > viewHeight - border * 2) height = viewHeight - border * 2;

			int barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + y;
			if (barTop < y) {
				barTop = y;
			}

			GlStateManager.disableTexture2D();
			worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(scrollBarLeft, y + listH, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarRight, y + listH, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarRight, y, 0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(scrollBarLeft, y, 0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
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

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		return flag;
	}
	
	protected boolean drawSlot(int slotIdx, int x, int y, int slotBuffer, Tessellator tess) {
		renderer.drawStringWithShadow(this.getElement(slotIdx), x+5, y+2, 14737632);
		return false;
	}
	
	protected void slotClicked(int id) {
		this.setText(this.getElement(id));
		this.close();
	}
	
	private void applyScrollLimits() {
		int listHeight = this.slotHeight*this.getElementCount() - listH + 4;
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
	
	protected abstract int getElementCount();
	
	protected abstract String getElement(int id);
	
	public void setText(String text) {
		this.text=text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setOpenable(boolean b) {
		this.isOpenable = b;
		this.wBtn = b?15:0;
		if (!b) {open=null;}
	}
	
	public void open(int x, int y) {
		openX=x; openY=y;
		open=this;
	}
	
	public static void close() {
		open=null;
	}

	public static boolean isOpen() {
		return open!=null;
	}
	
	public static abstract class GuiCheckboxDropdown extends GuiDropdown {

		public GuiCheckboxDropdown(FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH);
		}
		
		@Override
		protected boolean drawSlot(int slotIdx, int x, int y, int slotBuffer, Tessellator tess) {
			drawCheckbox(x, y, 11, 11, this.getElementState(slotIdx));
			renderer.drawStringWithShadow(this.getElement(slotIdx), x+25, y+2, 14737632);
			return false;
		}
		
		@Override
		protected abstract void slotClicked(int id);
		
		protected abstract boolean getElementState(int id);
		
	}
	
	public static abstract class GuiEditableDropdown extends GuiDropdown {
		
		private int maxStringLength = 32;
	    private int cursorCounter;
	    private boolean canLoseFocus = true;
	    private boolean isFocused;
	    private int lineScrollOffset;
	    private int cursorPosition;
	    private int selectionEnd;

		public GuiEditableDropdown(FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH);
		}
		
		@Override
		public void drawTextBox(int x, int y, int mX, int mY, boolean click) {
			boolean flag0 = mX >= x && mX < x + w && mY >= y && mY < y + h;
			drawRect(x - 1, y - 1, x + w + 1, y + h + 1, -6250336);
	        drawRect(x, y, x + w, y + h, -16777216);
	        
	        if (click) {
	        	if (this.canLoseFocus) {this.setFocused(flag0);}
				if (this.isFocused && flag0) {
					int i0 = mX - x - 4;
					String s0 = this.renderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), w - 8 - wBtn);
					this.setCursorPosition(this.renderer.trimStringToWidth(s0, i0).length() + this.lineScrollOffset);
				}
	        }

			int color = this.isEnabled ? this.enabledColor : this.disabledColor;
			int j = this.cursorPosition - this.lineScrollOffset;
			int k = this.selectionEnd - this.lineScrollOffset;
			String s = this.renderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), w - 8 - wBtn);
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
			int i1 = y + (h - 8) / 2;
			int j1 = x + 4;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = this.renderer.drawStringWithShadow(s1, x + 4, i1, color);
			}

			boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.maxStringLength;
			int k1 = j1;

			if (!flag) {
				k1 = j > 0 ? x + 4 + w - wBtn: x + 4;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				j1 = this.renderer.drawStringWithShadow(s.substring(j), j1, i1, color);
			}

			if (flag1) {
				if (flag2) {
					Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.renderer.FONT_HEIGHT, -3092272);
				} else {
					this.renderer.drawStringWithShadow("_", k1, i1, color);
				}
			}

			if (k != j) {
				int startX = k1, startY = i1 - 1, endX = x + 3 + this.renderer.getStringWidth(s.substring(0, k)), endY = i1 + 1 + this.renderer.FONT_HEIGHT;
				if (startX < endX) {int ni = startX; startX = endX; endX = ni;}
				if (startY < endY) {int nj = startY; startY = endY; endY = nj;}
				if (endX > x + w - wBtn) {endX = x + w - wBtn;}
				if (startX > x + w - wBtn) {startX = x + w - wBtn;}
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.enableColorLogic();
				GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
				bufferbuilder.pos(startX, endY, 0.0D).endVertex();
				bufferbuilder.pos(endX, endY, 0.0D).endVertex();
				bufferbuilder.pos(endX, startY, 0.0D).endVertex();
				bufferbuilder.pos(startX, startY, 0.0D).endVertex();
				tessellator.draw();
				GlStateManager.disableColorLogic();
				GlStateManager.enableTexture2D();
			}
			
		}

		public void updateCursorCounter() {
			++this.cursorCounter;
		}
		
		@Override
		public void setText(String textIn) {
			if (textIn.length() > this.maxStringLength) {
				this.text = textIn.substring(0, this.maxStringLength);
			} else {
				this.text = textIn;
			}
			this.setCursorPositionEnd();
		}
		
		public String getSelectedText() {
			int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
			int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
			return this.text.substring(i, j);
		}
		
		public void writeText(String textToWrite) {
			String s = "";
			String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
			int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
			int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
			int k = this.maxStringLength - this.text.length() - (i - j);

			if (!this.text.isEmpty()) {
				s = s + this.text.substring(0, i);
			}

			int l;

			if (k < s1.length()) {
				s = s + s1.substring(0, k);
				l = k;
			} else {
				s = s + s1;
				l = s1.length();
			}

			if (!this.text.isEmpty() && j < this.text.length()) {
				s = s + this.text.substring(j);
			}

			this.text = s;
			this.moveCursorBy(i - this.selectionEnd + l);
		}
		
		public void deleteWords(int num) {
			if (!this.text.isEmpty()) {
				if (this.selectionEnd != this.cursorPosition) {
					this.writeText("");
				} else {
					this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
				}
			}
		}

		public void deleteFromCursor(int num) {
			if (!this.text.isEmpty()) {
				if (this.selectionEnd != this.cursorPosition) {
					this.writeText("");
				} else {
					boolean flag = num < 0;
					int i = flag ? this.cursorPosition + num : this.cursorPosition;
					int j = flag ? this.cursorPosition : this.cursorPosition + num;
					String s = "";

					if (i >= 0) {
						s = this.text.substring(0, i);
					}

					if (j < this.text.length()) {
						s = s + this.text.substring(j);
					}

					this.text = s;
					if (flag) {
						this.moveCursorBy(num);
					}
				}
			}
		}

		public int getNthWordFromCursor(int numWords) {
			return this.getNthWordFromPos(numWords, this.getCursorPosition());
		}

		public int getNthWordFromPos(int n, int pos) {
			return this.getNthWordFromPosWS(n, pos, true);
		}

		public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
			int i = pos;
			boolean flag = n < 0;
			int j = Math.abs(n);

			for (int k = 0; k < j; ++k) {
				if (!flag) {
					int l = this.text.length();
					i = this.text.indexOf(32, i);

					if (i == -1) {
						i = l;
					} else {
						while (skipWs && i < l && this.text.charAt(i) == ' ') {
							++i;
						}
					}
				} else {
					while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
						--i;
					}

					while (i > 0 && this.text.charAt(i - 1) != ' ') {
						--i;
					}
				}
			}

			return i;
		}

		public void moveCursorBy(int num) {
			this.setCursorPosition(this.selectionEnd + num);
		}

		public void setCursorPosition(int pos) {
			this.cursorPosition = pos;
			int i = this.text.length();
			this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
			this.setSelectionPos(this.cursorPosition);
		}
		
		public void setCursorPositionZero() {
			this.setCursorPosition(0);
		}

		public void setCursorPositionEnd() {
			this.setCursorPosition(this.text.length());
		}

		public void setMaxStringLength(int length) {
			this.maxStringLength = length;
			if (this.text.length() > length) {
				this.text = this.text.substring(0, length);
			}
		}
		
		public int getCursorPosition() {
			return this.cursorPosition;
		}

		public void setFocused(boolean b) {
			if (b && !this.isFocused) {
				this.cursorCounter = 0;
			}
			this.isFocused = b;
			if (Minecraft.getMinecraft().currentScreen != null) {
				Minecraft.getMinecraft().currentScreen.setFocused(b);
			}
		}

		public void setEnabled(boolean b) {
			this.isEnabled = b;
		}
		
		public int getSelectionEnd() {
	        return this.selectionEnd;
	    }
		
		public void setSelectionPos(int position) {
			int i = this.text.length();
			if (position > i) {
				position = i;
			}
			if (position < 0) {
				position = 0;
			}
			this.selectionEnd = position;
			if (this.renderer != null) {
				if (this.lineScrollOffset > i) {
					this.lineScrollOffset = i;
				}

				int j = this.w - 8 - wBtn;
				String s = this.renderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
				int k = s.length() + this.lineScrollOffset;

				if (position == this.lineScrollOffset) {
					this.lineScrollOffset -= this.renderer.trimStringToWidth(this.text, j, true).length();
				}

				if (position > k) {
					this.lineScrollOffset += position - k;
				} else if (position <= this.lineScrollOffset) {
					this.lineScrollOffset -= this.lineScrollOffset - position;
				}

				this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
			}
		}

		public boolean textboxKeyTyped(char typedChar, int keyCode) {
			if (!this.isFocused) {
				return false;
			} else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
				this.setCursorPositionEnd();
				this.setSelectionPos(0);
				return true;
			} else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
				GuiScreen.setClipboardString(this.getSelectedText());
				return true;
			} else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
				if (this.isEnabled) {
					this.writeText(GuiScreen.getClipboardString());
				}
				return true;
			} else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
				GuiScreen.setClipboardString(this.getSelectedText());
				if (this.isEnabled) {
					this.writeText("");
				}
				return true;
			} else {
				switch (keyCode) {
					case 14:
						if (GuiScreen.isCtrlKeyDown()) {
							if (this.isEnabled) {
								this.deleteWords(-1);
							}
						} else if (this.isEnabled) {
							this.deleteFromCursor(-1);
						}
						return true;
					case 199:
						if (GuiScreen.isShiftKeyDown()) {
							this.setSelectionPos(0);
						} else {
							this.setCursorPositionZero();
						}
						return true;
					case 203:
						if (GuiScreen.isShiftKeyDown()) {
							if (GuiScreen.isCtrlKeyDown()) {
								this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
							} else {
								this.setSelectionPos(this.getSelectionEnd() - 1);
							}
						} else if (GuiScreen.isCtrlKeyDown()) {
							this.setCursorPosition(this.getNthWordFromCursor(-1));
						} else {
							this.moveCursorBy(-1);
						}
						return true;
					case 205:
						if (GuiScreen.isShiftKeyDown()) {
							if (GuiScreen.isCtrlKeyDown()) {
								this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
							} else {
								this.setSelectionPos(this.getSelectionEnd() + 1);
							}
						} else if (GuiScreen.isCtrlKeyDown()) {
							this.setCursorPosition(this.getNthWordFromCursor(1));
						} else {
							this.moveCursorBy(1);
						}
						return true;
					case 207:
						if (GuiScreen.isShiftKeyDown()) {
							this.setSelectionPos(this.text.length());
						} else {
							this.setCursorPositionEnd();
						}
						return true;
					case 211:
						if (GuiScreen.isCtrlKeyDown()) {
							if (this.isEnabled) {
								this.deleteWords(1);
							}
						} else if (this.isEnabled) {
							this.deleteFromCursor(1);
						}
						return true;
					default:
						if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
							if (this.isEnabled) {
								this.writeText(Character.toString(typedChar));
							}
							return true;
						} else {
							return false;
						}
				}
			}
		}
		
	}

}
