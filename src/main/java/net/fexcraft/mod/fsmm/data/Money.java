package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.FSMM;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Money implements IForgeRegistryEntry<Money> {

	private ResourceLocation regname;
	private ItemStack stack;
	private long worth;

	public Money(JsonMap map, boolean internal){
		regname = new ResourceLocation((internal ? FSMM.MODID + ":" : "") + map.getString("id", "invalid_" + map.toString() + "_" + Time.getDate()));
		worth = map.getLong("worth", -1);
		int meta = map.getInteger("meta", -1);
		if(meta >= 0 && !internal) regname = new ResourceLocation(regname.toString() + "_" + meta);
		if(!internal){
			stackload(null, map, internal);
		}
	}

	public void stackload(net.minecraft.item.Item item, JsonMap map, boolean internal){
		if(item == null || !internal){
			String id = map.getString("id", "invalid_" + map.toString() + "_" + Time.getDate());
			item = net.minecraft.item.Item.getByNameOrId(internal ? FSMM.MODID + ":" + id : id);
			if(item == null){
				Print.log("[FSMM] ERROR - External Item with ID '" + regname.toString() + "' couldn't be found! This is bad!");
				Static.halt();
			}
		}
		NBTTagCompound compound = null;
		if(map.has("nbt")){
			try{
				compound = JsonToNBT.getTagFromJson(map.get("nbt").string_value());
			}
			catch(NBTException e){
				Print.log("[FSMM] ERROR - Could not load NBT from config of '" + regname.toString() + "'! This is bad!");
				Static.halt();
			}
		}
		//
		stack = new ItemStack(item, 1, map.getInteger("meta", -1));
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
	public String toString(){
		return super.toString() + "#" + this.getWorth();
	}

	public long getWorth(){
		return worth;
	}

	public ItemStack getItemStack(){
		return stack;
	}
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth(ItemStack stack);
		
	}

}
