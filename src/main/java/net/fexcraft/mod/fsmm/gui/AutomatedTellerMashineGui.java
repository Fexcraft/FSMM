package net.fexcraft.mod.fsmm.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutomatedTellerMashineGui extends GuiScreen {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/atm_main.png");
	//
	private int xhalf, yhalf;
	private EntityPlayer player;
	private World world;
	private BlockPos tile;
	//
	private String input, window;
	private boolean hiddeninput;
	
	public AutomatedTellerMashineGui(EntityPlayer player, World world, int x, int y, int z){
		this.player = player;
		this.world = world;
		this.tile = new BlockPos(x, y, z);
	}
	
	@Override
	public void initGui(){
		xhalf = (this.width - 176) / 2;
		yhalf = (this.height - 166) / 2;
		//TODO
		this.buttonList.clear();
	}
	
	@Override
    public void drawScreen(int mx, int my, float f){		
		this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(xhalf, yhalf, 0, 0, 176, 166);
        //
        
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return true;
	}
	
	@Override
    public void keyTyped(char typedChar, int keyCode) throws IOException{
		//TODO
        if(keyCode == 1){
            this.mc.displayGuiScreen((GuiScreen)null);
            if(this.mc.currentScreen == null){
                this.mc.setIngameFocus();
            }
        }
    }
	
	public void openPerspective(String window){
		this.window = window;
		input = "";
		hiddeninput = window.equals("password");
	}
	
}