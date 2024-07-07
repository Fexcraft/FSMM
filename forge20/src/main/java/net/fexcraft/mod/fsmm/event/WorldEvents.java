package net.fexcraft.mod.fsmm.event;

import net.fexcraft.mod.fsmm.FSMM;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "fsmm", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(LevelEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		FSMM.loadDataManager();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(LevelEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		FSMM.unloadDataManager();
	}
	
}
