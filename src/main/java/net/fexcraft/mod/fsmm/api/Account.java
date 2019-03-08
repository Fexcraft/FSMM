package net.fexcraft.mod.fsmm.api;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.util.Print;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;

/**
 * Universal Account Object.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public class Account extends Removable implements Manageable /*, net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound>*/ {
	
	private String id, type, bank;
	private long balance;
	private JsonObject additionaldata;
	
	/** From JSON Constructor */
	public Account(JsonObject obj){
		id = obj.get("id").getAsString();
		type = obj.get("type").getAsString();
		bank = obj.get("bank").getAsString();
		balance = obj.get("balance").getAsLong();
		additionaldata = obj.has("data") ? obj.get("data").getAsJsonObject() : null;
		this.updateLastAccess();
	}
	
	/** Manual Constructor */
	public Account(String id, String type, long balance, String bank, JsonObject data){
		this.id = id; this.type = type; this.balance = balance;
		this.bank = bank; this.additionaldata = data;
		this.updateLastAccess();
	}
	
	/** Unique ID of this Account. */
	public String getId(){ return id; }
	
	/** Current balance on this Account (1000 = 1 currency unit, usually) */
	public long getBalance(){
		//this.updateLastAccess();
		return balance;
	}
	
	/** Method to set the balance (1000 = 1 currency unit, usually)
	 * @param rpl new balance for this account
	 * @return new balance */
	public long setBalance(long rpl){
		this.updateLastAccess();
		return balance = rpl;
	}
	
	/** Bank ID of this Account. */
	public String getBankId(){ return bank; }
	
	/** Method to set the Bank ID */
	public boolean setBankId(String id){
		this.updateLastAccess();
		return bank.equals(id) ? false : (bank = id).equals(id);
	}
	
	/** Type of this Account, as not only players can hold Accounts. */
	public String getType(){ return type; }
	
	public ResourceLocation getAsResourceLocation(){
		return new ResourceLocation(this.getType(), this.getId());
	}
	
	@Nullable
	public JsonObject getData(){
		return additionaldata;
	}
	
	public void setData(JsonObject obj){
		this.updateLastAccess();
		additionaldata = obj;
	}
	
	@Override
	/** Mainly used for saving. */
	public JsonObject toJson(){
		this.updateLastAccess();
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type);
		obj.addProperty("bank", bank);
		obj.addProperty("balance", balance);
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
	
}