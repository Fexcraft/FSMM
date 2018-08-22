package net.fexcraft.mod.fsmm.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface Money extends IForgeRegistryEntry<Money> {

	public long getWorth();
	
	public ItemStack getItemStack();
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth(ItemStack stack);
		
	}

}
