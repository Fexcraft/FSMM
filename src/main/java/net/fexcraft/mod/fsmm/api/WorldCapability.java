package net.fexcraft.mod.fsmm.api;

import net.minecraft.util.ResourceLocation;

public interface WorldCapability {
	
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("fsmm:world");

	/**
	 * @param tempload if the account should be loaded temporarily in case it isn't loaded yet
	 * @param create if the account should be created or loaded from disc if it isn't yet
	**/
	public Account getAccount(String accid, boolean tempload, boolean create);
	
	/** 
	 * @param tempload if the account should be loaded temporarily in case it isn't loaded yet
	 * @param create if the bank should be created or loaded from disc if it isn't yet
	**/
	public Bank getBank(String id, boolean tempload, boolean create);

}
