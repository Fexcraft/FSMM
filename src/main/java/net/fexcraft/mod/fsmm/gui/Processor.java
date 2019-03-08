package net.fexcraft.mod.fsmm.gui;

import java.io.File;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.FMLCommonHandler;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.IPacketListener;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.fexcraft.mod.lib.fcl.PacketJsonObject;
import net.minecraft.entity.player.EntityPlayerMP;

public class Processor implements IPacketListener<PacketJsonObject> {

	@Override
	public String getId(){
		return "fsmm:atm_gui";
	}

	@Override
	public void process(PacketJsonObject pkt, Object[] objs){
		Print.debug(pkt.obj.getAsString());
		if(pkt.obj.has("request")){
			EntityPlayerMP player = (EntityPlayerMP)objs[0];
			Account playeracc = DataManager.getAccount("player:" + player.getGameProfile().getId().toString(), false, false, null);
			JsonObject reply = new JsonObject();
			switch(pkt.obj.get("request").getAsString()){
				case "main_data":{
					reply.addProperty("bank_id", playeracc.getBankId().toString());
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, true);
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
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					reply.addProperty("success", bank.processAction(Bank.Action.DEPOSIT, player, null, input, playeracc));
					break;
				}
				case "withdraw_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					reply.addProperty("success", bank.processAction(Bank.Action.WITHDRAW, player, playeracc, input, null));
					break;
				}
				case "account_types":{
					JsonArray types = new JsonArray();
					for(File fl : DataManager.ACCOUNT_DIR.listFiles()){
						if(fl.isDirectory() && !fl.isHidden()){
							types.add(JsonUtil.makeFromString(fl.getName()));
						}
					}
					if(types.size() == 0){
						types.add(JsonUtil.makeFromString("nothing found"));
					}
					reply.add("types", types);
					break;
				}
				case "accounts_of_type":{
					File file = new File(DataManager.ACCOUNT_DIR, pkt.obj.get("type").getAsString() + "/");
					JsonArray accounts = new JsonArray();
					if(file.exists() && file.isDirectory()){
						for(File fl : file.listFiles()){
							if(!fl.isDirectory() && !fl.isHidden() && fl.getName().endsWith(".json")){
								accounts.add(JsonUtil.makeFromString(fl.getName().substring(0, fl.getName().length() - 5)));
							}
						}
					}
					else{
						accounts.add(JsonUtil.makeFromString("type not found"));
					}
					if(accounts.size() == 0){
						accounts.add(JsonUtil.makeFromString("nothing found"));
					}
					reply.add("accounts", accounts);
					break;
				}
				case "transfer_result":{
					long input = pkt.obj.get("input").getAsLong(); if(input <= 0){ return; }
					Account receiver = null;
					if(!pkt.obj.get("receiver").getAsString().startsWith("player:")){
						receiver = DataManager.getAccount(pkt.obj.get("receiver").getAsString(), true, false);
					}
					else{
						String str = pkt.obj.get("receiver").getAsString().replace("player:", "");
						try{
							UUID.fromString(str);
							receiver = DataManager.getAccount(pkt.obj.get("receiver").getAsString(), true, false);
						}
						catch(Exception e0){ if(Print.dev()) e0.printStackTrace();
							try{
								UUID uuid = FMLCommonHandler.instance().getMinecraftServerInstance().func_152358_ax().func_152655_a(str).getId();
								receiver = DataManager.getAccount("player:" + uuid.toString(), true, false);
							}
							catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}
					if(receiver == null){
						Print.chat(player, "Error loading Receiver account.\n(" + pkt.obj.get("receiver").getAsString() + ");");
						return;
					}
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					if(bank == null){ Print.chat(player, "Error, bank not loaded."); return; }
					reply.addProperty("success", bank.processAction(Bank.Action.TRANSFER, player, playeracc, input, receiver));
					reply.addProperty("receiver", receiver.getAsResourceLocation().toString());
					break;
				}
			}
			reply.addProperty("payload", pkt.obj.get("request").getAsString());
			reply.addProperty("target_listener", "fsmm:atm_gui");
			PacketHandler.getInstance().sendTo(new PacketJsonObject(reply), player);
		}
	}
	
}