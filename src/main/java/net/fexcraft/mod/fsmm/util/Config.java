package net.fexcraft.mod.fsmm.util;

import java.io.File;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.impl.GenericMoney;
import net.fexcraft.mod.fsmm.impl.GenericMoneyItem;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.registry.RegistryUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {
	
	public static File CONFIG_PATH;
	public static long STARTING_BALANCE;
	public static UUID DEFAULT_BANK;
	public static boolean DEBUG;
	private static Configuration config;
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
		config = new Configuration(new File(event.getSuggestedConfigurationFile().getParentFile(), "/fsmm/config.cfg"), "2", true);
		resync(true);
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
			//TODO
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
		//TODO banks
		return obj;
	}

	public static void resync(){
		resync(false);
	}
	
	private static void resync(boolean b){
		if(b){
			config.setCategoryRequiresMcRestart("General", true);
			config.setCategoryRequiresWorldRestart("General", true);
			config.setCategoryRequiresMcRestart("Logging", false);
			config.setCategoryRequiresWorldRestart("Logging", false);
			config.load();
		}
		STARTING_BALANCE = config.getInt("starting_balance", "General", 100000, 0, Integer.MAX_VALUE, "Starting balance for a new player. (1000 == 1F$)");
		DEFAULT_BANK = UUID.fromString(config.getString("default_bank", "General", "00000000-0000-0000-0000-000000000000", "Default Bank the player will have an account in.\nMust be an valid UUID!"));
		DEBUG = config.getBoolean("debug", "Logging", false, "Prints some maybe useful data into console, suggested for bug-hunting.");
		config.save();
	}
	
	private static class EventHandler {
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
			if(event.getModID().equals("fsmm")){
				resync();
			}
		}
	}

	public static Configuration getConfig(){
		return config;
	}
	
}