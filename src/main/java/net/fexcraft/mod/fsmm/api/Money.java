package net.fexcraft.mod.fsmm.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface Money{

	public long getWorth();
	
	public ItemStack getItemStack();

	public ResourceLocation getRegistryName();

	public Money setRegistryName(ResourceLocation name);
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth(ItemStack stack);
		
	}

}
