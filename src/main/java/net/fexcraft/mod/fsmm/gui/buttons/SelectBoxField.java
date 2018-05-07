package net.fexcraft.mod.fsmm.gui.buttons;

import org.lwjgl.opengl.GL11;

import net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class SelectBoxField extends GuiButton {
	
	public SelectBoxField(int id, int x, int y){
		super(id, x, y + ((id - 21) * 12), 116, 10, "loading...");
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
		if(!this.visible){ return; }
		mc.getTextureManager().bindTexture(AutomatedTellerMashineGui.SELECT_TEX);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		this.drawTexturedModalRect(this.x, this.y, 25, this.hovered ? 13 : 25, this.width, this.height);
        if(mc.fontRenderer.getStringWidth(displayString) > 110){
        	GL11.glScaled(0.5, 0.5, 0.5);
        	String[] str = displayString.split(" ");
        	mc.fontRenderer.drawString(str[0], (x + 3) * 2, (y + 1) * 2, MapColor.CYAN.colorValue);
        	if(str.length > 1){
            	mc.fontRenderer.drawString(str[1], (x + 3) * 2, (y + 5) * 2, MapColor.CYAN.colorValue);
        	}
        	GL11.glScaled(2.0, 2.0, 2.0);
        }
        else{
        	mc.fontRenderer.drawString(displayString, x + 3, y + 1, MapColor.SNOW.colorValue);
        }
	}
	
}