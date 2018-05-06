package net.fexcraft.mod.fsmm.gui;

import com.google.gson.JsonObject;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketJsonObject;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.entity.player.EntityPlayerMP;

public class Processor implements IPacketListener<PacketJsonObject> {

	@Override
	public String getId(){
		return "fsmm:atm_gui";
	}

	@Override
	public void process(PacketJsonObject pkt, Object[] objs){
		Print.debug(pkt.obj);
		if(pkt.obj.has("request")){
			EntityPlayerMP player = (EntityPlayerMP)objs[0];
			Account playeracc = AccountManager.INSTANCE.getAccount("player", player.getGameProfile().getId().toString(), true);
			JsonObject reply = new JsonObject();
			switch(pkt.obj.get("request").getAsString()){
				case "main_data":{
					reply.addProperty("bank_id", playeracc.getBankId().toString());
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("bank_name", bank == null ? "Invalid Null Bank" : bank.getName());
					break;
				}
				case "show_balance":{
					reply.addProperty("balance", playeracc.getBalance());
					break;
				}
				case "deposit_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("success", bank.processDeposit(player, playeracc, input));
					break;
				}
				case "withdraw_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("success", bank.processWithdraw(player, playeracc, input));
				}
			}
			reply.addProperty("payload", pkt.obj.get("request").getAsString());
			reply.addProperty("target_listener", "fsmm:atm_gui");
			PacketHandler.getInstance().sendTo(new PacketJsonObject(reply), player);
		}
		/*try{
			if(Config.DEBUG){
				Print.log("PKT R - Server: " + pkt.obj.toString());
			}
			JsonObject obj = JsonUtil.getJsonForPacket("fsmm_atm_gui");
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getServer().getPlayerList().getPlayerByUsername(pkt.obj.get("sender").getAsString());
			Account account = AccountManager.INSTANCE.getAccount("player", player.getGameProfile().getId().toString(), true);
			if(pkt.obj.get("task").getAsString().equals("get_balance")){
				obj.addProperty("balance", account.getBalance());
				obj.addProperty("log", "");
			}
			else{
				String[] amount = pkt.obj.get("amount").getAsString().split(Config.INVERT_COMMA ? "." : ",");
				long value = Long.parseLong(amount[0].replace(Config.INVERT_COMMA ? "," : ".", "")) * 1000;
				if(amount.length == 2){
					amount[1] += amount[1].length() == 1 ? "00" : amount[1].length() == 2 ? "0" : "";
					value += Long.parseLong(amount[1]);
				}
				if(pkt.obj.has("task")){
					switch(pkt.obj.get("task").getAsString()){
						case "deposit":
							obj.addProperty("log", "Deposited: " + AccountManager.INSTANCE.getBank(account.getBankId()).processDeposit(player, account, value));
							break;
						case "transfer":
							boolean loaded = false;
							String rec = pkt.obj.get("receiver").getAsString();
							String[] ar = rec.split(":");
							Account receiver = null;
							UUID uuid = null;
							if(ar.length > 1 && !ar[0].equals("player")){
								receiver = AccountManager.INSTANCE.getAccount(ar[0], ar[1]);
							}
							else{
								try{
									uuid = UUID.fromString(ar.length > 1 ? ar[1] : ar[0]);
								}
								catch(Exception e){
									uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(ar.length > 1 ? ar[1] : ar[0]).getId();
								}
								receiver = AccountManager.INSTANCE.getAccount("player", uuid.toString());
							}
							loaded = receiver != null;
							if(!loaded){
								receiver = AccountManager.INSTANCE.getAccount("player", uuid.toString(), true);
							}
							obj.addProperty("log", "Transferred: " + AccountManager.INSTANCE.getBank(account.getBankId()).processTransfer(player, account, value, receiver));
							if(!loaded){
								AccountManager.INSTANCE.unloadAccount(receiver);
							}
							break;
						case "withdraw":
							obj.addProperty("log", "Withdrawn: " + AccountManager.INSTANCE.getBank(account.getBankId()).processWithdraw(player, account, value));
							break;
						default:
							return;
					}
				}
				obj.addProperty("balance", account.getBalance());
			}
			PacketHandler.getInstance().sendTo(new PacketJsonObject(obj), player);
			if(Config.DEBUG){
				Print.log("PKT S - Client: " + obj.toString());
			}
		}
		catch(Exception ex){
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getServer().getPlayerList().getPlayerByUsername(pkt.obj.get("sender").getAsString());
			JsonObject obj = JsonUtil.getJsonForPacket("fsmm_atm_gui");
			obj.addProperty("log", ex.getMessage() == null ? "null" : ex.getMessage());
			PacketHandler.getInstance().sendTo(new PacketJsonObject(obj), player);
			if(Config.DEBUG){
				Print.log("PKT S - Server: " + obj.toString());
				ex.printStackTrace();
			}
		}*/
	}
	
}