package net.fexcraft.mod.fsmm.data;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Base Bank Object.<br>
 * Unlike the Account Object, even though it processes most things itself,<br>
 * it needs at least one method to be overridden/implemented.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public class Bank extends Removable implements Manageable {
	
	private String id;
	protected String name;
	protected long balance;
	private JsonObject additionaldata;
	protected TreeMap<String, String> fees;
	protected ArrayList<String> status = new ArrayList<>();
	
	/** From JSON Constructor */
	public Bank(JsonObject obj){
		id = obj.get("uuid").getAsString();
		name = obj.get("name").getAsString();
		balance = obj.has("balance") ? obj.get("balance").getAsLong() : 0;
		additionaldata = obj.has("data") ? obj.get("data").getAsJsonObject() : null;
		if(obj.has("fees")){
			fees = new TreeMap<>();
			obj.get("fees").getAsJsonObject().entrySet().forEach(entry -> {
				try{
					fees.put(entry.getKey(), entry.getValue().getAsString());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
		if(obj.has("status")){
			obj.get("status").getAsJsonArray().forEach(elm -> status.add(elm.getAsString()));
		}
		DataManager.getBankNameCache().put(id, name);
		this.updateLastAccess();
	}
	
	/** Manual Constructor */
	public Bank(String id, String name, long balance, JsonObject data, TreeMap<String, String> map){
		this.id = id; this.name = name; this.balance = balance;
		this.fees = map; this.additionaldata = data;
		this.updateLastAccess();
	}
	
	/** Unique ID of this Bank. */
	public String getId(){ return id; }
	
	/** Name of this Bank. */
	public String getName(){ return name; }

	/** Method to set the Bank Name. */
	public boolean setName(String name){
		this.updateLastAccess();
		return this.name.equals(name) ? false : (this.name = name).equals(name);
	}
	
	/** Current balance of this Bank (1000 = 1 currency unit, usually) */
	public long getBalance(){
		this.updateLastAccess();
		return balance;
	}
	
	/** Method to set the balance (1000 = 1 currency unit, usually)
	 * @param rpl new balance for this account
	 * @return new balance */
	public long setBalance(long rpl){
		this.updateLastAccess();
		return balance = rpl;
	}
	
	@Nullable
	public JsonObject getData(){
		return additionaldata;
	}
	
	public void setData(JsonObject obj){
		this.updateLastAccess();
		additionaldata = obj;
	}
	
	@Nullable
	public TreeMap<String, String> getFees(){
		return fees;
	}

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
					DataManager.save(sender);
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
						DataManager.save(receiver);
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
					DataManager.save(sender);
					DataManager.save(receiver);
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
	
	public boolean processAction(Action action, ICommandSender log, Account sender, long amount, Account receiver){
		return processAction(action, log, sender, amount, receiver, true);
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
	
	public static enum Action { TRANSFER, WITHDRAW, DEPOSIT }
	
	@Override
	/** Mainly used for saving. */
	public JsonObject toJson(){
		this.updateLastAccess();
		JsonObject obj = new JsonObject();
		obj.addProperty("uuid", id);
		obj.addProperty("name", name);
		obj.addProperty("balance", balance);
		if(fees != null){
			JsonObject of = new JsonObject();
			for(Entry<String, String> entry : fees.entrySet()){
				of.addProperty(entry.getKey(), entry.getValue());
			}
			obj.add("fees", of);
		}
		if(additionaldata != null){
			obj.add("data", additionaldata);
		}
		if(!status.isEmpty()){
			JsonArray array = new JsonArray();
			for(String str : status) array.add(str);
			obj.add("status", array);
		}
		return obj;
	}

	@Override
	public void modifyBalance(Manageable.Action action, long amount, ICommandSender log){
		switch(action){
			case SET :{ balance = amount; return; }
			case SUB :{
				if(balance - amount >= 0){ balance -= amount; }
				else{
					Print.chat(log, "Not enough money to subtract this amount! (B:" + (balance / 1000) + " - S:" + (amount / 1000) + ")");
				}
				return;
			}
			case ADD:{
				if(balance + amount >= Long.MAX_VALUE){
					Print.chat(log, "Max Value reached.");
				}
				else{ balance += amount; }
			}
			default: return;
		}
	}
	
	public ArrayList<String> getStatus(){
		return status;
	}

	public boolean hasFee(String fee_id){
		return fees != null && fees.containsKey(fee_id);
	}

	//

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

}