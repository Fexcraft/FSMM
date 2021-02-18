package net.fexcraft.mod.fsmm.events;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.AccountPermission;
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
	
	@SubscribeEvent()
	public static void onGatherAccounts(ATMEvent.GatherAccounts event){
		event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true));
	}
	
}
