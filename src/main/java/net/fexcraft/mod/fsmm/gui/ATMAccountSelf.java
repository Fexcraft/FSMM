package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMAccountSelf extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/account_self.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton action, cancel, exit, expand;
	private BasicButton[] numbers = new BasicButton[10];
	private BasicText acc0, acc1, bal, fee, tot;
	private boolean expanded, mode;

	public ATMAccountSelf(EntityPlayer player, boolean bool){
		super(texture, new ATMContainer(player), player);
		this.deftexrect = false;
		this.mode = bool;
		this.xSize = 256;
		this.ySize = 147;
	}

	@Override
	protected void init(){
		this.buttons.put("expand", expand = new BasicButton("expand", guiLeft + 191, guiTop + 79, 191, 148, 51, 8, true));
		this.container.sync("account", "bank");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		expand.visible = !expanded;
		if(container.bank != null){
			//show fee
		}
		if(container.account != null){
			//show balance
		}
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, 86);
		if(expanded){
			this.drawTexturedModalRect(guiLeft + 184, guiTop + 79, 184, 79, 64, 68);
		}
		else{
			this.drawTexturedModalRect(guiLeft + 184, guiTop + 79, 184, 148, 64, 12);
		}
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(expand.visible && expand.hovered) tooltip.add(Formatter.format("&7Click to switch into NumberField input mode."));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		switch(button.name){
			case "expand": return expanded = true;
			case "exit": return !(expanded = false);
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
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
