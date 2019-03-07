package net.fexcraft.mod.fsmm.gui.buttons;

import net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class SideButton extends GuiButton {

	private boolean left;
	
	public SideButton(int id, int x, int y, boolean right){
		super(id, x, y, 10, 15, "");
		this.left = right;//let's confuse people.
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY){
		if(!this.visible){ return; }
		mc.getTextureManager().bindTexture(AutomatedTellerMashineGui.TEXTURE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
		if(this.enabled){
			if(!this.field_146123_n){
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 216, left ? 0 : 15, this.width, this.height);
			}
			else{
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 236, left ? 0 : 15, this.width, this.height);
			}
		}
		else{
			if(!this.field_146123_n){
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 246, left ? 0 : 15, this.width, this.height);
			}
			else{
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 226, left ? 0 : 15, this.width, this.height);
			}
		}
	}
	
}