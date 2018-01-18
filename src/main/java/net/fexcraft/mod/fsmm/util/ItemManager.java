package net.fexcraft.mod.fsmm.util;

import java.util.List;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.registry.RegistryUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemManager {
	
	public static long countInInventory(EntityPlayer player){
		long value = 0l;
		for(int in = 0; in < player.inventory.mainInventory.size(); in++){
			NonNullList<ItemStack> is = player.inventory.mainInventory;
			if(!is.get(in).isEmpty() && is.get(in).getItem() instanceof MoneyItem){
				MoneyItem item = (MoneyItem)is.get(in).getItem();
				Print.debug(item, item.getMoneyType());
				value += item.getWorth() * is.get(in).getCount();
			}
		}
		return value;
	}
	
	public static boolean hasSpace(EntityPlayer player, boolean countMoneyItemAsSpace){
		int i = 0;
		for(ItemStack stack : player.inventory.mainInventory){
			while(i >= 1){
				break;
			}
			if(stack == null || stack.isEmpty()){
				i++;
			}
			else if(stack.getItem() instanceof MoneyItem && countMoneyItemAsSpace){
				i++;
			}
			else{
				continue;
			}
		}
		return i == 0 ? false : true;
	}
	
	public static void addToInventory(EntityPlayer player, long amount){
		setInInventory(player, (amount += countInInventory(player)) > Long.MAX_VALUE ? Long.MAX_VALUE : amount);
	}

	public static void removeFromInventory(EntityPlayer player, long amount){
		long old = countInInventory(player);
		old -= amount;
		if(old < 0){
			old = 0;
		}
		for(int i = 0; i < player.inventory.mainInventory.size(); i++){
			if(player.inventory.mainInventory.get(i) == null){
				continue;
			}
			if(player.inventory.mainInventory.get(i).getItem() instanceof MoneyItem){
				player.inventory.removeStackFromSlot(i);
			}
		}
		setInInventory(player, old);
	}
	
	public static void setInInventory(EntityPlayer player, long amount){
		for(int i = 0; i < player.inventory.mainInventory.size(); i++){
			if(player.inventory.mainInventory.get(i) == null){
				continue;
			}
			if(player.inventory.mainInventory.get(i).getItem() instanceof MoneyItem){
				player.inventory.removeStackFromSlot(i);
			}
		}
		List<Money> list = FSMM.getSortedMoneyList();
		for(int i = 0; i < list.size(); i++){
			while(amount - list.get(i).getWorth() >= 0){
				ItemStack stack = new ItemStack(RegistryUtil.getItem(list.get(i).getRegistryName()), 1);
				if(hasSpace(player, false)){
					player.inventory.addItemStackToInventory(stack);
				}
				else{
					player.getEntityWorld().spawnEntity(new EntityItem(player.getEntityWorld(), player.posX, player.posY, player.posZ, stack));
				}
				amount -= list.get(i).getWorth();
			}
			continue;
		}
		if(amount > 0){
			Print.chat(player, Config.getWorthAsString(amount, true, true) + " couldn't be added to inventory cause no matching items were found.");
		}
	}

}