package net.fexcraft.mod.fsmm.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.lib.fcl.Network;

public class UpdateHandler {

	private static String CV = FSMM.VERSION, NV, LMCV;
	public static String Status = null;
	private static String type;
	
	//Color Stuff
	private static String PREFIX = Formatter.format("&0[&3FSMM&0]");
	private static String grayBracket = "&7).";
	
	public static void initialize(){
		getDataFromServer();
		sync();
		
		if(NV != null) {
			if(!NV.equalsIgnoreCase(CV)) {
				Status = PREFIX + "&7 New Version avaible! (&a" + NV + grayBracket
				+ "\n" + PREFIX + "&7 Your Client version: (&c" + CV + grayBracket
				+ "\n" + PREFIX + "&7 Update type: (&3" + type + grayBracket;
			}
		}
		if(LMCV != null && !LMCV.equals("1.7.10")){
			if(Status == null){
				Status = PREFIX + "&7 Now avaible for MC " + LMCV + "!";
			}
			else{
				Status += "\n" + PREFIX + "&7 Now avaible for MC " + LMCV + "!";
			}
		}
	}

	private static void sync() {
		NV = data.get("latest_version").getAsString();
		LMCV = data.get("latest_mc_version").getAsString();
		type = data.get("type").getAsString();
	}
	
	private static JsonObject data;
	
	public static void getDataFromServer(){
		JsonObject json = Network.getModData("fsmm");
		if(json == null){
			data = new JsonObject();
			data.addProperty("latest_version", FSMM.VERSION);
			data.addProperty("latest_mc_version", "1.7.10");
			data.addProperty("type", "error.could.not.connect.to.server;\nNo Internet?");
		}
		else{
			try{
				boolean found = false;
				for(JsonElement elm : json.get("versions").getAsJsonArray()){
					if(elm.getAsJsonObject().get("version").getAsString().equals("1.7.10")){
						data = elm.getAsJsonObject();
						found = true; break;
					}
				}
				if(!found){
					data = new JsonObject();
					data.addProperty("latest_version", FSMM.VERSION);
					data.addProperty("latest_mc_version", "1.7.10");
					data.addProperty("type", "mc.version.not.found;");
				}
			}
			catch(Exception e){
				e.printStackTrace();
				data = new JsonObject();
				data.addProperty("latest_version", FSMM.VERSION);
				data.addProperty("latest_mc_version", "1.7.10");
				data.addProperty("type", "error.check.console");
			}
		}
	}
	
}