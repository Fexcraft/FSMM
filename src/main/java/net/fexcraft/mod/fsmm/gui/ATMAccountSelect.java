package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMAccountSelect extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/account_select.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton up, dw, type, uid, search;
	private BasicText acc0, acc1;
	private BasicText[] accs = new BasicText[8];
	private BasicButton[] acc = new BasicButton[4];
	private int scroll, mode;

	public ATMAccountSelect(EntityPlayer player, int mode){
		super(texture, new ATMContainer(player), player);
		this.mode = mode;
		this.xSize = 256;
		this.ySize = 152;
	}

	@Override
	protected void init(){
		this.texts.put("acc0", acc0 = new BasicText(guiLeft + 6, guiTop + 6, 244, null, "Synchronizing...."));
		this.texts.put("acc1", acc1 = new BasicText(guiLeft + 6, guiTop + 16, 244, null, "Please wait.").autoscale());
		for(int i = 0; i < 4; i++){
			int j = i * 2;
			this.texts.put("accs" + j, accs[j] = new BasicText(guiLeft + 6, guiTop + 58 + (i * 22), 244, null, "- - -"));
			this.texts.put("accs" + (j + 1), accs[j + 1] = new BasicText(guiLeft + 6, guiTop + 68 + (i * 22), 244, null, "- - -"));
			this.buttons.put("acc" + j, acc[i] = new BasicButton("acc" + i, guiLeft + 5, guiTop + 57 + (i * 22), 5, 57, 246, 20, true));
		}
		this.buttons.put("type", type = new BasicButton("type", guiLeft + 242, guiTop + 32, 242, 32, 8, 8, true));
		this.buttons.put("uid", uid = new BasicButton("uid", guiLeft + 242, guiTop + 42, 242, 42, 8, 8, true));
		this.buttons.put("search", search = new BasicButton("search", guiLeft + 233, guiTop + 42, 233, 42, 8, 8, true));
		this.buttons.put("up", up = new BasicButton("up", guiLeft + 228, guiTop + 144, 228, 144, 7, 7, true));
		this.buttons.put("dw", dw = new BasicButton("dw", guiLeft + 237, guiTop + 144, 237, 144, 7, 7, true));
		this.container.sync("account", "account_list" + (mode == 0 ? "_own" : ""));
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.account != null){
			acc0.string = container.account.getName();
			acc1.string = container.account.getType() + ":" + container.account.getId();
		}
		if(container.accounts != null){
			//
		}
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		//
		if(container.accounts != null){
			for(int i = 0; i < 4; i++){
				//
			}
		}
		if(type.hovered){
			tooltip.add(Formatter.format("&7Type of &9Account &7to be &6searched&7."));
			tooltip.add(Formatter.format("&7(enter the full type name)."));
		}
		if(uid.hovered){
			tooltip.add(Formatter.format("&7ID of &9Account &7to be &6searched&7."));
			tooltip.add(Formatter.format("&7(you can just write bits of the name/id)"));
		}
		if(search.hovered) tooltip.add(Formatter.format("&7Search"));
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
        	if(mode == 0) openGui(GuiHandler.ATM_MAIN, new int[]{ 0, 0, 0 }, LISTENERID);
        	if(mode == 1){
        		//
        	}
            return;
        }
        else super.keyTyped(typedChar, keyCode);
    }

}
