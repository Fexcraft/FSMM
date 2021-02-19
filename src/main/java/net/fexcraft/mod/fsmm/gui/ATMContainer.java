package net.fexcraft.mod.fsmm.gui;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.impl.GenericBank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class ATMContainer extends GenericContainer {
	
	protected PlayerCapability cap;
	protected Account account;
	protected Bank bank;

	public ATMContainer(EntityPlayer player){
		super(player);
		cap = player.getCapability(FSMMCapabilities.PLAYER, null);
		account = cap.getSelectedAccountInATM() == null ? cap.getAccount() : DataManager.getAccount(cap.getSelectedAccountInATM(), true, true);
		bank = DataManager.getBank(cap.getSelectedBankInATM() == null ? account.getBankId() : cap.getSelectedAccountInATM(), true, true);
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
					compound.setString("cargo", "sync");
					this.send(Side.CLIENT, compound);
					break;
				}
			}
		}
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
