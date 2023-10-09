package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Transfer;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMViewTransfers extends GenericGui<ATMContainer> {

	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/view_transfers.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private List<AccountPermission> accounts;
	private BasicButton up, dw;
	private BasicText acc0, acc1;
	private BasicText[] trn_fr = new BasicText[5];
	private BasicText[] trn_am = new BasicText[5];
	private BasicText[] trn_tp = new BasicText[5];
	private BasicButton[] trn_bt = new BasicButton[5];
	private int scroll;

	public ATMViewTransfers(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.xSize = 256;
		this.ySize = 198;
	}

	@Override
	protected void init(){
		this.texts.put("acc0", acc0 = new BasicText(guiLeft + 6, guiTop + 6, 244, null, "Synchronizing....").autoscale());
		this.texts.put("acc1", acc1 = new BasicText(guiLeft + 6, guiTop + 16, 244, null, "Please wait.").autoscale());
		for(int i = 0; i < 5; i++){
			texts.put("trn_fr" + i, trn_fr[i] = new BasicText(guiLeft + 7, guiTop + 32 + (i * 32), 242, null, "").autoscale());
			texts.put("trn_am" + i, trn_am[i] = new BasicText(guiLeft + 7, guiTop + 42 + (i * 32), 242, null, "").autoscale());
			texts.put("trn_tp" + i, trn_tp[i] = new BasicText(guiLeft + 7, guiTop + 52 + (i * 32), 242, null, "").autoscale());
			buttons.put("trn_bt" + i, trn_bt[i] = new BasicButton("bt" + i, guiLeft + 5, guiTop + 31 + (i * 32), 5, 31, 246, 30, true));
		}
		this.buttons.put("up", up = new BasicButton("up", guiLeft + 228, guiTop + 190, 228, 190, 7, 7, true));
		this.buttons.put("dw", dw = new BasicButton("dw", guiLeft + 237, guiTop + 190, 237, 190, 7, 7, true));
		this.container.sync("account_transfers");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.account != null){
			acc0.string = container.account.getName();
			acc1.string = container.account.getType() + ":" + container.account.getId();
			Transfer transfer = null;
			for(int i = 0; i < 5; i++){
				int j = i + scroll;
				if(j >= container.account.getTransfers().size()){
					trn_bt[i].enabled = false;
					trn_fr[i].string = "";
					trn_am[i].string = "";
					trn_tp[i].string = "";
				}
				else{
					transfer = container.account.getTransfers().get(j);
					trn_bt[i].enabled = true;
					trn_fr[i].string = transfer.name;
					trn_am[i].string = Formatter.format((transfer.negative ? "&c" : "&e") + (transfer.negative ? "-" : "")+ Config.getWorthAsString(transfer.amount));
					trn_tp[i].string = transfer.action.name();
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
		if(container.account != null){
			for(int i = 0; i < 5; i++){
				if(!trn_bt[i].hovered(mouseX, mouseY)) continue;
				int j = i + scroll;
				if(j >= container.account.getTransfers().size()) continue;
				Transfer transfer = container.account.getTransfers().get(j);
				tooltip.add(Formatter.format("&9Transfer Info"));
				tooltip.add(Formatter.format("&7Time: &a" + Time.getAsString(transfer.time)));
				tooltip.add(Formatter.format("&7Account Name: &b" + transfer.name));
				tooltip.add(Formatter.format("&7Account Type: &b" + transfer.type));
				tooltip.add(Formatter.format("&7Account ID: &b" + transfer.from));
				tooltip.add(Formatter.format("&7Transfer Type: &b" + transfer.action.name().toLowerCase()));
				tooltip.add(Formatter.format("&7Fees Included: " + (transfer.included ? "&aYes" : "&cNo")));
				tooltip.add(Formatter.format("&7Amount: &e" + (transfer.negative ? "-" : "") + Config.getWorthAsString(transfer.amount)));
				tooltip.add(Formatter.format("&7Fee: &e" + Config.getWorthAsString(transfer.fee)));
				long am = transfer.amount + (transfer.included ? 0 : transfer.fee);
				tooltip.add(Formatter.format("&7Total: &e" + (transfer.negative ? "-" : "") + Config.getWorthAsString(am)));
			}
		}
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
    public void keyTyped(char cher, int key) throws IOException{
        if(key == 1) openGui(GuiHandler.ATM_MAIN, new int[]{ 0, 0, 0 }, LISTENERID);
        else super.keyTyped(cher, key);
    }

}
