package net.fexcraft.mod.fsmm.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.registry.UCResourceLocation;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Command extends CommandBase{

	public static final String PREFIX = Formatter.format("&0[&bFSMM&0]&7 ");
	private final static ArrayList<String> aliases = new ArrayList<String>();
	static{ aliases.add("money"); aliases.add("balance"); aliases.add("currency"); }
	protected static LinkedHashMap<String, FSMMSubCommand> SUB_CMDS = new LinkedHashMap<>();
  
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
	public int getRequiredPermissionLevel(){
		return 0;
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
    			Print.chat(sender, "&bInventory&0: &a" + Config.getWorthAsString(value));
				//
				PlayerCapability cap = sender.getCommandSenderEntity().getCapability(FSMMCapabilities.PLAYER, null);
				if(cap.getSelectedAccount() != null && !cap.getSelectedAccount().getTypeAndId().equals(cap.getAccount().getTypeAndId())){
					AccountPermission perm = cap.getSelectedAccount();
					Print.chat(sender, "&bPersonal Balance&0: &a" + Config.getWorthAsString(sender.getCommandSenderEntity().getCapability(FSMMCapabilities.PLAYER, null).getAccount().getBalance()));
    				Print.chat(sender, "&bSelected Account&0: &a" + cap.getSelectedAccount().getTypeAndId());
					Print.chat(sender, "&bSelected Balance&0: &a" + Config.getWorthAsString(cap.getSelectedAccount().getAccount().getBalance()));
				}
				else{
					Print.chat(sender, "&bAccount Balance&0: &a" + Config.getWorthAsString(sender.getCommandSenderEntity().getCapability(FSMMCapabilities.PLAYER, null).getAccount().getBalance()));
				}
    		}
    		else{
    			Bank bank = DataManager.getDefaultBank();
    			Print.chat(sender, "&bDefault Bank Balance&0: &a" + Config.getWorthAsString(bank.getBalance()));
    		}
    		return;
    	}
    	boolean op = isp ? server.isSinglePlayer() ? true : PermissionAPI.hasPermission((EntityPlayer)sender, "fsmm.admin") : true;
    	switch(args[0]){
	    	case "help":{
	        	Print.chat(sender, PREFIX + "= = = = = = = = = = =");
	        	Print.chat(sender, "&bUser commands:");
	        	Print.chat(sender, "&7/fsmm (shows balance/money)");
	        	Print.chat(sender, "&7/fsmm help");
	        	Print.chat(sender, "&7/fsmm version");
	        	Print.chat(sender, "&dAdmin commands:");
	        	Print.chat(sender, "&7/fsmm set <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm add <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm sub <type:id/name> <amount>");
	        	Print.chat(sender, "&7/fsmm info <type:id/name>");
	        	Print.chat(sender, "&7/fsmm status");
				for(FSMMSubCommand cmd : SUB_CMDS.values()) cmd.printHelp(sender);
	    		return;
	    	}
    		case "version":{
	        	Print.chat(sender,"&bFSMM Version: &e" + FSMM.VERSION + "&0.");
				for(FSMMSubCommand cmd : SUB_CMDS.values()) cmd.printVersion(sender);
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
    			process(sender, args, (account, online) -> {
    				long am = Long.parseLong(args[2]);
    				switch(args[0]){
    					case "set":{
    						account.setBalance(am < 0 ? 0 : am);
    						break;
    					}
    					case "add":{
    						am = account.getBalance() + am;
    						if(am < 0) am = 0;
    						account.setBalance(am);
    						break;
    					}
    					case "sub":{
    						am = account.getBalance() - am;
    						if(am < 0) am = 0;
    						account.setBalance(am);
    						break;
    					}
    					default: return;
    				}
    				Print.chat(sender, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
    				if(!online) Print.chat(sender, "&7&oYou modified the balance of an Offline Account.");
    			});
    			return;
    		}
    		case "info":{
    			if(!op){
        			Print.chat(sender, "&cNo Permission.");
    				return;
    			}
    			if(args.length < 2){
        			Print.chat(sender, "&cMissing Arguments.");
        			return;
    			}
	        	process(sender, args, (account, online) -> {
	        		Print.chat(sender, "&bAccount&0: &7" + account.getTypeAndId());
	        		Print.chat(sender, "&bBalance&0: &7" + Config.getWorthAsString(account.getBalance()));
	        		if(!online) Print.chat(sender, "&o&7Account Holder is currently offline.");
	        	});
    			return;
    		}
    		case "status":{
    			Print.chat(sender, "&bAccounts loaded (by type): &7");
    			long temp = 0;
    			for(String str : DataManager.getAccountTypes(false)){
    				Map<String, Account> map = DataManager.getAccountsOfType(str);
    				temp = map.values().stream().filter(pre -> pre.lastAccessed() >= 0).count();
    				Print.chat(sender, "&2> &b" + str + ": &7" + map.size() + (temp > 0 ? " &8(&a" + temp + "temp.&8)" : ""));
    			}
    			Print.chat(sender, "&bBanks active: &7" + DataManager.getBanks().size());
    			Print.chat(sender, "&aLast scheduled unload: &r&7" + Time.getAsString(DataManager.LAST_TIMERTASK));
				for(FSMMSubCommand cmd : SUB_CMDS.values()) cmd.printStatus(sender);
    			return;
    		}
			default: break;
    	}
		for(Entry<String, FSMMSubCommand> entry : SUB_CMDS.entrySet()){
			if(entry.getKey().equals(args[0])){
				entry.getValue().process(server, sender, args);
				return;
			}
		}
		Print.chat(sender, "&cInvalid Argument. Try &7/fsmm help");
    }

	private void process(ICommandSender sender, String[] args, BiConsumer<Account, Boolean> cons){
		ResourceLocation rs;
		if(args[1].contains(":")){
			rs = new UCResourceLocation(args[1].split(":"));
			if(rs.getNamespace().equals("player")){
				try{
					UUID.fromString(rs.getPath());
					//all OK
				}
				catch(Exception e){
					//not an UUID, let's convert
					UUID uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rs.getPath()).getId();
					rs = new UCResourceLocation(rs.getNamespace(), uuid.toString());
				}
			}
		}
		else{
			UUID uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[1]).getId();
			rs = new UCResourceLocation("player", uuid.toString());
		}
		Account account = DataManager.getAccount(rs.toString(), false, false);
		boolean online = account != null;
		if(!online) account = DataManager.getAccount(rs.toString(), true, false);
		if(account == null){
			Print.chat(sender, "Account not found.");
			return;
		}
		cons.accept(account, online);
		if(!online){
			DataManager.unloadAccount(account);
		}
	}

	@Override 
    public boolean isUsernameIndex(String[] var1, int var2){ 
    	return false;
    }
    
}

