package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.blocks.ATM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int ATM_MAIN = 0;
	public static final int BANK_INFO = 10;
	public static final int BANK_SELECT = 11;
	public static final int VIEW_TRANSFERS = 19;
	public static final int ACCOUNT_SELECT_ACTIVE = 200;
	public static final int ACCOUNT_SELECT_RECEIVER = 201;
	public static final int ACCOUNT_WITHDRAW = 21;
	public static final int ACCOUNT_DEPOSIT= 22;
	public static final int ACCOUNT_TRANSFER = 23;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == ACCOUNT_DEPOSIT || ID == ACCOUNT_WITHDRAW){
			if(player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof ATM == false){
				Print.chat(player, "Action not available via Mobile Banking.");
				return null;
			}
		}
		return new ATMContainer(player, new int[]{ x, y, z });
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch(ID){
			case ATM_MAIN:
				return new ATMMain(player, new int[]{ x, y, z });
			case BANK_INFO:
				return new ATMBankInfo(player, new int[]{ x, y, z });
			case BANK_SELECT:
				return new ATMBankSelect(player, new int[]{ x, y, z });
			case VIEW_TRANSFERS:
				return new ATMViewTransfers(player, new int[]{ x, y, z });
			case ACCOUNT_SELECT_ACTIVE:
			case ACCOUNT_SELECT_RECEIVER:
				return new ATMAccountSelect(player, ID == ACCOUNT_SELECT_ACTIVE, new int[]{ x, y, z });
			case ACCOUNT_WITHDRAW:
			case ACCOUNT_DEPOSIT:
				if(player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof ATM == false){
					Print.chat(player, "Action not available via Mobile Banking.");
					return null;
				}
				return new ATMAccountSelf(player, ID == ACCOUNT_DEPOSIT, new int[]{ x, y, z });
			case ACCOUNT_TRANSFER:
				return new ATMAccountTransfer(player, new int[]{ x, y, z });
			default:
				return null;
		}
    }
}
