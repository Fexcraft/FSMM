package net.fexcraft.mod.fsmm.impl.votifier;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VotifierEvents {
	
	@SubscribeEvent
	public void onRewardParse(com.github.upcraftlp.votifier.api.RewardCreatedEvent event){
		if(event.getRewardType().equals("fsmm_item")){
			event.setRewardResult(new ItemReward(event.getJson()));
		}
		else if(event.getRewardType().equals("fsmm_currency")){
			event.setRewardResult(new CurrencyReward(event.getJson()));
		} else return;
	}

	public static void postTestVoteEvent(ICommandSender sender){
		MinecraftForge.EVENT_BUS.post(new com.github.upcraftlp.votifier.api.VoteReceivedEvent(
			(EntityPlayerMP)sender, "FSMM Tester", "localhost", Long.toString(System.nanoTime() / 1_000_000L)));
	}

}
