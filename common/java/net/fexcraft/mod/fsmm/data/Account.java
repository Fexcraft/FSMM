package net.fexcraft.mod.fsmm.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.fsmm.event.AccountEvent;
import net.fexcraft.mod.fsmm.event.FsmmEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.fexcraft.mod.uni.world.MessageSender;

/**
 * Universal Account Object.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public class Account extends Removable implements Manageable {

	private final IDL idtype;
	private String name;
	private Bank bank;
	private long balance;
	private JsonMap additionaldata;
	private CopyOnWriteArrayList<Transfer> transfers = new CopyOnWriteArrayList<Transfer>();
	
	/** From JSON Constructor */
	public Account(JsonMap map){
		idtype = IDLManager.getIDLCached(map.get("type").string_value() + ":" + map.get("id").string_value());
		bank = DataManager.getBank(map.getString("bank", Config.DEFAULT_BANK));
		balance = map.get("balance").long_value();
		additionaldata = map.has("data") ? map.getMap("data") : null;
		name = map.has("name") ? map.get("name").string_value() : null;
		if(map.has("transfers")){
			for(JsonValue<?> elm : map.getArray("transfers").value){
				transfers.add(new Transfer(elm.asMap()));
			}
		}
		updateLastAccess();
	}
	
	/** Manual Constructor */
	public Account(String id, String type, long bal, Bank bank_, JsonMap data){
		idtype = IDLManager.getIDLCached(type + ":" + id);
		balance = bal;
		bank = bank_;
		additionaldata = data;
		updateLastAccess();
	}
	
	/** Unique ID of this Account. */
	public String getId(){ return idtype.id(); }
	
	/** Current balance on this Account (1000 = 1 currency unit). */
	public long getBalance(){
		//updateLastAccess();
		return balance;
	}
	
	/** Method to set the balance (1000 = 1 currency unit)
	 * @param rpl new balance for this account
	 * @return new balance */
	public long setBalance(long rpl){
		FsmmEvent.run(new AccountEvent.BalanceUpdated(this, balance, rpl));
		updateLastAccess();
		return balance = rpl;
	}
	
	/** Bank of this Account. */
	public Bank getBank(){
		return bank;
	}

	public void setBank(Bank nbank){
		updateLastAccess();
		bank = nbank;
	}
	
	/** Type of this Account. */
	public String getType(){
		return idtype.space();
	}
	
	public IDL getIDL(){
		return idtype;
	}
	
	public String getTypeAndId(){
		return idtype.colon();
	}

	public JsonMap getData(){
		return additionaldata;
	}
	
	public void setData(JsonMap obj){
		updateLastAccess();
		additionaldata = obj;
	}

	public String getName(){
		return name == null ? idtype.id() : name;
	}
	
	public Account setName(String nname){
		name = nname;
		return this;
	}

	/** Mainly used for saving. */
	public JsonMap toJson(boolean withtransfers){
		updateLastAccess();
		JsonMap obj = new JsonMap();
		obj.add("id", idtype.id());
		obj.add("type", idtype.space());
		obj.add("bank", bank.id);
		obj.add("balance", balance);
		if(additionaldata != null){
			obj.add("data", additionaldata);
		}
		if(name != null) obj.add("name", name);
		if(withtransfers){
			JsonArray array = new JsonArray();
			for(Transfer transfer : transfers) array.add(transfer.toJson());
			if(array.size() > 0) obj.add("transfers", array);
		}
		return obj;
	}

	@Override
	/** Mainly used for saving. */
	public JsonMap toJson(){
		return toJson(true);
	}

	@Override
	public void modifyBalance(Manageable.Action action, long amount, MessageSender log){
		switch(action){
			case SET :{
				FsmmEvent.run(new AccountEvent.BalanceUpdated(this, balance, amount));
				balance = amount;
				return;
			}
			case SUB :{
				if(balance - amount >= 0){
					FsmmEvent.run(new AccountEvent.BalanceUpdated(this, balance, balance -= amount));
				}
				else{
					log.send("Not enough money to subtract this amount! (B:" + (balance / 1000) + " - S:" + (amount / 1000) + ")");
				}
				return;
			}
			case ADD:{
				if(balance + amount >= Long.MAX_VALUE){
					log.send("Max Value reached.");
				}
				else{
					FsmmEvent.run(new AccountEvent.BalanceUpdated(this, balance, balance += amount));
				}
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