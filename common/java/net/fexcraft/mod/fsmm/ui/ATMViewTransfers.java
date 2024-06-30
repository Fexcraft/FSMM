package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.data.Transfer;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.List;
import java.util.Map;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMViewTransfers extends UserInterface {

	private ATMContainer menu;
	private int scroll;

	public ATMViewTransfers(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
		menu.sync("account_transfers");
	}

	@Override
	public boolean onAction(UIButton button, String id,  int x, int y, int b){
		switch(id){
			case "up":{
				scroll--;
				if(scroll < 0) scroll = 0;
				return true;
			}
			case "down":{
				scroll++;
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
		if(menu.account != null){
			texts.get("account_name").value(menu.account.getName());
			texts.get("account_id").value(menu.account.getType() + ":" + menu.account.getId());
			Transfer transfer = null;
			for(int i = 0; i < 5; i++){
				int j = i + scroll;
				if(j >= menu.account.getTransfers().size()){
					buttons.get("transfer_" + i).visible(false);
					texts.get("transfer_receiver_" + i).value("");
					texts.get("transfer_amount_" + i).value("");
					texts.get("transfer_type_" + i).value("");
				}
				else{
					transfer = menu.account.getTransfers().get(j);
					buttons.get("transfer_" + i).visible(false);
					texts.get("transfer_receiver_" + i).value(transfer.name);
					texts.get("transfer_amount_" + i).value(Formatter.format((transfer.negative ? "&c" : "&e") + (transfer.negative ? "-" : "")+ Config.getWorthAsString(transfer.amount)));
					texts.get("transfer_type_" + i).value(transfer.action.name());
				}
			}
		}
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		for(Map.Entry<String, UIButton> entry : buttons.entrySet()){
			if(!entry.getKey().startsWith("transfer")) continue;
			if(entry.getValue().hovered(gLeft, gTop, mx, my)){
				int idx = Integer.parseInt(entry.getKey().substring(9));
				int j = idx + scroll;
				if(j >= menu.account.getTransfers().size()) continue;
				Transfer transfer = menu.account.getTransfers().get(j);
				list.add(Formatter.format("&9Transfer Info"));
				list.add(Formatter.format("&7Time: &a" + Time.getAsString(transfer.time)));
				list.add(Formatter.format("&7Account Name: &b" + transfer.name));
				list.add(Formatter.format("&7Account Type: &b" + transfer.type));
				list.add(Formatter.format("&7Account ID: &b" + transfer.from));
				list.add(Formatter.format("&7Transfer Type: &b" + transfer.action.name().toLowerCase()));
				list.add(Formatter.format("&7Fees Included: " + (transfer.included ? "&aYes" : "&cNo")));
				list.add(Formatter.format("&7Amount: &e" + (transfer.negative ? "-" : "") + Config.getWorthAsString(transfer.amount)));
				list.add(Formatter.format("&7Fee: &e" + Config.getWorthAsString(transfer.fee)));
				long am = transfer.amount + (transfer.included ? 0 : transfer.fee);
				list.add(Formatter.format("&7Total: &e" + (transfer.negative ? "-" : "") + Config.getWorthAsString(am)));
			}
		}
	}

	@Override
	public void postdraw(float ticks, int mx, int my){
		//
	}

	@Override
	public void scrollwheel(int am, int mx, int my){
		scroll += am > 0 ? 1 : -1;
		if(scroll < 0) scroll = 0;
	}

}
