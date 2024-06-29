package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.blocks.ATM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import static net.fexcraft.mod.fsmm.util.FsmmUIKeys.*;

public class GuiHandler implements IGuiHandler {
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == ID12_ATM_ACC_DEPOSIT || ID == ID12_ATM_ACC_WITHDRAW){
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
			case ID12_ATM_MAIN:
				return new ATMMain(player, new int[]{ x, y, z });
			case ID12_ATM_BANK_INFO:
				return new ATMBankInfo(player, new int[]{ x, y, z });
			case ID12_ATM_BANK_SELECT:
				return new ATMBankSelect(player, new int[]{ x, y, z });
			case ID12_ATM_TRANSFERS:
				return new ATMViewTransfers(player, new int[]{ x, y, z });
			case ID12_ATM_ACC_SELECT:
			case ID12_ATM_ACC_RECEIVER:
				return new ATMAccountSelect(player, ID == ID12_ATM_ACC_SELECT, new int[]{ x, y, z });
			case ID12_ATM_ACC_WITHDRAW:
			case ID12_ATM_ACC_DEPOSIT:
				if(player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof ATM == false){
					Print.chat(player, "Action not available via Mobile Banking.");
					return null;
				}
				return new ATMAccountSelf(player, ID == ID12_ATM_ACC_DEPOSIT, new int[]{ x, y, z });
			case ID12_ATM_ACC_TRANSFER:
				return new ATMAccountTransfer(player, new int[]{ x, y, z });
			default:
				return null;
		}
    }
}
