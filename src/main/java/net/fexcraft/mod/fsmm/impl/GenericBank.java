package net.fexcraft.mod.fsmm.impl;

import java.util.TreeMap;

import com.google.gson.JsonObject;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Manageable;
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
			result = (long)(pc < 0 ? 0 : pc > 100 ? 100 : (amount / 100) * pc);
		}
		else{
			result = Long.parseLong(fee);
			result = result < 0 ? 0 : result > amount ? amount : result;
		}
		return result;
	}

	@Override
	public boolean processAction(Bank.Action action, ICommandSender log, Account sender, long amount, Account receiver){
		EntityPlayer player; long fee = 0;
		switch(action){
			case WITHDRAW:{
				if(sender == null){
					Print.chat(log, "Withdraw failed! Account is null.");
					Print.debug(log.getName() + " -> player account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Withdraw failed! Amount null or negative. (T:" + amount + " || B:" + sender.getBalance() + ");");
					Print.debug(log.getName() + " tried to withdraw a negative amout of money!");
					return false;
				}
				player = (EntityPlayer)log;
				if(fees != null){
					String feestr = fees.get("player:" + (sender.getId().equals(player.getGameProfile().getId().toString()) ? "self" : sender.getType()));
					fee = parseFee(feestr, amount);
				}
				if(sender.getBalance() - amount >= 0){
					sender.modifyBalance(Manageable.Action.SUB, amount, player);
					ItemManager.addToInventory(player, amount - fee);
					String str = sender.getAsResourceLocation().toString() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + player.getName() + ";";
					Print.debug(str); FSMM.LOGGER.info(str);
					return true;
				}
				Print.chat(player, "Withdraw failed! Not enough money. (W:" + amount + " || B:" + sender.getBalance() + ");");
				Print.debug(sender.getAsResourceLocation().toString() + " : Withdraw failed! Player does not have enough money. (T:" + amount + " || F:" + fee + ");");
				return false;
			}
			case DEPOSIT:{
				if(receiver == null){
					Print.chat(log, "Deposit failed! Account is null.");
					Print.debug(log.getName() + " -> player account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Deposit failed! Amount null or negative. (T:" + amount + " || I:" + ItemManager.countInInventory(log) + ");");
					Print.debug(log.getName() + " tried to deposit a negative amout of money!");
					return false;
				}
				player = (EntityPlayer)log;
				if(receiver.getBalance() + amount <= Long.MAX_VALUE){
					if(ItemManager.countInInventory(player) - amount >= 0){
						fee = fees == null ? 0 : parseFee(fees.get("self:" + receiver.getType()), amount);
						ItemManager.removeFromInventory(player, amount);
						receiver.modifyBalance(Manageable.Action.ADD, amount - fee, player);
						String str = player.getName() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + receiver.getAsResourceLocation().toString() + ";";
						Print.debug(str); FSMM.LOGGER.info(str);
						return true;
					}
					else{
						Print.chat(player, "Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + receiver.getBalance() + ");");
						Print.log(receiver.getAsResourceLocation().toString() + ": Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + receiver.getBalance() + ");");
						return false;
					}
				}
				Print.chat(player, "Deposit failed! Result is above limit.. (D:" + amount + " || B:" + receiver.getBalance() + ");");
				Print.log(receiver.getAsResourceLocation().toString() + " : Deposit failed! Result is above limit. (D:" + amount + " || B:" + receiver.getBalance() + ");");
				return false;
			}
			case TRANSFER:{
				if(sender == null){
					Print.chat(log, "Transfer failed! Sender is null.");
					Print.debug(log.getName() + " -> sender account is null.");
					return false;
				}
				if(receiver == null){
					Print.chat(log, "Transfer failed! Receiver is null.");
					Print.debug(log.getName() + " -> receiver account is null.");
					return false;
				}
				if(amount <= 0){
					Print.chat(log, "Transfer failed! Amount null or negative. (T:" + amount + ");");
					Print.debug(log.getName() + " tried to transfer a negative amout of money to " + receiver.getAsResourceLocation().toString() + "!");
					return false;
				}
				fee = fees == null ? 0 : parseFee(fees.get(sender.getType() + ":" + receiver.getType()), amount);
				if(sender.getBalance() - amount >= 0){
					sender.modifyBalance(Manageable.Action.SUB, amount, log);
					receiver.modifyBalance(Manageable.Action.ADD, amount - fee, log);
					String str = sender.getAsResourceLocation().toString() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + receiver.getAsResourceLocation().toString() + ";";
					Print.debug(str); FSMM.LOGGER.info(str);
					return true;
				}
				Print.chat(log, "Transfer failed! Not enough money on your Account.");
				Print.debug(sender.getAsResourceLocation().toString() + " -> " + sender.getAsResourceLocation().toString() + " : Transfer failed! Sender don't has enough money. (T:" + amount + " || F:" + fee + ");");
				return false;
			}
			default:{
				Print.chat(log, "Invalid Bank Action. " + action.name() + " || " + log.getName() + " || "
					+ (sender == null ? "null" : sender.getAsResourceLocation().toString()) + " || " + amount + " || " + (receiver == null ? "null" : receiver.getAsResourceLocation().toString()));
				return false;
			}
		}
	}

	@Override
	public boolean isNull(){
		return false;
	}
	
}
