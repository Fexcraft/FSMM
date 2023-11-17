package net.fexcraft.mod.fsmm.events;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(event.player.world.isRemote){ return; }
		Print.debug("Loading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = DataManager.getAccount("player:" + event.player.getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	}
    	if(account.lastAccessed() >= 0){ account.setTemporary(false); }
    	//
    	NBTTagCompound compound = Config.LOCAL.toNBT();
    	compound.setString("payload", "config_sync");
		compound.setString("target_listener", "fsmm:atm_gui");
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(compound), (EntityPlayerMP)event.player);
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.player.getGameProfile().getId().toString());
    }
    
    @SideOnly(Side.CLIENT) @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP) return;
		long worth = Config.getItemStackWorth(event.getItemStack());
		if(worth <= 0) return;
		String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
		if(event.getItemStack().getCount() > 1){
			str += " &8(&7" + Config.getWorthAsString(worth * event.getItemStack().getCount(), true, worth < 10) + "&8)";
		}
		event.getToolTip().add(Formatter.format(str));
    }
    
}