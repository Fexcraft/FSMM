package net.fexcraft.mod.fsmm.impl.cap;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.AccountPermission;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	@CapabilityInject(PlayerCapability.class)
	private PlayerCapability instance;
	
	public PlayerCapabilityUtil(EntityPlayer player){
		instance = FSMMCapabilities.PLAYER.getDefaultInstance().setEntityPlayer(player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == FSMMCapabilities.PLAYER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == FSMMCapabilities.PLAYER ? FSMMCapabilities.PLAYER.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return FSMMCapabilities.PLAYER.getStorage().writeNBT(FSMMCapabilities.PLAYER, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		FSMMCapabilities.PLAYER.getStorage().readNBT(FSMMCapabilities.PLAYER, instance, null, nbt);
	}
	
	public static class Storage implements IStorage<PlayerCapability> {

		@Override
		public NBTBase writeNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side){
			return new NBTTagCompound();
		}

		@Override
		public void readNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side, NBTBase nbt){
			//
		}
		
	}
	
	public static class Callable implements java.util.concurrent.Callable<PlayerCapability>{

		@Override
		public PlayerCapability call() throws Exception {
			return new Implementation();
		}
		
	}
	
	public static class Implementation implements PlayerCapability {
		
		private EntityPlayer player;
		private Account account;
		private AccountPermission atmacc;
		private String atmbank;

		@Override
		public <T> T setEntityPlayer(EntityPlayer player){
			this.player = player; return (T)this;
		}

		@Override
		public Account getAccount(){
			return account == null ? account = DataManager.getAccount("player:" + player.getGameProfile().getId().toString(), false, true).setName(player.getName()) : account;
		}

		@Override
		public Bank getBank(){
			return DataManager.getBank(getAccount().getBankId(), true, false);
		}

		@Override
		public long getMoneyInInventory(){
			return ItemManager.countInInventory(player);
		}

		@Override
		public long subMoneyFromInventory(long expected_amount){
			return ItemManager.removeFromInventory(player, expected_amount);
		}

		@Override
		public long addMoneyToInventory(long expected_amount){
			return ItemManager.addToInventory(player, expected_amount);
		}

		@Override
		public long setMoneyInInventory(long expected_amount){
			return ItemManager.setInInventory(player, expected_amount);
		}

		@Override
		public AccountPermission getSelectedAccountInATM(){
			return atmacc;
		}

		@Override
		public void setSelectedAccountInATM(AccountPermission perm){
			atmacc = perm;
		}

		@Override
		public String getSelectedBankInATM(){
			return atmbank;
		}

		@Override
		public void setSelectedBankInATM(String bankid){
			atmbank = bankid;
		}

		@Override
		public EntityPlayer getEntityPlayer(){
			return player;
		}
		
	}

}
