package net.fexcraft.mod.fsmm.data;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.mc.registry.UCResourceLocation;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.events.AccountEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

/**
 * Universal Account Object.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public class Account extends Removable implements Manageable /*, net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound>*/ {
	
	private String id, type, bank, name;
	private long balance;
	private JsonObject additionaldata;
	private ArrayList<Transfer> transfers = new ArrayList<Transfer>();
	
	/** From JSON Constructor */
	public Account(JsonObject obj){
		id = obj.get("id").getAsString();
		type = obj.get("type").getAsString();
		bank = obj.get("bank").getAsString();
		balance = obj.get("balance").getAsLong();
		additionaldata = obj.has("data") ? obj.get("data").getAsJsonObject() : null;
		name = obj.has("name") ? obj.get("name").getAsString() : null;
		if(obj.has("transfers")){
			JsonArray array = obj.get("transfers").getAsJsonArray();
			for(JsonElement elm : array.getAsJsonArray()){
				transfers.add(new Transfer(elm.getAsJsonObject()));
			}
		}
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
		MinecraftForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, rpl));
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
		return new UCResourceLocation(this.getType(), this.getId());
	}
	
	public String getTypeAndId(){
		return this.getType() + ":" + this.getId();
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
	public String getName(){
		return name == null ? id : name;
	}
	
	public Account setName(String name){
		this.name = name;
		return this;
	}

	/** Mainly used for saving. */
	public JsonObject toJson(boolean withtransfers){
		this.updateLastAccess();
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type);
		obj.addProperty("bank", bank);
		obj.addProperty("balance", balance);
		if(additionaldata != null){
			obj.add("data", additionaldata);
		}
		if(name != null) obj.addProperty("name", name);
		if(withtransfers){
			JsonArray array = new JsonArray();
			for(Transfer transfer : transfers) array.add(transfer.toJson());
			if(array.size() > 0) obj.add("transfers", array);
		}
		return obj;
	}

	@Override
	/** Mainly used for saving. */
	public JsonObject toJson(){
		return toJson(true);
	}

	@Override
	public void modifyBalance(Manageable.Action action, long amount, ICommandSender log){
		switch(action){
			case SET :{
				MinecraftForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, amount));
				balance = amount; return;
			}
			case SUB :{
				if(balance - amount >= 0){
					MinecraftForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, balance -= amount));
				}
				else{
					Print.chat(log, "Not enough money to subtract this amount! (B:" + (balance / 1000) + " - S:" + (amount / 1000) + ")");
				}
				return;
			}
			case ADD:{
				if(balance + amount >= Long.MAX_VALUE){
					Print.chat(log, "Max Value reached.");
				}
				else{ MinecraftForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, balance += amount)); }
			}
			default: return;
		}
	}

	public void addTransfer(Transfer transfer){
		transfers.add(0, transfer);
		while(transfers.size() > Config.TRANSFER_CACHE){
			transfers.remove(Config.TRANSFER_CACHE);
		}
	}

	public List<Transfer> getTransfers(){
		return transfers;
	}

}