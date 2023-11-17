package net.fexcraft.mod.fsmm.util;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Money;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemManager {
	
	public static long countInInventory(ICommandSender sender){
		return sender instanceof EntityPlayer ? countInInventory((EntityPlayer)sender) : -1;
	}
	
	public static long countInInventory(EntityPlayer player){
		long value = 0l;
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.isEmpty()) continue;
			long worth = Config.getItemStackWorth(stack);
			Print.debug(stack.toString(), stack.getItem() instanceof Money.Item ? ((Money.Item)stack.getItem()).getType().toString() : "not internal money item");
			value += worth * stack.getCount();
		}
		return value;
	}
	
	public static boolean hasSpace(EntityPlayer player, boolean countMoneyItemAsSpace){
		int i = 0;
		for(ItemStack stack : player.inventory.mainInventory){
			while(i >= 1) break;
			if(stack.isEmpty()){
				i++;
			}
			else if(Config.getItemStackWorth(stack) > 0 && countMoneyItemAsSpace){
				i++;
			}
			else{
				continue;
			}
		}
		return i > 0;
	}
	
	public static long addToInventory(EntityPlayer player, long amount){
		return setInInventory(player, (amount += countInInventory(player)) > Long.MAX_VALUE ? Long.MAX_VALUE : amount);
	}

	public static long removeFromInventory(EntityPlayer player, long amount){
		long old = countInInventory(player);
		old -= amount; if(old < 0){ amount += old; old = 0; }
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.isEmpty()) continue;
			if(Config.getItemStackWorth(stack) > 0){
				stack.shrink(64);
			}
		}
		setInInventory(player, old);
		return amount;
	}
	
	public static long setInInventory(EntityPlayer player, long amount){
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.isEmpty()) continue;
			if(Config.getItemStackWorth(stack) > 0){
				stack.shrink(64);
			}
		}
		List<Money> list = FSMM.getSortedMoneyList();
		Money money = null;
		for(int i = 0; i < list.size(); i++){
			Print.debug(list.get(i).getWorth(), list.get(i).getRegistryName());
			while(amount - (money = list.get(i)).getWorth() >= 0){
				ItemStack stack = money.getItemStack().copy();
				if(hasSpace(player, false)){
					player.inventory.addItemStackToInventory(stack);
				}
				else{
					player.getEntityWorld().spawnEntity(new EntityItem(player.getEntityWorld(), player.posX, player.posY, player.posZ, stack));
				}
				amount -= money.getWorth();
			}
			continue;
		}
		if(amount > 0){
			Print.chat(player, Config.getWorthAsString(amount, true, true) + " couldn't be added to inventory because no matching items were found.");
		}
		return amount;
	}

}