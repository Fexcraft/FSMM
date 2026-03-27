package net.fexcraft.mod.fsmm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fexcraft.mod.fsmm.local.AtmBlock;
import net.fexcraft.mod.fsmm.local.MobileAtm;
import net.fexcraft.mod.fsmm.local.MoneyItem;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.getFormatted;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FSMMC implements ClientModInitializer {

	public static CreativeModeTab TAB;

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
		TAB = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(AtmBlock.INST))
			.title(Component.literal("Fex's Small Money Mod"))
			.displayItems((con, output) -> {
				output.accept(AtmBlock.INST);
				output.accept(MobileAtm.INST);
				for(MoneyItem item : MoneyItem.sorted) output.accept(item);
			})
			.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.parse("fsmm:main"), TAB);
	}

}