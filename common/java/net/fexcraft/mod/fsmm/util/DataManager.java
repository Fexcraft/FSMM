package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.CallbackContainer;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.IDL;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class DataManager extends TimerTask {

	public static LinkedHashMap<IDL, Money> CURRENCY = new LinkedHashMap<>();
	private static final Map<String, Map<String, Account>> ACCOUNTS = new ConcurrentHashMap<>();
	private static final Map<String, Bank> BANKS = new ConcurrentHashMap<>();
	public static CallbackContainer AFTER_INIT = new CallbackContainer();
	public static File ACCOUNT_DIR, BANK_DIR;
	public static long LAST_TIMERTASK;
	protected static Timer timer;

	public DataManager(File file){
		timer = new Timer();
		ACCOUNT_DIR = new File(file, "/fsmm/accounts/");
		if(!ACCOUNT_DIR.exists()) ACCOUNT_DIR.mkdirs();
		for(File dir : ACCOUNT_DIR.listFiles()){
			if(!dir.isDirectory()) continue;
			String[] fls = dir.list();
			if(fls == null || fls.length == 0) continue;
			ACCOUNTS.put(dir.getName(), new ConcurrentHashMap<>());
		}
		BANK_DIR = new File(file, "/fsmm/banks/");
		if(!BANK_DIR.exists()){ BANK_DIR.mkdirs(); }
		if(Config.DEFAULT_BANKS != null){
			Config.DEFAULT_BANKS.forEach(str -> addBank(new Bank(str)));
		}
		if(BANKS.isEmpty() || !BANKS.containsKey(Config.DEFAULT_BANK)){
			BANKS.put(Config.DEFAULT_BANK, new Bank(Config.DEFAULT_BANK));
		}
		for(Bank bank : BANKS.values()) loadBank(bank);
		AFTER_INIT.complete();
	}

	@Override
	public void run(){
		ImmutableSet<String> set = ImmutableSet.copyOf(ACCOUNTS.keySet());
		LAST_TIMERTASK = Time.getDate();
		long mndt = LAST_TIMERTASK - (Config.UNLOAD_FREQUENCY * Time.MIN_MS - 5000);
		if(EnvInfo.DEV) FSMM.LOGGER.info("Starting scheduled inactive account unloading. (" + LAST_TIMERTASK + ")");
		for(String type : set){
			ImmutableMap<String, Account> map = ImmutableMap.copyOf(ACCOUNTS.get(type));
			for(Entry<String, Account> entry : map.entrySet()){
				if(entry.getValue().lastActive() >= 0 && entry.getValue().lastActive() < mndt){
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
	
	public static void unloadAccount(IDL loc){
		unloadAccount(loc.space(), loc.path());
	}

	private static void unloadAccount(String type, String id){
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

	public static void unholdPlayerAccount(UUID id, Class<?> clazz){
		unholdPlayerAccount(id.toString(), clazz);
	}

	public static void unholdPlayerAccount(String id, Class<?> clazz){
		if(!ACCOUNTS.containsKey("player")) return;
		if(ACCOUNTS.get("player").containsKey(id)){
			Account acc = ACCOUNTS.get("player").get(id);
			save(acc);
			acc.remHolder(clazz);
		}
	}

	public static final DataManager getInstance(){
		return FSMM.CACHE;
	}

	/**
	 *  Gets an account via account id.
	 * @param accid account id in [type]:[id] format
	 * @param mode 0 = only get from memory, 1 = load if file exists, 2 = create if missing
	 * @return
	 */
	public static final Account getAccount(String accid, int mode){
		return getAccount(accid, mode, null);
	}

	/**
	 *  Gets an account via account id.
	 * @param accid account id in [type]:[id] format
	 * @param mode 0 = only get from memory, 1 = load if file exists, 2 = create if missing
	 * @param cons optional consumer that is run if a new account is created
	 * @return
	 */
	public static final Account getAccount(String accid, int mode, Consumer<Account> cons){
		String[] arr = accid.split(":");
		if(arr.length < 2){ return null; }
		if(ACCOUNTS.containsKey(arr[0]) && ACCOUNTS.get(arr[0]).containsKey(arr[1])){
			return ACCOUNTS.get(arr[0]).get(arr[1]);
		}
		return mode > 0 ? loadAccount(arr, mode > 1, cons) : null;
	}
	
	private static Account loadAccount(String[] arr, boolean create, Consumer<Account> cons){
		File file = new File(ACCOUNT_DIR, arr[0] + "/" + arr[1] + ".json");
		if(file.exists()){
			try{
				Account account = new Account(JsonHandler.parse(file), arr[0], arr[1]);
				if(!account.getType().equals(arr[0]) || !account.getId().equals(arr[1])){
					FSMM.LOGGER.info(arr[0] + ":" + arr[1] + " != " + account.getType() + ":" + account.getId());
					throw new RuntimeException("Account data from file doesn't match request! This is a file error which should get controlled.\n" + file.getPath());
				}
				addAccount(arr[0], account);
				return account;
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
				FSMM.LOGGER.info("Created new account for " + arr[0] + ":" + arr[1] + " at Bank '" + account.getBank().id +"'!");
				if(cons != null){
					try{
						cons.accept(account);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				return account;
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

	public static final Map<String, Account> getAccountsOfType(String type){
		return ACCOUNTS.get(type);
	}

	/**
	 * @return bank with the id, or default server bank
	 */
	public static final Bank getBank(String id){
		if(BANKS.containsKey(id)){
			return BANKS.get(id);
		}
		return getDefaultBank();
	}

	public static void loadBank(Bank bank){
		if(bank == null) return;
		bank.load(JsonHandler.parse(new File(BANK_DIR, bank.id + ".json")));
	}

	public static final Bank getDefaultBank(){
		return BANKS.get(Config.DEFAULT_BANK);
	}
	
	public static boolean addBank(Bank bank){
		return BANKS.put(bank.id, bank) == null;
	}

	public static final Map<String, Bank> getBanks(){
		return BANKS;
	}

	public void schedule(){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = Time.getDate();
		while((mid += Config.UNLOAD_FREQUENCY * Time.MIN_MS) < date);
        timer.schedule(this, new Date(mid), Config.UNLOAD_FREQUENCY * Time.MIN_MS);
	}

	///** @param offline show all account types or only loaded ones */
	public static String[] getAccountTypes(/*boolean offline*/){
		//if(offline) return ACCOUNT_DIR.list();
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

	public static List<Money> getSortedMoneyList(){
		return CURRENCY.values().stream().sorted((o1, o2) -> o1.getWorth() < o2.getWorth() ? 1 : -1).collect(Collectors.toList());
	}
	
}