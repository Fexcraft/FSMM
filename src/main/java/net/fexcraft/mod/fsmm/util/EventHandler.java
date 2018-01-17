package net.fexcraft.mod.fsmm.util;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventHandler {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(UpdateHandler.Status != null){
        	event.player.sendMessage(new TextComponentString(Formatter.format(UpdateHandler.Status)));
    	}
    	Account account = AccountManager.INSTANCE.getAccount("player", event.player.getGameProfile().getId().toString(), true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
    	Account account = AccountManager.INSTANCE.getAccount("player", event.player.getGameProfile().getId().toString());
		AccountManager.INSTANCE.unloadAccount(account);
    }
    
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Unload event){
    	AccountManager.INSTANCE.saveAll();
    }
    
    @Mod.EventHandler
    public static void onShutdown(FMLServerStoppingEvent event){
    	AccountManager.INSTANCE.saveAll();
    }
    
}