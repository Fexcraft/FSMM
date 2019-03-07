package net.fexcraft.mod.fcl;

public interface IPacketListener <PACKET extends IPacket> {

    public String getId();

    public void process(PACKET packet, Object[] objs);

}
