package net.fexcraft.mod.fsmm;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fcl.FCL;
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
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.FsmmUIKeys;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.inv.ItemWrapper;
import net.fexcraft.mod.uni.tag.TagCW;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import static net.fexcraft.lib.common.utils.Formatter.format;
import static net.fexcraft.mod.fsmm.local.FsmmCmd.getFormatted;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FSMM implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("fsmm");
	public static DataManager CACHE;
	public static Config CONFIG;
	private boolean loaded;

	@Override
	public void onInitialize(){
		CONFIG = new Config(new File(FabricLoader.getInstance().getConfigDir().toFile(), "fsmm.json"));
		UniEntity.register(new PlayerAccData(null));

		FCL.registerItem("fsmm:mobile", prop -> new MobileAtm(prop));
		FCL.registerBlock("fsmm:atm", prop -> new AtmBlock(prop));

		FsmmUIKeys.IS_ATM = (ply, pos) -> ((Entity)ply.entity.direct()).level().getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() instanceof AtmBlock;
		FsmmUIKeys.register(this);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			FSMM.LOGGER.info("Loading account of " + handler.player.getName() + " || " + handler.player.getGameProfile().getId().toString());
			Account account = DataManager.getAccount("player:" + handler.player.getGameProfile().getId().toString(), false, true);
			if(Config.NOTIFY_BALANCE_ON_JOIN){
				UniEntity ent = UniEntity.get(handler.player);
				ent.entity.send(format("&m&3Balance &r&7(in bank)&0: &a") + Config.getWorthAsString(account.getBalance()));
				ent.entity.send(format("&m&3Balance &r&7(in Inv0)&0: &a") + Config.getWorthAsString(ItemManager.countInInventory(handler.player)));
			}
			if(account.lastAccessed() >= 0) account.setTemporary(false);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			FSMM.LOGGER.info("Unloading account of " + handler.player.getName() + " || " + handler.player.getGameProfile().getId().toString());
			DataManager.unloadAccount("player", handler.player.getGameProfile().getId().toString());
		});
		ServerWorldEvents.LOAD.register((server, level) -> {
			if(level != server.overworld()) return;
			FSMM.loadDataManager();
			setup();
		});
		ServerWorldEvents.UNLOAD.register((server, level) -> {
			if(level != server.overworld()) return;
			FSMM.unloadDataManager();
		});
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if(loaded) return;
			DataManager.CURRENCY.values().forEach(val -> {
				JsonMap map = Config.MONEY_INIT_CACHE.get(val);
				if(map != null){
					ItemWrapper item = ItemWrapper.get(map.get("id").string_value());
					val.loadstack(item, map);
				}
			});
			Config.MONEY_INIT_CACHE.clear();
			loaded = true;
		});
		CommandRegistrationCallback.EVENT.register((dis, con, lis) -> FsmmCmd.regCmd(dis));
	}

	private void setup(){
		MoneyItem.sort();
		//
		if(EnvInfo.DEV){
			FsmmEvent.addListener(AccountEvent.BalanceUpdated.class, fe -> log("bal-upd: " + fe.getOldBalance() + " -> " + fe.getNewBalance()));
		}
		FsmmEvent.addListener(ATMEvent.GatherAccounts.class, event -> {
			event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true, true));
			if(FCL.SERVER.get().isSingleplayer()){
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
			Optional<GameProfile> gp = FCL.SERVER.get().getProfileCache().get(event.getSearchedId());
			if(gp.isPresent() && new File(DataManager.ACCOUNT_DIR, "player/" + gp.get().getId().toString() + ".json").exists()){
				putAccPerm(event.getAccountsMap(), "player:" + gp.get().getId().toString());
			}
			else if(new File(DataManager.ACCOUNT_DIR, "player/" + event.getSearchedId() + ".json").exists()){
				putAccPerm(event.getAccountsMap(), "player:" + event.getSearchedId());
			}
		});
	}

	public static boolean isDataManagerLoaded(){
		return CACHE != null;
	}

	public static void log(Object obj){
		LOGGER.info(obj + "");
	}

	public static void loadDataManager(){
		if(isDataManagerLoaded()) return;
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll(); FSMM.CACHE.clearAll();
		}
		FSMM.CACHE = new DataManager(FCL.SERVER.get().getServerDirectory().toAbsolutePath().toFile());
		FSMM.CACHE.schedule();
	}

	public static void unloadDataManager(){
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll();
			FSMM.CACHE.clearAll();
			FSMM.CACHE = null;
		}
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

	public static void registerItem(Money money){
		FCL.registerItem(money.getID().colon(), prop -> new MoneyItem(prop, money));
	}

	public static TagCW getTagfromJson(JsonMap map){
		return null;
	}

}