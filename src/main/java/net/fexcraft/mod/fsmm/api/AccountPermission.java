package net.fexcraft.mod.fsmm.api;

import net.fexcraft.mod.fsmm.util.DataManager;

public class AccountPermission {
	
	public static final AccountPermission FULL = new AccountPermission((String)null, true, true, true, true);
	public final String account_id;
	protected Account account;
	public final boolean withdraw, deposit, transfer, manage;
	//public final long limit;
	
	public AccountPermission(String accid, boolean wd, boolean dp, boolean tr, boolean mg){
		this.account_id = accid;
		this.withdraw = wd;
		this.deposit = dp;
		this.transfer = tr;
		this.manage = mg;
	}

	public AccountPermission(Account account, boolean wd, boolean dp, boolean tr, boolean mg){
		this(account.getId(), wd, dp, tr, mg);
		this.account = account;
	}
	
	public Account getAccount(){
		if(account == null){
			account = DataManager.getAccount(account_id, true, true);
		}
		return account;
	}

}
