package net.fexcraft.mod.fsmm.event;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.getFormatted;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "fsmm", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {
	
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
    	if(event.getEntity().level().isClientSide) return;
		FSMM.LOGGER.info("Loading account of " + event.getEntity().getName() + " || " + event.getEntity().getGameProfile().getId().toString());
    	Account account = DataManager.getAccount("player:" + event.getEntity().getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
			event.getEntity().sendSystemMessage(getFormatted("&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance())));
    		event.getEntity().sendSystemMessage(getFormatted("&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.getEntity()))));
    	}
    	if(account.lastAccessed() >= 0) account.setTemporary(false);
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		FSMM.LOGGER.info("Unloading account of " + event.getEntity().getName() + " || " + event.getEntity().getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.getEntity().getGameProfile().getId().toString());
    }
    
}