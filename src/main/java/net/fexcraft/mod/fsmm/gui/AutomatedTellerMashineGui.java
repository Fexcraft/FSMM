package net.fexcraft.mod.fsmm.gui;

import java.io.IOException;

import net.fexcraft.mod.fsmm.gui.buttons.NumberButton;
import net.fexcraft.mod.fsmm.gui.buttons.SideButton;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutomatedTellerMashineGui extends GuiScreen {
	
	public static final ResourceLocation TEXTURE = new ResourceLocation("fsmm:textures/gui/atm_main.png");
	//
	private int xhalf, yhalf;
	private EntityPlayer player;
	private World world;
	private BlockPos tile;
	//
	private String input, pswd, window;
	private boolean hiddeninput;
	//
	private SideButton[] sidebuttons = new SideButton[8];
	private NumberButton[] numberbuttons = new NumberButton[13];
	
	public AutomatedTellerMashineGui(EntityPlayer player, World world, int x, int y, int z){
		this.player = player;
		this.world = world;
		this.tile = new BlockPos(x, y, z);
		window = "main";
	}
	
	@Override
	public void initGui(){
		xhalf = (this.width - 176) / 2;
		yhalf = (this.height - 166) / 2;
		//TODO
		this.buttonList.clear();
		for(int i = 0; i < sidebuttons.length; i++){
			boolean left = i % 2 == 0; int j = left ? i / 2 : (i - 1) / 2;
			buttonList.add(sidebuttons[i] = new SideButton(i, xhalf + (left ? 5 : 161), yhalf + (12 + (j * 23)), left));
		}
		buttonList.add(numberbuttons[ 0] = new NumberButton( 8, xhalf + 57, yhalf + 145,  0));
		buttonList.add(numberbuttons[ 1] = new NumberButton( 9, xhalf +  6, yhalf + 111,  1));
		buttonList.add(numberbuttons[ 2] = new NumberButton(10, xhalf + 23, yhalf + 111,  2));
		buttonList.add(numberbuttons[ 3] = new NumberButton(12, xhalf + 40, yhalf + 111,  3));
		buttonList.add(numberbuttons[ 4] = new NumberButton(13, xhalf +  6, yhalf + 128,  4));
		buttonList.add(numberbuttons[ 5] = new NumberButton(14, xhalf + 23, yhalf + 128,  5));
		buttonList.add(numberbuttons[ 6] = new NumberButton(15, xhalf + 40, yhalf + 128,  6));
		buttonList.add(numberbuttons[ 7] = new NumberButton(16, xhalf +  6, yhalf + 145,  7));
		buttonList.add(numberbuttons[ 8] = new NumberButton(17, xhalf + 23, yhalf + 145,  8));
		buttonList.add(numberbuttons[ 9] = new NumberButton(18, xhalf + 40, yhalf + 145,  9));
		buttonList.add(numberbuttons[10] = new NumberButton(19, xhalf + 57, yhalf + 111, 10));
		buttonList.add(numberbuttons[11] = new NumberButton(20, xhalf + 57, yhalf + 128, 11));
		buttonList.add(numberbuttons[12] = new NumberButton(21, xhalf + 74, yhalf + 145, 12));
	}
	
	@Override
    public void drawScreen(int mx, int my, float pt){		
		this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(xhalf, yhalf, 0, 0, 176, 166);
        this.buttonList.forEach(button -> button.drawButton(mc, mx, my, pt));
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
	
	@Override
    protected void actionPerformed(GuiButton button){
		Print.debug(window, button.id);
		switch(window){
			default: return;
		}
	}
	
	public void openPerspective(String window){
		this.window = window;
		input = "";
		hiddeninput = window.equals("password");
	}
	
}