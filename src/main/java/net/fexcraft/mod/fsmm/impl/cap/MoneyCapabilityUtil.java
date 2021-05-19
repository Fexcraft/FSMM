package net.fexcraft.mod.fsmm.impl.cap;

import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class MoneyCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	private MoneyCapability instance;
	
	public MoneyCapabilityUtil(ItemStack stack){
		instance = FSMMCapabilities.MONEY_ITEMSTACK.getDefaultInstance();
		instance.setStack(stack);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == FSMMCapabilities.MONEY_ITEMSTACK;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == FSMMCapabilities.MONEY_ITEMSTACK ? FSMMCapabilities.MONEY_ITEMSTACK.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return FSMMCapabilities.MONEY_ITEMSTACK.getStorage().writeNBT(FSMMCapabilities.MONEY_ITEMSTACK, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		FSMMCapabilities.MONEY_ITEMSTACK.getStorage().readNBT(FSMMCapabilities.MONEY_ITEMSTACK, instance, null, nbt);
	}
	
	public static class Storage implements IStorage<MoneyCapability> {

		@Override
		public NBTBase writeNBT(Capability<MoneyCapability> capability, MoneyCapability instance, EnumFacing side){
			return new NBTTagCompound();
		}

		@Override
		public void readNBT(Capability<MoneyCapability> capability, MoneyCapability instance, EnumFacing side, NBTBase nbt){
			//
		}
		
	}
	
	public static class Callable implements java.util.concurrent.Callable<MoneyCapability>{

		@Override
		public MoneyCapability call() throws Exception {
			return new Implementation();
		}
		
	}
	
	public static class Implementation implements MoneyCapability {
		
		private ItemStack stack;
		private CObject comparable;

		@Override
		public ItemStack getStack(){
			return stack;
		}

		@Override
		public void setStack(ItemStack stack){
			this.stack = stack;
		}

		@Override
		public long getWorth(){
			if(stack == null || stack.getCount() <= 0){ return 0; }
			if(stack.getItem() instanceof Money.Item){
				return ((Money.Item)stack.getItem()).getWorth(stack);
			}
			if(comparable != null && comparable.stillEquals(stack)){
				return comparable.getWorth();
			}
			comparable = new CObject();
			return comparable.equal(stack).getWorth();
		}
		
	}
	
	private static class CObject {
		
		private int meta;
		private long worth = -1;
		private NBTTagCompound nbt;
		
		private CObject equal(ItemStack stack){
			meta = stack.getItemDamage();
			worth = Config.getItemStackWorth(stack);
			nbt = stack.getTagCompound() == null ? null : stack.getTagCompound().copy();
			return this;
		}
		
		public boolean stillEquals(ItemStack stack){
			return meta == stack.getItemDamage() && worth > -1 && nbtSame(stack.getTagCompound());
		}

		private boolean nbtSame(NBTTagCompound compound){
			return nbt == null && compound == null ? true : nbt != null && compound != null ? nbt.equals(compound) : false;
		}
		
		public long getWorth(){
			return worth;
		}
		
	}

}
