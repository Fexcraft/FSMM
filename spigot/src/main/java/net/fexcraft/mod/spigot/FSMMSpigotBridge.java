package net.fexcraft.mod.spigot;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.uni.world.WrapperHolder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public final class FSMMSpigotBridge extends JavaPlugin {

    @Override
    public void onEnable(){
        Bukkit.getServicesManager().register(Economy.class, new EconImpl(), getServer().getPluginManager().getPlugin("Vault"), ServicePriority.Highest);
    }

    @Override
    public void onDisable(){
        //
    }

    public static class EconImpl implements Economy {

        public UUID uuidFromString(String name){
            try{
                return UUID.fromString(name);
            }
            catch(Exception e){
                return WrapperHolder.getUUIDFor(name);
            }
        }

        public Account getAcc(String name){
            return getAcc(uuidFromString(name));
        }

        public Account getAcc(UUID uuid){
            return DataManager.getAccount("player:" + uuid, true, true);
        }

        @Override
        public boolean isEnabled(){
            return true;
        }

        @Override
        public String getName(){
            return "FSMM";
        }

        @Override
        public boolean hasBankSupport(){
            return false;
        }

        @Override
        public int fractionalDigits(){
            return 0;
        }

        @Override
        public String format(double amount){
            return Config.getWorthAsString((long)(amount * 1000));
        }

        @Override
        public String currencyNamePlural(){
            return Config.CURRENCY_SIGN;
        }

        @Override
        public String currencyNameSingular(){
            return Config.CURRENCY_SIGN;
        }

        @Override
        public boolean hasAccount(String playerName){
            return DataManager.exists("player", uuidFromString(playerName).toString());
        }

        @Override
        public boolean hasAccount(OfflinePlayer player){
            return DataManager.exists("player", player.getUniqueId().toString());
        }

        @Override
        public boolean hasAccount(String playerName, String worldName){
            return hasAccount(playerName);
        }

        @Override
        public boolean hasAccount(OfflinePlayer player, String worldName){
            return hasAccount(player);
        }

        @Override
        public double getBalance(String playerName){
            return getAcc(playerName).getBalance() / 1000d;
        }

        @Override
        public double getBalance(OfflinePlayer player){
            return getAcc(player.getUniqueId().toString()).getBalance() / 1000d;
        }

        @Override
        public double getBalance(String playerName, String world){
            return getBalance(playerName);
        }

        @Override
        public double getBalance(OfflinePlayer player, String world){
            return getBalance(player);
        }

        @Override
        public boolean has(String playerName, double amount){
            return getBalance(playerName) >= amount;
        }

        @Override
        public boolean has(OfflinePlayer player, double amount){
            return getBalance(player) >= amount;
        }

        @Override
        public boolean has(String playerName, String worldName, double amount){
            return has(playerName, amount);
        }

        @Override
        public boolean has(OfflinePlayer player, String worldName, double amount){
            return has(player, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, double amount){
            return remBal(getAcc(playerName), amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount){
            return remBal(getAcc(player.getUniqueId()), amount);
        }

        public EconomyResponse remBal(Account acc, double amount){
            long am = (long)(amount * 1000);
            if(am > acc.getBalance()) return new EconomyResponse(0, acc.getBalance() / 1000d, EconomyResponse.ResponseType.FAILURE, null);
            else{
                acc.setBalance(acc.getBalance() - am);
                return new EconomyResponse(amount, acc.getBalance() / 1000d, EconomyResponse.ResponseType.SUCCESS, null);
            }
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount){
            return withdrawPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount){
            return withdrawPlayer(player, amount);
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, double amount){
            return addBal(getAcc(playerName), amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount){
            return addBal(getAcc(player.getUniqueId()), amount);
        }

        public EconomyResponse addBal(Account acc, double amount){
            long am = (long)(amount * 1000);
            if(acc.getBalance() + am >= Long.MAX_VALUE) return new EconomyResponse(0, acc.getBalance() / 1000d, EconomyResponse.ResponseType.FAILURE, "long limit");
            else{
                acc.setBalance(acc.getBalance() + am);
                return new EconomyResponse(amount, acc.getBalance() / 1000d, EconomyResponse.ResponseType.SUCCESS, null);
            }
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, String worldName, double amount){
            return depositPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount){
            return depositPlayer(player, amount);
        }

        @Override
        public EconomyResponse createBank(String name, String player){
            return null;
        }

        @Override
        public EconomyResponse createBank(String name, OfflinePlayer player){
            return null;
        }

        @Override
        public EconomyResponse deleteBank(String name){
            return null;
        }

        @Override
        public EconomyResponse bankBalance(String name){
            return null;
        }

        @Override
        public EconomyResponse bankHas(String name, double amount){
            return null;
        }

        @Override
        public EconomyResponse bankWithdraw(String name, double amount){
            return null;
        }

        @Override
        public EconomyResponse bankDeposit(String name, double amount){
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String name, String playerName){
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String name, OfflinePlayer player){
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String name, String playerName){
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String name, OfflinePlayer player){
            return null;
        }

        @Override
        public List<String> getBanks(){
            return Collections.emptyList();
        }

        @Override
        public boolean createPlayerAccount(String playerName){
            return true;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player){
            return true;
        }

        @Override
        public boolean createPlayerAccount(String playerName, String worldName){
            return true;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player, String worldName){
            return true;
        }
    }

}
