package net.fexcraft.mod.fsmm.local;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MobileAtm extends Item {

	public MobileAtm(Properties prop){
		super(prop.stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand){
		if(level.isClientSide || player.isCrouching() || hand == InteractionHand.OFF_HAND){
			return InteractionResult.PASS;
		}
		UniEntity.get(player).entity.openUI(FsmmUIKeys.UI_ATM_MAIN, V3I.NULL);
		return InteractionResult.SUCCESS;
    }
	
}