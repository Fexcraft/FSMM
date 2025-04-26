package net.fexcraft.mod.fsmm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fexcraft.mod.fsmm.util.Config;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.getFormatted;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FSMMC implements ClientModInitializer {

	@Override
	public void onInitializeClient(){
		if(!Config.SHOW_ITEM_WORTH) return;
		ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			if(!Config.SHOW_ITEM_WORTH) return;
			long worth = Config.getStackWorth(stack);
			if(worth <= 0) return;
			String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
			if(stack.getCount() > 1){
				str += " &8(&7" + Config.getWorthAsString(worth * stack.getCount(), true, worth < 10) + "&8)";
			}
			lines.add(getFormatted(str));
		});
	}

}