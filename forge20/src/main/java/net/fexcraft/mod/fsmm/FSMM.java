package net.fexcraft.mod.fsmm;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.fsmm.data.PlayerAccData;
import net.fexcraft.mod.fsmm.event.ATMEvent;
import net.fexcraft.mod.fsmm.event.AccountEvent;
import net.fexcraft.mod.fsmm.event.FsmmEvent;
import net.fexcraft.mod.fsmm.local.AtmBlock;
import net.fexcraft.mod.fsmm.local.FsmmCmd;
import net.fexcraft.mod.fsmm.local.MobileAtm;
import net.fexcraft.mod.fsmm.local.MoneyItem;
import net.fexcraft.mod.fsmm.ui.*;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.UniReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.getFormatted;
import static net.fexcraft.mod.fsmm.util.FsmmUIKeys.*;
import static net.fexcraft.mod.fsmm.util.FsmmUIKeys.UI_ATM_ACC_SELECT;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod(FSMM.MODID)
public class FSMM {

	public static final String MODID = "fsmm";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static DataManager CACHE;
	public static Config CONFIG;
	//
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	//
	public static final RegistryObject<Block> ATM_BLOCK = BLOCKS.register("atm", () -> new AtmBlock());
	public static final RegistryObject<Item> ATM_ITEM = ITEMS.register("atm", () -> new BlockItem(ATM_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<MobileAtm> MOBILE_ATM = ITEMS.register("mobile", () -> new MobileAtm());

	public static final RegistryObject<CreativeModeTab> FSMM_TAB = CREATIVE_MODE_TABS.register("fsmm", () -> CreativeModeTab.builder()
		.withTabsBefore(CreativeModeTabs.INGREDIENTS)
		.title(Component.literal("Fex's Small Money Mod"))
		.icon(() -> ATM_ITEM.get().getDefaultInstance())
		.displayItems((parameters, output) -> {
			output.accept(ATM_ITEM.get());
			output.accept(MOBILE_ATM.get());
		}).build());

	public FSMM(){
		CONFIG = new Config(new File(FMLPaths.CONFIGDIR.get().toFile(), "fsmm.json"));
		UniEntity.register(PlayerAccData.class, true);

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);
		BLOCKS.register(bus);
		ITEMS.register(bus);
		CREATIVE_MODE_TABS.register(bus);
		MinecraftForge.EVENT_BUS.register(this);

		FsmmUIKeys.IS_ATM = (ply, pos) -> ((Entity)ply.entity.direct()).level().getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() instanceof AtmBlock;
		UniReg.registerMod(MODID, this);
		UniReg.registerUI(UI_ATM_MAIN, ATMMain.class);
		UniReg.registerMenu(UI_ATM_MAIN, "assets/fsmm/uis/atm_main", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_INFO, ATMBankInfo.class);
		UniReg.registerMenu(UI_ATM_BANK_INFO, "assets/fsmm/uis/atm_bank_info", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_SELECT, ATMBankSelect.class);
		UniReg.registerMenu(UI_ATM_BANK_SELECT, "assets/fsmm/uis/atm_bank_select", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_WITHDRAW, ATMWithdraw.class);
		UniReg.registerMenu(UI_ATM_ACC_WITHDRAW, "assets/fsmm/uis/atm_acc_withdraw", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_DEPOSIT, ATMDeposit.class);
		UniReg.registerMenu(UI_ATM_ACC_DEPOSIT, "assets/fsmm/uis/atm_acc_deposit", ATMContainer.class);
		UniReg.registerUI(UI_ATM_TRANSFERS, ATMViewTransfers.class);
		UniReg.registerMenu(UI_ATM_TRANSFERS, "assets/fsmm/uis/atm_transfers", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_TRANSFER, ATMTransfer.class);
		UniReg.registerMenu(UI_ATM_ACC_TRANSFER, "assets/fsmm/uis/atm_acc_transfer", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_RECEIVER, ATMSelectReceiver.class);
		UniReg.registerMenu(UI_ATM_ACC_RECEIVER, "assets/fsmm/uis/atm_select_receiver", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_SELECT, ATMSelectAccount.class);
		UniReg.registerMenu(UI_ATM_ACC_SELECT, "assets/fsmm/uis/atm_select_account", ATMContainer.class);

		//TODO ATM Recipe
	}

	public static void registerItem(Money money){
		FSMM.ITEMS.register(money.getID().path(), () -> new MoneyItem(money));
	}

	private void commonSetup(final FMLCommonSetupEvent setup){
		MoneyItem.sort();
		DataManager.CURRENCY.values().forEach(val -> {
			//loadstack
		});
		//
		if(EnvInfo.DEV){
			FsmmEvent.addListener(AccountEvent.BalanceUpdated.class, fe -> log("bal-upd: " + fe.getOldBalance() + " -> " + fe.getNewBalance()));
		}
		FsmmEvent.addListener(ATMEvent.GatherAccounts.class, event -> {
			event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true, true));
			if(ServerLifecycleHooks.getCurrentServer().isSingleplayer()){
				event.getAccountsList().add(new AccountPermission(event.getBank().getAccount(), true, true, true, true));
			}
		});
		FsmmEvent.addListener(ATMEvent.SearchAccounts.class, event -> {
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
			Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(event.getSearchedId());
			if(gp.isPresent() && new File(DataManager.ACCOUNT_DIR, "player/" + gp.get().getId().toString() + ".json").exists()){
				putAccPerm(event.getAccountsMap(), "player:" + gp.get().getId().toString());
			}
			else if(new File(DataManager.ACCOUNT_DIR, "player/" + event.getSearchedId() + ".json").exists()){
				putAccPerm(event.getAccountsMap(), "player:" + event.getSearchedId());
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

	private static boolean conAccPerm(HashMap<String, AccountPermission> map, String type){
		for(AccountPermission perm : map.values()){
			if(perm.getType().equals(type)) return true;
		}
		return false;
	}

	public static void log(Object obj){
		LOGGER.info(obj + "");
	}

	public static void loadDataManager(){
		if(isDataManagerLoaded()) return;
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll(); FSMM.CACHE.clearAll();
		}
		FSMM.CACHE = new DataManager(ServerLifecycleHooks.getCurrentServer().getServerDirectory());
		FSMM.CACHE.schedule();
	}

	public static void unloadDataManager(){
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll();
			FSMM.CACHE.clearAll();
			FSMM.CACHE = null;
		}
	}

	public static boolean isDataManagerLoaded(){
		return CACHE != null;
	}

	//

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event){
			if(!Config.SHOW_ITEM_WORTH) return;
			long worth = Config.getStackWorth(event.getItemStack());
			if(worth <= 0) return;
			String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
			if(event.getItemStack().getCount() > 1){
				str += " &8(&7" + Config.getWorthAsString(worth * event.getItemStack().getCount(), true, worth < 10) + "&8)";
			}
			event.getToolTip().add(getFormatted(str));
		}

	}

	@SubscribeEvent
	public void onCmdReg(RegisterCommandsEvent event){
		FsmmCmd.regCmd(event.getDispatcher());
	}

}
