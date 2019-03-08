package net.fexcraft.mod.fsmm.util;

import java.util.List;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
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
		ItemStack[] is = player.inventory.mainInventory;
		ItemStack stack = null;
		/*for(int in = 0; in < player.inventory.mainInventory.length; in++){
			if(!(stack = is[in]!=null && stack.hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null))){
				MoneyCapability cap = stack.getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null);
				Print.debug(stack.toString(), stack.getItem() instanceof Money.Item ? ((Money.Item)stack.getItem()).getType().toString() : "not internal money item");
				value += cap.getWorth() * is[in].stackSize;
			}
		}*///TODO
		return value;
	}
	
	public static boolean hasSpace(EntityPlayer player, boolean countMoneyItemAsSpace){
		int i = 0;
		for(ItemStack stack : player.inventory.mainInventory){
			while(i >= 1){
				break;
			}
			if(stack == null){
				i++;
			}
			/*else if(stack.hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null)
				&& stack.getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null).getWorth() > 0
				&& countMoneyItemAsSpace){
				i++;
			}*///TODO
			else{
				continue;
			}
		}
		return i == 0 ? false : true;
	}
	
	public static long addToInventory(EntityPlayer player, long amount){
		return setInInventory(player, (amount += countInInventory(player)) > Long.MAX_VALUE ? Long.MAX_VALUE : amount);
	}

	public static long removeFromInventory(EntityPlayer player, long amount){
		long old = countInInventory(player);
		old -= amount; if(old < 0){ amount += old; old = 0; }
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			if(player.inventory.mainInventory[i] == null){
				continue;
			}
			/*if(player.inventory.mainInventory[i].hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null)
				&& player.inventory.mainInventory[i].getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null).getWorth() > 0){
				player.inventory.setInventorySlotContents(i, null);
			}*///TODO
		}
		setInInventory(player, old);
		return amount;
	}
	
	public static long setInInventory(EntityPlayer player, long amount){
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			if(player.inventory.mainInventory[i] == null){
				continue;
			}
			/*if(player.inventory.mainInventory[i].hasCapability(FSMMCapabilities.MONEY_ITEMSTACK, null)
				&& player.inventory.mainInventory[i].getCapability(FSMMCapabilities.MONEY_ITEMSTACK, null).getWorth() > 0){
				player.inventory.setInventorySlotContents(i, null);
			}*///TODO
		}
		List<Money> list = FSMM.getSortedMoneyList();
		Money money = null;
		for(int i = 0; i < list.size(); i++){
			Print.debug(list.get(i).getWorth()+"", list.get(i).getRegistryName().toString());
			while(amount - (money = list.get(i)).getWorth() >= 0){
				ItemStack stack = money.getItemStack().copy();
				if(hasSpace(player, false)){
					player.inventory.addItemStackToInventory(stack);
				}
				else{
					player.getEntityWorld().spawnEntityInWorld(new EntityItem(player.getEntityWorld(), player.posX, player.posY, player.posZ, stack));
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