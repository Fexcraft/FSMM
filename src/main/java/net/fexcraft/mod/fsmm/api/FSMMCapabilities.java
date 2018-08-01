package net.fexcraft.mod.fsmm.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class FSMMCapabilities {

	@CapabilityInject(MoneyCapability.class)
	public static final Capability<MoneyCapability> MONEY_ITEMSTACK = null;

	@CapabilityInject(PlayerCapability.class)
	public static final Capability<PlayerCapability> PLAYER = null;

	@CapabilityInject(WorldCapability.class)
	public static final Capability<WorldCapability> WORLD = null;
	
}