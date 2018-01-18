package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.impl.GenericAccount;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;

public class AccountManager{
	
	/** &lt; Type, Id, Account &gt; */
	private final Table<String, String, Account> ACCOUNTS = TreeBasedTable.create();
	private final TreeMap<UUID, Bank> BANKS = new TreeMap<UUID, Bank>();
	private final List<String> LOADED_TYPES = new ArrayList<String>();
	//
	public static File ACCOUNT_SAVE_DIRECTORY, BANK_SAVE_DIRECTORY;
	public static AccountManager INSTANCE;
	
	public void initialize(File file){
		if(INSTANCE != null){
			Print.log("There is already an AccountManager Instance initialized!");
			Static.halt();
		}
		ACCOUNT_SAVE_DIRECTORY = new File(file, "/fsmm/accounts/");
		BANK_SAVE_DIRECTORY = new File(file, "/fsmm/banks/");
		if(!ACCOUNT_SAVE_DIRECTORY.exists()){
			ACCOUNT_SAVE_DIRECTORY.mkdirs();
		}
		if(!BANK_SAVE_DIRECTORY.exists()){
			BANK_SAVE_DIRECTORY.mkdirs();
		}
		INSTANCE = this;
	}
	
	//ACCOUNTS
	
	public Account loadAccount(String type, String id, Class<? extends Account> clazz){
		if(ACCOUNTS.contains(type, id)){
			return ACCOUNTS.get(type, id);
		}
		File file = new File(ACCOUNT_SAVE_DIRECTORY, type + "/" + id + ".json");
		if(!file.exists()){
			return createAccount(type, id, clazz);
		}
		Account account = null;
		try{
			account = clazz.getConstructor(String.class, JsonObject.class).newInstance(type, JsonUtil.get(file));
			//new GenericAccount(type, obj);
		}
		catch(Exception e){
			e.printStackTrace();
			//This shouldn't happen unless someone passes an invalid class to construct.
		}
		ACCOUNTS.put(type, id, account);
		return account;
	}

	private Account createAccount(String type, String id, Class<? extends Account> clazz){
		File old = new File(ACCOUNT_SAVE_DIRECTORY, type + "/" + id + ".fd");
		JsonObject obj = null;
		if(old.exists()){
			obj = JsonUtil.get(old);
			obj.addProperty("id", obj.get("uuid").getAsString());
			double oldbal = obj.get("balance").getAsDouble();
			oldbal *= 1000;
			obj.addProperty("balance", Math.round(oldbal));//TODO check this
			if(!obj.has("data")){
				obj.add("data", new JsonObject());
			}
		}
		else{
			obj = new JsonObject();
			obj.addProperty("id", id);
			obj.addProperty("balance", type.equals("player") ? Config.STARTING_BALANCE : 0);
			obj.addProperty("type", type);
			obj.addProperty("bank", Config.DEFAULT_BANK.toString());
			obj.add("data", new JsonObject());
		}
		Account account = null;
		try{
			account = clazz.getConstructor(String.class, JsonObject.class).newInstance(type, obj);
			//new GenericAccount(type, obj);
		}
		catch(Exception e){
			e.printStackTrace();
			//This shouldn't happen unless someone passes an invalid class to construct.
		}
		ACCOUNTS.put(type, id, account);
		saveAccount(account);
		if(!LOADED_TYPES.contains(type)){
			LOADED_TYPES.add(type);
		}
		return account;
	}

	public void unloadAccount(Account account){
		saveAccount(account);
		ACCOUNTS.remove(account.getType(), account.getId());
	}
	
	public void saveAccount(Account account){
		if(account == null){
			return;
		}
		JsonObject obj = new JsonObject();
		obj.addProperty("id", account.getId());
		obj.addProperty("balance", account.getBalance());
		obj.addProperty("type", account.getType());
		obj.addProperty("bank", account.getBankId().toString());
		obj.add("data", account.getData());
		JsonUtil.write(new File(ACCOUNT_SAVE_DIRECTORY, account.getType() + "/" + account.getId() + ".json"), obj);
	}
	
	public Account getAccount(String type, String id){
		return getAccount(type, id, false, null);
	}
	
	public Account getAccount(String type, String id, boolean create){
		return getAccount(type, id, create, null);
	}
	
	public Account getAccount(String type, String id, boolean create, Class<? extends Account> clazz){
		Account account = ACCOUNTS.get(type, id);
		return account == null ? create ? loadAccount(type, id, clazz == null ? GenericAccount.class : clazz) : null : account;
	}
	
	public Table<String, String, Account> getAccounts(){
		return ACCOUNTS;
	}
	
	//BANKS
	
	public Bank loadBank(UUID uuid, Class<? extends Bank> clazz){
		if(BANKS.containsKey(uuid)){
			return BANKS.get(uuid);
		}
		File file = new File(BANK_SAVE_DIRECTORY, uuid.toString() + ".json");
		if(!file.exists()){
			return createBank(uuid, clazz);
		}
		Bank bank = null;
		try{
			bank = clazz.getConstructor(UUID.class, JsonObject.class).newInstance(uuid, JsonUtil.get(file));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		BANKS.put(uuid, bank);
		return bank;
	}

	private Bank createBank(UUID uuid, Class<? extends Bank> clazz){
		File old = new File(BANK_SAVE_DIRECTORY, uuid.toString() + ".fd");
		JsonObject obj = null;
		if(old.exists()){
			obj = JsonUtil.get(old);
		}
		else{
			obj = new JsonObject();
			obj.addProperty("uuid", uuid.toString());
			obj.addProperty("name", "Unnamed Bank");
			obj.addProperty("balance", 0);
			obj.add("data", new JsonObject());
		}
		Bank bank = null;
		try{
			bank = clazz.getConstructor(UUID.class, JsonObject.class).newInstance(uuid, obj);
		}
		catch(Exception e){
			e.printStackTrace();
			//This shouldn't happen unless someone passes an invalid class to construct.
		}
		BANKS.put(uuid, bank);
		saveBank(bank);
		return bank;
	}
	
	public void unloadBank(Bank bank){
		if(bank == null){
			return;
		}
		saveBank(bank);
		BANKS.remove(bank.getId());
	}

	public void saveBank(Bank bank){
		JsonObject obj = new JsonObject();
		obj.addProperty("uuid", bank.getId().toString());
		obj.addProperty("name", bank.getName());
		obj.addProperty("balance", bank.getBalance());
		obj.add("data", bank.getData());
		JsonUtil.write(new File(BANK_SAVE_DIRECTORY, bank.getId().toString() + ".json"), obj);
	}
	
	public Bank getBank(UUID uuid){
		return getBank(uuid, false, null);
	}
	
	public Bank getBank(UUID uuid, boolean create){
		return getBank(uuid, create, null);
	}
	
	public Bank getBank(UUID uuid, boolean create, Class<? extends Bank> clazz){
		Bank bank = BANKS.get(uuid);
		return bank == null ? create ? loadBank(uuid, clazz == null ? GenericBank.class : clazz) : null : bank;
	}
	
	public TreeMap<UUID, Bank> getBanks(){
		return BANKS;
	}
	
	//GENERAL
	
	public final List<String> getLoadedAccountTypes(){
		return LOADED_TYPES;
	}
	
	public final void saveAll(){
		saveAccounts();
		saveBanks();
	}

	public final void saveAccounts(){
		AccountManager.INSTANCE.getAccounts().rowMap().forEach((str, map) -> {
    		map.forEach((key, val) -> {
        		Print.debug("Saving... ( " + str + " || " + key + " );");
    			AccountManager.INSTANCE.saveAccount(val);
    		});
    	});
	}

	public final void saveBanks(){
    	AccountManager.INSTANCE.getBanks().forEach((key, val) -> {
    		Print.debug("Saving... ( bank || " + key.toString() + " );");
    		AccountManager.INSTANCE.saveBank(val);
    	});
	}
	
}