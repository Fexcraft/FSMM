package net.fexcraft.mod.fsmm.impl;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GenericMoney implements Money {
	
	private ResourceLocation regname;
	private ItemStack stack;
	private long worth;
	
	public GenericMoney(JsonObject obj, boolean internal){
		regname = new ResourceLocation((internal ? FSMM.MODID + ":" : "") + JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + Time.getDate()));
		worth = JsonUtil.getIfExists(obj, "worth", -1).longValue();
		int meta = JsonUtil.getIfExists(obj, "meta", -1).intValue();
		if(meta >= 0 && !internal){ regname = new ResourceLocation(regname.toString() + "_" + meta); }
		if(!internal){
			stackload(null, obj, internal);
		}
	}
	
	public void stackload(net.minecraft.item.Item item, JsonObject obj, boolean internal){
		if(item == null || !internal){
			String id = JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + Time.getDate());
			item = net.minecraft.item.Item.getByNameOrId(internal ? FSMM.MODID + ":" + id : id);
			if(item == null){
				Print.log("[FSMM] ERROR - External Item with ID '" + regname.toString() + "' couldn't be found! This is bad!");
				Static.halt();
			}
		}
		NBTTagCompound compound = null;
		if(obj.has("nbt")){
			try{
				compound = JsonToNBT.getTagFromJson(obj.get("nbt").getAsString());
			}
			catch(NBTException e){
				Print.log("[FSMM] ERROR - Could not load NBT from config of '" + regname.toString() + "'! This is bad!");
				Static.halt();
			}
		}
		//
		stack = new ItemStack(item, 1, JsonUtil.getIfExists(obj, "meta", -1).intValue());
		if(compound != null){ stack.setTagCompound(compound); }
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
	public ItemStack getItemStack(){
		return stack;
	}
	
}