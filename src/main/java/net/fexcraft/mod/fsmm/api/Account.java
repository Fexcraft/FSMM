package net.fexcraft.mod.fsmm.api;

import java.util.UUID;

import net.minecraft.command.ICommandSender;

public interface Account {
	
	public String getId();
	
	public long getBalance();
	
	public boolean modifyBalance(String action, long amount, ICommandSender sender);
	
	public UUID getBankId();
	
	public boolean setBankId(UUID uuid);
	
	public String getType();
	
}