package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class ATMBankSelect extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/bank_select.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton up, dw, info;
	private BasicButton[] bi = new BasicButton[8], bs = new BasicButton[8];
	private BasicText bank;
	private BasicText[] banks = new BasicText[8];
	private int scroll;

	public ATMBankSelect(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.xSize = 256;
		this.ySize = 124;
	}

	@Override
	protected void init(){
		this.texts.put("bank", bank = new BasicText(guiLeft + 6, guiTop +  6, 235, null, "Synchronizing...."));
		this.buttons.put("info", info = new BasicButton("info", guiLeft + 242, guiTop + 6, 1, 247, 8, 8, true));
		for(int i = 0; i < 8; i++){
			this.texts.put("b" + i, banks[i] = new BasicText(guiLeft + 6, guiTop + 22 + (i * 12), 226, null, "- - -"));
			this.buttons.put("bi" + i, bi[i] = new BasicButton("bi" + i, guiLeft + 233, guiTop + 22 + (i * 12), 1, 247, 8, 8, true));
			this.buttons.put("bs" + i, bs[i] = new BasicButton("bs" + i, guiLeft + 242, guiTop + 22 + (i * 12), 10, 247, 8, 8, true));
		}
		this.buttons.put("up", up = new BasicButton("up", guiLeft + 228, guiTop + 116, 228, 116, 7, 7, true));
		this.buttons.put("dw", dw = new BasicButton("dw", guiLeft + 237, guiTop + 116, 237, 116, 7, 7, true));
		this.container.sync("bank", "bank_list");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.bank != null){
			bank.string = container.bank.getName();
		}
		if(container.banks != null){
			for(int i = 0; i < 8; i++){
				int j = i + scroll;
				if(j >= container.banks.size()){
					bi[i].visible = bs[i].visible = false;
					banks[i].string = "";
				}
				else{
					bi[i].visible = bs[i].visible = true;
					banks[i].string = container.banks.get(j).getValue();
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
		if(info.hovered){
			tooltip.add(Formatter.format("&7View info of &9your &7Bank."));
			tooltip.add(Formatter.format("&9Bank ID: &7" + container.bank.id));
		}
		if(container.banks != null){
			for(int i = 0; i < 8; i++){
				if(i + scroll >= container.banks.size()) break;
				if(bi[i].hovered){
					tooltip.add(Formatter.format("&7View info of &6this &7Bank."));
					tooltip.add(Formatter.format("&9Bank ID: &7" + container.banks.get(i + scroll).getKey()));
				}
				if(bs[i].hovered){
					tooltip.add(Formatter.format("&cMove &baccount &cto this Bank."));
					tooltip.add(Formatter.format("&7(Check fees first before moving bank!)"));
				}
			}
		}
		if(up.hovered) tooltip.add(Formatter.format("&7Scroll Up"));
		if(dw.hovered) tooltip.add(Formatter.format("&7Scroll Down"));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		if(button.name.startsWith("bi")){
			int i = Integer.parseInt(button.name.substring(2));
			if(i < 0 || i >= 8) return false;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("cargo", "bank_info");
			compound.setString("bank", container.banks.get(i + scroll).getKey());
			container.send(Side.SERVER, compound);
			return true;
		}
		else if(button.name.startsWith("bs")){
			int i = Integer.parseInt(button.name.substring(2));
			if(i < 0 || i >= 8) return false;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("cargo", "bank_select");
			compound.setString("bank", container.banks.get(i + scroll).getKey());
			container.send(Side.SERVER, compound);
			return true;
		}
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
			case "info":{
				openGui(GuiHandler.BANK_INFO, new int[]{ 0, 0, 0 }, LISTENERID);
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
			openGui(GuiHandler.ATM_MAIN, new int[]{ 0, 0, 0 }, LISTENERID);
            return;
        }
        else super.keyTyped(typedChar, keyCode);
    }

}
