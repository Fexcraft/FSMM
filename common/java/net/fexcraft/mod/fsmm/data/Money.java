package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.fexcraft.mod.uni.inv.ItemWrapper;
import net.fexcraft.mod.uni.inv.StackWrapper;
import net.fexcraft.mod.uni.inv.UniStack;
import net.fexcraft.mod.uni.tag.TagCW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Money {

	private IDL regname;
	private StackWrapper stack;
	private long worth;

	public Money(String id, long iworth){
		regname = IDLManager.getIDLCached("fsmm:" + id);
		worth = iworth;
	}

	public Money(JsonMap map){
		regname = IDLManager.getIDLCached(map.getString("id", "invalid_" + map + "_" + Time.getDate()));
		worth = map.getLong("worth", -1);
		int meta = map.getInteger("meta", -1);
		if(meta >= 0) regname = IDLManager.getIDLCached(regname.colon() + "_" + meta);
	}

	public void loadstack(ItemWrapper item, JsonMap map){
		TagCW com = null;
		if(map.has("nbt")){
			try{
				com = FSMM.getTagfromJson(map);
			}
			catch(Exception e){
				FSMM.LOGGER.info("ERROR - Could not load NBT from config of '" + regname.toString() + "'! This is bad!");
			}
		}
		stack = UniStack.createStack(item);
		stack.damage(map.getInteger("meta", -1));
		if(com != null) stack.setTag(com);
	}

	public IDL getID(){
		return regname;
	}

	@Override
	public String toString(){
		return super.toString() + "#" + this.getWorth();
	}

	public long getWorth(){
		return worth;
	}

	public StackWrapper getStack(){
		return stack;
	}
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth();
		
	}

}
