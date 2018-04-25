package net.fexcraft.mod.fsmm.impl.cap;

import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class MoneyCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	@CapabilityInject(MoneyCapability.class)
	public static final Capability<MoneyCapability> CAPABILITY = null;
	private MoneyCapability instance;
	
	public MoneyCapabilityUtil(ItemStack stack){
		instance = CAPABILITY.getDefaultInstance();
		instance.setStack(stack);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == CAPABILITY ? CAPABILITY.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
	}
	
	//
	
	public static class Storage implements IStorage<MoneyCapability> {

		@Override
		public NBTBase writeNBT(Capability<MoneyCapability> capability, MoneyCapability instance, EnumFacing side){
			return new NBTTagString(instance == null ? "null" : instance.getStack() == null ? "stack_null" : instance.getWorth() + "_stack_worth");
			//I know this is nonsense, but else itemstacks kept getting errors and didn't save.
		}

		@Override
		public void readNBT(Capability<MoneyCapability> capability, MoneyCapability instance, EnumFacing side, NBTBase nbt){
			//
		}
		
	}
	
	//
	
	public static class Callable implements java.util.concurrent.Callable<MoneyCapability>{

		@Override
		public MoneyCapability call() throws Exception {
			return new MoneyCap();
		}
		
	}

}
