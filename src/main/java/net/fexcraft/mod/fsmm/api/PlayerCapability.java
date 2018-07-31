package net.fexcraft.mod.fsmm.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface PlayerCapability {
	
	@CapabilityInject(PlayerCapability.class)
	public static final Capability<PlayerCapability> CAPABILITY = null;
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("fsmm:player");

	public <T> T setEntityPlayer(EntityPlayer player);
	
	public Account getAccount();
	
	public Bank getBank();
	
	/** Gets the worth of all (as money defined) Items in Inventory. **/
	public long getMoneyInInventory();
	
	/** Tries to subtract the defined amount from Inventory, <s>returns amount which couldn't be subtracted. **/
	public long subMoneyFromInventory(long expected_amount);
	
	/** Tries to add the defined amount to Inventory, <s>returns amount which couldn't be added.</s> **/
	public long addMoneyToInventory(long expected_amount);
	
	/** Tries to add the defined amount to Inventory, <s>returns amount which couldn't be processed.</s> **/
	public long setMoneyInInventory(long expected_amount);

}
