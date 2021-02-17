package net.fexcraft.mod.fsmm.gui;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMMain extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/main.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton bi, ca, wd, dp, tr;

	public ATMMain(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.xSize = 256;
		this.ySize = 88;
	}

	@Override
	protected void init(){
		this.texts.put("b0", new BasicText(guiLeft + 6, guiTop +  6, 244, null, "BANK NAME HERE"));
		this.texts.put("b1", new BasicText(guiLeft + 6, guiTop + 18, 244, null, "BANK INFO HERE"));
		this.texts.put("b2", new BasicText(guiLeft + 6, guiTop + 30, 244, null, "BANK INFO HERE"));
		this.texts.put("b3", new BasicText(guiLeft + 6, guiTop + 42, 235, null, "BANK INFO HERE"));
		this.texts.put("ac", new BasicText(guiLeft + 6, guiTop + 58, 235, null, "SEL. ACCOUNT HERE"));
		this.texts.put("ba", new BasicText(guiLeft + 6, guiTop + 70, 211, null, "BALANCE"));
		this.buttons.put("bi", bi = new BasicButton("bi", guiLeft + 242, guiTop + 42, 242, 42, 8, 8, true));
		this.buttons.put("ca", ca = new BasicButton("ca", guiLeft + 242, guiTop + 58, 242, 58, 8, 8, true));
		this.buttons.put("wd", wd = new BasicButton("wd", guiLeft + 219, guiTop + 69, 219, 69, 10, 10, true));
		this.buttons.put("dp", dp = new BasicButton("dp", guiLeft + 230, guiTop + 69, 230, 69, 10, 10, true));
		this.buttons.put("tr", tr = new BasicButton("tr", guiLeft + 241, guiTop + 69, 241, 69, 10, 10, true));
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		//
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(bi.hovered) tooltip.add(Formatter.format("&7Bank Info &8/ &9 Change Bank"));
		if(ca.hovered) tooltip.add(Formatter.format("&aChange &7managed &9Account"));
		if(wd.hovered) tooltip.add(Formatter.format("&eWidthdraw &7from &9Account"));
		if(dp.hovered) tooltip.add(Formatter.format("&6Deposit &7to &9Account"));
		if(tr.hovered) tooltip.add(Formatter.format("&bTransfer &7to another &9Account"));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		switch(button.name){
			case "bi":{
				
				return true;
			}
			case "ca":{
				
				return true;
			}
			case "wd":{
				
				return true;
			}
			case "dp":{
				
				return true;
			}
			case "tr":{
				
				return true;
			}
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}

}
