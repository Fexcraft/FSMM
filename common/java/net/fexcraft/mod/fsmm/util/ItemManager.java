package net.fexcraft.mod.fsmm.util;

import java.util.List;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.item.StackWrapper;
import net.fexcraft.mod.uni.world.EntityW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ItemManager {

	public static long countInInventory(Object player){
		UniEntity ent = UniEntity.get(player);
		return ent == null ? -1 : countInInventory(ent.entity);
	}
	
	public static long countInInventory(EntityW player){
		if(!player.isPlayer()) return -1;
		long value = 0l;
		StackWrapper wrapper = null;
		for(int i = 0; i < player.getInventorySize(); i++){
			wrapper = player.getStackAt(i);
			if(wrapper.empty()) continue;
			long worth = Config.getStackWorth(wrapper);
			value += worth * wrapper.count();
		}
		return value;
	}
	
	public static boolean hasSpace(EntityW player, boolean countMoneyItemAsSpace){
		StackWrapper wrapper = null;
		for(int idx = 0; idx < player.getInventorySize(); idx++){
			wrapper = player.getStackAt(idx);
			if(wrapper.empty() || (countMoneyItemAsSpace && Config.getStackWorth(wrapper) > 0)) return true;
		}
		return false;
	}
	
	public static long addToInventory(EntityW player, long amount){
		return setInInventory(player, (amount += countInInventory(player)) >= Long.MAX_VALUE ? Long.MAX_VALUE : amount);
	}

	public static long removeFromInventory(EntityW player, long amount){
		long old = countInInventory(player);
		old -= amount;
		if(old < 0){
			amount += old;
			old = 0;
		}
		StackWrapper wrapper = null;
		for(int idx = 0; idx < player.getInventorySize(); idx++){
			wrapper = player.getStackAt(idx);
			if(wrapper.empty()) continue;
			if(Config.getStackWorth(wrapper) > 0){
				wrapper.count(0);
			}
		}
		setInInventory(player, old);
		return amount;
	}
	
	public static long setInInventory(EntityW player, long amount){
		StackWrapper wrapper = null;
		for(int idx = 0; idx < player.getInventorySize(); idx++){
			wrapper = player.getStackAt(idx);
			if(wrapper.empty()) continue;
			if(Config.getStackWorth(wrapper) > 0){
				wrapper.count(0);
			}
		}
		List<Money> list = DataManager.getSortedMoneyList();
		Money money = null;
		for(int i = 0; i < list.size(); i++){
			while(amount - (money = list.get(i)).getWorth() >= 0){
				StackWrapper stack = money.getStack().copy();
				if(hasSpace(player, false)){
					player.addStack(stack);
				}
				else{
					player.drop(stack, 0.5f);
				}
				amount -= money.getWorth();
			}
		}
		if(amount > 0){
			player.send(Config.getWorthAsString(amount, true, true) + " couldn't be added to inventory because no matching items were found.");
		}
		return amount;
	}

}