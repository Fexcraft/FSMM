package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.impl.GenericMoney;
import net.fexcraft.mod.fsmm.impl.GenericMoneyItem;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class Config {
	
	public static File CONFIG_PATH;
	public static int STARTING_BALANCE, UNLOAD_FREQUENCY;
	public static String DEFAULT_BANK;
	public static boolean NOTIFY_BALANCE_ON_JOIN, INVERT_COMMA, SHOW_CENTESIMALS, ENABLE_BANK_CARDS;
	public static boolean SHOW_ITEM_WORTH_IN_TOOLTIP = true;
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
	private static TreeMap<String, Long> EXTERNAL_ITEMS = new TreeMap<String, Long>();
	private static TreeMap<String, Long> EXTERNAL_ITEMS_METAWORTH = new TreeMap<String, Long>();
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(new File(event.getSuggestedConfigurationFile().getParentFile(), "/fsmm/config.cfg"), "2.1", true);
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
		File file = new File(CONFIG_PATH, "/fsmm/configuration.json");
		if(!file.exists()){
			JsonUtil.write(file, getDefaultContent());
		}
		JsonObject obj = JsonUtil.get(file);
		if(obj.has("Items")){
			obj.get("Items").getAsJsonArray().forEach((elm) -> {
				GenericMoney money = new GenericMoney(elm.getAsJsonObject(), true);
				FSMM.CURRENCY.put(money.getRegistryName().toString(), money); new GenericMoneyItem(money);
				//money.stackload(FCLRegistry.getItem("fsmm:" + money.getRegistryName().getResourcePath()), elm.getAsJsonObject(), true);
			});
		}
		//
		if(obj.has("Banks") && ((file = new File(CONFIG_PATH, "/fsmm/banks/")).exists() ? file.listFiles().length <= 0 : true)){
			obj.get("Banks").getAsJsonArray().forEach((elm) -> {
				String uuid = elm.getAsJsonObject().get("uuid").getAsString();
				if(DataManager.getBanks().containsKey(uuid)){
					DataManager.addBank(new GenericBank(elm.getAsJsonObject()));
				}
				else{
					Print.log("Tried to load bank with ID '" + uuid + "' from config, but there is already a bank with that ID loaded!");
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
		def.addProperty("uuid", DEFAULT_BANK);
		def.addProperty("name", "Default Server Bank");
		def.add("data", new JsonObject());
		banks.add(def);
		obj.add("Banks", banks);
		//
		JsonObject extexp = new JsonObject();
		JsonArray ext = new JsonArray();
		extexp.addProperty("id", "minecraft:nether_star");
		extexp.addProperty("worth", 100000);
		extexp.addProperty("register", false);
		ext.add(extexp);
		obj.add("ExternalItems", ext);
		//
		return obj;
	}
	
	public static void refresh(){
		STARTING_BALANCE = config.getInt("starting_balance", "General", 100000, 0, Integer.MAX_VALUE, "Starting balance for a new player. (1000 == 1F$)");
		DEFAULT_BANK = config.getString("default_bank", "General", "00000000", "Default Bank the player will have an account in.!");
		NOTIFY_BALANCE_ON_JOIN = config.getBoolean("notify_balance_on_join", "Display/Logging", true, "Should the player be notified about his current balance when joining the game?");
		CURRENCY_SIGN = config.getString("currency_sign", "Display/Logging", "F$", "So now you can even set a custom Currency Sign.");
		INVERT_COMMA = config.getBoolean("invert_comma", "Display/Logging", false, "Invert ',' and '.' display.");
		SHOW_CENTESIMALS = config.getBoolean("show_centesimals", "Display/Logging", false, "Should centesimals be shown? E.g. '29,503' instead of '29.50'.");
		SHOW_ITEM_WORTH_IN_TOOLTIP = config.getBoolean("show_item_worth", "Display/Logging", true, "Should the Item's Worth be shown in the tooltip?");
		UNLOAD_FREQUENCY = config.getInt("unload_frequency", "General", 600000, Print.dev() ? 30000 : 60000, 86400000 / 2, "Frequency of how often it should be checked if (temporarily loaded) accounts/banks should be unloaded. Time in milliseconds.");
		//
		COMMA = INVERT_COMMA ? "." : ","; DOT = INVERT_COMMA ? "," : ".";
	}
	
	public static class EventHandler {
		
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
			if(event.modID.equals("fsmm")){ refresh(); if(config.hasChanged()){ config.save(); }}
		}
		
	    //@SubscribeEvent
	    public static void onRegistry(){
			File file = new File(Config.CONFIG_PATH, "/fsmm/configuration.json");
			if(!file.exists()){
				return;
			}
			JsonObject obj = JsonUtil.get(file);
			if(obj.has("ExternalItems")){
				obj.get("ExternalItems").getAsJsonArray().forEach(elm -> {
					JsonObject jsn = elm.getAsJsonObject();
					ResourceLocation rs = new ResourceLocation(jsn.get("id").getAsString());
					long worth = jsn.get("worth").getAsLong();
					int meta = jsn.has("meta") ? jsn.get("meta").getAsInt() : -1;
					//
					if(meta >= 0){
						EXTERNAL_ITEMS_METAWORTH.put(rs.toString() + ":" + meta, worth);
						if(!EXTERNAL_ITEMS.containsKey(rs.toString())){
							EXTERNAL_ITEMS.put(rs.toString(), 0l);
						}
					}
					else{
						EXTERNAL_ITEMS.put(rs.toString(), worth);
					}
					if(jsn.has("register") && jsn.get("register").getAsBoolean()){
						//event.getRegistry().register(new GenericMoney(jsn, false));
						FSMM.CURRENCY.put(rs.toString(), new GenericMoney(jsn, false));
					}
				});
			}
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

	@SuppressWarnings("rawtypes")
	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Config.getConfig().getCategory("General")));
		list.add(new ConfigElement(Config.getConfig().getCategory("Display/Logging")));
		return list;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static final long getItemStackWorth(ItemStack stack){
		if(stack.getItem() instanceof Money.Item){
			return ((Money.Item)stack.getItem()).getWorth(stack);
		}
		if(EXTERNAL_ITEMS_METAWORTH.containsKey(stack.getItem().delegate + ":" + stack.getItemDamage())){
			return EXTERNAL_ITEMS_METAWORTH.get(stack.getItem().delegate + ":" + stack.getItemDamage());
		}
		if(EXTERNAL_ITEMS.containsKey(stack.getItem().delegate)){
			return EXTERNAL_ITEMS.get(stack.getItem().delegate);
		}
		return 0;
	}

	//TODO check this
	public static boolean containsAsExternalItemStack(ItemStack stack){
		try{
			return EXTERNAL_ITEMS.containsKey(stack.getItem().delegate)
				|| EXTERNAL_ITEMS_METAWORTH.containsKey(stack.getItem().delegate + ":" + stack.getItemDamage());
		}
		catch(Exception e){ e.printStackTrace(); return false; }
	}

	public static String getComma(){
		return COMMA;
	}

	public static String getDot(){
		return DOT;
	}
	
}