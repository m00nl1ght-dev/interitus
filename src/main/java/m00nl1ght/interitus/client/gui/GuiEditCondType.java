package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.client.ConditionTypeClient;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;

public class GuiEditCondType extends GuiScreen {
	
	private final GuiScreen parent;
	private ConditionTypeClient type;
	private GuiButton doneButton, cancelButton;
	private GuiTextField nameField;
	private final boolean newtype;
	private String warning = "";

	public GuiEditCondType(GuiScreen parent, String type, NBTTagCompound tag) {
		this.parent = parent;
		if (type.isEmpty() || tag==null) { // -> new cond type
			this.type = new ConditionTypeClient.ConditionMaterialSetClient();
			this.newtype = true;
		} else {
			this.type = ConditionTypeClient.build(type, tag);
			this.newtype = false;
		}
	}
	
	@Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
        this.type.updateGui();
    }
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.type.init(this.width / 2 - 154, 80, fontRenderer);
		this.buttonList.clear();
		this.doneButton = this.addButton(new GuiButton(0, this.width / 2 + 4, 305, 150, 20, "Done"));
		this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 - 4 - 150, 305, 150, 20, "Cancel"));
		this.nameField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 154, 50, 300, 20);
        this.nameField.setMaxStringLength(20);
        this.nameField.setEnabled(newtype);
        this.nameField.setText(type.getName());
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		GuiDropdown.close();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled && !GuiDropdown.isOpen()) {
			if (button.id == 0) {
				String name = this.nameField.getText();
				if (newtype) {
					if (name.isEmpty()) {
						warning = "Please enter a name!";
						return;
					}
					for (String str : StructurePackInfo.condtypes) {
						if (str.equals(name)) {
							warning = "Name already used! Choose a different one.";
							return;
						}
					}
					StructurePackInfo.condtypes.add(name);
				}
				CDefaultPackage.updateCondType(ConditionTypeClient.save(type), name);
				this.mc.displayGuiScreen(parent);
			} else if (button.id == 1) {
				this.mc.displayGuiScreen(parent);
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.actionPerformed(this.cancelButton);
			return;
		}
		if (this.nameField.getVisible()) {
			this.nameField.textboxKeyTyped(typedChar, keyCode);
		}
		this.type.keyTyped(typedChar, keyCode);
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
		boolean mBtn = Mouse.isButtonDown(0);

		this.drawCenteredString(this.fontRenderer, newtype?"Create new Condition Type":("Edit Condition Type <"+type.getName()+">"), this.width / 2, 10, 16777215);
		this.drawString(this.fontRenderer, warning.isEmpty()?"Name":warning, this.width / 2 - 152, 38, 16777215);
		this.nameField.drawTextBox();
		
		this.type = this.type.drawGui(mouseX, mouseY, mBtn && !GuiDropdown.mState && !GuiDropdown.isOpen(), this.fontRenderer);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		GuiDropdown.drawDropdown(mouseX, mouseY, mBtn);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
