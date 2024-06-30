package net.fexcraft.mod.fsmm.data;

import net.fexcraft.lib.mc.api.registry.fItem;
import net.fexcraft.lib.mc.registry.FCLRegistry;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@fItem(modid = "fsmm", name = "mobile")
public class MobileAtm extends Item{

	public MobileAtm(){
		super();
		setCreativeTab(FSMM.tabFSMM);
		setMaxStackSize(1);
	}

	@Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        if(world.isRemote || player.isSneaking() || hand== EnumHand.OFF_HAND){
			return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
		}
		player.openGui(FSMM.getInstance(), FsmmUIKeys.ID12_ATM_MAIN, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
}