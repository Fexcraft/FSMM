package net.fexcraft.mod.fsmm.gui.buttons;

import net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class SideButton extends GuiButton {

	private boolean left;
	
	public SideButton(int id, int x, int y, boolean right){
		super(id, x, y, 10, 15, "");
		this.left = right;//let's confuse people.
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
		if(!this.visible){ return; }
		mc.getTextureManager().bindTexture(AutomatedTellerMashineGui.TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		if(this.enabled){
			if(!this.hovered){
				this.drawTexturedModalRect(this.x, this.y, 216, left ? 0 : 15, this.width, this.height);
			}
			else{
				this.drawTexturedModalRect(this.x, this.y, 236, left ? 0 : 15, this.width, this.height);
			}
		}
		else{
			if(!this.hovered){
				this.drawTexturedModalRect(this.x, this.y, 246, left ? 0 : 15, this.width, this.height);
			}
			else{
				this.drawTexturedModalRect(this.x, this.y, 226, left ? 0 : 15, this.width, this.height);
			}
		}
	}
	
}