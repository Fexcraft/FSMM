package net.fexcraft.mod.fsmm.data.cap;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.FSMMCapabilities;
import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerCapSerializer implements ICapabilitySerializable<NBTBase>{
	
	private PlayerCapability instance;
	
	public PlayerCapSerializer(EntityPlayer player){
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

}
