package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.data.*;
import net.fexcraft.mod.fsmm.event.ATMEvent;
import net.fexcraft.mod.fsmm.event.FsmmEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.ui.ContainerInterface;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMContainer extends ContainerInterface {

	protected ArrayList<Map.Entry<String, String>> banks;
	protected ArrayList<AccountPermission> accounts;
	protected ArrayList<String> types;
	protected PlayerAccData pass;
	protected AccountPermission perm;
	protected Account account, receiver;
	protected long inventory;
	protected Bank bank;

	public ATMContainer(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		if(ply.entity.isOnClient()) return;
		pass = ply.getApp(PlayerAccData.class);
		perm = pass.getSelectedAccount() == null ? AccountPermission.FULL : pass.getSelectedAccount();
		account = pass.getSelectedAccount() == null ? pass.getAccount() : perm.getAccount();
		receiver = pass.getSelectedReceiver();
		bank = pass.getSelectedBankInATM() == null ? account.getBank() : pass.getSelectedBankInATM();
		pass.setSelectedBankInATM(null);
	}

	@Override
	public void init(){
		if(uiid.equals(FsmmUIKeys.UI_ATM_ACC_DEPOSIT) || uiid.equals(FsmmUIKeys.UI_ATM_ACC_WITHDRAW)){
			if(!FsmmUIKeys.IS_ATM.apply(player, pos)){
				player.entity.closeUI();
				if(!player.entity.isOnClient()) player.entity.send("ui.fsmm.atm_only");
			}
		}
	}

	@Override
	public Object get(String key, Object... objs){
		return null;
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(client){
			switch(com.getString("cargo")){
				case "sync":{
					if(com.has("account")){
						account = new Account(JsonHandler.parse(com.getString("account"), true).asMap(), null, null);
					}
					if(com.has("receiver")){
						receiver = new Account(JsonHandler.parse(com.getString("receiver"), true).asMap(), null, null);
					}
					if(com.has("bank")){
						bank = new Bank(JsonHandler.parse(com.getString("bank"), true).asMap());
					}
					if(com.has("account_types")){
						types = new ArrayList<>();
						TagLW list = com.getList("account_types");
						for(int i = 0; i < list.size(); i++) types.add(list.getString(i));
					}
					if(com.has("bank_list")){
						TreeMap<String, String> banks = new TreeMap<>();
						TagLW list = com.getList("bank_list");
						for(int i = 0; i < list.size(); i++){
							String[] str = list.getString(i).split(":");
							if(bank != null && str[0].equals(bank.id)) continue;
							banks.put(str[0], str[1]);
						}
						this.banks = new ArrayList<>();
						this.banks.addAll(banks.entrySet());
					}
					if(com.has("account_list")){
						accounts = new ArrayList<>();
						TagLW list = com.getList("account_list");
						for(int i = 0; i < list.size(); i++){
							accounts.add(new AccountPermission(list.getCompound(i)));
						}
					}
					if(com.has("inventory")){
						inventory = com.getLong("inventory");
					}
					break;
				}
			}
			return;
		}
		switch(com.getString("cargo")){
			case "bank":{
				player.entity.openUI(FsmmUIKeys.UI_ATM_BANK_SELECT, pos);
				break;
			}
			case "transfers":{
				player.entity.openUI(FsmmUIKeys.UI_ATM_TRANSFERS, pos);
				break;
			}
			case "select":{
				player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_SELECT, pos);
				break;
			}
			case "receiver":{
				player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_RECEIVER, pos);
				break;
			}
			case "withdraw":{
				if(!FsmmUIKeys.IS_ATM.apply(player, pos)){
					player.entity.send("ui.fsmm.not_mobile");
					return;
				}
				player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_WITHDRAW, pos);
				break;
			}
			case "deposit":{
				if(!FsmmUIKeys.IS_ATM.apply(player, pos)){
					player.entity.send("ui.fsmm.not_mobile");
					return;
				}
				player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_DEPOSIT, pos);
				break;
			}
			case "transfer":{
				player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_TRANSFER, pos);
				break;
			}
			case "bank_info":{
				pass.setSelectedBankInATM(DataManager.getBank(com.getString("bank")));
				player.entity.openUI(FsmmUIKeys.UI_ATM_BANK_INFO, pos);
				break;
			}
			case "bank_select":{
				if(!perm.manage){
					player.entity.send("ui.fsmm.atm.account_no_manage_perm");
					player.entity.closeUI();
					break;
				}
				Bank bank = DataManager.getBank(com.getString("bank"));
				String feeid = account.getType() + ":setup_account";
				long fee = bank.hasFee(feeid) ? Long.parseLong(bank.getFees().get(feeid).replace("%", "")) : 0;
				if(account.getBalance() < fee){
					player.entity.send("ui.fsmm.atm.not_enough_to_move_bank");
					player.entity.closeUI();
				}
				else{
					if(fee > 0) account.modifyBalance(Manageable.Action.SUB, fee, player.entity);
					account.setBank(bank);
					player.entity.openUI(FsmmUIKeys.UI_ATM_MAIN, pos);
				}
				break;
			}
			case "sync":{
				TagCW compound = TagCW.create();
				if(com.getBoolean("account")){
					compound.set("account", account.toJson(false).toString());
				}
				if(com.getBoolean("account_transfers")){
					compound.set("account", account.toJson(true).toString());
				}
				if(com.getBoolean("receiver") && receiver != null){
					compound.set("receiver", receiver.toJson(false).toString());
				}
				if(com.getBoolean("bank")){
					compound.set("bank", bank.toJson().toString());
				}
				if(com.getBoolean("bank_list")){
					compound.set("bank_list", getBankList());
				}
				if(com.getBoolean("account_types")){
					TagLW list = TagLW.create();
					for(String type : DataManager.getAccountTypes()) list.add(type);
					compound.set("account_types", list);
				}
				if(com.getBoolean("account_list")){
					ATMEvent.GatherAccounts event = new ATMEvent.GatherAccounts(player);
					FsmmEvent.run(event);
					accounts = event.getAccountsList();
					TagLW list = TagLW.create();
					accounts.forEach(account -> {
						list.add(TagCW.wrap(account.toNBT()));
					});
					compound.set("account_list", list);
				}
				if(com.getBoolean("inventory")){
					compound.set("inventory", ItemManager.countInInventory(player.entity));
				}
				compound.set("cargo", "sync");
				SEND_TO_CLIENT.accept(compound, player);
				break;
			}
			case "action_deposit":
			case "action_withdraw":{
				boolean deposit = com.getString("cargo").endsWith("deposit");
				if(!(deposit ? perm.deposit : perm.withdraw)){
					player.entity.send(deposit ? "ui.fsmm.atm.no_deposit_perm" : "ui.fsmm.atm.no_withdraw_perm");
					return;
				}
				if(processSelfAction(com.getLong("amount"), deposit)){
					player.entity.closeUI();
				}
				break;
			}
			case "action_transfer":{
				if(!perm.transfer){
					player.entity.send("ui.fsmm.atm.no_transfer_perm");
					return;
				}
				long amount = com.getLong("amount");
				if(amount <= 0) return;
				if(receiver == null){
					player.entity.send("ui.fsmm.atm.select_receiver");
					return;
				}
				if(account.getBank().processAction(Bank.Action.TRANSFER, player.entity, account, amount, receiver, false)){
					player.entity.send("ui.fsmm.atm.transfer_processed", Config.getWorthAsString(amount, false));
					player.entity.closeUI();
				}
				else{
					player.entity.send("ui.fsmm.atm.transfer_failed");
				}
				break;
			}
			case "account_search":{
				String type = com.getString("type").toLowerCase();
				String id = com.getString("id").toLowerCase();
				if(type.trim().length() == 0 || id.trim().length() == 0) break;
				TagCW compound = TagCW.create();
				ATMEvent.SearchAccounts event = new ATMEvent.SearchAccounts(player, type, id);
				FsmmEvent.run(event);
				accounts = new ArrayList<>();
				accounts.addAll(event.getAccountsMap().values());
				TagLW list = TagLW.create();
				accounts.forEach(account -> {
					list.add(TagCW.wrap(account.toNBT()));
				});
				compound.set("account_list", list);
				compound.set("cargo", "sync");
				SEND_TO_CLIENT.accept(compound, player);
				break;
			}
			case "account_select":{
				AccountPermission acc = null;
				String type = com.getString("type"), id = com.getString("id");
				boolean mode = com.getBoolean("mode");
				for(AccountPermission perm : accounts){
					if(perm.getAccount().getType().equals(type) && perm.getAccount().getId().equals(id)){
						acc = perm;
						break;
					}
				}
				if(acc != null){
					if(mode){
						pass.setSelectedAccount(acc);
						player.entity.openUI(FsmmUIKeys.UI_ATM_MAIN, pos);
					}
					else{
						pass.setSelectedReceiver(acc.getAccount());
						player.entity.openUI(FsmmUIKeys.UI_ATM_ACC_TRANSFER, pos);
					}
				}
				else{
					player.entity.send("ui.fsmm.atm.no_account_server");
					player.entity.closeUI();
				}
				break;
			}
		}
	}

	private boolean processSelfAction(long amount, boolean deposit){
		if(amount <= 0) return false;
		String dep = deposit ? "&eDeposit" : "&aWithdraw";
		if(account.getBank().processAction(deposit ? Bank.Action.DEPOSIT : Bank.Action.WITHDRAW, player.entity, account, amount, account, false)){
			player.entity.send("ui.fsmm.atm."+ (deposit ? "deposit" : "withdraw") + "_processed", Config.getWorthAsString(amount, false));
			return true;
		}
		else{
			player.entity.send("ui.fsmm.atm."+ (deposit ? "deposit" : "withdraw") + "_failed");
			return false;
		}
	}

	private TagLW getBankList(){
		TagLW list = TagLW.create();
		DataManager.getBanks().forEach((key, val) -> {
			list.add(key + ":" + val.getName());
		});
		return list;
	}

	public void sync(String... types){
		TagCW compound = TagCW.create();
		compound.set("cargo", "sync");
		for(String str : types){
			compound.set(str, true);
		}
		SEND_TO_SERVER.accept(compound);
	}

}
