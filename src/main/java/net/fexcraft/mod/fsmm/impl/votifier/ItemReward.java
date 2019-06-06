package net.fexcraft.mod.fsmm.impl.votifier;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.reward.Reward;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class ItemReward extends Reward {
	
	private long worth;

	public ItemReward(JsonObject json){
		this.worth = JsonUtil.getIfExists(json, "worth", 1000l).longValue();
		Print.debug("Created ITEM_REWARD with a worth of " + worth + "!");
	}

	@Override
	public String getType(){
		return "fsmm_item";
	}

	@Override
	public void activate(MinecraftServer server, EntityPlayer player, String timestamp, String service, String address) throws RewardException {
		ItemManager.addToInventory(player, worth); Print.chat(player, "&7You received &9" + Config.getWorthAsString(worth) + " &7for voting!");
	}

}
