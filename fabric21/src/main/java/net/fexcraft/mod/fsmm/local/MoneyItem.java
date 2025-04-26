package net.fexcraft.mod.fsmm.local;

import net.fexcraft.mod.fsmm.data.Money;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MoneyItem extends Item implements Money.Item {
	
	public static final ArrayList<MoneyItem> sorted = new ArrayList<>();
	private final Money type;
	
	public MoneyItem(Properties prop, Money money){
		super(prop.stacksTo(50));
		type = money;
		sorted.add(this);
	}

	public static void sort(){
		Collections.sort(sorted, new Comparator<MoneyItem>(){
			@Override
			public int compare(MoneyItem o1, MoneyItem o2){
				if(o1.type.getWorth() == o2.type.getWorth()) return o1.type.getID().colon().compareTo(o2.type.getID().colon());
				return o1.type.getWorth() > o2.type.getWorth() ? -1 : 1;
			}
	    });
	}

	@Override
	public Money getType(){
		return type;
	}

	@Override
	public long getWorth(){
		return type.getWorth();
	}
	
}