package net.fexcraft.mod.fsmm.impl.votifier;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.reward.Reward;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CurrencyReward extends Reward {
	
	private long amount;

	public CurrencyReward(JsonObject json){
		this.amount = JsonUtil.getIfExists(json, "amount", 1000l).longValue();
		Print.debug("Created CURRENCY_REWARD with a worth of " + amount + "!");
	}

	@Override
	public String getType(){
		return "fsmm_currency";
	}

	@Override
	public void activate(MinecraftServer server, EntityPlayer player, String timestamp, String service, String address) throws RewardException {
		Account account = player.getCapability(FSMMCapabilities.PLAYER, null).getAccount(); account.setBalance(account.getBalance() + amount);
		Print.chat(player, "&9" + Config.getWorthAsString(amount) + " &7was added into your account for voting.");
	}

}