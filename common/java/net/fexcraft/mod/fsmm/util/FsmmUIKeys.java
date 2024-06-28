package net.fexcraft.mod.fsmm.util;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.ui.UIKey;

import java.util.function.BiFunction;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmUIKeys {

	public static final UIKey UI_ATM_MAIN = new UIKey(1, "fsmm:atm_main");
	public static final UIKey UI_ATM_BANK_INFO = new UIKey(10, "fsmm:atm_bank_info");
	public static final UIKey UI_ATM_BANK_SELECT = new UIKey(11, "fsmm:atm_bank_select");
	public static final UIKey UI_ATM_ACC_SELECT = new UIKey(20, "fsmm:atm_account_select");
	public static final UIKey UI_ATM_ACC_RECEIVER = new UIKey(21, "fsmm:atm_account_receiver");
	public static final UIKey UI_ATM_ACC_WITHDRAW = new UIKey(22, "fsmm:atm_account_withdraw");
	public static final UIKey UI_ATM_ACC_DEPOSIT = new UIKey(23, "fsmm:atm_account_deposit");
	public static final UIKey UI_ATM_ACC_TRANSFER = new UIKey(24, "fsmm:atm_account_transfer");
	public static final UIKey UI_ATM_TRANSFERS = new UIKey(25, "fsmm:atm_transfers");

	public static BiFunction<UniEntity, V3I, Boolean> IS_ATM = null;

}
