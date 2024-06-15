package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Bank.Action;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Transfer {

	public long time;
	public long amount;
	public long fee;
	public String from;
	public String type;
	public String name;
	public Action action;
	public boolean included;
	public boolean negative;

	public Transfer(JsonMap data){
		time = data.get("time").long_value();
		amount = data.get("amount").long_value();
		fee = data.get("fee").long_value();
		if(fee > 0) included = data.get("included").bool();
		from = data.get("from").string_value();
		name = data.get("name").string_value();
		type = data.get("type").string_value();
		action = Action.valueOf(data.get("action").string_value());
		if(data.has("minus")) negative = data.get("minus").bool();
	}

	public Transfer(long am, long f, boolean in, Action act, String nm, Account acc){
		time = Time.getDate();
		amount = (negative = am < 0) ? -am : am;
		fee = f;
		included = in;
		action = act;
		name = nm;
		from = acc.getId();
		name = acc.getName();
		type = acc.getType();
	}

	public JsonMap toJson(){
		JsonMap obj = new JsonMap();
		obj.add("time", time);
		obj.add("amount", amount);
		obj.add("fee", fee);
		if(included) obj.add("included", included);
		obj.add("from", from);
		obj.add("name", name);
		obj.add("type", type);
		obj.add("action", action.name());
		if(negative) obj.add("minus", true);
		return obj;
	}

}
