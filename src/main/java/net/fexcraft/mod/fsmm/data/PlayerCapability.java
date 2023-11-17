package net.fexcraft.mod.fsmm.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public interface PlayerCapability {
	
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
	
	/** Gets the currently/last selected Account in the ATM. */
	public AccountPermission getSelectedAccountInATM();
	
	/** Sets the currently/last selected Account in the ATM. */
	public void setSelectedAccountInATM(AccountPermission perm);
	
	/** Gets the currently selected receiver Account in the ATM. */
	public Account getSelectedReiverInATM();
	
	/** Sets the currently selected receiver Account in the ATM. */
	public void setSelectedReceiverInATM(Account account);

	/** Gets the currently looked at Bank in the ATM. */
	public Bank getSelectedBankInATM();

	/** Sets the currently looked at Bank in the ATM. */
	public void setSelectedBankInATM(Bank bank);

	public EntityPlayer getEntityPlayer();

}
