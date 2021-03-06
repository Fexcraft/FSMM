package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.fsmm.FSMM;
import net.minecraft.entity.player.EntityPlayerMP;

public class Processor implements IPacketListener<PacketNBTTagCompound> {
	
	public static final String LISTENERID = "fsmm:atm_gui";

	@Override
	public String getId(){
		return LISTENERID;
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		if(!packet.nbt.hasKey("task")) return;
		EntityPlayerMP player = (EntityPlayerMP)objs[0];
		switch(packet.nbt.getString("task")){
			case "open_gui":{
				int gui = packet.nbt.getInteger("gui");
				int[] args = packet.nbt.hasKey("args") ? packet.nbt.getIntArray("args") : new int[3];
				player.openGui(FSMM.getInstance(), gui, player.world, args[0], args[1], args[2]);
				return;
			}
		}
	}
	
}