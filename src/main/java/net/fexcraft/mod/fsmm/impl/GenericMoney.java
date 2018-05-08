package net.fexcraft.mod.fsmm.impl;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class GenericMoney implements Money {
	
	private ResourceLocation regname;
	private long worth;
	private int meta;
	private Item item;
	
	public GenericMoney(JsonObject obj, boolean internal){
		if(internal){
			regname = new ResourceLocation(FSMM.MODID, JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + Time.getDate()));
		}
		else{
			regname = new ResourceLocation(JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + Time.getDate()));
			item = Item.getByNameOrId(regname.toString());
			if(item == null){
				Print.log("[FSMM] ERROR - External Item with ID '" + regname.toString() + "' couldn't be found! This is bad!");
				Static.halt();
			}
		}
		worth = JsonUtil.getIfExists(obj, "worth", -1).longValue();
		meta = JsonUtil.getIfExists(obj, "meta", -1).intValue();
		if(meta >= 0 && !internal){
			regname = new ResourceLocation(regname.toString() + "_" + meta);
		}
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

	@Override
	public boolean hasItemMeta(){
		return meta >= 0;
	}

	@Override
	public int getItemMeta(){
		return hasItemMeta() ? meta : 0;
	}

	@Override
	public Item getItem(){
		return item;
	}
	
}