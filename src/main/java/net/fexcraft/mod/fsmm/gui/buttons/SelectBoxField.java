package net.fexcraft.mod.fsmm.gui.buttons;

import org.lwjgl.opengl.GL11;

import net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class SelectBoxField extends GuiButton {
	
	public SelectBoxField(int id, int x, int y){
		super(id, x, y + ((id - 21) * 12), 116, 10, "loading...");
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY){
		if(!this.visible){ return; }
		mc.getTextureManager().bindTexture(AutomatedTellerMashineGui.SELECT_TEX);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
		this.drawTexturedModalRect(this.xPosition, this.yPosition, 25, this.field_146123_n ? 13 : 25, this.width, this.height);
        if(mc.fontRenderer.getStringWidth(displayString) > 110){
        	GL11.glScaled(0.5, 0.5, 0.5);
        	String[] str = displayString.split(" ");
        	mc.fontRenderer.drawString(str[0], (xPosition + 3) * 2, (yPosition + 1) * 2, MapColor.snowColor.colorValue);
        	if(str.length > 1){
            	mc.fontRenderer.drawString(str[1], (xPosition + 3) * 2, (yPosition + 5) * 2, MapColor.snowColor.colorValue);
        	}
        	GL11.glScaled(2.0, 2.0, 2.0);
        }
        else{
        	mc.fontRenderer.drawString(displayString, xPosition + 3, yPosition + 1, MapColor.snowColor.colorValue);
        }
	}
	
}