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
	
	/*@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag){
		tooltip.add(Formatter.format("&9Worth&0: &a" + Config.getWorthAsString(type.getWorth())));
    }*/
	
}