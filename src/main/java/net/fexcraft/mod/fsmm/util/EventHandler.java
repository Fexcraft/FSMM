package net.fexcraft.mod.fsmm.util;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.fexcraft.mod.fcl.Formatter;
import net.fexcraft.mod.fsmm.api.Account;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class EventHandler {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(UpdateHandler.Status != null){
        	event.player.addChatComponentMessage(new ChatComponentText(Formatter.format(UpdateHandler.Status)));
    	}
    	if(event.player.worldObj.isRemote){ return; }
		Print.debug("Loading account of " + event.player.getDisplayName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = DataManager.getAccount("player:" + event.player.getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN && account!=null){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	} else if (Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance could not be loaded");
		}
    	if(account.lastAccessed() >= 0){ account.setTemporary(false); }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getDisplayName() + " || " + event.player.getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.player.getGameProfile().getId().toString());
    }
    
    @SideOnly(Side.CLIENT) @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP || FSMMCapabilities.MONEY_ITEMSTACK == null){ return; }
    	if(event.itemStack.hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null)){
    		long worth = event.itemStack.getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null).getWorth();
    		String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
    		if(event.itemStack.stackSize > 1){
    			str += " &8(&7" + Config.getWorthAsString(worth * event.itemStack.stackSize, true, worth < 10) + "&8)";
    		}
    		event.toolTip.add(Formatter.format(str));
    	}
    }
    
    /*@SubscribeEvent
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
    }*/
    
}