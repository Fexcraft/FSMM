package net.fexcraft.mod.fsmm.account;

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
	
	public static long countMoneyInInventoryOf(EntityPlayer player){
		long value = 0l;
		for(int in = 0; in < player.inventory.mainInventory.size(); in++){
			NonNullList<ItemStack> is = player.inventory.mainInventory;
			if(is.get(in) != null && is.get(in).getItem() instanceof MoneyItem){
				value = value + (((MoneyItem)is.get(in).getItem()).getWorth() * is.get(in).getCount());
			}
		}
		return value;//Util.round(value);
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
		long old = countMoneyInInventoryOf(player);
		amount += old;
		//amount = Util.round(amount);
		removeFromInventory(player, old);
		List<Money> list = FSMM.getSortedMoneyList();
		//list = Util.reverse(list);
		for(int i = 0; i < list.size(); i++){
			while(amount - list.get(i).getWorth() >= 0){
				//amount = Util.round(amount);
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
			Print.chat(player, amount  + "F$ couldn't be added to inventory cause no matching items were found.");
		}
	}

	public static void removeFromInventory(EntityPlayer player, long amount){
		float old = countMoneyInInventoryOf(player);
		old -= amount;
		//old = Util.round(old);
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
	
	public static void setInInventory(EntityPlayer player, float amount){
		for(int i = 0; i < player.inventory.mainInventory.size(); i++){
			if(player.inventory.mainInventory.get(i) == null){
				continue;
			}
			if(player.inventory.mainInventory.get(i).getItem() instanceof MoneyItem){
				player.inventory.removeStackFromSlot(i);
			}
		}
		
		List<Money> list = FSMM.getSortedMoneyList();
		//list = Util.reverse(list);
		for(int i = 0; i < list.size(); i++){
			while(amount - list.get(i).getWorth() >= 0){
				//amount = Util.round(amount);
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
			Print.chat(player, amount  + "F$ couldn't be added to inventory cause no matching items were found.");
		}
	}

}