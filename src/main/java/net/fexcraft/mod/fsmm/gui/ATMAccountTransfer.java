package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class ATMAccountTransfer extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/account_transfer.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton action, select, expand;
	private BasicButton[] numbers = new BasicButton[12];
	private BasicText acc0, acc1, rec0, rec1, bal, fee, tot, amount;
	private boolean expanded;
	private TextField amount_field;
	private String oldtext = "";
	private long bf, am;

	public ATMAccountTransfer(EntityPlayer player){
		super(texture, new ATMContainer(player), player);
		this.deftexrect = false;
		//this.defbackground = false;
		this.xSize = 256;
		this.ySize = 173;
	}

	@Override
	protected void init(){
		this.texts.put("acc0", acc0 = new BasicText(guiLeft + 6, guiTop + 6, 244, null, "Synchronizing....").autoscale());
		this.texts.put("acc1", acc1 = new BasicText(guiLeft + 6, guiTop + 16, 244, null, "Please wait.").autoscale());
		this.texts.put("rec0", rec0 = new BasicText(guiLeft + 6, guiTop + 32, 235, null, "").autoscale());
		this.texts.put("rec1", rec1 = new BasicText(guiLeft + 6, guiTop + 42, 244, null, "").autoscale());
		this.texts.put("balance", bal = new BasicText(guiLeft + 6, guiTop + 58, 244, null, "").autoscale());
		this.texts.put("amount", amount = new BasicText(guiLeft + 6, guiTop + 70, 244, MapColor.SNOW.colorValue, "").autoscale());
		this.fields.put("amount", amount_field = new TextField(0, fontRenderer, guiLeft + 6, guiTop + 70, 244, 8).setColor(MapColor.SNOW.colorValue).setEnableBackground(false));
		this.texts.put("fee", fee = new BasicText(guiLeft + 6, guiTop + 82, 244, null, "").autoscale());
		this.texts.put("total", tot = new BasicText(guiLeft + 6, guiTop + 94, 233, null, "").autoscale());
		this.buttons.put("confirm", action = new BasicButton("action", guiLeft + 241, guiTop + 93, 241, 93, 10, 10, true));
		this.buttons.put("select", select = new BasicButton("select", guiLeft + 242, guiTop + 32, 242, 32, 8, 8, true));
		this.buttons.put("expand", expand = new BasicButton("expand", guiLeft + 191, guiTop + 105, 191, 174, 51, 8, true));
		for(int i = 0; i < numbers.length; i++){
			int x = 192 + ((i % 3) * 17), y = 105 + ((i / 3) * 16);
			String id = i < 9 ? "n" + (i + 1) : i == 9 ? "cancel" : i == 10 ? "n0" : "exit";
			this.buttons.put(id, numbers[i] = new BasicButton(id, guiLeft + x, guiTop + y, x, y, 15, 15, true));
		}
		this.container.sync("account", "bank", "receiver");
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		amount_field.setVisible(expand.visible = !(amount.visible = expanded));
		for(BasicButton button : numbers) button.visible = expanded;
		if(!oldtext.equals(amount_field.getText())){
			oldtext = amount_field.getText();
			updateValues(true);
		}
		if(container.bank != null){
			fee.string = am == 0 || bf == 0 ? "-" : Config.getWorthAsString(bf) + "  (fee)"; 
		}
		String pref = "";
		if(container.account != null){
			acc0.string = container.account.getName();
			acc1.string = container.account.getType() + ":" + container.account.getId();
			bal.string = Config.getWorthAsString(container.account.getBalance()) + "  (balance)";
			if(am + bf > container.account.getBalance()) pref = "&c";
		}
		if(container.receiver != null){
			rec0.string = container.receiver.getName();
			rec1.string = container.receiver.getType() + ":" + container.receiver.getId();
		}
		else{
			rec0.string = "Please select a receiver.";
			rec1.string = "";
		}
		amount.string = Config.getWorthAsString(am, false);
		tot.string = Formatter.format(pref + Config.getWorthAsString(am + bf)) + "  (total)";
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, 112);
		if(expanded){
			this.drawTexturedModalRect(guiLeft + 184, guiTop + 105, 184, 105, 64, 68);
		}
		else{
			this.drawTexturedModalRect(guiLeft + 184, guiTop + 105, 184, 174, 64, 12);
		}
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(expand.visible && expand.hovered) tooltip.add(Formatter.format("&7Open number pad."));
		if(action.hovered) tooltip.add(Formatter.format("&9Confirm &bTransfer"));
		if(select.hovered) tooltip.add(Formatter.format("&7Open Account Selection"));
		if(expanded && numbers[9].hovered) tooltip.add(Formatter.format("&cCancel Input"));
		if(expanded && numbers[11].hovered) tooltip.add(Formatter.format("&7Close number pad."));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		if(button.name.startsWith("n")){
			int i = Integer.parseInt(button.name.substring(1));
			am *= 10;
			am += i;
			updateValues(false);
			return true;
		}
		switch(button.name){
			case "action":{
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("cargo", "action_transfer");
				compound.setLong("amount", am);
				container.send(Side.SERVER, compound);
				return true;
			}
			case "select":{
				openGui(GuiHandler.ACCOUNT_SELECT, new int[]{ 1, 0, 0 }, LISTENERID);
				return true;
			}
			case "cancel":{
				am = bf = 0;
				return true;
			}
			case "expand": return expand(true);
			case "exit": return expand(false);
		}
		return false;
	}

	private boolean expand(boolean bool){
		if(expanded){
			amount_field.setText(Config.getWorthAsString(am, false));
		}
		else{
			am = format();
		}
		expanded = bool;
		return true;
	}

	private void updateValues(boolean fromfield){
		if(fromfield){
			am = format();
		}
		TreeMap<String, String> fees = container.bank == null ? null : container.bank.getFees();
		String type = container.account == null ? "player" : container.account.getType();
		String rec = container.receiver == null ? "null" : container.receiver.getType();
		bf = Bank.parseFee(fees == null ? null : fees.get(type + ":" + rec), am);
	}
	
	private static final DecimalFormat df = new DecimalFormat("#.000", new DecimalFormatSymbols(Locale.US));
	static { df.setRoundingMode(RoundingMode.DOWN); }
	
	private final long format(){
		try{
			String str = amount_field.getText().replace(Config.getDot(), "").replace(",", ".");
			if(str.length() == 0) return 0;
			String format = df.format(Double.parseDouble(str));
			return Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			Print.chat(player, "INVALID INPUT: " + e.getMessage());
			if(Static.dev()) e.printStackTrace();
			return 0;
		}
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
        super.keyTyped(typedChar, keyCode);
    }

}
