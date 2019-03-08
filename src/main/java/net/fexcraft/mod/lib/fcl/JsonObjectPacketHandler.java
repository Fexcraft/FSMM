package net.fexcraft.mod.lib.fcl;

import java.util.HashMap;


import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import net.fexcraft.mod.fsmm.util.Print;
import net.minecraft.client.Minecraft;

public class JsonObjectPacketHandler{

    private static HashMap<String, IPacketListener<PacketJsonObject>> sls = new HashMap<String, IPacketListener<PacketJsonObject>>();
    private static HashMap<String, IPacketListener<PacketJsonObject>> cls = new HashMap<String, IPacketListener<PacketJsonObject>>();

    public static class Server implements IMessageHandler<PacketJsonObject, IMessage> {
        @Override
        public IMessage onMessage(final PacketJsonObject packet, final MessageContext ctx) {
            if(!packet.obj.has("target_listener")){
                Print.log("[FCL] Received JSON Packet, but had no target listener, ignoring!");
                Print.log("[OBJ] " + packet.obj.toString());
                return null;
            }
            IPacketListener<PacketJsonObject> listener = sls.get(packet.obj.get("target_listener").getAsString());
            if(listener != null){
                listener.process(packet, new Object[]{ctx.getServerHandler().playerEntity});
            }
            return null;
        }
    }

    public static class Client implements IMessageHandler<PacketJsonObject, IMessage> {
        @Override
        public IMessage onMessage(final PacketJsonObject packet, final MessageContext ctx) {
            if(!packet.obj.has("target_listener")){
                Print.log("[FCL] Received JSON Packet, but had no target listener, ignoring!");
                Print.log("[OBJ] " + packet.obj.toString());
                return null;
            }
            IPacketListener<PacketJsonObject> listener = cls.get(packet.obj.get("target_listener").getAsString());
            if(listener != null){
                listener.process(packet, new Object[]{Minecraft.getMinecraft().thePlayer});
            }
            return null;
        }
    }

    public static void addListener(Side side, IPacketListener<PacketJsonObject> listener){
        if(side.isClient()){
            cls.put(listener.getId(), listener);
        }
        else{
            sls.put(listener.getId(), listener);
        }
    }

}