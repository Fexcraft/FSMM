package net.fexcraft.mod.lib.fcl;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.fexcraft.mod.fsmm.util.Print;

public class PacketHandler {

    private static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel("fsmm");

    public static void init(){
        Print.log("Initialising Packet Handler.");
        instance.registerMessage(JsonObjectPacketHandler.Server.class, PacketJsonObject.class, 1, Side.SERVER);
        instance.registerMessage(JsonObjectPacketHandler.Client.class, PacketJsonObject.class, 2, Side.CLIENT);
        Print.log("Done initialising Packet Handler.");
    }

    public static SimpleNetworkWrapper getInstance(){
        return instance;
    }

}
