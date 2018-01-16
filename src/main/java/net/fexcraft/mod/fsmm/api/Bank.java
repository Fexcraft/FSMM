package net.fexcraft.mod.fsmm.api;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public interface Bank {
	
	public UUID getId();
	
	public String getName();
	
	public boolean processTransfer(Account sender, long amount, Account target);

	public boolean processWithdraw(EntityPlayer player, Account account, long amount);
	
	public boolean processDeposit(EntityPlayer player, Account account, long amount);
	
	public void loadBank();
	
	public void saveBank();
	
}