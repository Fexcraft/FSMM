package net.fexcraft.mod.lib.fcl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.fexcraft.mod.fsmm.util.Print;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class Network{

    private static boolean fcl_version_checked = false;

    /** Checks if connection (to main server) is available. */
    public static boolean isConnected(){
        try{
            URL url = new URL("http://www.fexcraft.net/files/TXT/connection.test");
            url.openConnection().connect();
            return true;
        }
        catch(IOException e){
            return false;
        }
    }

    public static JsonObject getModData(String modid){
        return getModData(modid, null);
    }

    public static JsonObject getModData(String modid, String current_version){
        JsonObject obj = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=requestdata&modid=" + modid);
        if(obj == null){
            return null;
        }
        if(obj.has("blocked_versions") && current_version != null){
            java.util.ArrayList<String> arr = new java.util.ArrayList<String>();
            for(JsonElement elm : obj.get("blocked_versions").getAsJsonArray()){
                arr.add(elm.getAsString());
            }
            java.util.ArrayList<String> array = new java.util.ArrayList<String>();
            for(String s : arr){
                ResourceLocation rs = new ResourceLocation(s);
                if(rs.getResourceDomain().equals("1.7.10")){
                    array.add(rs.getResourcePath());
                }
            }
            for(String s : array){
                if(s.equals(current_version)){
                    Print.log("THIS VERSION OF " + modid.toUpperCase() + " IS BLOCKED/REMOVED;");
                    FMLCommonHandler.instance().exitJava(1, true);
                    break;
                }
            }
        }
        else if(obj.has("blocked_versions") && current_version == null && !fcl_version_checked){
            JsonObject fcl = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=requestdata&modid=fcl");
            java.util.ArrayList<String> arr = new java.util.ArrayList<String>();
            for(JsonElement elm : fcl.get("blocked_versions").getAsJsonArray()){
                arr.add(elm.getAsString());
            }
            java.util.ArrayList<String> array = new ArrayList<String>();
            for(String s : arr){
                ResourceLocation rs = new ResourceLocation(s);
                if(rs.getResourceDomain().equals("1.7.10")){
                    array.add(s);
                }
            }
            for(String s : array){
                if(s.equals(current_version)){
                    Print.log("THIS VERSION OF " + modid.toUpperCase() + " IS BLOCKED/REMOVED;");
                    FMLCommonHandler.instance().exitJava(1, true);
                    break;
                }
            }
            fcl_version_checked = true;
        }
        return obj;
    }

    public static boolean isModRegistered(String modid){
        try{
            JsonObject obj = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=exists&modid=" + modid);
            return obj == null ? false : obj.get("exists").getAsBoolean();
        }
        catch(Exception e){
            return false;
        }
    }

    public static boolean isBanned(UUID id){
        return ServerValidator.isBanned(id);
    }

    public static MinecraftServer getMinecraftServer(){
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public static void initializeValidator(Side side){
        if(side.isServer()){
            ServerValidator.initialize();
        }
        else{
            ClientValidator.initialize();
        }
    }

    public static class ClientValidator {

        public static void initialize(){
            String uuid = net.minecraft.client.Minecraft.getMinecraft().getSession().getPlayerID();
            JsonObject elm = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=blacklist&id=" + uuid);
            if(elm != null && elm.has("unbanned") && !elm.get("unbanned").getAsBoolean()){
                FMLCommonHandler.instance().exitJava(0, true);
            }
        }

    }

    public static class ServerValidator {

        private static final Set<UUID> blist = new TreeSet<UUID>();

        public static void initialize(){
            JsonObject check = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=blacklist&id=server");
            if(check == null){
                Print.log("Couldn't validate Server.");
            }
            if(check != null && check.has("unbanned") && !check.get("unbanned").getAsBoolean()){
                Print.log("ERROR, SERVER IS BLACKLISTED;");
                Print.log("CONTACT FEXCRAFT.NET STAFF IF YOU THINK IS AN ERROR;");
                FMLCommonHandler.instance().exitJava(0, true);
            }
            //
            JsonObject obj = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=blacklist");
            if(obj == null){
                Print.log("Couldn't retrieve BL.");
                return;
            }
            for(JsonElement elm : obj.get("blacklist").getAsJsonArray()){
                try{
                    blist.add(UUID.fromString(elm.getAsString()));
                }
                catch(Exception e){
                    //Print.debug("[BL] Couldn't parse " + elm.toString() + ".");
                }
            }
        }

        public static boolean isBanned(UUID id){
            return blist.contains(id);
        }

    }

    public static void browse(ICommandSender sender, String url){
        Desktop d = Desktop.getDesktop();
        if(Network.isConnected()){
            try{ d.browse(new URI(url)); }
            catch(IOException | URISyntaxException e){
                Print.chat(sender, ChatFormatting.BLACK + "[" + ChatFormatting.DARK_AQUA + "FCL" + ChatFormatting.BLACK + "]" + ChatFormatting.GRAY +  " Error, couldn't open link.");
                e.printStackTrace();
            }
        }
        else{
            Print.chat(sender, ChatFormatting.BLACK + "[" + ChatFormatting.DARK_AQUA + "FCL" + ChatFormatting.BLACK + "]" + ChatFormatting.GRAY + " Error, could not check for connection.");
        }
    }

}
