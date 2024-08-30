package net.fexcraft.mod.fsmm.local;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.PlayerAccData;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmCmd {

	public static final String PREFIX = "&0[&bFSMM&0]&7 ";

	public static void regCmd(CommandDispatcher<CommandSourceStack> dispatcher){
		dispatcher.register(literal("fsmm")
			.then(literal("balance").executes(cmd -> {
				if(cmd.getSource().isPlayer()){
					long value = ItemManager.countInInventory(cmd.getSource().getPlayer());
					chat(cmd, "&bInventory&0: &a" + Config.getWorthAsString(value));
					PlayerAccData data = UniEntity.get(cmd.getSource().getPlayer()).getApp("fsmm");
					if(data.getSelectedAccount() != null && !data.getSelectedAccount().getTypeAndId().equals(data.getAccount().getTypeAndId())){
						AccountPermission perm = data.getSelectedAccount();
						chat(cmd, "&bPersonal Balance&0: &a" + Config.getWorthAsString(data.getAccount().getBalance()));
						chat(cmd, "&bSelected Account&0: &a" + data.getSelectedAccount().getTypeAndId());
						chat(cmd, "&bSelected Balance&0: &a" + Config.getWorthAsString(data.getSelectedAccount().getAccount().getBalance()));
					}
					else{
						chat(cmd, "&bAccount Balance&0: &a" + Config.getWorthAsString(data.getAccount().getBalance()));
					}
				}
				else{
					Bank bank = DataManager.getDefaultBank();
					chat(cmd, "&bDefault Bank Balance&0: &a" + Config.getWorthAsString(bank.getBalance()));
				}
				return 0;
			}))
			.then(literal("uuid").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal(cmd.getSource().getPlayerOrException().getGameProfile().getId().toString()));
				return 0;
			}))
			.then(literal("set").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
					.then(argument("acc-id", StringArgumentType.string())
						.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
							.executes(cmd -> {
									try{
										process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
											long am = cmd.getArgument("amount", Integer.class);
											account.setBalance(am);
											chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
											if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
										});
									}
									catch(Exception e){
										e.printStackTrace();
										chat(cmd, "&c&oErrors during command execution.");
									}
									return 0;
								}
							)))))
			.then(literal("add").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
					.then(argument("acc-id", StringArgumentType.string())
						.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
							.executes(cmd -> {
									try{
										process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
											long am = cmd.getArgument("amount", Integer.class);
											account.setBalance((am += account.getBalance()) < 0 ? 0 : am);
											chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
											if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
										});
									}
									catch(Exception e){
										e.printStackTrace();
										chat(cmd, "&c&oErrors during command execution.");
									}
									return 0;
								}
							)))))
			.then(literal("sub").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
					.then(argument("acc-id", StringArgumentType.string())
						.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
							.executes(cmd -> {
									try{
										process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
											long am = cmd.getArgument("amount", Integer.class);
											account.setBalance((am -= account.getBalance()) < 0 ? 0 : am);
											chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
											if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
										});
									}
									catch(Exception e){
										e.printStackTrace();
										chat(cmd, "&c&oErrors during command execution.");
									}
									return 0;
								}
							)))))
			.then(literal("info").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
					.then(argument("acc-id", StringArgumentType.string())
						.executes(cmd -> {
								try{
									process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
										chat(cmd, "&bAccount&0: &7" + account.getTypeAndId());
										chat(cmd, "&bBalance&0: &7" + Config.getWorthAsString(account.getBalance()));
										if(!online) chat(cmd, "&o&7Account Holder is currently offline.");
									});
								}
								catch(Exception e){
									e.printStackTrace();
									chat(cmd, "&c&oErrors during command execution.");
								}
								return 0;
							}
						))))
			.then(literal("status").requires(pre -> isOp(pre)).executes(cmd -> {
				chat(cmd, "&bAccounts loaded (by type): &7");
				long temp = 0;
				for(String str : DataManager.getAccountTypes(false)){
					Map<String, Account> map = DataManager.getAccountsOfType(str);
					temp = map.values().stream().filter(pre -> pre.lastAccessed() >= 0).count();
					chat(cmd, "&2> &b" + str + ": &7" + map.size() + (temp > 0 ? " &8(&a" + temp + "temp.&8)" : ""));
				}
				chat(cmd, "&bBanks active: &7" + DataManager.getBanks().size());
				chat(cmd, "&aLast scheduled unload: &r&7" + Time.getAsString(DataManager.LAST_TIMERTASK));
				return 0;
			}))
			.executes(cmd -> {
				chat(cmd, PREFIX + "============");
				chat(cmd, "&bUser commands:");
				chat(cmd, "&7/fsmm balance");
				chat(cmd, "&7/fsmm uuid");
				chat(cmd, "&dAdmin commands:");
				chat(cmd, "&7/fsmm set <type:id/name> <amount>");
				chat(cmd, "&7/fsmm add <type:id/name> <amount>");
				chat(cmd, "&7/fsmm sub <type:id/name> <amount>");
				chat(cmd, "&7/fsmm info <type:id/name>");
				chat(cmd, "&7/fsmm status");
				return 0;
			})
		);
	}

	public static boolean isOp(CommandSourceStack css){
		if(css == null || !css.isPlayer()) return false;
		if(ServerLifecycleHooks.getCurrentServer().isSingleplayer()) return true;
		return ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(css.getPlayer().getGameProfile());
	}

	private static void process(Player sender, String type, String acc, BiConsumer<Account, Boolean> cons){
		ResourceLocation rs = new ResourceLocation(type, acc.toLowerCase());
		if(rs.getNamespace().equals("player")){
			try{
				UUID.fromString(rs.getPath());
			}
			catch(Exception e){
				Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(rs.getPath());
				rs = new ResourceLocation(type, gp.get().getId().toString());
			}
		}
		Account account = DataManager.getAccount(rs.toString(), false, false);
		boolean online = account != null;
		if(!online) account = DataManager.getAccount(rs.toString(), true, false);
		if(account == null){
			chat(sender, "Account not found.");
			chat(sender, "Searched: " + rs.toString());
			return;
		}
		cons.accept(account, online);
		if(!online){
			DataManager.unloadAccount(account);
		}
	}

	public static ResourceLocation getId(Item item){
		return BuiltInRegistries.ITEM.getKey(item);
	}

	public static ResourceLocation getId(ItemStack item){
		return BuiltInRegistries.ITEM.getKey(item.getItem());
	}

	public static Component getFormatted(String str){
		return Component.literal(Formatter.format(str));
	}

	public static void chat(CommandSource src, String str){
		src.sendSystemMessage(getFormatted(str));
	}

	public static void log(String str){
		FSMM.LOGGER.info(str);
	}

	public static void chat(CommandContext<CommandSourceStack> cmd, String str){
		chat(cmd.getSource().source, str);
	}

	public static void chat(EntityW entity, String str){
		chat((CommandSource)entity.local(), str);
	}


}
