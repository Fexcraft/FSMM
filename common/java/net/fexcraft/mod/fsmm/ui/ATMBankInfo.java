package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMBankInfo extends UserInterface {

	private ATMContainer menu;
	private String[] keys, vals;
	private int scroll;

	public ATMBankInfo(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
	}

	@Override
	public void init(){
		menu.sync("bank");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
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
		if(menu.bank != null){
			texts.get("bank_name").value(menu.bank.getName());
			texts.get("bank_line_0").value(menu.bank.getStatus().size() > 0 ? Formatter.format(menu.bank.getStatus().get(0)) : "");
			texts.get("bank_line_1").value(menu.bank.getStatus().size() > 1 ? Formatter.format(menu.bank.getStatus().get(1)) : "");
			texts.get("bank_line_2").value(menu.bank.getStatus().size() > 2 ? Formatter.format(menu.bank.getStatus().get(2)) : "");
			if(keys == null){
				if(menu.bank.getFees() == null){
					keys = vals = new String[0];
				}
				else {
					keys = menu.bank.getFees().keySet().toArray(new String[0]);
					vals = menu.bank.getFees().values().toArray(new String[0]);
				}
			}
			if(keys.length > 0){
				for(int i = 0; i < 8; i++){
					int j = i + scroll;
					if(j >= keys.length) texts.get("bank_fees_" + j).value("");
					else{
						String val = vals[j].contains("%") ? vals[j] : Config.getWorthAsString(Long.parseLong(vals[j]));
						texts.get("bank_fees_" + j).value(keys[j].replace(":", " -> ") + " = " + val);
					}
				}
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
