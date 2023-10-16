package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMBankInfo extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/bank_info.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton up, dw;
	private BasicText b0, b1, b2, b3;
	private BasicText[] fees = new BasicText[8];
	private String[] keys, vals;
	private int scroll;

	public ATMBankInfo(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.xSize = 256;
		this.ySize = 164;
	}

	@Override
	protected void init(){
		this.texts.put("b0", b0 = new BasicText(guiLeft + 6, guiTop +  6, 244, null, "Synchronizing...."));
		this.texts.put("b1", b1 = new BasicText(guiLeft + 6, guiTop + 22, 244, null, "Please wait."));
		this.texts.put("b2", b2 = new BasicText(guiLeft + 6, guiTop + 34, 244, null, "- - -"));
		this.texts.put("b3", b3 = new BasicText(guiLeft + 6, guiTop + 46, 244, null, "- - -"));
		for(int i = 0; i < 8; i++){
			this.texts.put("f" + i, fees[i] = new BasicText(guiLeft + 6, guiTop + 62 + (i * 12), 244, null, "- - -"));
		}
		this.buttons.put("up", up = new BasicButton("up", guiLeft + 228, guiTop + 156, 228, 156, 7, 7, true));
		this.buttons.put("dw", dw = new BasicButton("dw", guiLeft + 237, guiTop + 156, 237, 156, 7, 7, true));
		this.container.sync("bank");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.bank != null){
			b0.string = container.bank.getName();
			b1.string = container.bank.getStatus().size() > 0 ? Formatter.format(container.bank.getStatus().get(0)) : "";
			b2.string = container.bank.getStatus().size() > 1 ? Formatter.format(container.bank.getStatus().get(1)) : "";
			b3.string = container.bank.getStatus().size() > 2 ? Formatter.format(container.bank.getStatus().get(2)) : "";
			if(keys == null){
				if(container.bank.getFees() == null){
					keys = vals = new String[0];
				}
				else {
					keys = container.bank.getFees().keySet().toArray(new String[0]);
					vals = container.bank.getFees().values().toArray(new String[0]);
				}
			}
			if(keys.length > 0){
				for(int i = 0; i < 8; i++){
					int j = i + scroll;
					if(j >= keys.length) fees[i].string = "";
					else{
						String val = vals[j].contains("%") ? vals[j] : Config.getWorthAsString(Long.parseLong(vals[j]));
						fees[i].string = keys[j].replace(":", " -> ") + " = " + val;
					}
				}
			}
		}
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(up.hovered) tooltip.add(Formatter.format("&7Scroll Up"));
		if(dw.hovered) tooltip.add(Formatter.format("&7Scroll Down"));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		switch(button.name){
			case "up":{
				scroll--;
				if(scroll < 0) scroll = 0;
				return true;
			}
			case "dw":{
				scroll++;
				return true;
			}
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		scroll += am > 0 ? 1 : -1;
		if(scroll < 0) scroll = 0;
	}

	@Override
    public void keyTyped(char typedChar, int keyCode) throws IOException{
        if(keyCode == 1){
			openGui(GuiHandler.BANK_SELECT, new int[]{ 0, 0, 0 }, LISTENERID);
            return;
        }
        else super.keyTyped(typedChar, keyCode);
    }

}
