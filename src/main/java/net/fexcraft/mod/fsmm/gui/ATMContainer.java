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
import net.fexcraft.mod.fsmm.api.AccountPermission;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.Manageable.Action;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.events.ATMEvent.GatherAccounts;
import net.fexcraft.mod.fsmm.events.ATMEvent.SearchAccounts;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

public class ATMContainer extends GenericContainer {
	
	protected ArrayList<Entry<String, String>> banks;
	protected ArrayList<AccountPermission> accounts;
	protected PlayerCapability cap;
	protected AccountPermission perm;
	protected Account account;
	protected Bank bank;

	public ATMContainer(EntityPlayer player){
		super(player);
		cap = player.getCapability(FSMMCapabilities.PLAYER, null);
		perm = cap.getSelectedAccountInATM() == null ? AccountPermission.FULL : cap.getSelectedAccountInATM();
		account = cap.getSelectedAccountInATM() == null ? cap.getAccount() : perm.getAccount();
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
					if(packet.hasKey("account_list")){
						accounts = new ArrayList<>();
						NBTTagList list = (NBTTagList)packet.getTag("account_list");
						for(int i = 0; i < list.tagCount(); i++){
							accounts.add(new AccountPermission(list.getCompoundTagAt(i)));
						}
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
					if(packet.getBoolean("account_list")){
						GatherAccounts event = new GatherAccounts(player);
						MinecraftForge.EVENT_BUS.post(event);
						accounts = event.getAccountsList();
						NBTTagList list = new NBTTagList();
						accounts.forEach(account -> {
							list.appendTag(account.toNBT());
						});
						compound.setTag("account_list", list);
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
					if(!perm.manage){
						Print.chat(player, "&cYou do not have permission to manage this account.");
						player.closeScreen();
						break;
					}
					Bank bank = DataManager.getBank(packet.getString("bank"), true, true);
					String feeid = account.getType() + ":setup_account";
					long fee = bank.hasFee(feeid) ? Long.parseLong(bank.getFees().get(feeid).replace("%", "")) : 0;
					if(account.getBalance() < fee){
						Print.chat(player, "&eNot enough money on account to pay the move/setup fee.");
						player.closeScreen();
					}
					else{
						if(fee > 0) account.modifyBalance(Action.SUB, fee, player);
						account.setBankId(bank.getId());
						player.openGui(FSMM.getInstance(), ATM_MAIN, player.world, 0, 0, 0);
					}
					break;
				}
				case "account_select":{
					AccountPermission acc = null;
					String type = packet.getString("type"), id = packet.getString("id");
					int mode = packet.getInteger("mode");
					for(AccountPermission perm : accounts){
						if(perm.getAccount().getType().equals(type) && perm.getAccount().getId().equals(id)){
							acc = perm;
							break;
						}
					}
					if(acc != null){
						if(mode == 0){
							cap.setSelectedAccountInATM(acc);
							player.openGui(FSMM.MODID, GuiHandler.ATM_MAIN, player.world, 0, 0, 0);
						}
						if(mode == 1){
							cap.setSelectedReceiverInATM(acc.getAccount());
							player.openGui(FSMM.MODID, GuiHandler.ACCOUNT_TRANSFER, player.world, 0, 0, 0);
						}
					}
					else{
						Print.chat(player, "&cERROR: Account not found server side.");
						player.closeScreen();
					}
					break;
				}
				case "account_search":{
					String type = packet.getString("type").toLowerCase();
					String id = packet.getString("id").toLowerCase();
					if(type.trim().length() == 0 || id.trim().length() == 0 || id.length() < 3) break;
					NBTTagCompound compound = new NBTTagCompound();
					SearchAccounts event = new SearchAccounts(player, type, id);
					MinecraftForge.EVENT_BUS.post(event);
					accounts = new ArrayList<>();
					accounts.addAll(event.getAccountsMap().values());
					NBTTagList list = new NBTTagList();
					accounts.forEach(account -> {
						list.appendTag(account.toNBT());
					});
					compound.setTag("account_list", list);
					compound.setString("cargo", "sync");
					this.send(Side.CLIENT, compound);
					break;
				}
				case "action_deposit":
				case "action_withdraw":{
					if(processSelfAction(packet.getLong("amount"), packet.getString("cargo").endsWith("deposit"))){
						player.closeScreen();
					}
					break;
				}
				case "action_transfer":{
					
					break;
				}
			}
		}
	}

	private boolean processSelfAction(long amount, boolean deposit){
		if(amount <= 0) return false;
		String dep = deposit ? "&7Deposit" : "&7Withdraw";
		Bank bank = DataManager.getBank(account.getBankId(), true, false);
		if(bank.processAction(deposit ? Bank.Action.DEPOSIT : Bank.Action.WITHDRAW, player, account, amount, account)){
			Print.chat(player, dep + " of &e" + Config.getWorthAsString(amount, false) + " &7processed.");
			return true;
		}
		else{
			Print.chat(player, dep + " &cfailed&7.");
			return false;
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
