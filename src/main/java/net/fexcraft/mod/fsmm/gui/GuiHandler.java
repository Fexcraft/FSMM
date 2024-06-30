package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.blocks.ATM;
import net.fexcraft.mod.uni.ui.UIUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import static net.fexcraft.mod.fsmm.util.FsmmUIKeys.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class GuiHandler implements IGuiHandler {
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == ID12_ATM_ACC_DEPOSIT || ID == ID12_ATM_ACC_WITHDRAW){
			if(player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof ATM == false){
				Print.chat(player, "Action not available via Mobile Banking.");
				return null;
			}
		}
		return UIUtils.getServer("fsmm", ID, player, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		if(ID == ID12_ATM_ACC_DEPOSIT || ID == ID12_ATM_ACC_WITHDRAW){
			if(player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof ATM == false){
				Print.chat(player, "Action not available via Mobile Banking.");
				return null;
			}
		}
		return UIUtils.getClient("fsmm", ID, player, x, y, z);
    }
}
