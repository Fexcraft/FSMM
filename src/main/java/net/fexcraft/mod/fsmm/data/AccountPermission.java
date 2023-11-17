package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.nbt.NBTTagCompound;

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

	public AccountPermission(String accid){
		this(accid, false, false, false, false);
	}

	public AccountPermission(Account account){
		this(account.getId());
		this.account = account;
	}
	
	public AccountPermission(NBTTagCompound compound){
		account = new Account(JsonHandler.parse(compound.getString("a"), true).asMap());
		account_id = account.getId();
		withdraw = compound.getBoolean("w");
		deposit = compound.getBoolean("d");
		transfer = compound.getBoolean("t");
		manage = compound.getBoolean("m");
	}

	public Account getAccount(){
		if(account == null){
			account = DataManager.getAccount(account_id, true, true);
		}
		return account;
	}

	public NBTTagCompound toNBT(){
		NBTTagCompound com = new NBTTagCompound();
		com.setString("a", getAccount().toJson(false).toString());
		com.setBoolean("w", withdraw);
		com.setBoolean("d", deposit);
		com.setBoolean("t", transfer);
		com.setBoolean("m", manage);
		return com;
	}

	public String getType(){
		return account == null ? account_id.split(":")[0] : account.getType();
	}

	public String getId(){
		return account == null ? account_id.split(":")[1] : account.getId();
	}

}
