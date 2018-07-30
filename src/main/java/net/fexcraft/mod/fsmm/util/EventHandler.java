package net.fexcraft.mod.fsmm.util;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.api.WorldCapability;
import net.fexcraft.mod.fsmm.impl.cap.MoneyCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.WorldCapabilityUtil;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(UpdateHandler.Status != null){
        	event.player.sendMessage(new TextComponentString(Formatter.format(UpdateHandler.Status)));
    	}
    	if(event.player.world.isRemote){ return; }
		Print.debug("Loading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = event.player.world.getCapability(WorldCapability.CAPABILITY, null).getAccount("player:" + event.player.getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.player.getGameProfile().getId().toString());
    }
    
    @SideOnly(Side.CLIENT) @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP || MoneyCapability.CAPABILITY == null){ return; }
    	if(event.getItemStack().hasCapability(MoneyCapability.CAPABILITY, null)){
    		long worth = event.getItemStack().getCapability(MoneyCapability.CAPABILITY, null).getWorth();
    		String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
    		if(event.getItemStack().getCount() > 1){
    			str += " &8(&7" + Config.getWorthAsString(worth * event.getItemStack().getCount(), true, worth < 10) + "&8)";
    		}
    		event.getToolTip().add(Formatter.format(str));
    	}
    }
    
    @SubscribeEvent
    public void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event){
    	if(MoneyCapability.CAPABILITY != null && (event.getObject().getItem() instanceof Money.Item || Config.containsAsExternalItemStack(event.getObject()))){
    		event.addCapability(MoneyCapability.REGISTRY_NAME, new MoneyCapabilityUtil(event.getObject()));
    	}
    }
    
    @SubscribeEvent
    public void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event){
    	if(WorldCapability.CAPABILITY != null && event.getObject() != null){
    		event.addCapability(MoneyCapability.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
    	}
    }
    
}