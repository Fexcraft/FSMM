package net.fexcraft.mod.fsmm.api;

import net.minecraft.item.ItemStack;

public interface MoneyItem {
	
	public Money getMoneyType();

	public long getWorth(ItemStack stack);

}
