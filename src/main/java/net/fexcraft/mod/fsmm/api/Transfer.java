package net.fexcraft.mod.fsmm.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Bank.Action;

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

	public Transfer(JsonObject data){
		time = data.get("time").getAsLong();
		amount = data.get("amount").getAsLong();
		fee = data.get("fee").getAsLong();
		if(fee > 0) included = data.get("included").getAsBoolean();
		from = data.get("from").getAsString();
		name = data.get("name").getAsString();
		type = data.get("type").getAsString();
		action = Action.valueOf(data.get("action").getAsString());
		if(data.has("minus")) negative = data.get("minus").getAsBoolean();
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

	public JsonObject toJson(){
		JsonObject obj = new JsonObject();
		obj.addProperty("time", time);
		obj.addProperty("amount", amount);
		obj.addProperty("fee", fee);
		if(included) obj.addProperty("included", included);
		obj.addProperty("from", from);
		obj.addProperty("name", name);
		obj.addProperty("type", type);
		obj.addProperty("action", action.name());
		if(negative) obj.addProperty("minus", true);
		return obj;
	}

}
