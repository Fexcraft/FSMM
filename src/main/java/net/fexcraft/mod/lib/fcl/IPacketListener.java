package net.fexcraft.mod.lib.fcl;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public interface IPacketListener <PACKET extends IMessage> {

    public String getId();

    public void process(PACKET packet, Object[] objs);

}
