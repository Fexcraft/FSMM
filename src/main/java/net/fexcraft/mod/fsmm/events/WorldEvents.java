package net.fexcraft.mod.fsmm.events;

import java.io.File;
import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.AccountPermission;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(WorldEvent.Load event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
		FSMM.loadDataManager();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(WorldEvent.Unload event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
		FSMM.unloadDataManager();
	}
	
	@SubscribeEvent
	public static void onGatherAccounts(ATMEvent.GatherAccounts event){
		event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true, true));
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSearchAccounts(ATMEvent.SearchAccounts event){
		String id = null;
		if(!event.getSearchedType().equals("player")){
			if(!contains(event.getAccountsMap(), event.getSearchedType()) && DataManager.exists(event.getSearchedType(), event.getSearchedId())){
				id = event.getSearchedType() + ":" + event.getSearchedId();
				event.getAccountsMap().put(id, new AccountPermission(id));
			}
			return;
		}
		for(Account account : DataManager.getAccountsOfType("player").values()){
			if(account.getId().contains(event.getSearchedId()) || account.getName().contains(event.getSearchedId())){
				event.getAccountsMap().put(account.getTypeAndId(), new AccountPermission(account));
			}
		}
		for(String str : Static.getServer().getPlayerProfileCache().getUsernames()){
			if(str.contains(event.getSearchedId()) && !event.getAccountsMap().containsKey("player:" + str)){
				GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(str);
				id = "player:" + gp.getId().toString();
				event.getAccountsMap().put(id, new AccountPermission(id));
			}
		}
		File folder = new File(DataManager.ACCOUNT_DIR, "player/");
		if(!folder.exists()) return;
		String str = null;
		for(File file : folder.listFiles()){
			if(file.isDirectory() || file.isHidden()) continue;
			if(file.getName().endsWith(".json") && (str = file.getName().substring(0, file.getName().length() - 5)).toLowerCase().contains(event.getSearchedId())){
				if(!event.getAccountsMap().containsKey("player:" + str)){
					event.getAccountsMap().put(id = "player:" + str, new AccountPermission(id));
				}
			}
		}
	}

	/** Checking if another mod returned already anything for this type. */
	private static boolean contains(HashMap<String, AccountPermission> map, String type){
		for(AccountPermission perm : map.values()){
			if(perm.getType().equals(type)) return true;
		}
		return false;
	}
	
}
