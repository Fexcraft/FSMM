package net.fexcraft.mod.fsmm.gui;

import com.google.gson.JsonObject;
import com.jcraft.jogg.Packet;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketJsonObject;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Processor implements IPacketListener<PacketJsonObject> {

	@Override
	public String getId(){
		return "fsmm_atm_gui";
	}

	@Override
	public void process(PacketJsonObject pkt, Object[] objs){
		try{
			if(Config.DEBUG){
				Print.log("PKT R - Server: " + pkt.obj.toString());
			}
			JsonObject obj = JsonUtil.getJsonForPacket("fsmm_atm_gui");
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getServer().getPlayerList().getPlayerByUsername(pkt.obj.get("sender").getAsString());
			Account account = AccountManager.INSTANCE.getAccount("player", player.getGameProfile().getId().toString(), true);
			String[] amount = pkt.obj.get("amount").getAsString().split(Config.INVERT_COMMA ? "." : ",");
			//TOOD
			long value= 0;
			if(pkt.obj.has("task")){
				switch(pkt.obj.get("task").getAsString()){
					case "get_balance":
						obj.addProperty("balance", account.getBalance());
						obj.addProperty("log", "");
						break;
					case "deposit":
						obj.addProperty("log", "Deposited: " + AccountManager.INSTANCE.getBank(account.getBankId()).processDeposit(player, account, value));
						obj.addProperty("balance", account.getBalance());
						break;
					case "transfer":
						String rec = pkt.obj.get("receiver").getAsString();
						String[] ar = rec.split(":");
						Account receiver = null;
						if(ar.length > 1){
							receiver = AccountManager.INSTANCE.getAccount(ar[0], ar[1]);
						}
						else{
							receiver = AccountManager.INSTANCE.getAccount("player", FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(rec).getId().toString());
						}
						obj.addProperty("log", "Transferred: " + AccountManager.INSTANCE.getBank(account.getBankId()).processTransfer(account, value, receiver));
						obj.addProperty("balance", account.getBalance());
						break;
					case "withdraw":
						obj.addProperty("log", "Withdrawn: " + AccountManager.INSTANCE.getBank(account.getBankId()).processWithdraw(player, account, value));
						obj.addProperty("balance", account.getBalance());
						break;
					default:
						return;
				}
			}
			PacketHandler.getInstance().sendTo(new PacketJsonObject(obj), player);
			if(Config.DEBUG){
				Print.log("PKT S - Client: " + obj.toString());
			}
		}
		catch(Exception ex){
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getServer().getPlayerList().getPlayerByUsername(pkt.obj.get("sender").getAsString());
			JsonObject obj = JsonUtil.getJsonForPacket("fsmm_atm_gui");
			obj.addProperty("log", ex.getStackTrace().toString());
			PacketHandler.getInstance().sendTo(new PacketJsonObject(obj), player);
			if(Config.DEBUG){
				Print.log("PKT S - Server: " + obj.toString());
			}
		}
	}
	
}