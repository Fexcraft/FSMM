package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class ATMContainer extends GenericContainer {

	public ATMContainer(EntityPlayer player){
		super(player);
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		//
	}

}
