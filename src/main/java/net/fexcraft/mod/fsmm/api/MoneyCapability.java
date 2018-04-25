package net.fexcraft.mod.fsmm.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface MoneyCapability {
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("fsmm:money");

	public ItemStack getStack();

	public void setStack(ItemStack stack);
	
	public long getWorth();

}
