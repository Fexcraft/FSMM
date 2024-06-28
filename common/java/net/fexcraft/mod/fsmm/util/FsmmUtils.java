package net.fexcraft.mod.fsmm.util;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.function.BiFunction;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmUtils {

	public static final String UI_ATM_MAIN = "fsmm:atm_main";
	public static final String UI_ATM_BANK_INFO = "fsmm:atm_bank_info";
	public static final String UI_ATM_BANK_SELECT = "fsmm:atm_bank_select";
	public static final String UI_ATM_ACC_SELECT = "fsmm:atm_account_select";
	public static final String UI_ATM_ACC_RECEIVER = "fsmm:atm_account_receiver";
	public static final String UI_ATM_ACC_WITHDRAW = "fsmm:atm_account_withdraw";
	public static final String UI_ATM_ACC_DEPOSIT = "fsmm:atm_account_deposit";
	public static final String UI_ATM_ACC_TRANSFER = "fsmm:atm_account_transfer";
	public static final String UI_ATM_TRANSFERS = "fsmm:atm_transfers";

	public static BiFunction<UniEntity, V3I, Boolean> IS_ATM = null;
}
