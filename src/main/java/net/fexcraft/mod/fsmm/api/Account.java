package net.fexcraft.mod.fsmm.api;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.registry.UCResourceLocation;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;

public interface Account {
	
	public String getId();
	
	public long getBalance();
	
	public boolean modifyBalance(String action, long amount, ICommandSender sender);
	
	public boolean canModifyBalance(String action, String type, String id);
	
	public UUID getBankId();
	
	public boolean setBankId(UUID uuid);
	
	public String getType();
	
	public default ResourceLocation getAsResourceLocation(){
		return new UCResourceLocation(this.getType(), this.getId());
	}
	
	public JsonObject getData();
	
	public void setData(JsonObject obj);
	
}