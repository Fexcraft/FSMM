package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.minecraft.command.ICommandSender;

/**
 * Internal Usage Class, do not bother with.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */

public interface Manageable {
	
	public void modifyBalance(Action action, long amount, ICommandSender log);
	
	public static enum Action {
		ADD, SUB, SET;
	}

	public JsonMap toJson();
	
}