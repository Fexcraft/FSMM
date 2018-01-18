package net.fexcraft.mod.fsmm.api;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public interface Bank {
	
	public UUID getId();
	
	public String getName();
	
	public boolean processTransfer(ICommandSender ics, Account sender, long amount, Account receiver);

	public boolean processWithdraw(EntityPlayer player, Account account, long amount);
	
	public boolean processDeposit(EntityPlayer player, Account account, long amount);
	
	public JsonObject getData();
	
	public void setData(JsonObject obj);
	
	public long getBalance();
	
	public boolean modifyBalance(String action, long amount, ICommandSender sender);
	
}