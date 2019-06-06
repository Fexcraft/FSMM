package net.fexcraft.mod.fsmm.impl.votifier;

import com.github.upcraftlp.votifier.api.RewardCreatedEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VotifierEvents {
	
	@SubscribeEvent
	public void onRewardParse(RewardCreatedEvent event){
		if(event.getRewardType().equals("fsmm_item")){
			event.setRewardResult(new ItemReward(event.getJson()));
		}
		else if(event.getRewardType().equals("fsmm_currency")){
			event.setRewardResult(new CurrencyReward(event.getJson()));
		} else return;
	}

}
