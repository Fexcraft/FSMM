package net.fexcraft.mod.fsmm.util;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.registry.UCResourceLocation;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class Command extends CommandBase{

	public static final String PREFIX = Formatter.format("&0[&3FSMM&0]&7 ");
	private final static ArrayList<String> aliases = new ArrayList<String>();
	static{ aliases.add("money"); aliases.add("balance"); aliases.add("currency"); }
  
    public Command(){ return; }
    
    @Override 
    public String getName(){ 
        return "fsmm";
    } 

    @Override         
    public String getUsage(ICommandSender sender){ 
        return "/fsmm <args>";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public List<String> getAliases(){ 
        return aliases;
    } 

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	boolean isp = sender instanceof EntityPlayer;
    	if(args.length <= 0){
    		if(isp){
            	long value = ItemManager.countInInventory((EntityPlayer)sender);
    			Print.chat(sender,"&9In Inventory&0: &a" + Config.getWorthAsString(value));
    			Print.chat(sender, "&9In Bank&0: &a" + Config.getWorthAsString(sender.getCommandSenderEntity().getCapability(FSMMCapabilities.PLAYER, null).getAccount().getBalance()));
    		}
    		else if(DataManager.getBank(Config.DEFAULT_BANK, true, true) != null){
    			Bank bank = DataManager.getBank(Config.DEFAULT_BANK, true, false);
    			Print.chat(sender, "&9Default Bank Balance&0: &a" + Config.getWorthAsString(bank.getBalance()));
    		}
    		else{
    			Print.chat(sender, "No default bank found to display balance.");
    		}
    		return;
    	}
    	boolean op = isp ? server.isSinglePlayer() ? true : PermissionAPI.hasPermission((EntityPlayer)sender, "fsmm.admin") : true;
    	switch(args[0]){
	    	case "help":{
	        	Print.chat(sender, PREFIX + "= = = = = = = = = = =");
	        	Print.chat(sender, "&9User commands:");
	        	Print.chat(sender, "&7/fsmm (shows balance/money)");
	        	Print.chat(sender, "&7/fsmm help");
	        	Print.chat(sender, "&7/fsmm info");
	        	Print.chat(sender, "&7/fsmm version");
	        	Print.chat(sender, "&5Admin commands:");
	        	Print.chat(sender, "&7/fsmm set <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm add <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm sub <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm status");
	    		return;
	    	}
    		case "info":{
	        	Print.chat(sender, "&9Main command for FSMM related things. ");
    			return;
    		}
    		case "version":{
	        	Print.chat(sender,"&9FSMM Version: &e" + FSMM.VERSION + "&0.");
    			return;
    		}
    		case "set":
    		case "add":
    		case "sub":{
    			if(!op){
        			Print.chat(sender, "&cNo Permission.");
    				return;
    			}
    			if(args.length < 3){
        			Print.chat(sender, "&cMissing Arguments.");
        			return;
    			}
    			modify(sender, args);
    			return;
    		}
    		case "status":{
    			Print.chat(sender, "&9Accounts loaded (by type): &7");
    			long temp = 0;
    			for(String str : DataManager.getAccountTypes(false)){
    				TreeMap<String, Account> map = DataManager.getAccountsOfType(str);
    				temp = map.values().stream().filter(pre -> pre.lastAccessed() >= 0).count();
    				Print.chat(sender, "&2> &3" + str + ": &7" + map.size() + (temp > 0 ? " &8(&a" + temp + "temp.&8)" : ""));
    			}
    			temp = DataManager.getBanks().values().stream().filter(pre -> pre.lastAccessed() >= 0).count();
    			Print.chat(sender, "&9Banks loaded: &7" + DataManager.getBanks().size() + (temp > 0 ? " &8(&a" + temp + "temp.&8)" : ""));
    			Print.chat(sender, "&5Last scheduled unload: &r&7" + Time.getAsString(DataManager.LAST_TIMERTASK));
    			return;
    		}
    		case "test-vote":{
    			if(!FSMM.VOTIFIER_LOADED){
    				Print.chat(sender, "&aVotifier not detected.");
    			}
    			else{
    				if(!op) Print.chat(sender, "&cNo Permission for Vote testing.");
    				else{
    					Print.chat(sender, "&9&oTrying to send a test vote... please wait.");
    					MinecraftForge.EVENT_BUS.post(new com.github.upcraftlp.votifier.api.VoteReceivedEvent(
    						(EntityPlayerMP)sender, "FSMM Tester", "localhost", Long.toString(System.nanoTime() / 1_000_000L)));
    				}
    			}
    			return;
    		}
    		default:{
    			Print.chat(sender, "&cInvalid Argument.");
    			return;
    		}
    	}
    }

	private void modify(ICommandSender sender, String[] args){
		ResourceLocation rs = new UCResourceLocation(args[1].split(":"));
		if(rs.getResourceDomain().equals("player")){
			try{
				UUID.fromString(rs.getResourcePath());
				//all OK
			}
			catch(Exception e){
				//not an UUID, let's convert
				UUID uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rs.getResourcePath()).getId();
				rs = new UCResourceLocation(rs.getResourceDomain(), uuid.toString());
			}
		}
		Account account = DataManager.getAccount(rs.toString(), false, false);
		boolean online = account != null;
		if(!online){ account = DataManager.getAccount(rs.toString(), true, false); }
		if(account == null){ Print.chat(sender, "Account not found."); }
		account.setBalance(Long.parseLong(args[2]));
		Print.chat(sender, "&9New Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
		if(!online){
			Print.chat(sender, "&7&oYou modified the balance of an Offline Account.");
			DataManager.unloadAccount(account);
		}
	}

	@Override 
    public boolean isUsernameIndex(String[] var1, int var2){ 
    	return false;
    }
    
}

