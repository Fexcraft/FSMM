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

public class ATMAccountSelf extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/account_self.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private BasicButton action, /*cancel, exit,*/ expand;
	private BasicButton[] numbers = new BasicButton[12];
	private BasicText acc0, acc1, bal, fee, tot, amount;
	private boolean expanded, mode;
	private TextField amount_field;
	private String oldtext = "", suf;
	private long bf, am;

	public ATMAccountSelf(EntityPlayer player, boolean bool){
		super(texture, new ATMContainer(player), player);
		this.deftexrect = false;
		//this.defbackground = false;
		this.mode = bool;
		this.xSize = 256;
		this.ySize = 147;
		suf = mode ? " (inventory)" : "(balance)";
	}

	@Override
	protected void init(){
		this.texts.put("acc0", acc0 = new BasicText(guiLeft + 6, guiTop + 6, 244, null, "Synchronizing....").autoscale());
		this.texts.put("acc1", acc1 = new BasicText(guiLeft + 6, guiTop + 16, 244, null, "Please wait.").autoscale());
		this.texts.put("balance", bal = new BasicText(guiLeft + 6, guiTop + 32, 244, null, "").autoscale());
		this.texts.put("amount", amount = new BasicText(guiLeft + 6, guiTop + 44, 244, MapColor.SNOW.colorValue, "").autoscale());
		this.fields.put("amount", amount_field = new TextField(0, fontRenderer, guiLeft + 6, guiTop + 44, 244, 8).setColor(MapColor.SNOW.colorValue).setEnableBackground(false));
		this.texts.put("fee", fee = new BasicText(guiLeft + 6, guiTop + 56, 244, null, "").autoscale());
		this.texts.put("total", tot = new BasicText(guiLeft + 6, guiTop + 68, 233, null, "").autoscale());
		this.buttons.put("confirm", action = new BasicButton("action", guiLeft + 241, guiTop + 67, mode ? 0 : 10, 246, 10, 10, true));
		this.buttons.put("expand", expand = new BasicButton("expand", guiLeft + 191, guiTop + 79, 191, 148, 51, 8, true));
		for(int i = 0; i < numbers.length; i++){
			int x = 192 + ((i % 3) * 17), y = 79 + ((i / 3) * 16);
			String id = i < 9 ? "n" + (i + 1) : i == 9 ? "cancel" : i == 10 ? "n0" : "exit";
			this.buttons.put(id, numbers[i] = new BasicButton(id, guiLeft + x, guiTop + y, x, y, 15, 15, true));
		}
		this.container.sync("account", "bank", "inventory");
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
			bal.string = Config.getWorthAsString((mode ? container.inventory : container.account.getBalance())) + suf;
			if(am + bf > container.account.getBalance()) pref = "&c";
		}
		amount.string = Config.getWorthAsString(am, false);
		tot.string = Formatter.format(pref + Config.getWorthAsString(am + bf)) + "  (total)";
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
		if(expand.visible && expand.hovered) tooltip.add(Formatter.format("&7Open number pad."));
		if(action.hovered) tooltip.add(Formatter.format("&9Confirm " + (mode ? "&6Deposit" : "&eWidthdraw")));
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
				compound.setString("cargo", "action_" + (mode ? "deposit" : "withdraw"));
				compound.setLong("amount", am);
				container.send(Side.SERVER, compound);
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
		bf = Bank.parseFee(fees == null ? null : fees.get(mode ? "self:" + type : type + ":self"), am);
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
