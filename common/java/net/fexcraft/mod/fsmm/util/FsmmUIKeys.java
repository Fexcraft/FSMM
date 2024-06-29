package net.fexcraft.mod.fsmm.util;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.ui.UIKey;

import java.util.function.BiFunction;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmUIKeys {

	public static final int ID12_ATM_MAIN = 1;
	public static final int ID12_ATM_BANK_INFO = 10;
	public static final int ID12_ATM_BANK_SELECT = 11;
	public static final int ID12_ATM_ACC_SELECT = 20;
	public static final int ID12_ATM_ACC_RECEIVER = 21;
	public static final int ID12_ATM_ACC_WITHDRAW = 22;
	public static final int ID12_ATM_ACC_DEPOSIT = 23;
	public static final int ID12_ATM_ACC_TRANSFER = 24;
	public static final int ID12_ATM_TRANSFERS = 30;

	public static final UIKey UI_ATM_MAIN = new UIKey(ID12_ATM_MAIN, "fsmm:atm_main");
	public static final UIKey UI_ATM_BANK_INFO = new UIKey(ID12_ATM_BANK_INFO, "fsmm:atm_bank_info");
	public static final UIKey UI_ATM_BANK_SELECT = new UIKey(ID12_ATM_BANK_SELECT, "fsmm:atm_bank_select");
	public static final UIKey UI_ATM_ACC_SELECT = new UIKey(ID12_ATM_ACC_SELECT, "fsmm:atm_account_select");
	public static final UIKey UI_ATM_ACC_RECEIVER = new UIKey(ID12_ATM_ACC_RECEIVER, "fsmm:atm_account_receiver");
	public static final UIKey UI_ATM_ACC_WITHDRAW = new UIKey(ID12_ATM_ACC_WITHDRAW, "fsmm:atm_account_withdraw");
	public static final UIKey UI_ATM_ACC_DEPOSIT = new UIKey(ID12_ATM_ACC_DEPOSIT, "fsmm:atm_account_deposit");
	public static final UIKey UI_ATM_ACC_TRANSFER = new UIKey(ID12_ATM_ACC_TRANSFER, "fsmm:atm_account_transfer");
	public static final UIKey UI_ATM_TRANSFERS = new UIKey(ID12_ATM_TRANSFERS, "fsmm:atm_transfers");

	public static BiFunction<UniEntity, V3I, Boolean> IS_ATM = null;

}
