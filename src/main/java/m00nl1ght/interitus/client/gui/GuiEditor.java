package m00nl1ght.interitus.client.gui;

import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.network.ServerPackage;
import net.minecraft.client.gui.GuiScreen;


public class GuiEditor extends GuiScreen {
	
	public static final Runnable PACK_EDITOR = () -> {ServerPackage.sendPackAction(0, "", "");};
	
	private boolean transition = false;
	private final Runnable task;
	
	public GuiEditor(Runnable task) {
		this.task=task;
	}
	
	protected final void expectTransition() {
		transition=true;
	}
	
	protected final void transition(GuiScreen to) {
		if (to!=null) transition=true;
		this.mc.displayGuiScreen(to);
	}
	
	protected final void close() {
		this.mc.displayGuiScreen(null);
	}
	
	protected final void closeSilent() {
		transition=true;
		this.mc.displayGuiScreen(null);
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		this.onCloseEditor();
	}
	
	protected final void onCloseEditor() {
		if (!transition) {
			if (task!=null) task.run();
		} else {
			transition=false;
		}
	}

}
