package net.fexcraft.mod.fsmm.impl.cap;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.WorldCapability;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class WorldCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	private WorldCapability instance;
	
	public WorldCapabilityUtil(World world){
		instance = WorldCapability.CAPABILITY.getDefaultInstance();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == WorldCapability.CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == WorldCapability.CAPABILITY ? WorldCapability.CAPABILITY.cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return WorldCapability.CAPABILITY.getStorage().writeNBT(WorldCapability.CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		WorldCapability.CAPABILITY.getStorage().readNBT(WorldCapability.CAPABILITY, instance, null, nbt);
	}
	
	public static class Storage implements IStorage<WorldCapability> {

		@Override
		public NBTBase writeNBT(Capability<WorldCapability> capability, WorldCapability instance, EnumFacing side){
			return new NBTTagCompound();
		}

		@Override
		public void readNBT(Capability<WorldCapability> capability, WorldCapability instance, EnumFacing side, NBTBase nbt){
			//
		}
		
	}
	
	public static class Callable implements java.util.concurrent.Callable<WorldCapability>{

		@Override
		public WorldCapability call() throws Exception {
			return new Implementation();
		}
		
	}
	
	public static class Implementation implements WorldCapability {

		@Override
		public Account getAccount(String accid, boolean tempload, boolean create){
			return DataManager.getAccount(accid, tempload, create, null);
		}

		@Override
		public Bank getBank(String id, boolean tempload, boolean create){
			return DataManager.getBank(id, tempload, create, null);
		}
		
	}

}
