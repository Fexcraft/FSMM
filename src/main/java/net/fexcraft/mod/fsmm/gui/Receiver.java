package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.Config.SyncableConfig;
import net.minecraft.nbt.NBTTagList;

public class Receiver implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "fsmm:atm_gui";
	}
	
	@Override
	public void process(PacketNBTTagCompound pkt, Object[] objs){
		Print.debug(pkt.nbt);
		if(pkt.nbt.hasKey("payload")){
			switch(pkt.nbt.getString("payload")){
				case "main_data":{
					AutomatedTellerMashineGui.INSTANCE.openPerspective("main", pkt.nbt);
					break;
				}
				case "show_balance":{
					AutomatedTellerMashineGui.INSTANCE.openPerspective("show_balance", pkt.nbt);
					break;
				}
				case "deposit_result":
				case "withdraw_result":{
					AutomatedTellerMashineGui.INSTANCE.openPerspective(pkt.nbt.getString("payload"), pkt.nbt);
					break;
				}
				case "account_types":{
					AutomatedTellerMashineGui.INSTANCE.catlist = (NBTTagList)pkt.nbt.getTag("types");
					break;
				}
				case "accounts_of_type":{
					AutomatedTellerMashineGui.INSTANCE.idlist = (NBTTagList)pkt.nbt.getTag("accounts");
					break;
				}
				case "transfer_result":{
					AutomatedTellerMashineGui.INSTANCE.openPerspective(pkt.nbt.getString("payload"), pkt.nbt);
					break;
				}
				case "config_sync":{
					Config.REMOTE = SyncableConfig.fromNBT(pkt.nbt); Config.REMOTE.apply();
					Print.log("[FSMM] Received Config Sync Packet from Server."); break;
				}
			}
		}
	}
	
}