package net.fexcraft.mod.fsmm.impl;

import java.util.UUID;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
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

	@Override
	public boolean processTransfer(Account sender, long amount, Account target){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processWithdraw(EntityPlayer player, Account account, long amount){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processDeposit(EntityPlayer player, Account account, long amount){
		// TODO Auto-generated method stub
		return false;
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
