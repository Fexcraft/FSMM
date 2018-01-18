package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.impl.GenericMoney;
import net.fexcraft.mod.fsmm.impl.GenericMoneyItem;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.registry.RegistryUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {
	
	public static File CONFIG_PATH;
	public static long STARTING_BALANCE;
	public static UUID DEFAULT_BANK;
	public static boolean DEBUG, NOTIFY_BALANCE_ON_JOIN, INVERT_COMMA, SHOW_CENTESIMALS;
	public static String CURRENCY_SIGN;
	private static Configuration config;
	private static String COMMA = ",", DOT = ".";
	//
	private static final TreeMap<String, Long> DEFAULT = new TreeMap<String, Long>();
	static {
		DEFAULT.put("1cent", 10l);
		DEFAULT.put("2cent", 20l);
		DEFAULT.put("5cent", 50l);
		DEFAULT.put("10cent", 100l);
		DEFAULT.put("20cent", 200l);
		DEFAULT.put("50cent", 500l);
		DEFAULT.put("1foney", 1000l);
		DEFAULT.put("2foney", 2000l);
		DEFAULT.put("5foney", 5000l);
		DEFAULT.put("10foney", 10000l);
		DEFAULT.put("20foney", 20000l);
		DEFAULT.put("50foney", 50000l);
		DEFAULT.put("100foney", 100000l);
		DEFAULT.put("200foney", 200000l);
		DEFAULT.put("500foney", 500000l);
		DEFAULT.put("1000foney", 1000000l);
		DEFAULT.put("2000foney", 2000000l);
		DEFAULT.put("5000foney", 5000000l);
		DEFAULT.put("10000foney", 10000000l);
		DEFAULT.put("20000foney", 20000000l);
		DEFAULT.put("50000foney", 50000000l);
		DEFAULT.put("100kfoney", 100000000l);
		DEFAULT.put("200kfoney", 200000000l);
		DEFAULT.put("500kfoney", 500000000l);
	}
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(new File(event.getSuggestedConfigurationFile().getParentFile(), "/fsmm/config.cfg"), "2.0", true);
		config.load();
		config.setCategoryRequiresMcRestart("General", true);
		config.setCategoryRequiresWorldRestart("General", true);
		config.setCategoryComment("General", "General FSMM Settings.");
		config.setCategoryRequiresMcRestart("Display/Logging", false);
		config.setCategoryRequiresWorldRestart("Display/Logging", false);
		config.setCategoryComment("Display/Logging", "Display and Logging Settings.");
		refresh();
		config.save();
		//
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		//
		File file = new File(event.getSuggestedConfigurationFile().getParentFile(), "/fsmm/configuration.json");
		if(!file.exists()){
			JsonUtil.write(file, getDefaultContent());
		}
		JsonObject obj = JsonUtil.get(file);
		if(obj.has("Items")){
			obj.get("Items").getAsJsonArray().forEach((elm) -> {
				GenericMoney money = null;
				FSMM.CURRENCY.register(money = new GenericMoney(elm.getAsJsonObject()));
				RegistryUtil.get("fsmm").addItem(money.getRegistryName().getResourcePath(), new GenericMoneyItem(money), 1, null);
			});
		}
		if(obj.has("Banks")){
			obj.get("Banks").getAsJsonArray().forEach((elm) -> {
				UUID uuid = UUID.fromString(elm.getAsJsonObject().get("uuid").getAsString());
				if(!AccountManager.INSTANCE.getBanks().containsKey(uuid)){
					AccountManager.INSTANCE.getBanks().put(uuid, new GenericBank(uuid, elm.getAsJsonObject()));
				}
				else{
					//TODO fancy explanation
				}
			});
		}
	}
	
	private static JsonObject getDefaultContent(){
		JsonObject obj = new JsonObject();
		JsonArray items = new JsonArray();
		DEFAULT.forEach((id, worth) -> {
			JsonObject jsn = new JsonObject();
			jsn.addProperty("id", id);
			jsn.addProperty("worth", worth);
			items.add(jsn);
		});
		obj.add("Items", items);
		//
		JsonArray banks = new JsonArray();
		JsonObject def = new JsonObject();
		def.addProperty("uuid", DEFAULT_BANK.toString());
		def.addProperty("name", "Default Server Bank");
		def.add("data", new JsonObject());
		banks.add(def);
		obj.add("Banks", banks);
		//
		return obj;
	}
	
	private static void refresh(){
		STARTING_BALANCE = config.getInt("starting_balance", "General", 100000, 0, Integer.MAX_VALUE, "Starting balance for a new player. (1000 == 1F$)");
		DEFAULT_BANK = UUID.fromString(config.getString("default_bank", "General", "00000000-0000-0000-0000-000000000000", "Default Bank the player will have an account in.\nMust be an valid UUID!"));
		DEBUG = config.getBoolean("debug", "Display/Logging", false, "Prints some maybe useful data into console, suggested for bug-hunting.");
		NOTIFY_BALANCE_ON_JOIN = config.getBoolean("notify_balance_on_join", "Display/Logging", true, "Should the player be notified about his current balance when joining the game?");
		CURRENCY_SIGN = config.getString("currency_sign", "Display/Logging", "F$", "So now you can even set a custom Currency Sign.");
		INVERT_COMMA = config.getBoolean("invert_comma", "Display/Logging", false, "Invert ',' and '.' dispplay.");
		SHOW_CENTESIMALS = config.getBoolean("show_centesimals", "Display/Logging", false, "Should centesimals be shown? E.g. '29,503' instead of '29.50'.");
		//
		COMMA = INVERT_COMMA ? "." : ",";
		DOT = INVERT_COMMA ? "," : ".";
		if(Static.dev() != DEBUG){ Static.toggleDebug(); }
	}
	
	private static class EventHandler {
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
			if(event.getModID().equals("fsmm")){ refresh(); if(config.hasChanged()){ config.save(); }}
		}
	}

	public static final Configuration getConfig(){
		return config;
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
					str += arr[i] + ((i >= arr.length - 1) ? "" : DOT);
				}
				str = new StringBuilder(str).reverse().toString();
				return (str = SHOW_CENTESIMALS || ignore ? str : str.substring(0, str.length() - 1)) + (append ? CURRENCY_SIGN : "");
			}
			catch(Exception e){
				e.printStackTrace();
				return value + "ERR";
			}
		}
	}

	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Config.getConfig().getCategory("General")));
		list.add(new ConfigElement(Config.getConfig().getCategory("Display/Logging")));
		return list;
	}
	
}