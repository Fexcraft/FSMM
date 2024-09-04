package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.ItemManager;
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
public class ATMDeposit extends UserInterface {

	private ATMContainer menu;
	private String oldtext = "";
	private long am, bf;

	public ATMDeposit(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
	}

	@Override
	public void init(){
		menu.sync("account", "bank", "inventory");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		switch(id){
			case "confirm":{
				TagCW compound = TagCW.create();
				compound.set("cargo", "action_deposit");
				compound.set("amount", am);
				container.SEND_TO_SERVER.accept(compound);
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
			long inv = ItemManager.countInInventory(container.player.entity);
			texts.get("account_name").value(menu.account.getName());
			texts.get("account_id").value(menu.account.getType() + ":" + menu.account.getId());
			texts.get("balance").value(Config.getWorthAsString(inv) + " (inventory)");
			if(am + bf > inv) pref = "&c";
		}
		texts.get("total").value(Formatter.format(pref + Config.getWorthAsString(am + bf)) + "  (total)");
	}

	private void updateValues(){
		am = format();
		TreeMap<String, String> fees = menu.bank == null ? null : menu.bank.getFees();
		String type = menu.account == null ? "player" : menu.account.getType();
		bf = Bank.parseFee(fees == null ? null : fees.get("self:" + type), am);
	}

	private static final DecimalFormat df = new DecimalFormat("#.000", new DecimalFormatSymbols(Locale.US));
	static { df.setRoundingMode(RoundingMode.DOWN); }

	private final long format(){
		try{
			String str = fields.get("amount").text().replace(Config.DOT, "").replace(",", ".");
			if(str.length() == 0) return 0;
			String format = df.format(Double.parseDouble(str));
			return Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			container.player.entity.send( "INVALID INPUT: " + e.getMessage());
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
