package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.minecraft.util.ResourceLocation;

public class DataManager extends TimerTask {
	
	private static final Map<String, Map<String, Account>> ACCOUNTS = new ConcurrentHashMap<>();
	private static final Map<String, Bank> BANKS = new ConcurrentHashMap<>();
	public static File ACCOUNT_DIR, BANK_DIR;
	public static long LAST_TIMERTASK;
	protected static Timer timer;

	public DataManager(File file){
		timer = new Timer();
		ACCOUNT_DIR = new File(file, "/fsmm/accounts/");
		if(!ACCOUNT_DIR.exists()){ ACCOUNT_DIR.mkdirs(); }
		BANK_DIR = new File(file, "/fsmm/banks/");
		if(!BANK_DIR.exists()){ BANK_DIR.mkdirs(); }
		Config.loadDefaultBanks();
		for(File bfl : BANK_DIR.listFiles()){
			if(bfl.isDirectory()) continue;
			try{
				addBank(new Bank(JsonHandler.parse(bfl)));
			}
			catch(Throwable thr){
				thr.printStackTrace();
			}
		}
	}

	@Override
	public void run(){
		ImmutableSet<String> set = ImmutableSet.copyOf(ACCOUNTS.keySet());
		LAST_TIMERTASK = Time.getDate();
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
		saveAll();
	}
	
	public static final void save(Account account){
		if(account == null) return;
		File file = new File(ACCOUNT_DIR, account.getType() + "/" + account.getId() + ".json");
		if(!file.exists()) file.getParentFile().mkdirs();
		JsonHandler.print(file, account.toJson(), PrintOption.FLAT);
	}
	
	public static final void save(Bank bank){
		if(bank == null) return;
		File file = new File(BANK_DIR, bank.id + ".json");
		if(!file.exists()) file.getParentFile().mkdirs();
		JsonHandler.print(file, bank.toJson(), PrintOption.FLAT);
	}

	public final void saveAll(){
		for(Map<String, Account> map : ACCOUNTS.values()){
			for(Account account : map.values()){
				try{
					save(account);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		for(Bank bank : BANKS.values()){
			try{
				save(bank);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void clearAll(){
		ACCOUNTS.clear();
		BANKS.clear();
		timer.cancel();
	}
	
	public static void unloadAccount(Account account){
		unloadAccount(account.getType(), account.getId());
	}
	
	public static void unloadAccount(ResourceLocation loc){
		unloadAccount(loc.getNamespace(), loc.getPath());
	}

	public static void unloadAccount(String type, String id){
		if(ACCOUNTS.containsKey(type) && ACCOUNTS.get(type).containsKey(id)){
			try{
				save(ACCOUNTS.get(type).remove(id));
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}

	public static final DataManager getInstance(){
		return FSMM.CACHE;
	}
	
	@Nullable
	public static final Account getAccount(String accid, boolean tempload, boolean create){
		String[] arr = accid.split(":");
		if(arr.length < 2){ return null; }
		if(ACCOUNTS.containsKey(arr[0]) && ACCOUNTS.get(arr[0]).containsKey(arr[1])){
			Account account = ACCOUNTS.get(arr[0]).get(arr[1]);
			return !tempload && account.isTemporary() ? account.setTemporary(false) : account;
		}
		return tempload || create ? loadAccount(arr, tempload, create) : null;
	}
	
	private static final Account loadAccount(String[] arr, boolean tempload, boolean create){
		File file = new File(ACCOUNT_DIR, arr[0] + "/" + arr[1] + ".json");
		if(file.exists()){
			try{
				Account account = new Account(JsonHandler.parse(file));
				if(!account.getType().equals(arr[0]) || !account.getId().equals(arr[1])){
					throw new RuntimeException("Account data from file doesn't match request! This is a file error which should get controlled.\n" + file.getPath());
				}
				addAccount(arr[0], account);
				return account.setTemporary(tempload);
			}
			catch(RuntimeException e){
				e.printStackTrace();
				return null;
			}
		}
		else if(create){
			try{
				Account account = new Account(arr[1], arr[0], arr[0].equals("player") ? Config.STARTING_BALANCE : 0, getDefaultBank(), null);
				addAccount(arr[0], account);
				FSMM.LOGGER.info("Created new account for " + arr[0] + ":" + arr[1] + "!");
				return account.setTemporary(tempload);
			}
			catch(RuntimeException e){
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
			ACCOUNTS.put(type, new ConcurrentHashMap<>());
		}
		return getAccountsOfType(type).put(account.getId(), account) == null;
	}
	
	public static boolean addAccount(Account account){
		return addAccount(account.getType(), account);
	}
	
	@Nullable
	public static final Map<String, Account> getAccountsOfType(String type){
		return ACCOUNTS.get(type);
	}

	/**
	 * @return loaded bank with the id, loads bank if savefile present, otherwise returns default bank
	 */
	public static final Bank getBank(String id){
		if(BANKS.containsKey(id)){
			return BANKS.get(id);
		}
		File file = new File(BANK_DIR, id + ".json");
		if(file.exists()){
			Bank bank = new Bank(JsonHandler.parse(file));
			addBank(bank);
			return bank;
		}
		else return getDefaultBank();
	}

	public static final Bank getDefaultBank(){
		return BANKS.get(Config.DEFAULT_BANK);
	}
	
	public static boolean addBank(Bank bank){
		return BANKS.put(bank.id, bank) == null;
	}
	
	@Nullable
	public static final Map<String, Bank> getBanks(){
		return BANKS;
	}

	public void schedule(){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = Time.getDate();
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

	public static boolean exists(String type, String id){
		if(ACCOUNTS.containsKey(type) && ACCOUNTS.get(type).containsKey(id)) return true;
		File folder = new File(DataManager.ACCOUNT_DIR, type + "/");
		if(!folder.exists()) return false;
		for(File file : folder.listFiles()){
			if(file.isDirectory() || file.isHidden()) continue;
			if(file.getName().endsWith(".json") && file.getName().substring(0, file.getName().length() - 5).equals(id)) return true;
		}
		return false;
	}
	
}