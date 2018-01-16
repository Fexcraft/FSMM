package net.fexcraft.mod.fsmm.impl;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.minecraft.util.ResourceLocation;

public class GenericMoney implements Money {
	
	private ResourceLocation regname;
	private long worth;
	
	public GenericMoney(JsonObject obj){
		regname = new ResourceLocation(FSMM.MODID, JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + Time.getDate()));
		worth = JsonUtil.getIfExists(obj, "worth", -1).longValue();
	}

	@Override
	public Money setRegistryName(ResourceLocation name){
		regname = name;
		return this;
	}

	@Override
	public ResourceLocation getRegistryName(){
		return regname;
	}

	@Override
	public Class<Money> getRegistryType(){
		return Money.class;
	}

	@Override
	public long getWorth(){
		return worth;
	}
	
	@Override
	public String toString(){
		return super.toString() + "#" + this.getWorth();
	}
	
}