package net.fexcraft.mod.fsmm.data.cap;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerCapImpl implements PlayerCapability {

    private EntityPlayer player;
    private Account account;
    private AccountPermission atmacc;
    private Account selected;
    private Bank atmbank;

    @Override
    public <T> T setEntityPlayer(EntityPlayer player) {
        this.player = player;
        return (T) this;
    }

    @Override
    public Account getAccount() {
        return account == null ? account = DataManager.getAccount("player:" + player.getGameProfile().getId().toString(), false, true).setName(player.getName()) : account;
    }

    @Override
    public Bank getBank() {
        return getAccount().getBank();
    }

    @Override
    public long getMoneyInInventory() {
        return ItemManager.countInInventory(player);
    }

    @Override
    public long subMoneyFromInventory(long expected_amount) {
        return ItemManager.removeFromInventory(player, expected_amount);
    }

    @Override
    public long addMoneyToInventory(long expected_amount) {
        return ItemManager.addToInventory(player, expected_amount);
    }

    @Override
    public long setMoneyInInventory(long expected_amount) {
        return ItemManager.setInInventory(player, expected_amount);
    }

    @Override
    public AccountPermission getSelectedAccountInATM() {
        return atmacc;
    }

    @Override
    public void setSelectedAccountInATM(AccountPermission perm) {
        atmacc = perm;
    }

    @Override
    public Bank getSelectedBankInATM() {
        return atmbank;
    }

    @Override
    public void setSelectedBankInATM(Bank bank) {
        atmbank = bank;
    }

    @Override
    public EntityPlayer getEntityPlayer() {
        return player;
    }

    @Override
    public Account getSelectedReiverInATM() {
        return selected;
    }

    @Override
    public void setSelectedReceiverInATM(Account account) {
        selected = account;
    }

}
