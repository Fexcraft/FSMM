package net.fexcraft.mod.fsmm.impl;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GenericMoneyItem extends Item implements MoneyItem {
	
	private final Money type;
	
	public GenericMoneyItem(Money money){
		super();
		setCreativeTab(FSMM.tabFSMM);
		setMaxStackSize(50);
		this.type = money;
	}

	@Override
	public Money getMoneyType(){
		return type;
	}

	@Override
	public long getWorth(ItemStack stack){
		return type.getWorth();
	}
	
}