package net.fexcraft.mod.fsmm.api;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface Money extends IForgeRegistryEntry<Money> {

	public long getWorth();
	
	public boolean hasItemMeta();
	
	public int getItemMeta();
	
	public @Nullable Item getItem();

}
