package net.fexcraft.mod.fsmm.impl;

import java.util.UUID;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class GenericBank implements Bank {
	
	private UUID uuid;
	private String name;
	private JsonObject data;
	private long balance;
	private Table<String, String, String> prices = TreeBasedTable.create();
	
	public GenericBank(UUID uuid, JsonObject obj){
		this.uuid = uuid;
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Bank");
		data = JsonUtil.getIfExists(obj, "data", new JsonObject()).getAsJsonObject();
		balance = JsonUtil.getIfExists(obj, "balance", 0).longValue();
		if(data.has("prices")){
			data.get("prices").getAsJsonArray().forEach((elm) -> {
				JsonObject jsn = elm.getAsJsonObject();
				String type = jsn.get("type").getAsString();
				String action = jsn.get("action").getAsString();
				String price = jsn.get("fee").getAsString();
				prices.put(type, action, price);
			});
		}
	}

	@Override
	public UUID getId(){
		return uuid;
	}

	@Override
	public String getName(){
		return name;
	}
	
	private long parseFee(String fee, long amount){
		if(fee == null){
			return 0;
		}
		long result = 0;
		if(fee.endsWith("%")){
			byte pc = Byte.parseByte(fee.replace("%", ""));
			result = pc < 0 ? 0 : pc > 100 ? 100 : (amount / 100) * pc;
		}
		else{
			result = Long.parseLong(fee);
			result = result < 0 ? 0 : result > amount ? amount : result;
		}
		return result;
	}

	@Override
	public boolean processTransfer(ICommandSender ics, Account sender, long amount, Account receiver){
		if(sender == null){
			Print.chat(ics, "Transfer failed! Sender is null.");
			Print.debug(ics.getName() + " -> sender account is null.");
			return false;
		}
		if(receiver == null){
			Print.chat(ics, "Transfer failed! Receiver is null.");
			Print.debug(ics.getName() + " -> receiver account is null.");
			return false;
		}
		if(amount <= 0){
			Print.chat(ics, "Transfer failed! Amount null or negative. (T:" + amount + ");");
			Print.debug(ics.getName() + " tried to transfer a negative amout of money to " + receiver.getAsResourceLocation().toString() + "!");
			return false;
		}
		String feestr = prices.get(sender.getType() + ":" + receiver.getType(), "transfer");
		long fee = parseFee(feestr, amount);
		if(sender.getBalance() - amount >= 0){
			sender.modifyBalance("sub", amount, ics);
			receiver.modifyBalance("add", amount - fee, ics);
			Print.debug(sender.getAsResourceLocation().toString() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + receiver.getAsResourceLocation().toString() + ";");
			return true;
		}
		Print.chat(ics, "Transfer failed! Not enough money on your Account.");
		Print.debug(sender.getAsResourceLocation().toString() + " -> " + sender.getAsResourceLocation().toString() + " : Transfer failed! Sender don't has enough money. (T:" + amount + " || F:" + fee + ");");
		return false;
	}

	@Override
	public boolean processWithdraw(EntityPlayer player, Account account, long amount){
		if(account == null){
			Print.chat(player, "Withdraw failed! Account is null.");
			Print.debug(player.getName() + " -> player account is null.");
			return false;
		}
		if(amount <= 0){
			Print.chat(player, "Withdraw failed! Amount null or negative. (T:" + amount + " || B:" + account.getBalance() + ");");
			Print.debug(player.getName() + " tried to withdraw a negative amout of money!");
			return false;
		}
		if(account.canModifyBalance("sub", "player", player.getGameProfile().getId().toString())){
			String feestr = prices.get("player:" + (account.getId().equals(player.getGameProfile().getId().toString()) ? "self" : account.getType()), "transfer");
			long fee = parseFee(feestr, amount);
			if(account.getBalance() - amount >= 0){
				account.modifyBalance("sub", amount, player);
				ItemManager.addToInventory(player, amount - fee);
				Print.debug(account.getAsResourceLocation().toString() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + player.getName() + ";");
				return true;
			}
			Print.chat(player, "Withdraw failed! Not enough money. (W:" + amount + " || B:" + account.getBalance() + ");");
			Print.debug(account.getAsResourceLocation().toString() + " : Withdraw failed! Player does not have enough money. (T:" + amount + " || F:" + fee + ");");
			return false;
		}
		else{
			Print.chat(player, "Withdraw failed! No permission.");
			Print.debug(player.getName() + " -> 'SUB' access to specified account was denied.");
			return false;
		}
	}

	@Override
	public boolean processDeposit(EntityPlayer player, Account account, long amount){
		if(account == null){
			Print.chat(player, "Deposit failed! Account is null.");
			Print.debug(player.getName() + " -> player account is null.");
			return false;
		}
		if(amount <= 0){
			Print.chat(player, "Deposit failed! Amount null or negative. (T:" + amount + " || I:" + ItemManager.countInInventory(player) + ");");
			Print.debug(player.getName() + " tried to deposit a negative amout of money!");
			return false;
		}
		if(account.canModifyBalance("add", "player", player.getGameProfile().getId().toString())){
			String feestr = prices.get("player:" + (account.getId().equals(player.getGameProfile().getId().toString()) ? "self" : account.getType()), "transfer");
			long fee = parseFee(feestr, amount);
			if(account.getBalance() + amount <= Long.MAX_VALUE){
				if(ItemManager.countInInventory(player) - amount < 0){
					ItemManager.removeFromInventory(player, amount);
					account.modifyBalance("add", amount - fee, player);
					Print.debug(player.getName() + " -> ([T:" + amount + "] -- [F:" + fee + "] == [R:" + (amount - fee) + "]) -> " + account.getAsResourceLocation().toString() + ";");
					return true;
				}
				else{
					Print.chat(player, "Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + account.getBalance() + ");");
					Print.log(account.getAsResourceLocation().toString() + ": Deposit failed! Not enough money in Inventory. (D:" + amount + " || B:" + account.getBalance() + ");");
					return false;
				}
			}
			Print.chat(player, "Deposit failed! Result is above limit.. (D:" + amount + " || B:" + account.getBalance() + ");");
			Print.log(account.getAsResourceLocation().toString() + " : Deposit failed! Result is above limit. (D:" + amount + " || B:" + account.getBalance() + ");");
			return false;
		}
		else{
			Print.chat(player, "Deposit failed! No permission.");
			Print.debug(player.getName() + " -> 'ADD' access to specified account was denied.");
			return false;
		}
	}

	@Override
	public JsonObject getData(){
		return data;
	}

	@Override
	public void setData(JsonObject obj){
		data = obj;
	}

	@Override
	public long getBalance(){
		return balance;
	}

	@Override
	public boolean modifyBalance(String action, long amount, ICommandSender sender){
		switch(action){
			case "set":{
				balance = amount;
				return true;
			}
			case "sub":{
				if(balance - amount >= 0){
					balance -= amount;
					return true;
				}
				else{
					Print.chat(sender, "Not enough money to subtract this amount! (B:" + (balance / 1000) + " - S:" + (amount / 1000) + ")");
					return false;
				}
			}
			case "add":{
				if(balance + amount >= Long.MAX_VALUE){
					Print.chat(sender, "Max Value reached.");
					return false;
				}
				else{
					balance += amount;
					return true;
				}
			}
			default: return false;
		}
	}
	
}
