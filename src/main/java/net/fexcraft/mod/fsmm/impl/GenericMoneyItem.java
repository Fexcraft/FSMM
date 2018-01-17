package net.fexcraft.mod.fsmm.impl;

import java.util.List;

import javax.annotation.Nullable;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public long getWorth(){
		return type.getWorth();
	}
	
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag){
		tooltip.add(Formatter.format("&9Worth&0: &a" + (type.getWorth() / 1000) + Config.CURRENCY_SIGN));
    }
	
}