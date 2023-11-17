package net.fexcraft.mod.fsmm.data;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.math.Time;

/**
 * Internal Usage Class, do not bother with.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public abstract class Removable {
	
	private boolean temporary;
	private long last_access;
	
	/** Time of when this Account/Bank was last accessed, used for removing temporary loaded account/bankss. */
	public long lastAccessed(){
		return temporary ? last_access : -1;
	}
	
	/** Self explaining. */
	public long updateLastAccess(){
		return last_access = temporary ? Time.getDate() : -1;
	}
	
	
	/** Set this instance as "temporary loaded", as such, to be removed next check for inactive accounts/banks.*/
	public <T extends Removable> T setTemporary(boolean value){
		this.temporary = value;
		this.updateLastAccess();
		return (T)this;
	}
	
	public boolean isTemporary(){
		return temporary;
	}
	
}