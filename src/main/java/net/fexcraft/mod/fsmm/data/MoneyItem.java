package net.fexcraft.mod.fsmm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.fexcraft.mod.fsmm.FSMM;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MoneyItem extends Item implements Money.Item {
	
	public static final ArrayList<MoneyItem> sorted = new ArrayList<>();
	private final Money type;
	
	public MoneyItem(Money money){
		super();
		setCreativeTab(FSMM.tabFSMM);
		setMaxStackSize(50);
		type = money;
		sorted.add(this);
	}

	public static void sort(){
		Collections.sort(sorted, new Comparator<MoneyItem>(){
			@Override
			public int compare(MoneyItem o1, MoneyItem o2){
				if(o1.type.getWorth() == o2.type.getWorth()) return o1.getRegistryName().compareTo(o2.getRegistryName());
				return o1.type.getWorth() > o2.type.getWorth() ? -1 : 1;
			}
	    });
	}

	@Override
	public Money getType(){
		return type;
	}

	@Override
	public long getWorth(ItemStack stack){
		return type.getWorth();
	}
	
}