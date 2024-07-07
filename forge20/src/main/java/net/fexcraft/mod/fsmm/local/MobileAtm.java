package net.fexcraft.mod.fsmm.local;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MobileAtm extends Item {

	public MobileAtm(){
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
		if(level.isClientSide || player.isCrouching() || hand == InteractionHand.OFF_HAND){
			return InteractionResultHolder.pass(player.getMainHandItem());
		}
		UniEntity.get(player).entity.openUI(FsmmUIKeys.UI_ATM_MAIN, V3I.NULL);
		return InteractionResultHolder.success(player.getMainHandItem());
    }
	
}