package net.fexcraft.mod.fsmm.gui;

import java.io.File;
import java.util.UUID;

import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class Processor implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "fsmm:atm_gui";
	}

	@Override
	public void process(PacketNBTTagCompound pkt, Object[] objs){
		Print.debug(pkt.nbt);
		if(pkt.nbt.hasKey("request")){
			EntityPlayerMP player = (EntityPlayerMP)objs[0];
			Account playeracc = DataManager.getAccount("player:" + player.getGameProfile().getId().toString(), false, false, null);
			NBTTagCompound reply = new NBTTagCompound();
			switch(pkt.nbt.getString("request")){
				case "main_data":{
					reply.setString("bank_id", playeracc.getBankId().toString());
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, true);
					reply.setString("bank_name", bank == null ? "Invalid Null Bank" : bank.getName());
					break;
				}
				case "show_balance":{
					reply.setLong("balance", playeracc.getBalance());
					break;
				}
				case "deposit_result":{
					long input = pkt.nbt.getLong("input");
					if(input <= 0){ return; }
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					reply.setBoolean("success", bank.processAction(Bank.Action.DEPOSIT, player, null, input, playeracc));
					break;
				}
				case "withdraw_result":{
					long input = pkt.nbt.getLong("input");
					if(input <= 0){ return; }
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					reply.setBoolean("success", bank.processAction(Bank.Action.WITHDRAW, player, playeracc, input, null));
					break;
				}
				case "account_types":{
					NBTTagList types = new NBTTagList();
					for(File fl : DataManager.ACCOUNT_DIR.listFiles()){
						if(fl.isDirectory() && !fl.isHidden()){
							types.appendTag(new NBTTagString(fl.getName()));
						}
					}
					if(types.tagCount() == 0){
						types.appendTag(new NBTTagString("nothing found"));
					}
					reply.setTag("types", types);
					break;
				}
				case "accounts_of_type":{
					File file = new File(DataManager.ACCOUNT_DIR, pkt.nbt.getString("type") + "/");
					NBTTagList accounts = new NBTTagList();
					if(file.exists() && file.isDirectory()){
						for(File fl : file.listFiles()){
							if(!fl.isDirectory() && !fl.isHidden() && fl.getName().endsWith(".json")){
								accounts.appendTag(new NBTTagString(fl.getName().substring(0, fl.getName().length() - 5)));
							}
						}
					}
					else{
						accounts.appendTag(new NBTTagString("type not found"));
					}
					if(accounts.tagCount() == 0){
						accounts.appendTag(new NBTTagString("nothing found"));
					}
					reply.setTag("accounts", accounts);
					break;
				}
				case "transfer_result":{
					long input = pkt.nbt.getLong("input"); if(input <= 0){ return; }
					Account receiver = null;
					if(!pkt.nbt.getString("receiver").startsWith("player:")){
						receiver = DataManager.getAccount(pkt.nbt.getString("receiver"), true, false);
					}
					else{
						String str = pkt.nbt.getString("receiver").replace("player:", "");
						try{
							UUID.fromString(str);
							receiver = DataManager.getAccount(pkt.nbt.getString("receiver"), true, false);
						}
						catch(Exception e0){ if(Static.dev()) e0.printStackTrace();
							try{
								UUID uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(str).getId();
								receiver = DataManager.getAccount("player:" + uuid.toString(), true, false);
							}
							catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}
					if(receiver == null){
						Print.chat(player, "Error loading Receiver account.\n(" + pkt.nbt.getString("receiver") + ");");
						return;
					}
					Bank bank = DataManager.getBank(playeracc.getBankId(), true, false);
					if(bank == null){ Print.chat(player, "Error, bank not loaded."); return; }
					reply.setBoolean("success", bank.processAction(Bank.Action.TRANSFER, player, playeracc, input, receiver));
					reply.setString("receiver", receiver.getAsResourceLocation().toString());
					break;
				}
			}
			reply.setString("payload", pkt.nbt.getString("request"));
			reply.setString("target_listener", "fsmm:atm_gui");
			PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(reply), player);
		}
	}
	
}