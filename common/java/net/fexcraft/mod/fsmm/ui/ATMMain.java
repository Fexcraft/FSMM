package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMMain extends UserInterface {

	private ATMContainer menu;

	public ATMMain(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
	}

	@Override
	public void init(){
		menu.sync("account", "bank");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		TagCW com = TagCW.create();
		com.set("cargo", id);
		container.SEND_TO_SERVER.accept(com);
		return true;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		return false;
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(menu.bank != null){
			texts.get("bank_line_0").value(menu.bank.getName());
			texts.get("bank_line_1").value(menu.bank.getStatus().size() > 0 ? Formatter.format(menu.bank.getStatus().get(0)) : "");
			texts.get("bank_line_2").value(menu.bank.getStatus().size() > 1 ? Formatter.format(menu.bank.getStatus().get(1)) : "");
			texts.get("bank_line_3").value(menu.bank.getStatus().size() > 2 ? Formatter.format(menu.bank.getStatus().get(2)) : "");
		}
		if(menu.account != null){
			texts.get("account_name").value(menu.account.getName());
			texts.get("account_balance").value(Config.getWorthAsString(menu.account.getBalance()));
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
