package net.fexcraft.mod.fsmm;

import java.io.File;
import java.util.HashMap;

import com.mojang.authlib.GameProfile;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.crafting.RecipeRegistry;
import net.fexcraft.lib.mc.registry.FCLRegistry;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.local.ATM;
import net.fexcraft.mod.fsmm.data.*;
import net.fexcraft.mod.fsmm.event.ATMEvent;
import net.fexcraft.mod.fsmm.event.AccountEvent;
import net.fexcraft.mod.fsmm.event.FsmmEvent;
import net.fexcraft.mod.fsmm.util.*;
import net.fexcraft.mod.fsmm.local.MoneyItem;
import net.fexcraft.mod.fsmm.ui.*;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.UniReg;
import net.fexcraft.mod.uni.item.ItemWrapper;
import net.fexcraft.mod.uni.tag.TagCW;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import static net.fexcraft.mod.fsmm.util.FsmmUIKeys.*;

@Mod(modid = FSMM.MODID, name = "Fex's Small Money Mod", version = FSMM.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*",
	dependencies = "required-after:fcl;before:votifier", guiFactory = "net.fexcraft.mod.fsmm.util.GuiFactory")
public class FSMM {

	public static final String MODID = "fsmm";
	public static final String VERSION = "3.0.0";

    @Mod.Instance(MODID)
    private static FSMM INSTANCE;
    public static DataManager CACHE;
    public static final Logger LOGGER = Print.getCustomLogger("fsmm", "transfers", "FSMM", null);
	public static Config CONFIG;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		UniEntity.register(new PlayerAccData(null));
		//
		FsmmUIKeys.IS_ATM = (ply, pos) -> ((Entity)ply.entity.direct()).world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() instanceof ATM;
		UniReg.registerMod(MODID, INSTANCE);
		UniReg.registerUI(UI_ATM_MAIN, ATMMain.class);
		UniReg.registerMenu(UI_ATM_MAIN, "fsmm:uis/atm_main", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_INFO, ATMBankInfo.class);
		UniReg.registerMenu(UI_ATM_BANK_INFO, "fsmm:uis/atm_bank_info", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_SELECT, ATMBankSelect.class);
		UniReg.registerMenu(UI_ATM_BANK_SELECT, "fsmm:uis/atm_bank_select", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_WITHDRAW, ATMWithdraw.class);
		UniReg.registerMenu(UI_ATM_ACC_WITHDRAW, "fsmm:uis/atm_acc_withdraw", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_DEPOSIT, ATMDeposit.class);
		UniReg.registerMenu(UI_ATM_ACC_DEPOSIT, "fsmm:uis/atm_acc_deposit", ATMContainer.class);
		UniReg.registerUI(UI_ATM_TRANSFERS, ATMViewTransfers.class);
		UniReg.registerMenu(UI_ATM_TRANSFERS, "fsmm:uis/atm_transfers", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_TRANSFER, ATMTransfer.class);
		UniReg.registerMenu(UI_ATM_ACC_TRANSFER, "fsmm:uis/atm_acc_transfer", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_RECEIVER, ATMSelectReceiver.class);
		UniReg.registerMenu(UI_ATM_ACC_RECEIVER, "fsmm:uis/atm_select_receiver", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_SELECT, ATMSelectAccount.class);
		UniReg.registerMenu(UI_ATM_ACC_SELECT, "fsmm:uis/atm_select_account", ATMContainer.class);
		//
		FCLRegistry.newAutoRegistry("fsmm");
		CONFIG = new Config(new File(event.getModConfigurationDirectory(), "fsmm.json"));
		if(Config.ENABLE_ATM_RECIPE){
			RecipeRegistry.addShapelessRecipe("fsmm:atm", "atm_recipe",
				new ItemStack(FCLRegistry.getItem("fsmm:atm")),
				Ingredient.fromStacks(new ItemStack(Items.REDSTONE)),
				Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)),
				Ingredient.fromStacks(new ItemStack(Blocks.IRON_BLOCK)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON))
			);
		}
		if(Config.ENABLE_MOBILE_RECIPE){
			RecipeRegistry.addShapelessRecipe("fsmm:mobile", "mobile_recipe",
				new ItemStack(FCLRegistry.getItem("fsmm:mobile")),
				Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)),
				Ingredient.fromStacks(new ItemStack(Items.REDSTONE)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON)),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE_BUTTON))
			);
		}
		MoneyItem.sort();
	}

	public static void registerItem(Money money){
		FCLRegistry.getAutoRegistry("fsmm").addItem(money.getID().path(), new MoneyItem(money), 1, null);
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM") {
	    @Override
	    public ItemStack createIcon() {
	    	return new ItemStack(FCLRegistry.getBlock("fsmm:atm"));
	    }
	    @SideOnly(Side.CLIENT)
	    public void displayAllRelevantItems(NonNullList<ItemStack> items){
	        for(MoneyItem item : MoneyItem.sorted){
	        	items.add(new ItemStack(item));
	        }
	        items.add(new ItemStack(FCLRegistry.getBlock("fsmm:atm")));
	    }
	};
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new Command());
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		PermissionAPI.registerNode("fsmm.admin", DefaultPermissionLevel.OP, "FSMM Admin Permission");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent post){
		DataManager.CURRENCY.values().forEach(val -> {
			JsonMap map = Config.MONEY_INIT_CACHE.get(val);
			if(map != null){
				ItemWrapper item = ItemWrapper.get(map.get("id").string_value());
				val.loadstack(item, map);
			}
		});
		Config.MONEY_INIT_CACHE.clear();
		if(EnvInfo.DEV){
			FsmmEvent.addListener(AccountEvent.BalanceUpdated.class, fe -> Print.log("bal-upd: " + fe.getOldBalance() + " -> " + fe.getNewBalance()));
		}
		FsmmEvent.addListener(ATMEvent.GatherAccounts.class, event -> {
			event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true, true));
			if(Static.getServer().isSinglePlayer()){
				event.getAccountsList().add(new AccountPermission(event.getBank().getAccount(), true, true, true, true));
			}
		});
		FsmmEvent.addListener(ATMEvent.SearchAccounts.class, event -> {
			if(event.getSearchedId().length() < Config.MIN_SEARCH_CHARS) return;
			if(!event.getSearchedType().equals("player")){
				if(!conAccPerm(event.getAccountsMap(), event.getSearchedType()) && DataManager.exists(event.getSearchedType(), event.getSearchedId())){
					putAccPerm(event.getAccountsMap(), event.getSearchedType() + ":" + event.getSearchedId());
				}
				return;
			}
			for(Account account : DataManager.getAccountsOfType("player").values()){
				if(account.getId().contains(event.getSearchedId()) || account.getName().contains(event.getSearchedId())){
					event.getAccountsMap().put(account.getTypeAndId(), new AccountPermission(account));
				}
			}
			if(Config.PARTIAL_ACCOUNT_NAME_SEARCH){
				for(String str : Static.getServer().getPlayerProfileCache().getUsernames()){
					if(str.contains(event.getSearchedId()) && !event.getAccountsMap().containsKey("player:" + str)){
						GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(str);
						if(gp == null) continue;
						putAccPermIn(event.getAccountsMap(), "player:" + gp.getId().toString());
					}
				}
				File folder = new File(DataManager.ACCOUNT_DIR, "player/");
				if(!folder.exists()) return;
				String str = null;
				for(File file : folder.listFiles()){
					if(file.isDirectory() || file.isHidden()) continue;
					if(file.getName().endsWith(".json") && (str = file.getName().substring(0, file.getName().length() - 5)).toLowerCase().contains(event.getSearchedId())){
						putAccPerm(event.getAccountsMap(), "player:" + str);
					}
				}
			}
			else{
				GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(event.getSearchedId());
				if(gp != null && new File(DataManager.ACCOUNT_DIR, "player/" + gp.getId().toString() + ".json").exists()){
					putAccPerm(event.getAccountsMap(), "player:" + gp.getId().toString());
				}
				else if(new File(DataManager.ACCOUNT_DIR, "player/" + event.getSearchedId() + ".json").exists()){
					putAccPerm(event.getAccountsMap(), "player:" + event.getSearchedId());
				}
			}
		});
    }

	private static void putAccPerm(HashMap<String, AccountPermission> map, String id){
		if(map.containsKey(id)) return;
		map.put(id, new AccountPermission(id));
	}

	private static void putAccPermIn(HashMap<String, AccountPermission> map, String id){
		map.put(id, new AccountPermission(id));
	}

	/** Checks if another mod has already returned anything for this type. */
	private static boolean conAccPerm(HashMap<String, AccountPermission> map, String type){
		for(AccountPermission perm : map.values()){
			if(perm.getType().equals(type)) return true;
		}
		return false;
	}
    
    public static FSMM getInstance(){
    	return INSTANCE;
    }

	public static void loadDataManager(){
		if(isDataManagerLoaded()){
			Print.debug("SKIPPING LOADING FSMM DATAMANAGER");
			return;
		}
		Print.debug("LOADING FSMM DATAMANAGER");
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll(); FSMM.CACHE.clearAll();
		}
		FSMM.CACHE = new DataManager(Static.getServer().getEntityWorld().getSaveHandler().getWorldDirectory());
		FSMM.CACHE.schedule();
	}

	public static void unloadDataManager(){
		Print.debug("UN-LOADING FSMM DATAMANAGER");
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll();
			FSMM.CACHE.clearAll();
			FSMM.CACHE = null;
		}
	}
	
	public static boolean isDataManagerLoaded(){
		return CACHE != null;
	}

	public static void log(Object obj){
		LOGGER.info(obj + "");
	}

	public static TagCW getTagfromJson(JsonMap map){
		try{
			NBTTagCompound com = JsonToNBT.getTagFromJson(map.toString());
			return com == null ? null : TagCW.wrap(com);
		}
		catch(NBTException e){
			e.printStackTrace();
			return null;
		}
	}
	
}