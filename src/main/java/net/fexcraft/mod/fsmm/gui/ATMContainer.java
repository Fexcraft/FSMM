package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.GuiHandler.ATM_MAIN;
import static net.fexcraft.mod.fsmm.gui.GuiHandler.BANK_INFO;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.Manageable.Action;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;

public class ATMContainer extends GenericContainer {
	
	protected ArrayList<Entry<String, String>> banks;
	protected PlayerCapability cap;
	protected Account account;
	protected Bank bank;

	public ATMContainer(EntityPlayer player){
		super(player);
		cap = player.getCapability(FSMMCapabilities.PLAYER, null);
		account = cap.getSelectedAccountInATM() == null ? cap.getAccount() : DataManager.getAccount(cap.getSelectedAccountInATM(), true, true);
		bank = DataManager.getBank(cap.getSelectedBankInATM() == null ? account.getBankId() : cap.getSelectedBankInATM(), true, true);
		cap.setSelectedBankInATM(null);
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		Print.debug(side, packet);
		if(side.isClient()){
			switch(packet.getString("cargo")){
				case "sync":{
					if(packet.hasKey("account")){
						account = new Account(JsonUtil.getObjectFromString(packet.getString("account")));
					}
					if(packet.hasKey("bank")){
						bank = new GenericBank(JsonUtil.getObjectFromString(packet.getString("bank")));
					}
					if(packet.hasKey("bank_list")){
						TreeMap<String, String> banks = new TreeMap<>();
						NBTTagList list = (NBTTagList)packet.getTag("bank_list");
						for(int i = 0; i < list.tagCount(); i++){
							String[] str = list.getStringTagAt(i).split(":");
							if(bank != null && str[0].equals(bank.getId())) continue;
							banks.put(str[0], str[1]);
						}
						this.banks = new ArrayList<>();
						this.banks.addAll(banks.entrySet());
					}
					break;
				}
			}
		}
		else{
			switch(packet.getString("cargo")){
				case "sync":{
					NBTTagCompound compound = new NBTTagCompound();
					if(packet.getBoolean("account")){
						compound.setString("account", account.toJson().toString());
					}
					if(packet.getBoolean("bank")){
						compound.setString("bank", bank.toJson().toString());
					}
					if(packet.getBoolean("bank_list")){
						compound.setTag("bank_list", getBankList());
					}
					compound.setString("cargo", "sync");
					this.send(Side.CLIENT, compound);
					break;
				}
				case "bank_info":{
					cap.setSelectedBankInATM(packet.getString("bank"));
					cap.getEntityPlayer().openGui(FSMM.getInstance(), BANK_INFO, player.world, 0, 0, 0);
					break;
				}
				case "bank_select":{
					Bank bank = DataManager.getBank(packet.getString("bank"), true, true);
					String feeid = account.getType() + ":setup_account";
					long fee = bank.hasFee(feeid) ? Long.parseLong(bank.getFees().get(feeid).replace("%", "")) : 0;
					if(account.getBalance() < fee){
						Print.chat(player, "Not enough money on account to pay the move/setup fee.");
						player.closeScreen();
					}
					else{
						if(fee > 0) account.modifyBalance(Action.SUB, fee, player);
						account.setBankId(bank.getId());
						player.openGui(FSMM.getInstance(), ATM_MAIN, player.world, 0, 0, 0);
					}
					break;
				}
			}
		}
	}

	private NBTBase getBankList(){
		NBTTagList list = new NBTTagList();
		DataManager.getBankNameCache().forEach((key, val) -> {
			list.appendTag(new NBTTagString(key + ":" + val));
		});
		return list;
	}

	public void sync(String... types){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("cargo", "sync");
		for(String str : types){
			compound.setBoolean(str, true);
		}
		this.send(Side.SERVER, compound);
	}

}
