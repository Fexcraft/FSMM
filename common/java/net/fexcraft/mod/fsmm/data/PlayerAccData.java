package net.fexcraft.mod.fsmm.data;

import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.Appendable;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PlayerAccData implements Appendable<UniEntity> {

	private UniEntity player;
	private Account account;
	private AccountPermission atmacc;
	private Account selected;
	private Bank atmbank;

	public PlayerAccData(UniEntity unient){
		player = unient;
	}

	public Account getAccount(){
		if(account == null){
			account = DataManager.getAccount("player:" + player.entity.getUUID().toString(), false, true);
			account.setName(player.entity.getName());
		}
		return account;
	}

	public Bank getBank(){
		return getAccount().getBank();
	}

	/** Returns the worth of all (as money defined) Items in Inventory. **/
	public long getMoneyInInventory(){
		return ItemManager.countInInventory(player.entity);
	}

	/** Tries to subtract the defined amount from Inventory, <s>returns the amount which couldn't be subtracted. **/
	public long subMoneyFromInventory(long expected_amount){
		return ItemManager.removeFromInventory(player.entity, expected_amount);
	}

	/** Tries to add the defined amount to Inventory, <s>returns the amount which couldn't be added.</s> **/
	public long addMoneyToInventory(long expected_amount){
		return ItemManager.addToInventory(player.entity, expected_amount);
	}

	/** Tries to add the defined amount to Inventory, <s>returns the amount which couldn't be processed.</s> **/
	public long setMoneyInInventory(long expected_amount){
		return ItemManager.setInInventory(player.entity, expected_amount);
	}

	/** Returns the currently/last selected Account in the ATM. */
	public AccountPermission getSelectedAccount(){
		return atmacc;
	}

	/** Sets the currently/last selected Account in the ATM. */
	public void setSelectedAccount(AccountPermission perm){
		atmacc = perm;
	}

	/** Returns the currently selected receiver Account in the ATM. */
	public Account getSelectedReceiver(){
		return selected;
	}

	/** Sets the currently selected receiver Account in the ATM. */
	public void setSelectedReceiver(Account account){
		selected = account;
	}

	/** Returns the currently looked at Bank in the ATM. */
	public Bank getSelectedBankInATM(){
		return atmbank;
	}

	/** Sets the currently looked at Bank in the ATM. */
	public void setSelectedBankInATM(Bank bank){
		atmbank = bank;
	}

	public Object getPlayer(){
		return player.entity.direct();
	}

	@Override
	public void save(UniEntity player, TagCW com){
		//
	}

	@Override
	public void load(UniEntity player, TagCW com){
		//
	}

	@Override
	public PlayerAccData create(UniEntity unient){
		if(!unient.entity.isPlayer()) return null;
		return new PlayerAccData(unient);
	}

	@Override
	public String id(){
		return "fsmm";
	}

}
