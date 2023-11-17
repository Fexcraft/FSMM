package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.util.ArrayList;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ATMMain extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/main.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton bi, vt, ca, wd, dp, tr;
	private BasicText b0, b1, b2, b3, ac, ba;

	public ATMMain(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.xSize = 256;
		this.ySize = 88;
	}

	@Override
	protected void init(){
		this.texts.put("b0", b0 = new BasicText(guiLeft + 6, guiTop +  6, 244, null, "Synchronizing...."));
		this.texts.put("b1", b1 = new BasicText(guiLeft + 6, guiTop + 18, 244, null, "Please wait."));
		this.texts.put("b2", b2 = new BasicText(guiLeft + 6, guiTop + 30, 244, null, "- - -"));
		this.texts.put("b3", b3 = new BasicText(guiLeft + 6, guiTop + 42, 235, null, "- - -"));
		this.texts.put("ac", ac = new BasicText(guiLeft + 6, guiTop + 58, 235, null, "- - -"));
		this.texts.put("ba", ba = new BasicText(guiLeft + 6, guiTop + 70, 211, null, "0" + Config.CURRENCY_SIGN));
		this.buttons.put("bi", bi = new BasicButton("bi", guiLeft + 242, guiTop + 42, 242, 42, 8, 8, true));
		this.buttons.put("vt", vt = new BasicButton("vt", guiLeft + 231, guiTop + 58, 231, 58, 8, 8, true));
		this.buttons.put("ca", ca = new BasicButton("ca", guiLeft + 242, guiTop + 58, 242, 58, 8, 8, true));
		this.buttons.put("wd", wd = new BasicButton("wd", guiLeft + 219, guiTop + 69, 219, 69, 10, 10, true));
		this.buttons.put("dp", dp = new BasicButton("dp", guiLeft + 230, guiTop + 69, 230, 69, 10, 10, true));
		this.buttons.put("tr", tr = new BasicButton("tr", guiLeft + 241, guiTop + 69, 241, 69, 10, 10, true));
		this.container.sync("account", "bank");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.bank != null){
			b0.string = container.bank.getName();
			b1.string = container.bank.getStatus().size() > 0 ? Formatter.format(container.bank.getStatus().get(0)) : "";
			b2.string = container.bank.getStatus().size() > 1 ? Formatter.format(container.bank.getStatus().get(1)) : "";
			b3.string = container.bank.getStatus().size() > 2 ? Formatter.format(container.bank.getStatus().get(2)) : "";
		}
		if(container.account != null){
			ac.string = container.account.getName();
			ba.string = Config.getWorthAsString(container.account.getBalance());
		}
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(bi.hovered) tooltip.add(Formatter.format("&7Bank Info &8/ &9 Change Bank"));
		if(vt.hovered) tooltip.add(Formatter.format("&bView Account Activity"));
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
				openGui(GuiHandler.BANK_SELECT, new int[]{ 0, 0, 0 }, LISTENERID);
				return true;
			}
			case "vt":{
				openGui(GuiHandler.VIEW_TRANSFERS, new int[]{ 0, 0, 0 }, LISTENERID);
				return true;
			}
			case "ca":{
				openGui(GuiHandler.ACCOUNT_SELECT, new int[]{ 0, 0, 0 }, LISTENERID);
				return true;
			}
			case "wd":{
				openGui(GuiHandler.ACCOUNT_WITHDRAW, new int[]{ 0, 0, 0 }, LISTENERID);
				return true;
			}
			case "dp":{
				openGui(GuiHandler.ACCOUNT_DEPOSIT, new int[]{ 0, 0, 0 }, LISTENERID);
				return true;
			}
			case "tr":{
				openGui(GuiHandler.ACCOUNT_TRANSFER, new int[]{ 0, 0, 0 }, LISTENERID);
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
