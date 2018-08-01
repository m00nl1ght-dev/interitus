package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiEditStructurePack extends GuiScreen {
	
	private final GuiScreen parent;
	private GuiButton doneButton, structuresButton, lootButton, condButton, signButton;
	private GuiTextField nameField;
	
	public GuiEditStructurePack(GuiScreen parent) {
		this.parent=parent;
	}
	
	@Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Done"));
		this.structuresButton = this.addButton(new GuiButton(1, this.width / 2 - 154, 150, 150, 20, "Structures"));
		this.lootButton = this.addButton(new GuiButton(2, this.width / 2 - 154, 180, 150, 20, "Loot Lists"));
		this.condButton = this.addButton(new GuiButton(4, this.width / 2 - 154, 210, 150, 20, "Conditions"));
		this.signButton = this.addButton(new GuiButton(3, this.width / 2 - 154, 80, 150, 20, "Sign this pack"));
		this.signButton.enabled = !StructurePackInfo.active.read_only;
		this.nameField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 154, 50, 300, 20);
        this.nameField.setMaxStringLength(40);
        this.nameField.setText(StructurePackInfo.active.description);
        this.nameField.setEnabled(!StructurePackInfo.active.read_only);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				sendDescription();
				this.mc.displayGuiScreen(parent);
			} else if (button.id == 1) {
				sendDescription();
				this.mc.displayGuiScreen(new GuiPackStructures(this));
			} else if (button.id == 2) {
				sendDescription();
				this.mc.displayGuiScreen(new GuiPackLootLists(this));
			} else if (button.id == 3) {
				sendDescription();
				this.mc.displayGuiScreen(new GuiConfirm(this, "Do you really want to sign the pack <"+StructurePackInfo.active.name+">?", "A signed pack can no longer be edited.", "Cancel", "Confirm", this::confirmCallback));
			} else if (button.id == 4) {
				sendDescription();
				this.mc.displayGuiScreen(new GuiPackConditions(this));
			}
		}
	}
	
	private void sendDescription() {
		if (!StructurePackInfo.active.description.equals(nameField.getText()) && CDefaultPackage.packGuiAction(6, nameField.getText(), "")) {
			StructurePackInfo.active.description = nameField.getText();
		}
	}
	
	public boolean confirmCallback(int i) {
		if (i==1) {
			if (CDefaultPackage.packGuiAction(7, "", "")) {
				this.signButton.enabled = false;
				this.nameField.setEnabled(false);
				StructurePackInfo.active.read_only = true;
				StructurePackInfo.active.author = Minecraft.getMinecraft().player.getName();
			}
		}
		return true;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.actionPerformed(this.doneButton);
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

		this.drawCenteredString(this.fontRenderer, "Edit Structure Pack <"+StructurePackInfo.active.name+">", this.width / 2, 10, 16777215);
		this.drawString(this.fontRenderer, "Description", this.width / 2 - 152, 38, 16777215);
		this.nameField.drawTextBox();
		if (StructurePackInfo.active.read_only) this.drawString(this.fontRenderer,"This pack is read-only.", this.width / 2 - 154, 90, 16777215);
		this.drawString(this.fontRenderer,"Created by "+(StructurePackInfo.active.author.isEmpty()?"Unknown":StructurePackInfo.active.author), this.width / 2 - 154, 110, 16777215);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
