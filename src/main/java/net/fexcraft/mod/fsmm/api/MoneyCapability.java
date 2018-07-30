package net.fexcraft.mod.fsmm.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface MoneyCapability {
	
	@CapabilityInject(MoneyCapability.class)
	public static final Capability<MoneyCapability> CAPABILITY = null;
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("fsmm:money");

	public ItemStack getStack();

	public void setStack(ItemStack stack);
	
	public long getWorth();

}
