package net.fexcraft.mod.fsmm.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface FSMMSubCommand {

	public static boolean register(String arg, FSMMSubCommand command){
		return Command.SUB_CMDS.put(arg, command) != null;
	}

	public void process(MinecraftServer server, ICommandSender sender, String[] args);

	public default void printHelp(ICommandSender sender){}

	public default void printVersion(ICommandSender sender){}

	public default void printStatus(ICommandSender sender){}

}