package net.fexcraft.mod.fsmm.api;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface Money extends IForgeRegistryEntry<Money> {

	public long getWorth();
	
	public boolean hasItemMeta();
	
	public int getItemMeta();

}
