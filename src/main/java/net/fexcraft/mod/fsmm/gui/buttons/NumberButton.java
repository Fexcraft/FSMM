package net.fexcraft.mod.fsmm.gui.buttons;

import net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class NumberButton extends GuiButton {

	private int number;
	
	public NumberButton(int id, int x, int y, int number){
		super(id, x, y, number > 9 ? number == 12 ? 33 : 49 : 15, 15, "");
		this.number = number;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
		if(!this.visible){ return; }
		mc.getTextureManager().bindTexture(AutomatedTellerMashineGui.TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		this.drawTexturedModalRect(this.x, this.y, getX(enabled, hovered), getY(enabled, hovered), this.width, this.height);
	}

	private int getX(boolean enabled, boolean hovered){
		switch(number){
			case  0: return enabled ? hovered ? 139 : 45 : hovered ? 139 : 45;
			case  1: return enabled ? hovered ?  94 :  0 : hovered ?  94 :  0;
			case  2: return enabled ? hovered ? 109 : 15 : hovered ? 109 : 15;
			case  3: return enabled ? hovered ? 124 : 30 : hovered ? 124 : 30;
			case  4: return enabled ? hovered ?  94 :  0 : hovered ?  94 :  0;
			case  5: return enabled ? hovered ? 109 : 15 : hovered ? 109 : 15;
			case  6: return enabled ? hovered ? 124 : 30 : hovered ? 124 : 30;
			case  7: return enabled ? hovered ?  94 :  0 : hovered ?  94 :  0;
			case  8: return enabled ? hovered ? 109 : 15 : hovered ? 109 : 15;
			case  9: return enabled ? hovered ? 124 : 30 : hovered ? 124 : 30;
			case 10: return enabled ? hovered ? 139 : 45 : hovered ? 139 : 45;
			case 11: return enabled ? hovered ? 139 : 45 : hovered ? 139 : 45;
			case 12: return enabled ? hovered ? 156 : 62 : hovered ? 156 : 62;
		}
		return 0;
	}

	private int getY(boolean enabled, boolean hovered){
		switch(number){
			case  0: return enabled ? hovered ? 196 : 241 : hovered ? 241 : 196;
			case  1: return enabled ? hovered ? 166 : 211 : hovered ? 211 : 166;
			case  2: return enabled ? hovered ? 166 : 211 : hovered ? 211 : 166;
			case  3: return enabled ? hovered ? 166 : 211 : hovered ? 211 : 166;
			case  4: return enabled ? hovered ? 181 : 226 : hovered ? 226 : 181;
			case  5: return enabled ? hovered ? 181 : 226 : hovered ? 226 : 181;
			case  6: return enabled ? hovered ? 181 : 226 : hovered ? 226 : 181;
			case  7: return enabled ? hovered ? 196 : 241 : hovered ? 241 : 196;
			case  8: return enabled ? hovered ? 196 : 241 : hovered ? 241 : 196;
			case  9: return enabled ? hovered ? 196 : 241 : hovered ? 241 : 196;
			case 10: return enabled ? hovered ? 166 : 211 : hovered ? 211 : 166;
			case 11: return enabled ? hovered ? 181 : 226 : hovered ? 226 : 181;
			case 12: return enabled ? hovered ? 196 : 241 : hovered ? 241 : 196;
		}
		return 0;
	}
	
}