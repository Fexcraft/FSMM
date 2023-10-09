package net.fexcraft.mod.fsmm.data;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class FSMMCapabilities {

	@CapabilityInject(PlayerCapability.class)
	public static final Capability<PlayerCapability> PLAYER = null;
	
}