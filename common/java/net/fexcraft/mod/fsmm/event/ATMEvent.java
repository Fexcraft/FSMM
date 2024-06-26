package net.fexcraft.mod.fsmm.event;

import java.util.ArrayList;
import java.util.HashMap;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.PlayerAccData;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.world.EntityW;

public class ATMEvent extends FsmmEvent {
	
	private final UniEntity player;
	private final Account account;
	
	private ATMEvent(UniEntity player){
		this.player = player;
		this.account = player.get(PlayerAccData.class).getAccount();
	}
	
	public EntityW getPlayer(){
		return player.entity;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public Bank getBank(){
		return account.getBank();
	}
	
	/** Event so other mods can add into this list accounts manageable by this player. */
	public static class GatherAccounts extends ATMEvent {
		
		private ArrayList<AccountPermission> accounts = new ArrayList<>();
		
		public GatherAccounts(UniEntity player){
			super(player);
		}
		
		public ArrayList<AccountPermission> getAccountsList(){
			return accounts;
		}
		
	}
	
	/** Event so other mods can add search results. */
	public static class SearchAccounts extends ATMEvent {
		
		private HashMap<String, AccountPermission> accounts = new HashMap<>();
		private String type, id;
		
		public SearchAccounts(UniEntity player, String type, String id){
			super(player);
			this.type = type;
			this.id = id;
		}
		
		public HashMap<String, AccountPermission> getAccountsMap(){
			return accounts;
		}
		
		public String getSearchedType(){
			return type;
		}
		
		public String getSearchedId(){
			return id;
		}
		
	}

}
