package net.fexcraft.mod.fsmm.impl;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.minecraft.item.Item;

public class GenericMoneyItem extends Item implements MoneyItem {
	
	private Money type;
	
	public GenericMoneyItem(Money money){
		setCreativeTab(FSMM.tabFSMM);
		setMaxStackSize(50);
	}

	@Override
	public Money getMoneyType(){
		return type;
	}

	@Override
	public long getWorth(){
		return type.getWorth();
	}
	
}