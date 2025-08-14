package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMSelectReceiver extends UserInterface {

	private ATMContainer menu;
	private List<AccountPermission> accounts;
	private int seltype = -1;
	private int scroll;

	public ATMSelectReceiver(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
	}

	@Override
	public void init(){
		menu.sync("account_types", "account");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		switch(id){
			case "search":{
				search();
				return true;
			}
			case "result_0":{
				select(0);
				return true;
			}
			case "result_1":{
				select(1);
				return true;
			}
			case "result_2":{
				select(2);
				return true;
			}
			case "result_3":{
				select(3);
				return true;
			}
			case "up":{
				scroll--;
				if(scroll < 0) scroll = 0;
				return true;
			}
			case "down":{
				scroll++;
				return true;
			}
			case "prev":{
				if(--seltype < 0) seltype = menu.types.size() - 1;
				return true;
			}
			case "next":{
				if(++seltype >= menu.types.size()) seltype = 0;
				return true;
			}
		}
		return true;
	}

	private void select(int idx){
		if(accounts == null || idx + scroll >= accounts.size()) return;
		AccountPermission perm = accounts.get(idx + scroll);
		TagCW compound = TagCW.create();
		compound.set("cargo", "account_select");
		compound.set("type", perm.getAccount().getType());
		compound.set("id", perm.getAccount().getId());
		compound.set("mode", false);
		menu.SEND_TO_SERVER.accept(compound);
	}

	private void search(){
		if(accounts != null) accounts.clear();
		String type = menu.types.get(seltype), id = fields.get("id").text();
		boolean notype = type.trim().length() == 0, noid = id.trim().length() == 0;
		if(notype){
			container.player.entity.send("&cYou need to enter the searched account type.");
			return;
		}
		if((noid || id.trim().length() < 1)){
			container.player.entity.send("&cYou need to enter at least " + Config.MIN_SEARCH_CHARS + " characters of the searched id.");
			return;
		}
		if(notype && noid) return;
		TagCW compound = TagCW.create();
		compound.set("cargo", "account_search");
		compound.set("type", type);
		compound.set("id", id);
		menu.SEND_TO_SERVER.accept(compound);
		menu.accounts = null;
		accounts = null;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		return false;
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(seltype < 0 && menu.types != null){
			seltype = menu.types.indexOf("player");
		}
		if(seltype >= 0){
			texts.get("type").value(menu.types.get(seltype));
		}
		if(menu.account != null){
			texts.get("account_name").value(menu.account.getName());
			texts.get("account_id").value(menu.account.getType() + ":" + menu.account.getId());
		}
		if(menu.accounts != null){
			if(accounts == null){
				accounts = new ArrayList<>();
				accounts.addAll(menu.accounts);
			}
			Account account;
			for(int i = 0; i < 4; i++){
				int k = i + scroll;
				if(k >= accounts.size()){
					buttons.get("result_" + i).visible(false);
					texts.get("result_name_" + i).value("");
					texts.get("result_id_" + i).value("");
				}
				else{
					buttons.get("result_" + i).visible(true);
					account = accounts.get(k).getAccount();
					texts.get("result_name_" + i).value(account.getName());
					texts.get("result_id_" + i).value(account.getType() + ":" + account.getId());
				}
			}
		}
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		//
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
