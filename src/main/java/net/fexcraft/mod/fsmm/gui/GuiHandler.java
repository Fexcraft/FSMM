package net.fexcraft.mod.fsmm.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int ATM_MAIN = 0;
	public static final int BANK_INFO = 10;
	public static final int BANK_SELECT = 11;
	public static final int ACCOUNT_SELECT = 20;
	public static final int ACCOUNT_WITHDRAW = 21;
	public static final int ACCOUNT_DEPOSIT= 22;
	public static final int ACCOUNT_TRANSFER = 23;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ATMContainer(player);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch(ID){
			case ATM_MAIN:
				return new ATMMain(player);
			default:
				return null;
		}
    }
}
