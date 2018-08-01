package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import m00nl1ght.interitus.structures.StructurePackInfo.PackInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiCreatePack extends GuiScreen {
	
	private GuiButton cancelButton, doneButton;
	private GuiTextField nameField;
	private PackInfo from;
	private GuiScreen parent;
	private String info = "";
	
	public GuiCreatePack(PackInfo from, GuiScreen parent) {
		this.from=from; this.parent=parent;
	}
	
	@Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.cancelButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, 305, 150, 20, "Cancel"));
		this.doneButton = this.addButton(new GuiButton(1, this.width / 2 + 4, 305, 150, 20, "Create"));
		this.nameField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 154, 100, 200, 20);
        this.nameField.setMaxStringLength(24);
        this.nameField.setText("");
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				this.mc.displayGuiScreen(parent);
			} else if (button.id == 1) {
				String name = this.nameField.getText();
				if (name.isEmpty()) {
					info="Invalid name: Name is empty.";
					return;
				}
				if (StructurePackInfo.packExists(name)) {
					info="Invalid name: The structure pack <"+name+"> already exists.";
					return;
				}
				if (CDefaultPackage.packGuiAction(3, name, from==null?"":from.name)) {
					StructurePackInfo.packs.add(new PackInfo(name, from));
					this.mc.displayGuiScreen(parent);
					info="";
					return;
				}
				info="An unknown error occured while creating the pack.";
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.actionPerformed(this.cancelButton);
		}
		if (this.nameField.getVisible()) {
			this.nameField.textboxKeyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (this.nameField.getVisible()) {
			this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		this.drawCenteredString(this.fontRenderer, from==null?"Create Structure Pack":"Create copy of "+from.name, this.width / 2, 10, 16777215);
		this.drawString(this.fontRenderer, "Enter a name for the new structure pack:", this.width / 2 - 154, 84, 16777215);
		this.nameField.drawTextBox();
		this.drawString(this.fontRenderer, info, this.width / 2 - 154, 150, 16777120);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
