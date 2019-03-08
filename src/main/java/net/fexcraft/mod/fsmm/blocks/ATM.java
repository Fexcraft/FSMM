package net.fexcraft.mod.fsmm.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

public class ATM extends Block {
	
    public static Block INSTANCE;

	public ATM(){
    	super(new Material(MapColor.stoneColor));
    	this.setCreativeTab(FSMM.tabFSMM);
    	this.setHarvestLevel("pickaxe", 1);
    	this.setHardness(1.0F);
    	this.setResistance(10.0F);
    	this.setBlockName("fsmm:atm");
    	GameRegistry.registerBlock(INSTANCE = this, ItemBlack.class, "atm");
	}
	
	public static class ItemBlack extends ItemBlock {

		public ItemBlack(Block block){
			super(block); this.setUnlocalizedName("fsmm:atm");
		}
		
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
		if(!player.isSneaking()){
			player.openGui(FSMM.getInstance(), GuiHandler.NEW_ATM, world, x, y, z);
			return true;
		} else return false;
	}
	
}