package net.fexcraft.mod.fsmm.impl;

import com.google.gson.JsonObject;

import cpw.mods.fml.common.FMLCommonHandler;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.time.LocalDate;

public class GenericMoney implements Money {
	
	private ResourceLocation regname;
	private ItemStack stack;
	private long worth;
	
	public GenericMoney(JsonObject obj, boolean internal){
		regname = new ResourceLocation((internal ? FSMM.MODID + ":" : "") + JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + LocalDate.now().getDayOfMonth()));
		worth = JsonUtil.getIfExists(obj, "worth", -1).longValue();
		int meta = JsonUtil.getIfExists(obj, "meta", -1).intValue();
		if(meta >= 0 && !internal){ regname = new ResourceLocation(regname.toString() + "_" + meta); }
		if(!internal){
			stackload(null, obj, internal);
		}
	}

	public GenericMoney(ResourceLocation reg){
		regname=reg;
		worth=1;
	}
	
	public void stackload(net.minecraft.item.Item item, JsonObject obj, boolean internal){
		if(item == null || !internal){
			String id = JsonUtil.getIfExists(obj, "id", "invalid_" + obj.toString() + "_" + LocalDate.now().getDayOfMonth());
			if(internal){
				item = cpw.mods.fml.common.registry.GameRegistry.findItem(FSMM.MODID,id);
			} else {
				item = cpw.mods.fml.common.registry.GameRegistry.findItem(id.split(":")[0],id.split(":")[1]);
			}
			if(item == null){
				Print.log("[FSMM] ERROR - External Item with ID '" + regname.toString() + "' couldn't be found! This is bad!");
				FMLCommonHandler.instance().exitJava(1, true);
			}
		}
		NBTTagCompound compound = null;
		if(obj.has("nbt")){
			try{
				compound = (NBTTagCompound) (JsonToNBT.func_150315_a(obj.get("nbt").getAsString()));
			}
			catch(NBTException e){
				Print.log("[FSMM] ERROR - Could not load NBT from config of '" + regname.toString() + "'! This is bad!");
				FMLCommonHandler.instance().exitJava(1, true);
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

	/**
	 * Get the referent pointed at by this delegate. This will be the currently active item or block, and will change
	 * as world saves come and go. Note that item.delegate.get() may NOT be the same object as item, due to item and
	 * block substitution.
	 *
	 * @return The referred object
	 */

	public Money get() {
		return this;
	}


	public String name() {
		return regname.getResourcePath();
	}


	public Class<Money> type() {
		return Money.class;
	}
}