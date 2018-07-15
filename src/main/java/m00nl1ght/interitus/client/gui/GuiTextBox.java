package m00nl1ght.interitus.client.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

public class GuiTextBox extends Gui {
	
	protected int w, h;
	protected String text = "";
	protected final FontRenderer renderer;
    protected boolean isEnabled = true;
    protected int enabledColor = 14737632;
    protected int disabledColor = 7368816;
	private int maxStringLength = 32;
    private int cursorCounter;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private Predicate<String> validator = Predicates.<String>alwaysTrue();

	public GuiTextBox(FontRenderer renderer, int w, int h) {
		this.w = w;
		this.h = h;
		this.renderer = renderer;
	}

	public String getText() {
		return text; 
	}

	public void drawTextBox(int x, int y, int mX, int mY, boolean click) {
		boolean flag0 = mX >= x && mX < x + w && mY >= y && mY < y + h;
		drawRect(x - 1, y - 1, x + w + 1, y + h + 1, -6250336);
		drawRect(x, y, x + w, y + h, -16777216);

		if (click) {
			if (this.canLoseFocus) {
				this.setFocused(flag0);
			}
			if (this.isFocused && flag0) {
				int i0 = mX - x - 4;
				String s0 = this.renderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), w - 8);
				this.setCursorPosition(this.renderer.trimStringToWidth(s0, i0).length() + this.lineScrollOffset);
			}
		}

		int color = this.isEnabled ? this.enabledColor : this.disabledColor;
		int j = this.cursorPosition - this.lineScrollOffset;
		int k = this.selectionEnd - this.lineScrollOffset;
		String s = this.renderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), w - 8);
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
			k1 = j > 0 ? x + 4 + w : x + 4;
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
			int startX = k1, startY = i1 - 1, endX = x + 3 + this.renderer.getStringWidth(s.substring(0, k)),
					endY = i1 + 1 + this.renderer.FONT_HEIGHT;
			if (startX < endX) {
				int ni = startX;
				startX = endX;
				endX = ni;
			}
			if (startY < endY) {
				int nj = startY;
				startY = endY;
				endY = nj;
			}
			if (endX > x + w) {
				endX = x + w;
			}
			if (startX > x + w) {
				startX = x + w;
			}
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

	public void setValidator(Predicate<String> validator) {
		this.validator = validator;
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

		if (this.validator.apply(s)) {
			this.text = s;
			this.moveCursorBy(i - this.selectionEnd + l);
		}
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

				if (this.validator.apply(s)) {
					this.text = s;
					if (flag) {
						this.moveCursorBy(num);
					}
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

			int j = this.w - 8;
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
