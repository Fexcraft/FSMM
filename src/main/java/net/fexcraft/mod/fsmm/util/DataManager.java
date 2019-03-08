package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.impl.NullBank;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.minecraft.util.ResourceLocation;

public class DataManager extends TimerTask {
	
	private static final TreeMap<String, TreeMap<String, Account>> ACCOUNTS = new TreeMap<>();
	private static final TreeMap<String, Bank> BANKS = new TreeMap<>();
	public static File ACCOUNT_DIR, BANK_DIR;
	public static long LAST_TIMERTASK;
	protected static Timer timer;

	public DataManager(File file){
		timer = new Timer();
		ACCOUNT_DIR = new File(file, "/fsmm/accounts/");
		if(!ACCOUNT_DIR.exists()){ ACCOUNT_DIR.mkdirs(); }
		BANK_DIR = new File(file, "/fsmm/banks/");
		if(!BANK_DIR.exists()){ BANK_DIR.mkdirs(); }
	}

	@Override
	public void run(){
		ImmutableSet<String> set = ImmutableSet.copyOf(ACCOUNTS.keySet());
		LAST_TIMERTASK = LocalDate.now().getDayOfMonth();
		long mndt = LAST_TIMERTASK - (Config.UNLOAD_FREQUENCY - 5000);
		Print.debug("Starting scheduled account and bank clearance. (" + LAST_TIMERTASK + ")");
		for(String type : set){
			ImmutableMap<String, Account> map = ImmutableMap.copyOf(ACCOUNTS.get(type));
			for(Entry<String, Account> entry : map.entrySet()){
				if(entry.getValue().lastAccessed() >= 0 && entry.getValue().lastAccessed() < mndt){
					unloadAccount(type, entry.getKey());
				}
			}
		}
		ImmutableMap<String, Bank> map = ImmutableMap.copyOf(BANKS);
		for(Entry<String, Bank> entry : map.entrySet()){
			if(entry.getValue().lastAccessed() >= 0 && entry.getValue().lastAccessed() < mndt){
				unloadBank(entry.getKey());
			}
		}
		saveAll();
	}
	
	public static final void save(Account account){
		if(account == null){ return; }
		File file = new File(ACCOUNT_DIR, account.getType() + "/" + account.getId() + ".json");
		if(!file.exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, account.toJson(), true);
	}
	
	public static final void save(Bank bank){
		if(bank == null){ return; }
		File file = new File(BANK_DIR, bank.getId() + ".json");
		if(!file.exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, bank.toJson(), true);
	}

	public static final void saveAll(){
		for(TreeMap<String, Account> map : ACCOUNTS.values()){
			for(Account account : map.values()){
				try{ save(account); } catch(Exception e){ e.printStackTrace(); return; }
			}
		}
		for(Bank bank : BANKS.values()){
			try{ save(bank); } catch(Exception e){ e.printStackTrace(); return; }
		}
	}
	
	public static void unloadAccount(Account account){
		unloadAccount(account.getType(), account.getId());
	}
	
	public static void unloadAccount(ResourceLocation loc){
		unloadAccount(loc.getResourceDomain(), loc.getResourcePath());
	}

	public static void unloadAccount(String type, String id){
		try{ save(ACCOUNTS.get(type).remove(id)); } catch(Exception e){ e.printStackTrace(); return; }
	}
	
	public static void unloadBank(String id){
		try{ save(BANKS.remove(id)); } catch(Exception e){ e.printStackTrace(); return; }
	}

	public static final DataManager getInstance(){
		return FSMM.CACHE;
	}

	@Nullable
	public static final Account getAccount(String accid, boolean tempload, boolean create){
		return getAccount(accid, tempload, create, null);
	}
	
	@Nullable
	public static final Account getAccount(String accid, boolean tempload, boolean create, Class<? extends Account> impl){
		String[] arr = accid.split(":");
		if(arr.length < 2){ return null; }
		if(ACCOUNTS.containsKey(arr[0]) && ACCOUNTS.get(arr[0]).containsKey(arr[1])){
			Account account = ACCOUNTS.get(arr[0]).get(arr[1]);
			return !tempload && account.isTemporary() ? account.setTemporary(false) : account;
		}
		return tempload || create ? loadAccount(arr, tempload, create, impl) : null;
	}
	
	private static final Account loadAccount(String[] arr, boolean tempload, boolean create, Class<? extends Account> impl){
		impl = impl == null ? Account.class : impl; File file = new File(ACCOUNT_DIR, arr[0] + "/" + arr[1] + ".json");
		if(file.exists()){
			try{
				Account account = impl.getConstructor(JsonObject.class).newInstance(JsonUtil.get(file));
				if(!account.getType().equals(arr[0]) || !account.getId().equals(arr[1])){
					throw new RuntimeException("Account data from file doesn't match request! This is file error which should get controlled.\n" + file.getPath());
				}
				addAccount(arr[0], account);
				return account.setTemporary(tempload);
			}
			catch(ReflectiveOperationException | RuntimeException e){
				e.printStackTrace();
				return null;
			}
		}
		else if(create){
			try{
				Account account = impl.getConstructor(String.class, String.class, long.class, String.class, JsonObject.class).newInstance(arr[1], arr[0], arr[0].equals("player") ? Config.STARTING_BALANCE : 0, Config.DEFAULT_BANK, null);
				addAccount(arr[0], account);
				//FSMM.LOGGER.info("Created new account for " + arr[0] + ":" + arr[1] + "!");
				return account.setTemporary(tempload);
			}
			catch(ReflectiveOperationException | RuntimeException e){
				e.printStackTrace();
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	public static boolean addAccount(String type, Account account){
		if(getAccountsOfType(type) == null){
			ACCOUNTS.put(type, new TreeMap<>());
		}
		return getAccountsOfType(type).put(account.getId(), account) == null;
	}
	
	public static boolean addAccount(Account account){
		return addAccount(account.getType(), account);
	}
	
	@Nullable
	public static final TreeMap<String, Account> getAccountsOfType(String type){
		return ACCOUNTS.get(type);
	}
	
	@Nullable
	public static final Bank getBank(String id, boolean tempload, boolean create){
		return getBank(id, tempload, create, null);
	}
	
	@Nullable
	public static final Bank getBank(String id, boolean tempload, boolean create, Class<? extends Bank> impl){
		if(BANKS.containsKey(id)){
			Bank bank = BANKS.get(id);
			return !tempload && bank.isTemporary() ? bank.setTemporary(false) : bank;
		}
		if(tempload || create){
			impl = impl == null ? GenericBank.class : impl; File file = new File(BANK_DIR, id + ".json");
			if(file.exists()){
				try{
					Bank bank = impl.getConstructor(JsonObject.class).newInstance(JsonUtil.get(file));
					if(!bank.getId().equals(id)){
						throw new RuntimeException("Bank data from file doesn't match request! This is file error which should get controlled.\n" + file.getPath());
					} addBank(bank);
					return bank.setTemporary(tempload);
				}
				catch(ReflectiveOperationException | RuntimeException e){
					e.printStackTrace();
					return NullBank.INSTANCE;
				}
			}
			else if(create){
				try{
					Bank bank = impl.getConstructor(String.class, String.class, long.class, JsonObject.class, TreeMap.class).newInstance(id, id.equals(Config.DEFAULT_BANK) ? "Default Server Bank" : "Generated Bank", Config.STARTING_BALANCE, null, null);
					addBank(bank); //FSMM.LOGGER.info("Created new bank with ID " + id + ".");
					return bank.setTemporary(tempload);
				}
				catch(ReflectiveOperationException | RuntimeException e){
					e.printStackTrace();
					return NullBank.INSTANCE;
				}
			}
			else return NullBank.INSTANCE;
		}
		return NullBank.INSTANCE;
	}
	
	public static boolean addBank(Bank bank){
		return BANKS.put(bank.getId(), bank) == null;
	}
	
	@Nullable
	public static final TreeMap<String, Bank> getBanks(){
		return BANKS;
	}

	public void schedule(){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = LocalDate.now().getDayOfMonth();
		while((mid += Config.UNLOAD_FREQUENCY) < date);
        timer.schedule(this, new Date(mid), Config.UNLOAD_FREQUENCY);
	}

	/** @param offline show all account types or only loaded ones */
	public static String[] getAccountTypes(boolean offline){
		if(offline){
			return ACCOUNT_DIR.list();
		}
		return ACCOUNTS.keySet().toArray(new String[0]);
	}
	
}