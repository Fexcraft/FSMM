package net.fexcraft.mod.fsmm.impl.cap;

import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.item.ItemStack;

public class MoneyCap implements MoneyCapability {
	
	private ItemStack stack;
	private int cache_meta;
	private long cache_worth;
	
	@Override
	public ItemStack getStack(){
		return stack;
	}

	@Override
	public void setStack(ItemStack stack){
		this.stack = stack;
		if(!(stack.getItem() instanceof MoneyItem)){
			cache_meta = Integer.MIN_VALUE;
			cache_worth = -1;
		}
	}

	@Override
	public long getWorth(){
		return getWorthFromConfig(stack);
	}

	public long getWorthFromConfig(ItemStack stack){
		if(stack == null || stack.getCount() <= 0){ return 0; }
		if(stack.getItem() instanceof MoneyItem){
			return ((MoneyItem)stack.getItem()).getWorth(stack);
		}
		if(cache_meta == stack.getItemDamage() && cache_worth > -1){
			return cache_worth;
		}
		cache_meta = stack.getItemDamage();
		cache_worth = Config.getItemStackWorth(stack);
		return cache_worth;
	}
	
}
