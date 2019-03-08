package net.fexcraft.mod.fsmm.util;

import cpw.mods.fml.common.FMLCommonHandler;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.text.SimpleDateFormat;
import java.util.*;

public class Command extends CommandBase{

	public static final String PREFIX = net.fexcraft.mod.lib.fcl.Formatter.format("&0[&3FSMM&0]&7 ");
	private final static ArrayList<String> aliases = new ArrayList<String>();
	static{ aliases.add("money"); aliases.add("balance"); aliases.add("currency"); }
  
    public Command(){ return; }
    
    @Override 
    public String getCommandName(){
        return "fsmm";
    } 

    @Override         
    public String getCommandUsage(ICommandSender sender){
        return "/fsmm <args>";
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender){
    	return true;
    }

    @Override 
    public List<String> getCommandAliases(){
        return aliases;
    } 

    @Override 
    public void processCommand(ICommandSender sender, String[] args){
    	boolean isp = sender instanceof EntityPlayer;
    	if(args.length <= 0){
    		if(isp){
            	long value = ItemManager.countInInventory((EntityPlayer)sender);
    			Print.chat(sender,"&9In Inventory&0: &a" + Config.getWorthAsString(value));
    			Account acc= DataManager.getAccount("player:" +((EntityPlayer)sender).getGameProfile().getId().toString(), false, false, null);
    			if(acc!=null){
				Print.chat(sender, "&9In Bank&0: &a" + Config.getWorthAsString(acc.getBalance()));
				} else {
					Print.chat(sender, "&9In Bank could not be loaded");
				}
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
    	boolean op = Minecraft.getMinecraft().isSingleplayer() || sender.canCommandSenderUseCommand(2,"");
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
    			Print.chat(sender, "&5Last scheduled unload: &r&7" + (new SimpleDateFormat("dd|MM|yyyy HH:mm:ss").format(DataManager.LAST_TIMERTASK >= 0 ? new Date(DataManager.LAST_TIMERTASK) : new Date())));
    			return;
    		}
    		default:{
    			Print.chat(sender, "&cInvalid Argument.");
    			return;
    		}
    	}
    }

	private void modify(ICommandSender sender, String[] args){
		ResourceLocation rs = new ResourceLocation(args[1].split(":")[0], args[1].split(":")[1]);
		if(rs.getResourceDomain().equals("player")){
			try{
				UUID.fromString(rs.getResourcePath());
				//all OK
			}
			catch(Exception e){
				//not an UUID, let's convert
				UUID uuid = FMLCommonHandler.instance().getMinecraftServerInstance().func_152358_ax().func_152655_a(rs.getResourcePath()).getId();
				rs = new ResourceLocation(rs.getResourceDomain(), uuid.toString());
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

