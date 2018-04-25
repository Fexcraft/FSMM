package net.fexcraft.mod.fsmm.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int NEW_ATM = 0;
	public static final int OLD_ATM = -1, SAFE = 1;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch(ID){
			case 0:
				return new AutomatedTellerMashineGui(player, world, x, y, z);
			case 1:
				return true;
			default:
				return null;
		}
    }
}
