package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.GuiHandler.ATM_MAIN;
import static net.fexcraft.mod.fsmm.gui.GuiHandler.BANK_INFO;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.FSMMCapabilities;
import net.fexcraft.mod.fsmm.data.Manageable.Action;
import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.fexcraft.mod.fsmm.events.ATMEvent.GatherAccounts;
import net.fexcraft.mod.fsmm.events.ATMEvent.SearchAccounts;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
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
	protected Account account, receiver;
	protected long inventory;
	protected Bank bank;

	public ATMContainer(EntityPlayer player){
		super(player);
		cap = player.getCapability(FSMMCapabilities.PLAYER, null);
		perm = cap.getSelectedAccountInATM() == null ? AccountPermission.FULL : cap.getSelectedAccountInATM();
		account = cap.getSelectedAccountInATM() == null ? cap.getAccount() : perm.getAccount();
		receiver = cap.getSelectedReiverInATM();
		bank = cap.getSelectedBankInATM() == null ? account.getBank() : cap.getSelectedBankInATM();
		cap.setSelectedBankInATM(null);
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		Print.debug(side, packet);
		if(side.isClient()){
			switch(packet.getString("cargo")){
				case "sync":{
					if(packet.hasKey("account")){
						account = new Account(JsonHandler.parse(packet.getString("account"), true).asMap());
					}
					if(packet.hasKey("receiver")){
						receiver = new Account(JsonHandler.parse(packet.getString("receiver"), true).asMap());
					}
					if(packet.hasKey("bank")){
						bank = new Bank(JsonHandler.parse(packet.getString("bank"), true).asMap());
					}
					if(packet.hasKey("bank_list")){
						TreeMap<String, String> banks = new TreeMap<>();
						NBTTagList list = (NBTTagList)packet.getTag("bank_list");
						for(int i = 0; i < list.tagCount(); i++){
							String[] str = list.getStringTagAt(i).split(":");
							if(bank != null && str[0].equals(bank.id)) continue;
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
					if(packet.hasKey("inventory")){
						inventory = packet.getLong("inventory");
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
						compound.setString("account", account.toJson(false).toString());
					}
					if(packet.getBoolean("account_transfers")){
						compound.setString("account", account.toJson(true).toString());
					}
					if(packet.getBoolean("receiver") && receiver != null){
						compound.setString("receiver", receiver.toJson(false).toString());
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
					if(packet.getBoolean("inventory")){
						compound.setLong("inventory", ItemManager.countInInventory(player));
					}
					compound.setString("cargo", "sync");
					this.send(Side.CLIENT, compound);
					break;
				}
				case "bank_info":{
					cap.setSelectedBankInATM(DataManager.getBank(packet.getString("bank")));
					cap.getEntityPlayer().openGui(FSMM.getInstance(), BANK_INFO, player.world, 0, 0, 0);
					break;
				}
				case "bank_select":{
					if(!perm.manage){
						Print.chat(player, "&cYou do not have permission to manage this account.");
						player.closeScreen();
						break;
					}
					Bank bank = DataManager.getBank(packet.getString("bank"));
					String feeid = account.getType() + ":setup_account";
					long fee = bank.hasFee(feeid) ? Long.parseLong(bank.getFees().get(feeid).replace("%", "")) : 0;
					if(account.getBalance() < fee){
						Print.chat(player, "&eNot enough money on account to pay the move/setup fee.");
						player.closeScreen();
					}
					else{
						if(fee > 0) account.modifyBalance(Action.SUB, fee, player);
						account.setBank(bank);
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
					if(type.trim().length() == 0 || id.trim().length() == 0 || id.length() < Config.MIN_SEARCH_CHARS) break;
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
					boolean deposit = packet.getString("cargo").endsWith("deposit");
					if(!(deposit ? perm.deposit : perm.withdraw)){
						Print.chat(player, "&cNo permission to " + (deposit ? "deposit to" : "withdraw from" ) + " this account.");
						return;
					}
					if(processSelfAction(packet.getLong("amount"), deposit)){
						player.closeScreen();
					}
					break;
				}
				case "action_transfer":{
					if(!perm.transfer){
						Print.chat(player, "&cNo permission to transfer from this account.");
						return;
					}
					long amount = packet.getLong("amount"); 
					if(amount <= 0) return;
					if(receiver == null){
						Print.chat(player, "&cPlease select a receiver!");
						return;
					}
					if(account.getBank().processAction(Bank.Action.TRANSFER, player, account, amount, receiver, false)){
						Print.chat(player, "&bTransfer &7of &e" + Config.getWorthAsString(amount, false) + " &7processed.");
						player.closeScreen();
					}
					else{
						Print.chat(player, "&bTransfer &cfailed&7.");
					}
					break;
				}
			}
		}
	}

	private boolean processSelfAction(long amount, boolean deposit){
		if(amount <= 0) return false;
		String dep = deposit ? "&eDeposit" : "&aWithdraw";
		if(account.getBank().processAction(deposit ? Bank.Action.DEPOSIT : Bank.Action.WITHDRAW, player, account, amount, account, false)){
			Print.chat(player, dep + " &7of &e" + Config.getWorthAsString(amount, false) + " &7processed.");
			return true;
		}
		else{
			Print.chat(player, dep + " &cfailed&7.");
			return false;
		}
	}

	private NBTBase getBankList(){
		NBTTagList list = new NBTTagList();
		DataManager.getBanks().forEach((key, val) -> {
			list.appendTag(new NBTTagString(key + ":" + val.getName()));
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
