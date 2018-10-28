package net.fexcraft.mod.fsmm.api;

import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.fexcraft.lib.mc.utils.Print;
import net.minecraft.command.ICommandSender;

/**
 * Base Bank Object.<br>
 * Unlike the Account Object, even though it processes most things itself,<br>
 * it needs at least one method to be overridden/implemented.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public abstract class Bank extends Removable implements Manageable {
	
	private String id;
	protected String name;
	protected long balance;
	private JsonObject additionaldata;
	protected TreeMap<String, String> fees;
	
	/** From JSON Constructor */
	public Bank(JsonObject obj){
		id = obj.get("uuid").getAsString();
		name = obj.get("name").getAsString();
		balance = obj.get("balance").getAsLong();
		additionaldata = obj.has("data") ? obj.get("data").getAsJsonObject() : null;
		if(obj.has("fees")){
			fees = new TreeMap<>();
			obj.entrySet().forEach(entry -> {
				try{
					fees.put(entry.getKey(), entry.getValue().getAsString());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
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
	
	/** To be overriden. */
	public abstract boolean processAction(Action action, ICommandSender log, Account sender, long amount, @Nullable Account receiver);
	
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
	
	public abstract boolean isNull();
	
}