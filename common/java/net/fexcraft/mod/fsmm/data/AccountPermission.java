package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.uni.tag.TagCW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class AccountPermission {
	
	public static final AccountPermission FULL = new AccountPermission((String)null, true, true, true, true);
	public final boolean withdraw;
	public final boolean transfer;
	public final boolean deposit;
	public final boolean manage;
	public final String accid;
	protected Account account;
	
	public AccountPermission(String aid, boolean wd, boolean dp, boolean tr, boolean mg){
		accid = aid;
		withdraw = wd;
		deposit = dp;
		transfer = tr;
		manage = mg;
	}

	public AccountPermission(Account acc, boolean wd, boolean dp, boolean tr, boolean mg){
		this(acc.getId(), wd, dp, tr, mg);
		account = acc;
	}

	public AccountPermission(String accid){
		this(accid, false, false, false, false);
	}

	public AccountPermission(Account acc){
		this(acc.getId());
		account = acc;
	}
	
	public AccountPermission(TagCW compound){
		account = new Account(JsonHandler.parse(compound.getString("a"), true).asMap(), null, null);
		accid = account.getId();
		withdraw = compound.getBoolean("w");
		deposit = compound.getBoolean("d");
		transfer = compound.getBoolean("t");
		manage = compound.getBoolean("m");
	}

	public Account getAccount(){
		if(account == null){
			account = DataManager.getAccount(accid, true, true);
		}
		return account;
	}

	public TagCW toNBT(){
		TagCW com = TagCW.create();
		com.set("a", getAccount().toJson(false).toString());
		com.set("w", withdraw);
		com.set("d", deposit);
		com.set("t", transfer);
		com.set("m", manage);
		return com;
	}

	public String getType(){
		return account == null ? accid.split(":")[0] : account.getType();
	}

	public String getId(){
		return account == null ? accid.split(":")[1] : account.getId();
	}

	public String getTypeAndId(){
		return account == null ? accid : account.getTypeAndId();
	}

}
