package net.fexcraft.mod.fsmm.events;

import java.util.ArrayList;
import java.util.HashMap;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ATMEvent extends Event {
	
	private final EntityPlayer player;
	private final Account account;
	
	private ATMEvent(EntityPlayer player){
		this.player = player;
		this.account = player.getCapability(FSMMCapabilities.PLAYER, null).getAccount();
	}
	
	public EntityPlayer getPlayer(){
		return player;
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
		
		public GatherAccounts(EntityPlayer player){
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
		
		public SearchAccounts(EntityPlayer player, String type, String id){
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
