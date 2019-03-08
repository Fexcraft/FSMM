package net.fexcraft.mod.fsmm.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ChatComponentText;

public class Print {

    private static Boolean dev;

    public static boolean dev(){
        if(dev == null){
            dev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        }
        return dev;
    }

    public static void chat(ICommandSender p, String s){
        if(p instanceof EntityPlayer) {
            ((EntityPlayer)p).addChatComponentMessage(new ChatComponentText(s));
        } else {
            debug(p.getCommandSenderName() + "was not an instance of entity player");
        }
    }


    public static void debug(String ... s){
        if(dev()){
            System.out.println("------------------FSMM Debug------------------");
            System.out.println(Thread.currentThread().getStackTrace()[2]);//print what function just called
            for(String str : s) {
                System.out.println(str);
            }
            System.out.println("------------------FSMM Debug------------------");
        }
    }


    public static void log(String s){
        debug(s);
    }

    public static void console(String s){
        System.out.println("------------------FSMM Debug------------------");
        System.out.println(Thread.currentThread().getStackTrace()[2]);//print what function just called this
        System.out.println(s);
        System.out.println("------------------FSMM Debug------------------");
    }
}
