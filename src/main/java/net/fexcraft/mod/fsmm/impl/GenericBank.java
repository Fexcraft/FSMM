package net.fexcraft.mod.fsmm.impl;

import java.util.TreeMap;

import com.google.gson.JsonObject;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Manageable;
import net.fexcraft.mod.fsmm.api.Transfer;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class GenericBank extends Bank {

	public GenericBank(JsonObject obj){
		super(obj);
	}
	
	public GenericBank(String id, String name, long balance, JsonObject data, TreeMap<String, String> map){
		super(id, name, balance, data, map);
	}
	
	public static long parseFee(String fee, long amount){
		if(fee == null){
			return 0;
		}
		long result = 0;
		if(fee.endsWith("%")){
			float pc = Float.parseFloat(fee.replace("%", ""));
			if(pc < 0) return 0;
			if(pc > 100) pc = 100;
			result = (long)((amount / 100) * pc);
		}
		else{
			result = Long.parseLong(fee);
			result = result < 0 ? 0 : /*result > amount ? amount :*/ result;
		}
		return result;
	}

	@Override
	public boolean processAction(Bank.Action action, ICommandSender log, Account sender, long amount, Account receiver, boolean included){
		EntityPlayer player;
		long fee = 0, total;
		switch(action){
			case WITHDRAW:{
				if(sender == null){
					Print.chat(log, "Withdraw failed! Account is null.");
					Print.debug(getName(log) + " -> player account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Withdraw failed! Amount is null or negative. (T:" + amount + " || B:" + sender.getBalance() + ");");
					Print.debug(getName(log) + " tried to withdraw a negative amount of money!");
					return false;
				}
				player = (EntityPlayer)log;
				if(fees != null){
					String feestr = fees.get(sender.getType() + ":self");
					fee = parseFee(feestr, amount);
				}
				total = amount + (included ? 0 : fee);
				if(sender.getBalance() - total >= 0){
					sender.modifyBalance(Manageable.Action.SUB, total, player);
					ItemManager.addToInventory(player, amount - (included ? fee : 0));
					log(player, action, amount, fee, total, included, sender, receiver);
					return true;
				}
				Print.chat(player, "Withdraw failed! Not enough money. (W:" + amount + " || B:" + sender.getBalance() + ");");
				Print.debug(sender.getAsResourceLocation().toString() + " : Withdraw failed! Player does not have enough money. (T:" + amount + " || F:" + fee + ");");
				return false;
			}
			case DEPOSIT:{
				if(receiver == null){
					Print.chat(log, "Deposit failed! Account is null.");
					Print.debug(getName(log) + " -> player account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Deposit failed! Amount null or negative. (T:" + amount + " || I:" + ItemManager.countInInventory(log) + ");");
					Print.debug(getName(log) + " tried to deposit a negative amount of money!");
					return false;
				}
				player = (EntityPlayer)log;
				if(receiver.getBalance() + amount <= Long.MAX_VALUE){
					fee = fees == null ? 0 : parseFee(fees.get("self:" + receiver.getType()), amount);
					total = amount + (included ? 0 : fee);
					if(ItemManager.countInInventory(player) - total >= 0){
						ItemManager.removeFromInventory(player, total);
						receiver.modifyBalance(Manageable.Action.ADD, amount - (included ? fee : 0), player);
						log(player, action, amount, fee, total, included, sender, receiver);
						return true;
					}
					else{
						Print.chat(player, "Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + receiver.getBalance() + ");");
						Print.log(receiver.getAsResourceLocation().toString() + ": Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + receiver.getBalance() + ");");
						return false;
					}
				}
				Print.chat(player, "Deposit failed! Result is above technical limit. (D:" + amount + " || B:" + receiver.getBalance() + ");");
				Print.log(receiver.getAsResourceLocation().toString() + " : Deposit failed! Result is above technical limit. (D:" + amount + " || B:" + receiver.getBalance() + ");");
				return false;
			}
			case TRANSFER:{
				if(sender == null){
					Print.chat(log, "Transfer failed! Sender is null.");
					Print.debug(getName(log) + " -> sender account is null.");
					return false;
				}
				if(receiver == null){
					Print.chat(log, "Transfer failed! Receiver is null.");
					Print.debug(getName(log) + " -> receiver account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Transfer failed! Amount is null or negative. (T:" + amount + ");");
					Print.debug(getName(log) + " tried to transfer a negative amount of money to " + receiver.getAsResourceLocation().toString() + "!");
					return false;
				}
				fee = fees == null ? 0 : parseFee(fees.get(sender.getType() + ":" + receiver.getType()), amount);
				total = amount + (included ? 0 : fee);
				if(sender.getBalance() - total >= 0){
					sender.modifyBalance(Manageable.Action.SUB, total, log);
					receiver.modifyBalance(Manageable.Action.ADD, amount - (included ? fee : 0), log);
					log(null, action, amount, fee, total, included, sender, receiver);
					return true;
				}
				Print.chat(log, "Transfer failed! Not enough money on sender Account.");
				Print.debug(sender.getAsResourceLocation().toString() + " -> " + sender.getAsResourceLocation().toString() + " : Transfer failed! Sender doesn't have enough money. (T:" + amount + " || F:" + fee + ");");
				return false;
			}
			default:{
				Print.chat(log, "Invalid Bank Action. " + action.name() + " || " + getName(log) + " || "
					+ (sender == null ? "null" : sender.getAsResourceLocation().toString()) + " || " + amount + " || " + (receiver == null ? "null" : receiver.getAsResourceLocation().toString()));
				return false;
			}
		}
	}

	private void log(EntityPlayer player, Action action, long amount, long fee, long total, boolean included, Account sender, Account receiver){
		String s, r;
		switch(action){
			case DEPOSIT:
				s = sender.getTypeAndId();
				r = player.getName();
				sender.addTransfer(new Transfer(amount, fee, included, action, sender.getName(), sender));
				break;
			case WITHDRAW:
				s = player.getName();
				r = receiver.getTypeAndId();
				sender.addTransfer(new Transfer(-amount, fee, included, action, sender.getName(), sender));
				break;
			case TRANSFER:
				s = sender.getTypeAndId();
				r = receiver.getTypeAndId();
				sender.addTransfer(new Transfer(-amount, fee, included, action, sender.getName(), receiver));
				receiver.addTransfer(new Transfer(amount, fee, included, action, sender.getName(), sender));
				break;
			default:
				s = "INVALID";
				r = "ACTION";
				break;
		}
		String str = s + " -> [A: " + amount + "] + [F: " + fee + (included ? "i" : "e") + "] == [R: " + total + "] -> " + r;
		FSMM.LOGGER.info(str);
		Print.debug(str);
		
	}
	
	public String getName(ICommandSender sender){
		return sender == null ? "[NULL]" : sender.getName();
	}

	@Override
	public boolean isNull(){
		return false;
	}
	
}
