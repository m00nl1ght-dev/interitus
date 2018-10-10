package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.util.function.Function;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiConfirm extends GuiEditor {
	
	private GuiButton button1, button2;
	private GuiScreen parent;
	private String text1, text2, opt1, opt2;
	private Function<Integer, Boolean> func;
	
	public GuiConfirm(GuiScreen parent, Runnable editorCB, String text1, String text2, String opt1, String opt2, Function<Integer, Boolean> func) {
		super(editorCB); this.parent=parent; this.text1=text1; this.text2=text2; this.opt1=opt1; this.opt2=opt2; this.func=func;
	}
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.button1 = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, 305, 150, 20, opt1));
		this.button2 = this.addButton(new GuiButton(1, this.width / 2 + 4, 305, 150, 20, opt2));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				if (func.apply(0)) {
					this.transition(parent);
				}
			} else if (button.id == 1) {
				if (func.apply(1)) {
					this.transition(parent);
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.actionPerformed(this.button1);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		this.drawString(this.fontRenderer, text1, this.width / 2 - 154, 130, 16777215);
		this.drawString(this.fontRenderer, text2, this.width / 2 - 154, 145, 16777215);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
