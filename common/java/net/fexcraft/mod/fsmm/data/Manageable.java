package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.uni.world.MessageSender;

/**
 * Internal Usage Class.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */

public interface Manageable {
	
	public void modifyBalance(Action action, long amount, MessageSender log);
	
	public static enum Action {
		ADD, SUB, SET;
	}

	public JsonMap toJson();
	
}