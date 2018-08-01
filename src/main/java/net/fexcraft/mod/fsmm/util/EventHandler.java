package net.fexcraft.mod.fsmm.util;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.api.WorldCapability;
import net.fexcraft.mod.fsmm.impl.cap.MoneyCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.PlayerCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.WorldCapabilityUtil;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
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
    	Account account = event.player.world.getCapability(FSMMCapabilities.WORLD, null).getAccount("player:" + event.player.getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	}
    	if(account.lastAccessed() >= 0){ account.setTemporary(false); }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.player.getGameProfile().getId().toString());
    }
    
    @SideOnly(Side.CLIENT) @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP || FSMMCapabilities.MONEY_ITEMSTACK == null){ return; }
    	if(event.getItemStack().hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null)){
    		long worth = event.getItemStack().getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null).getWorth();
    		String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
    		if(event.getItemStack().getCount() > 1){
    			str += " &8(&7" + Config.getWorthAsString(worth * event.getItemStack().getCount(), true, worth < 10) + "&8)";
    		}
    		event.getToolTip().add(Formatter.format(str));
    	}
    }
    
    @SubscribeEvent
    public void onAttachItemStackCapabilities(AttachCapabilitiesEvent<net.minecraft.item.ItemStack> event){
    	if(FSMMCapabilities.MONEY_ITEMSTACK != null && (event.getObject().getItem() instanceof Money.Item || Config.containsAsExternalItemStack(event.getObject()))){
    		event.addCapability(MoneyCapability.REGISTRY_NAME, new MoneyCapabilityUtil(event.getObject()));
    	}
    }
    
    @SubscribeEvent
    public void onAttachWorldCapabilities(AttachCapabilitiesEvent<net.minecraft.world.World> event){
    	if(FSMMCapabilities.WORLD != null && event.getObject() != null){
    		event.addCapability(WorldCapability.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
    	}
    }
    
    @SubscribeEvent
    public void onAttachEntityCapabilities(AttachCapabilitiesEvent<net.minecraft.entity.Entity> event){
    	if(FSMMCapabilities.PLAYER != null && event.getObject() instanceof EntityPlayer){
    		event.addCapability(PlayerCapability.REGISTRY_NAME, new PlayerCapabilityUtil((EntityPlayer)event.getObject()));
    	}
    }
    
}