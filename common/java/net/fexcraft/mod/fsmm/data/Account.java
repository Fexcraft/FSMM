package net.fexcraft.mod.fsmm.data;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.fsmm.FSMM;
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
public class Account implements Manageable {

	private CopyOnWriteArrayList<Transfer> transfers = new CopyOnWriteArrayList<Transfer>();
	private HashSet<Object> holders = new HashSet<>();
	private JsonMap additionaldata;
	private long last_activity;
	//
	private final IDL idtype;
	private long balance;
	private String name;
	private Bank bank;
	
	/** From JSON Constructor */
	public Account(JsonMap map, String def_type, String def_id){
		idtype = IDLManager.getIDLCached(map.getString("type", def_type) + ":" + map.getString("id", def_id));
		bank = DataManager.getBank(map.getString("bank", Config.DEFAULT_BANK));
		balance = map.getLong("balance", 0l);
		additionaldata = map.has("data") ? map.getMap("data") : null;
		name = map.has("name") ? map.get("name").string_value() : null;
		if(map.has("transfers")){
			for(JsonValue<?> elm : map.getArray("transfers").value){
				transfers.add(new Transfer(elm.asMap()));
			}
		}
		updateActivity();
	}
	
	/** Manual Constructor */
	public Account(String id, String type, long bal, Bank bank_, JsonMap data){
		idtype = IDLManager.getIDLCached(type + ":" + id);
		balance = bal;
		bank = bank_;
		additionaldata = data;
		updateActivity();
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
		updateActivity();
		return balance = rpl;
	}
	
	/** Bank of this Account. */
	public Bank getBank(){
		return bank;
	}

	public void setBank(Bank nbank){
		updateActivity();
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
		updateActivity();
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
		updateActivity();
		JsonMap obj = new JsonMap();
		obj.add("id", idtype.id());
		obj.add("type", idtype.space());
		if(bank != null) obj.add("bank", bank.id);
		else FSMM.log("Account '" + idtype + "' has no bank assigned.");
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

	/** Time of when this Account was last accessed, used for removing temporary loaded account. */
	public long lastActive(){
		return isHeld() ? -1 : last_activity;
	}

	public long updateActivity(){
		return last_activity = System.currentTimeMillis();
	}

	public Account addHolder(Object obj){
		holders.add(obj);
		updateActivity();
		return this;
	}

	public Account remHolder(Object obj){
		holders.remove(obj);
		updateActivity();
		return this;
	}

	public Account remHolder(Class<?> type){
		holders.removeIf(type::isInstance);
		updateActivity();
		return this;
	}

	public boolean isHeld(){
		return holders.size() > 0;
	}

}