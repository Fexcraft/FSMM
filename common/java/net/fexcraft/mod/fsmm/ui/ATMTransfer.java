package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.TreeMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMTransfer extends UserInterface {

	private ATMContainer menu;
	private String oldtext = "";
	private long am, bf;

	public ATMTransfer(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
		menu.sync("account", "bank", "receiver");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		switch(id){
			case "confirm":{
				TagCW compound = TagCW.create();
				compound.set("cargo", "action_transfer");
				compound.set("amount", am);
				container.SEND_TO_SERVER.accept(compound);
				return true;
			}
			case "select":{
				TagCW com = TagCW.create();
				com.set("cargo", "receiver");
				container.SEND_TO_SERVER.accept(com);
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		return false;
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(!oldtext.equals(fields.get("amount").text())){
			oldtext = fields.get("amount").text();
			updateValues();
		}
		if(menu.bank != null){
			texts.get("fee").value(am == 0 || bf == 0 ? "-" : Config.getWorthAsString(bf) + "  (fee)");
		}
		String pref = "";
		if(menu.account != null){
			texts.get("sender_name").value(menu.account.getName());
			texts.get("sender_id").value(menu.account.getType() + ":" + menu.account.getId());
			texts.get("balance").value(Config.getWorthAsString(menu.account.getBalance()) + "  (balance)");
			if(am + bf > menu.account.getBalance()) pref = "&c";
		}
		if(menu.receiver != null){
			texts.get("receiver_name").value(menu.receiver.getName());
			texts.get("receiver_id").value(menu.receiver.getType() + ":" + menu.receiver.getId());
		}
		else{
			texts.get("receiver_name").value("ui.fsmm.atm_select_rec_info");
			texts.get("receiver_name").translate();
			texts.get("receiver_id").value("");
		}
		texts.get("total").value(Formatter.format(pref + Config.getWorthAsString(am + bf)) + "  (total)");
	}

	private void updateValues(){
		am = format();
		TreeMap<String, String> fees = menu.bank == null ? null : menu.bank.getFees();
		String type = menu.account == null ? "player" : menu.account.getType();
		bf = Bank.parseFee(fees == null ? null : fees.get(type + ":self"), am);
	}

	private static final DecimalFormat df = new DecimalFormat("#.000", new DecimalFormatSymbols(Locale.US));
	static { df.setRoundingMode(RoundingMode.DOWN); }

	private long format(){
		try{
			String str = fields.get("amount").text().replace(Config.getDot(), "").replace(",", ".");
			if(str.length() == 0) return 0;
			String format = df.format(Double.parseDouble(str));
			return Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			container.player.entity.send("INVALID INPUT: " + e.getMessage());
			if(Static.dev()) e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void postdraw(float ticks, int mx, int my){
		//
	}

	@Override
	public void scrollwheel(int am, int mx, int my){
		//
	}

}
