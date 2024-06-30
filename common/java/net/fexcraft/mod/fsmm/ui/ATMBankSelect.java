package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMBankSelect extends UserInterface {

	private ATMContainer menu;
	private int scroll;

	public ATMBankSelect(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		menu = (ATMContainer)container;
		menu.sync("bank", "bank_list");
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		if(id.startsWith("info_")){
			int i = Integer.parseInt(id.substring(5));
			if(i < 0 || i >= 8) return false;
			TagCW compound = TagCW.create();
			compound.set("cargo", "bank_info");
			compound.set("bank", menu.banks.get(i + scroll).getKey());
			container.SEND_TO_SERVER.accept(compound);
			return true;
		}
		else if(id.startsWith("select_")){
			int i = Integer.parseInt(id.substring(7));
			if(i < 0 || i >= 8) return false;
			TagCW compound = TagCW.create();
			compound.set("cargo", "bank_select");
			compound.set("bank", menu.banks.get(i + scroll).getKey());
			container.SEND_TO_SERVER.accept(compound);
			return true;
		}
		switch(id){
			case "up":{
				scroll--;
				if(scroll < 0) scroll = 0;
				return true;
			}
			case "dw":{
				scroll++;
				return true;
			}
			case "info":{
				TagCW compound = TagCW.create();
				compound.set("cargo", "bank_info");
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
		if(menu.bank != null){
			texts.get("bank_name").value(menu.bank.getName());
		}
		if(menu.banks != null){
			for(int i = 0; i < 8; i++){
				int j = i + scroll;
				if(j >= menu.banks.size()){
					buttons.get("info_" + i).visible(false);
					buttons.get("select_" + i).visible(false);
					texts.get("bank_" + i).value("");
				}
				else{
					buttons.get("info_" + i).visible(false);
					buttons.get("select_" + i).visible(false);
					texts.get("bank_" + i).value(menu.banks.get(j).getValue());
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
