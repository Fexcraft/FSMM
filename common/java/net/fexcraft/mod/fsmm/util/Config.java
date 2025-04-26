package net.fexcraft.mod.fsmm.util;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.uni.ConfigBase;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.fexcraft.mod.uni.inv.StackWrapper;
import net.fexcraft.mod.uni.inv.UniStack;

import java.io.File;
import java.util.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Config extends ConfigBase {

	public static long STARTING_BALANCE;
	public static String DEFAULT_BANK;
	public static String CURRENCY_SIGN;
	public static String THOUSAND_SEPARATOR;
	public static boolean NOTIFY_BALANCE_ON_JOIN;
	public static boolean INVERT_COMMA_DOT;
	public static boolean SHOW_DECIMALS;
	public static boolean SHOW_CENTESIMALS;
	public static boolean SHOW_ITEM_WORTH;
	public static boolean PARTIAL_ACCOUNT_NAME_SEARCH;
	public static boolean ENABLE_CARDS;
	public static boolean ENABLE_ATM_RECIPE;
	public static boolean ENABLE_MOBILE_RECIPE;
	public static int UNLOAD_FREQUENCY;
	public static int MIN_SEARCH_CHARS;
	public static int TRANSFER_CACHE;
	//
	public static String COMMA = ",";
	public static String DOT = ".";
	//
	protected static LinkedHashMap<IDL, Long> EXTERNAL_ITEMS = new LinkedHashMap<>();
	protected static LinkedHashMap<String, Long> EXTERNAL_ITEMS_METAWORTH = new LinkedHashMap<>();
	public static HashMap<Money, JsonMap> MONEY_INIT_CACHE = new HashMap<>();
	public static ArrayList<String> DEFAULT_BANKS;
	protected static final TreeMap<String, Long> DEFAULT_ITEMS = new TreeMap<String, Long>();
	static {
		DEFAULT_ITEMS.put("1cent", 10l);
		DEFAULT_ITEMS.put("2cent", 20l);
		DEFAULT_ITEMS.put("5cent", 50l);
		DEFAULT_ITEMS.put("10cent", 100l);
		DEFAULT_ITEMS.put("20cent", 200l);
		DEFAULT_ITEMS.put("50cent", 500l);
		DEFAULT_ITEMS.put("1foney", 1000l);
		DEFAULT_ITEMS.put("2foney", 2000l);
		DEFAULT_ITEMS.put("5foney", 5000l);
		DEFAULT_ITEMS.put("10foney", 10000l);
		DEFAULT_ITEMS.put("20foney", 20000l);
		DEFAULT_ITEMS.put("50foney", 50000l);
		DEFAULT_ITEMS.put("100foney", 100000l);
		DEFAULT_ITEMS.put("200foney", 200000l);
		DEFAULT_ITEMS.put("500foney", 500000l);
		DEFAULT_ITEMS.put("1000foney", 1000000l);
		DEFAULT_ITEMS.put("2000foney", 2000000l);
		DEFAULT_ITEMS.put("5000foney", 5000000l);
		DEFAULT_ITEMS.put("10000foney", 10000000l);
		DEFAULT_ITEMS.put("20000foney", 20000000l);
		DEFAULT_ITEMS.put("50000foney", 50000000l);
		DEFAULT_ITEMS.put("100kfoney", 100000000l);
		DEFAULT_ITEMS.put("200kfoney", 200000000l);
		DEFAULT_ITEMS.put("500kfoney", 500000000l);
	}

	public Config(File fl){
		super(fl, "Fex's Small Money Mod");
	}

	@Override
	protected void fillInfo(JsonMap map){
		map.add("info", "FSMM 3 Main Configuration File");
		map.add("wiki", "https://fexcraft.net/wiki/mod/fsmm");
	}

	@Override
	protected void fillEntries(){
		String cgen = "general";
		String cdis = "display";
		String creg = "registry";
		entries.add(new ConfigEntry(this, cgen, "starting_balance", 100000).rang(0, 1000000)
			.info("Starting balance for a new player. (1000 units == 1 F$)")
			.cons((con, map) -> STARTING_BALANCE = con.getInteger(map))
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, cgen, "notify_balance_on_join", true)
			.info("Should the player be notified about his current balance when joining?")
			.cons((con, map) -> NOTIFY_BALANCE_ON_JOIN = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cgen, "unload_frequency", 5).rang(3, 60)
			.info("Frequency of how often it should be checked if (temporarily loaded) accounts and banks should be unloaded. Time in minutes.")
			.cons((con, map) -> UNLOAD_FREQUENCY = con.getInteger(map))
			.req(true, true)
		);
		entries.add(new ConfigEntry(this, cgen, "partial_account_name_search", false)
			.info("(1.12 only) If true, accounts can be searched by inputting only part of the name, otherwise on false, the full ID/Name is required. Please note that partial search may use more server resources.")
			.cons((con, map) -> PARTIAL_ACCOUNT_NAME_SEARCH = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cgen, "min_search_chars", 3).rang(1, 128)
			.info("Minimum characters to enter in the 'Name/ID' search bar for search to work.")
			.cons((con, map) -> MIN_SEARCH_CHARS = con.getInteger(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cgen, "transfer_cache", 50).rang(10, 1000)
			.info("Amount of executed transfer's data to be cached per account.")
			.cons((con, map) -> TRANSFER_CACHE = con.getInteger(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cgen, "enable_cards", true)
			.info("Should (Debit) Cards be enabled?")
			.cons((con, map) -> ENABLE_CARDS = con.getBoolean(map))
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, cgen, "enable_atm_recipe", true)
			.info("Should the default ATM recipe be enabled?")
			.cons((con, map) -> ENABLE_ATM_RECIPE = con.getBoolean(map))
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, cgen, "enable_mobile_recipe", true)
			.info("Should the default Mobile Banking Item recipe be enabled?")
			.cons((con, map) -> ENABLE_MOBILE_RECIPE = con.getBoolean(map))
			.req(false, true)
		);
		//
		entries.add(new ConfigEntry(this, creg, "default_bank", "default")
			.info("Default Bank of the Server. New accounts get this Bank assigned. Fallback Bank if a specific Bank was not found.")
			.cons((con, map) -> DEFAULT_BANK = con.getString(map))
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, creg, "default_banks", new JsonArray("default"))
			.info("List of Banks which get loaded into memory each server launch.")
			.cons((con, map) -> DEFAULT_BANKS = con.getJson(map).asArray().toStringList())
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, creg, "internal_items", genDefaultItems())
			.info("List of default FSMM Items. Remove entries from this list if you want to remove FSMM items. If you add new entries, you have to provide assets for them, e.g. via another mod or resourcepack.")
			.cons((con, map) -> {
				JsonMap items = con.getJson(map).asMap();
				for(Map.Entry<String, JsonValue<?>> entry : items.entries()){
					Money money = new Money(entry.getKey(), entry.getValue().long_value());
					DataManager.CURRENCY.put(money.getID(), money);
					MONEY_INIT_CACHE.put(money, new JsonMap("id", money.getID().colon(), "worth", money.getWorth()));
					FSMM.registerItem(money);
				}
			})
			.req(false, true)
		);
		entries.add(new ConfigEntry(this, creg, "external_items", genExternalItems())
			.info("List of non-FSMM items to be regarded as money. Both Vanilla and Modded items are supported. If 'register' is true, the specific item can be withdrawn using the ATM.")
			.cons((con, map) -> {
				JsonArray items = con.getJson(map).asArray();
				items.value.forEach(elm -> {
					JsonMap jsn = elm.asMap();
					IDL idl = IDLManager.getIDLCached(jsn.get("id").string_value());
					long worth = jsn.get("worth").long_value();
					int meta = jsn.getInteger("meta", -1);
					//
					if(meta >= 0){
						EXTERNAL_ITEMS_METAWORTH.put(idl.colon() + ":" + meta, worth);
						if(!EXTERNAL_ITEMS.containsKey(idl)){
							EXTERNAL_ITEMS.put(idl, 0l);
						}
					}
					else{
						EXTERNAL_ITEMS.put(idl, worth);
					}
					if(jsn.has("register") && jsn.get("register").bool()){
						Money money = new Money(jsn);
						DataManager.CURRENCY.put(money.getID(), money);
						MONEY_INIT_CACHE.put(money, jsn);
					}
				});
			})
			.req(false, true)
		);
		//
		entries.add(new ConfigEntry(this, cdis, "currency_sign", "F$")
			.info("Custom Currency Sign")
			.cons((con, map) -> CURRENCY_SIGN = con.getString(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cdis, "invert_comma_dot", false)
			.info("Invert ',' (comma) and '.' (dot) display.")
			.cons((con, map) -> INVERT_COMMA_DOT = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cdis, "show_decimals", true)
			.info("Should decimals be shown when zero? E.g. '234.00'")
			.cons((con, map) -> SHOW_DECIMALS = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cdis, "show_centesimals", false)
			.info("Should centesimals be shown? E.g. '29.503' instead of '29.50'.")
			.cons((con, map) -> SHOW_CENTESIMALS = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cdis, "show_item_worth", true)
			.info("Should the Item's Worth be shown in the tooltip?")
			.cons((con, map) -> SHOW_ITEM_WORTH = con.getBoolean(map))
			.req(false, false)
		);
		entries.add(new ConfigEntry(this, cdis, "thousand_separator", "null")
			.info("Custom thousand separator sign, leave as 'null' for default behaviour.")
			.cons((con, map) -> {
				String str = con.getString(map);
				THOUSAND_SEPARATOR = str.equals("null") ? null : str;
			})
			.req(false, false)
		);
	}

	private JsonMap genDefaultItems(){
		JsonMap map = new JsonMap();
		DEFAULT_ITEMS.forEach((id, worth) -> map.add(id, worth));
		return map;
	}

	private JsonArray genExternalItems(){
		JsonArray arr = new JsonArray();
		JsonMap ext = new JsonMap();
		ext.add("id", "minecraft:nether_star");
		ext.add("worth", 100000);
		ext.add("register", false);
		arr.add(ext);
		return arr;
	}

	@Override
	protected void onReload(JsonMap map){
		COMMA = INVERT_COMMA_DOT ? "." : ",";
		DOT = INVERT_COMMA_DOT ? "," : ".";
	}

	public static final String getWorthAsString(long value){
		return getWorthAsString(value, true, false);
	}

	public static final String getWorthAsString(long value, boolean append){
		return getWorthAsString(value, append, false);
	}

	public static final String getWorthAsString(long value, boolean append, boolean ignore){
		String str = value + "";
		if(value < 1000){
			if(!SHOW_DECIMALS && (value == 0 || (!SHOW_CENTESIMALS && !ignore && value < 100))) return "0" + (append ? CURRENCY_SIGN : "");
			str = value + "";
			str = str.length() == 1 ? "00" + str : str.length() == 2 ? "0" + str : str;
			return ((str = "0" + COMMA + str).length() == 5 && (ignore ? false : !SHOW_CENTESIMALS) ? str.substring(0, 4) : str) + (append ? CURRENCY_SIGN : "");
		}
		else{
			try{
				str = new StringBuilder(str).reverse().toString();
				String[] arr = str.split("(?<=\\G...)");
				str = arr[0] + COMMA;
				for(int i = 1; i < arr.length; i++){
					str += arr[i] + ((i >= arr.length - 1) ? "" : THOUSAND_SEPARATOR == null ? DOT : THOUSAND_SEPARATOR);
				}
				str = new StringBuilder(str).reverse().toString();
				return (str = SHOW_DECIMALS ? SHOW_CENTESIMALS || ignore ? str : str.substring(0, str.length() - 1) : str.substring(0, str.lastIndexOf(COMMA))) + (append ? CURRENCY_SIGN : "");
			}
			catch(Exception e){
				e.printStackTrace();
				return str + "ERR";
			}
		}
	}

	public static final long getStackWorth(Object stack){
		return getStackWorth(UniStack.getStack(stack));
	}

	public static final long getStackWorth(StackWrapper stack){
		if(stack.getItem().direct() instanceof Money.Item){
			return ((Money.Item)stack.getItem().direct()).getWorth();
		}
		if(EXTERNAL_ITEMS_METAWORTH.containsKey(stack.getID() + ":" + stack.damage())){
			return EXTERNAL_ITEMS_METAWORTH.get(stack.getID() + ":" + stack.damage());
		}
		if(EXTERNAL_ITEMS.containsKey(stack.getIDL())){
			return EXTERNAL_ITEMS.get(stack.getIDL());
		}
		return 0;
	}

	public static boolean containsAsExternalItemStack(StackWrapper stack){
		try{
			return EXTERNAL_ITEMS.containsKey(stack.getID())
				|| EXTERNAL_ITEMS_METAWORTH.containsKey(stack.getID() + ":" + stack.damage());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}
