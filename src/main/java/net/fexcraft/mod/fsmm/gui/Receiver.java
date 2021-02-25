package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.Config.SyncableConfig;

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
				case "config_sync":{
					Config.REMOTE = SyncableConfig.fromNBT(pkt.nbt);
					Config.REMOTE.apply();
					Print.log("[FSMM] Received Config Sync Packet from Server.");
					break;
				}
			}
		}
	}
	
}