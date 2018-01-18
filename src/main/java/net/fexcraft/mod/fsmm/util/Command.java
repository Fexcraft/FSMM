package net.fexcraft.mod.fsmm.util;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.registry.UCResourceLocation;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Command extends CommandBase{

	public static final String PREFIX = Formatter.format("&0[&3FSMM&0]&7 ");
	private final ArrayList<String> aliases;
  
    public Command(){ 
        aliases = new ArrayList<String>(); 
        aliases.add("money"); 
        aliases.add("balance");
        aliases.add("currency");
    }
    
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
        return this.aliases;
    } 

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	boolean isp = sender instanceof EntityPlayer;
    	if(args.length <= 0){
    		if(isp){
            	long value = ItemManager.countInInventory((EntityPlayer)sender);
    			Print.chat(sender,"&9In Inventory&0: &a " + Config.getWorthAsString(value));
    			Account account = AccountManager.INSTANCE.getAccount("player", ((EntityPlayer)sender).getGameProfile().getId().toString(), true);
    			Print.chat(sender, "&9In Bank&0: &a" + Config.getWorthAsString(account.getBalance()));
    		}
    		else if(AccountManager.INSTANCE.getBank(Config.DEFAULT_BANK) != null){
    			Bank bank = AccountManager.INSTANCE.getBank(Config.DEFAULT_BANK);
    			Print.chat(sender, "&9Default Bank Balance&0: &a" + Config.getWorthAsString(bank.getBalance()));
    		}
    		else{
    			Print.chat(sender, "No default bank found to display balance.");
    		}
    		return;
    	}
    	boolean op = isp ? server.isSinglePlayer() ? true : Static.isOp(sender.getName()) || PermManager.getPlayerPerms((EntityPlayer)sender).hasPermission("fsmm.admin") : true;
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
	    		return;
	    	}
    		case "info":{
	        	Print.chat(sender, "&9Main command for FSMM related stuff");
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
		Account account = AccountManager.INSTANCE.getAccount(rs.getResourceDomain(), rs.getResourcePath());
		boolean loaded = account == null;
		if(!loaded){
			account = AccountManager.INSTANCE.getAccount(rs.getResourceDomain(), rs.getResourcePath(), true);
		}
		if(account == null){
			Print.chat(sender, "ACC:NULL:ERR");
		}
		account.modifyBalance("set", Long.parseLong(args[2]), sender);
		Print.chat(sender, "&9New Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
		if(!loaded){
			AccountManager.INSTANCE.unloadAccount(account);
		}
	}

	@Override 
    public boolean isUsernameIndex(String[] var1, int var2){ 
    	return false;
    }
    
}

