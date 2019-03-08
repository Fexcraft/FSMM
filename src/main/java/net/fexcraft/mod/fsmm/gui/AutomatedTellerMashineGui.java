package net.fexcraft.mod.fsmm.gui;


import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.lib.fcl.IPacketListener;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.fexcraft.mod.lib.fcl.PacketJsonObject;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.gui.buttons.NumberButton;
import net.fexcraft.mod.fsmm.gui.buttons.SelectBoxField;
import net.fexcraft.mod.fsmm.gui.buttons.SideButton;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class AutomatedTellerMashineGui extends GuiScreen {
	
	public static final ResourceLocation TEXTURE = new ResourceLocation("fsmm:textures/gui/atm_main.png");
	public static final ResourceLocation SELECT_TEX = new ResourceLocation("fsmm:textures/gui/atm_select.png");
	//
	private int xhalf, yhalf;
	private EntityPlayer player;
	//private World world;
	//private BlockPos tile;
	public static AutomatedTellerMashineGui instance;
	//
	private String[] lines = new String[]{ "", "", "", "" };
	private String window, rec_cat = "", rec_id = "";
	private long input = 0l;
	private boolean selectbox;
	private int scroll;
	private JsonArray catlist, idlist;
	//
	private SideButton[] sidebuttons = new SideButton[8];
	private NumberButton[] numberbuttons = new NumberButton[13];
	private SelectBoxField[] fieldbuttons = new SelectBoxField[7];
	private GuiTextField receiver;
	
	public AutomatedTellerMashineGui(EntityPlayer player, World world, int x, int y, int z){
		this.player = player;
		//this.world = world;
		//this.tile = new BlockPos(x, y, z);
		this.openPerspective("loading", null);
		instance = this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui(){
		xhalf = (this.width - 176) / 2;
		yhalf = (this.height - 166) / 2;
		//TODO
		this.buttonList.clear();
		for(int i = 0; i < sidebuttons.length; i++){
			boolean left = i % 2 == 0; int j = left ? i / 2 : (i - 1) / 2;
			buttonList.add(sidebuttons[i] = new SideButton(i, xhalf + (left ? 5 : 161), yhalf + (12 + (j * 23)), left));
		}
		buttonList.add(numberbuttons[ 0] = new NumberButton( 8, xhalf + 57, yhalf + 145,  0));
		buttonList.add(numberbuttons[ 1] = new NumberButton( 9, xhalf +  6, yhalf + 111,  1));
		buttonList.add(numberbuttons[ 2] = new NumberButton(10, xhalf + 23, yhalf + 111,  2));
		buttonList.add(numberbuttons[ 3] = new NumberButton(11, xhalf + 40, yhalf + 111,  3));
		buttonList.add(numberbuttons[ 4] = new NumberButton(12, xhalf +  6, yhalf + 128,  4));
		buttonList.add(numberbuttons[ 5] = new NumberButton(13, xhalf + 23, yhalf + 128,  5));
		buttonList.add(numberbuttons[ 6] = new NumberButton(14, xhalf + 40, yhalf + 128,  6));
		buttonList.add(numberbuttons[ 7] = new NumberButton(15, xhalf +  6, yhalf + 145,  7));
		buttonList.add(numberbuttons[ 8] = new NumberButton(16, xhalf + 23, yhalf + 145,  8));
		buttonList.add(numberbuttons[ 9] = new NumberButton(17, xhalf + 40, yhalf + 145,  9));
		buttonList.add(numberbuttons[10] = new NumberButton(18, xhalf + 57, yhalf + 111, 10));
		buttonList.add(numberbuttons[11] = new NumberButton(19, xhalf + 57, yhalf + 128, 11));
		buttonList.add(numberbuttons[12] = new NumberButton(20, xhalf + 74, yhalf + 145, 12));
		for(int i = 0; i < 7; i++){
			buttonList.add(fieldbuttons[i] = new SelectBoxField(21 + i, xhalf + 25, yhalf + 13));
		}
		receiver = new GuiTextField( Minecraft.getMinecraft().fontRenderer, xhalf + 22, yhalf + 37, 132, 11);
		receiver.setVisible(false); receiver.setMaxStringLength(1024);
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public void drawScreen(int mx, int my, float pt){		
		this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(xhalf, yhalf, 0, 0, 176, 166);
        //
        for(SideButton button : sidebuttons){
        	button.enabled = !selectbox;
        }
        for(NumberButton button : numberbuttons){
        	button.enabled = !selectbox;
        }
        for(int i = 0; i < fieldbuttons.length; i++){
        	fieldbuttons[i].visible = selectbox;
        	int j = i + scroll;
        	if(rec_cat.equals("") && catlist != null && catlist.size() > 0 && idlist == null){
        		fieldbuttons[i].displayString = j + "| " + (j >= catlist.size() ? "" : catlist.get(j).getAsString());
        	}
        	if(rec_id.equals("") && idlist != null && idlist.size() > 0){
        		fieldbuttons[i].displayString = j + "| " + (j >= idlist.size() ? "" : idlist.get(j).getAsString());
        	}
        }
        //
        if(selectbox){
        	this.mc.getTextureManager().bindTexture(SELECT_TEX);
        	this.drawTexturedModalRect(xhalf + 20, yhalf + 8, 20, 8, 136, 92);
        }
        else{
            for(int i = 0; i < lines.length; i++){
            	int x = xhalf + 23, y = yhalf + 16 + (i * 23);
            	if( Minecraft.getMinecraft().fontRenderer.getStringWidth(lines[i]) > 130){
            		GL11.glScaled(0.5, 0.5, 0.5);
					Minecraft.getMinecraft().fontRenderer.drawSplitString(lines[i], x * 2, y * 2, 130, MapColor.cyanColor.colorValue);
            		GL11.glScaled(2.0, 2.0, 2.0);
            	}
            	else{
					Minecraft.getMinecraft().fontRenderer.drawString(lines[i], x, y, MapColor.blackColor.colorValue);
            	}
            }
        }
        //
        this.buttonList.forEach(button -> ((GuiButton)button).drawButton(mc, mx, my));
        receiver.setVisible(selectbox ? false : window.equals("transfer")); receiver.drawTextBox();
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		instance = null;
	}
	
	@Override
    public void keyTyped(char typedChar, int keyCode){
        if(keyCode == 1){
        	if(selectbox){
        		selectbox = false;
				rec_cat = rec_id = "";
				catlist = idlist = null;
        	}
        	else{
                this.mc.displayGuiScreen((GuiScreen)null);
                if(this.mc.currentScreen == null){
                    this.mc.setIngameFocus();
                }
        	}
            return;
        }
        if(receiver.textboxKeyTyped(typedChar, keyCode)){
        	//
        }
        super.keyTyped(typedChar, keyCode);
    }
	
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
    	super.mouseClicked(mouseX, mouseY, mouseButton); receiver.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	@Override
	public void handleMouseInput(){
		super.handleMouseInput();
		if(!window.equals("transfer") && !selectbox){ return; }
		int e = Mouse.getEventDWheel();
		if(e == 0){ return; }
		scroll += e > 0 ? -7 : 7;;
		scroll = scroll <= 0 ? 0 : scroll;
	}
	
	@Override
    protected void actionPerformed(GuiButton button){
		Print.debug(window, button.id+"");
		if(button.id == 20){
			this.mc.displayGuiScreen((GuiScreen)null);
            if(this.mc.currentScreen == null){
                this.mc.setIngameFocus();
            }
		}
		switch(window){
			case "main":{
				if(button.id == 4 || button.id == 5){
					this.openPerspective("manage_account", null);
					break;
				}
				if(button.id == 6 || button.id == 7){
					this.openPerspective("view_balance", null);
					break;
				}
				return;
			}
			case "show_balance":{
				if(button.id == 6 || button.id == 7){
					this.openPerspective("loading", null);
					break;
				}
				return;
			}
			case "manage_account":{
				switch(button.id){
					case 0: case 1:{
						this.openPerspective("transfer", null);
						break;
					}
					case 2: case 3:{
						this.openPerspective("deposit", null);
						break;
					}
					case 4: case 5:{
						this.openPerspective("withdraw", null);
						break;
					}
					case 6: case 7:{
						//this.openPerspective("//TODO", null);
						break;
					}
				}
				return;
			}
			case "transfer":{
				if(!selectbox){
					if(button.id == 2 || button.id == 3){
						selectbox = true;
						rec_cat = rec_id = "";
						for(SelectBoxField sbfbutton : fieldbuttons){
							sbfbutton.displayString = "loading...";
						}
						JsonObject obj = new JsonObject();
						obj.addProperty("target_listener", "fsmm:atm_gui");
						obj.addProperty("request", "account_types");
						PacketHandler.getInstance().sendToServer(new PacketJsonObject(obj));
						break;
					}
					if(button.id >= 8 && button.id <= 17){
						int i = button.id - 8;
						input = (input * 10) + i;
						lines[3] = Config.getWorthAsString(input, true, true);
					}
					if(button.id == 19){
						input = 0;
						lines[3] = Config.getWorthAsString(input, true, true);
					}
					if(button.id == 18 && receiver.getText().length() >= 3 && receiver.getText().contains(":") /*rec_cat.length() > 0 && rec_id.length() > 0*/){
						if(instance.input > 0){
							this.openPerspective("request_transfer", null);
						}
						else{
							Print.chat(mc.thePlayer, "Cannot transfer '0'!");
						}
					}
				}
				else{
					if(button.id >= 21){
						int i = (button.id - 21) + scroll;
						if(rec_cat.equals("")){
							String sel = i >= catlist.size() ? "" : catlist.get(i).getAsString();
							if(sel != null && !sel.equals("")){
								rec_cat = sel;
								JsonObject obj = new JsonObject();
								obj.addProperty("target_listener", "fsmm:atm_gui");
								obj.addProperty("request", "accounts_of_type");
								obj.addProperty("type", rec_cat);
								PacketHandler.getInstance().sendToServer(new PacketJsonObject(obj));
								for(SelectBoxField sbfbutton : fieldbuttons){
									sbfbutton.displayString = "loading...";
								}
							}
						}
						else if(rec_id.equals("")){
							String sel = i >= idlist.size() ? "" : idlist.get(i).getAsString();
							if(sel != null && !sel.equals("")){
								rec_id = sel;
								selectbox = false;
								catlist = null; idlist = null;
								lines[1] = rec_cat + ":" + rec_id;
								receiver.setText(lines[1]);
							}
						}
					}
				}
				return;
			}
			case "deposit": case "withdraw":{
				if(button.id == 6 || button.id == 7){
					this.openPerspective("loading", null);
				}
				if(button.id >= 8 && button.id <= 17){
					int i = button.id - 8;
					input = (input * 10) + i;
					lines[2] = Config.getWorthAsString(input, true, true);
				}
				if(button.id == 19){
					input = 0;
					lines[2] = Config.getWorthAsString(input, true, true);
				}
				if(button.id == 18){
					this.openPerspective(window.equals("deposit") ? "request_deposit" : "request_withdraw", null);
				}
				return;
			}
			case "deposit_result":
			case "withdraw_result":{
				if(button.id == 6 || button.id == 7){
					this.openPerspective("loading", null);
				}
				return;
			}
			default: return;
		}
	}
	
	public void openPerspective(String window, JsonObject obj){
		this.window = window;
		selectbox = false;
		switch(window){
			case "loading":{
				lines[0] = "Loading....";
				lines[1] = "Please wait.";
				lines[2] = lines[3] = "";
				rec_cat = rec_id = "";
				sendRequest("main_data");
				break;
			}
			case "main":{
				lines[0] = Formatter.PARAGRAPH_SIGN + "8[" + obj.get("bank_name").getAsString() + "]";
				lines[1] = Formatter.PARAGRAPH_SIGN + "2Welcome back " + player.getGameProfile().getName() + "!";
				lines[2] = "Manage Account";
				lines[3] = "View Balance";
				break;
			}
			case "view_balance":{
				lines[0] = "Getting balance data...";
				lines[1] = "Please wait.";
				lines[2] = lines[3] = "";
				sendRequest("show_balance");
				break;
			}
			case "show_balance":{
				lines[0] = "Your current balance:";
				lines[1] = Config.getWorthAsString(obj.get("balance").getAsLong(), true, true);
				lines[2] = "";
				lines[3] = " << Return";
				break;
			}
			case "manage_account":{
				lines[0] = "Transfer";
				lines[1] = "Deposit";
				lines[2] = "Widthdraw";
				lines[3] = " - - - ";
				break;
			}
			case "transfer":{
				input = 0;
				lines[0] = Formatter.PARAGRAPH_SIGN + "2Receiver:";
				lines[1] = rec_cat + ":" + rec_id; receiver.setText(lines[1]);
				lines[2] = Formatter.PARAGRAPH_SIGN + "9Amount:";
				lines[3] = Config.getWorthAsString(input, true, true);
				break;
			}
			case "deposit":
			case "withdraw":{
				input = 0;
				lines[0] = Formatter.PARAGRAPH_SIGN + "2" + (window.equals("deposit") ? "Deposit" : "Withdraw");
				lines[1] = Formatter.PARAGRAPH_SIGN + "9Amount:";
				lines[2] = Config.getWorthAsString(input, true, true);
				lines[3] = "<< Return";
				break;
			}
			case "request_deposit":
			case "request_withdraw":{
				lines[0] = "Contacting Server...";
				lines[1] = "Please wait.";
				lines[2] = lines[3] = "";
				if(input > 0){
					sendRequest(window.equals("request_deposit") ? "deposit_result" : "withdraw_result", true, false);
				}
				break;
			}
			case "deposit_result":
			case "withdraw_result":{
				boolean success = obj.get("success").getAsBoolean();
				boolean dep = window.equals("deposit_result");
				lines[0] = (dep ? "Deposit" : "Withdraw") + (success ? " Processed." : " Failed!");
				if(success){
					lines[1] = "Amount:";
					lines[2] = Config.getWorthAsString(input, true, true);
				}
				else{
					lines[1] = lines[2] = "";
				}
				input = 0;
				lines[3] = "<< Return";
				break;
			}
			case "request_transfer":{
				lines[0] = "Contacting Server...";
				lines[1] = "Please wait.";
				lines[2] = lines[3] = "";
				if(input > 0){
					sendRequest("transfer_result", true, true);
				}
				break;
			}
			case "transfer_result":{
				boolean success = obj.get("success").getAsBoolean();
				lines[0] = "Transfer " + (success ? " Processed." : "Failed!");
				if(success){
					lines[1] = "Amount:";
					lines[2] = Config.getWorthAsString(input, true, true);
					lines[3] = obj.get("receiver").getAsString();
				}
				else{
					lines[1] = lines[2] = lines[3] = "";
				}
				input = 0;
				break;
			}
		}
	}
	
	private static void sendRequest(String str){
		sendRequest(str, false, false);
	}
	
	private static void sendRequest(String str, boolean sinput, boolean srec){
		JsonObject obj = new JsonObject();
		obj.addProperty("target_listener", "fsmm:atm_gui");
		obj.addProperty("request", str);
		if(sinput){
			obj.addProperty("input", instance.input);
		}
		if(srec){
			//obj.addProperty("receiver", instance.rec_cat + ":" + instance.rec_id);
			obj.addProperty("receiver", instance.receiver.getText());
		}
		PacketHandler.getInstance().sendToServer(new PacketJsonObject(obj));
	}
	
	public static class Receiver implements IPacketListener<PacketJsonObject> {

		@Override
		public String getId(){
			return "fsmm:atm_gui";
		}
		
		@Override
		public void process(PacketJsonObject pkt, Object[] objs){
			Print.debug(pkt.obj.getAsString());
			if(pkt.obj.has("payload")){
				switch(pkt.obj.get("payload").getAsString()){
					case "main_data":{
						instance.openPerspective("main", pkt.obj);
						break;
					}
					case "show_balance":{
						instance.openPerspective("show_balance", pkt.obj);
						break;
					}
					case "deposit_result":
					case "withdraw_result":{
						instance.openPerspective(pkt.obj.get("payload").getAsString(), pkt.obj);
						break;
					}
					case "account_types":{
						instance.catlist = pkt.obj.get("types").getAsJsonArray();
						break;
					}
					case "accounts_of_type":{
						instance.idlist = pkt.obj.get("accounts").getAsJsonArray();
						break;
					}
					case "transfer_result":{
						instance.openPerspective(pkt.obj.get("payload").getAsString(), pkt.obj);
						break;
					}
				}
			}
		}
		
	}
	
}