package net.fexcraft.mod.fsmm.gui;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@Deprecated
public class AutomatedTellerMashineGui extends GuiScreen {
	
	public static final ResourceLocation TEXTURE = new ResourceLocation("fsmm:textures/gui/atm_main.png");
	public static final ResourceLocation SELECT_TEX = new ResourceLocation("fsmm:textures/gui/atm_select.png");
	//
	private int xhalf, yhalf;
	private EntityPlayer player;
	//private World world;
	//private BlockPos tile;
	public static AutomatedTellerMashineGui INSTANCE;
	//
	private String[] lines = new String[]{ "", "", "", "" };
	private String window, rec_cat = "", rec_id = "", lastamount = "";
	private long input = 0l;
	private boolean selectbox;
	private int scroll;
	protected NBTTagList catlist;
	protected NBTTagList idlist;
	//
	private GuiTextField receiver, amount;
	
	public AutomatedTellerMashineGui(EntityPlayer player, World world, int x, int y, int z){
		this.player = player;
		//this.world = world;
		//this.tile = new BlockPos(x, y, z);
		this.openPerspective("loading", null);
		INSTANCE = this;
	}
	
	@Override
	public void initGui(){
		xhalf = (this.width - 176) / 2;
		yhalf = (this.height - 166) / 2;
		//
		receiver = new GuiTextField(22, fontRenderer, xhalf + 22, yhalf + 37, 132, 11);
		receiver.setVisible(false); receiver.setMaxStringLength(1024);
		//
		amount = new GuiTextField(22, fontRenderer, xhalf + 22, yhalf + 61, 132, 11);
		amount.setVisible(false); amount.setMaxStringLength(1024);
	}
	
	@Override
    public void drawScreen(int mx, int my, float pt){		
		this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(xhalf, yhalf, 0, 0, 176, 166);
        //
        if(selectbox){
        	this.mc.getTextureManager().bindTexture(SELECT_TEX);
        	this.drawTexturedModalRect(xhalf + 20, yhalf + 8, 20, 8, 136, 92);
        }
        else{
            for(int i = 0; i < lines.length; i++){
            	int x = xhalf + 23, y = yhalf + 16 + (i * 23);
            	if(fontRenderer.getStringWidth(lines[i]) > 130){
            		GL11.glScaled(0.5, 0.5, 0.5);
            		this.fontRenderer.drawSplitString(lines[i], x * 2, y * 2, 130, MapColor.CYAN.colorValue);
            		GL11.glScaled(2.0, 2.0, 2.0);
            	}
            	else{
            		this.fontRenderer.drawString(lines[i], x, y, MapColor.BLACK.colorValue);
            	}
            }
        }
        //
        this.buttonList.forEach(button -> button.drawButton(mc, mx, my, pt));
        receiver.setVisible(selectbox ? false : window.equals("transfer"));
        receiver.drawTextBox(); amount.drawTextBox();
	}

	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		INSTANCE = null;
	}
	
	@Override
    public void keyTyped(char typedChar, int keyCode) throws IOException{
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
        if(receiver.textboxKeyTyped(typedChar, keyCode)){}
        if(amount.textboxKeyTyped(typedChar, keyCode)){}
        super.keyTyped(typedChar, keyCode);
    }
	
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    	super.mouseClicked(mouseX, mouseY, mouseButton);
    	receiver.mouseClicked(mouseX, mouseY, mouseButton);
    	amount.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	@Override
	public void handleMouseInput() throws IOException{
		super.handleMouseInput();
		if(!window.equals("transfer") && !selectbox){ return; }
		int e = Mouse.getEventDWheel();
		if(e == 0){ return; }
		scroll += e > 0 ? -7 : 7;;
		scroll = scroll <= 0 ? 0 : scroll;
	}
	
	@Override
    protected void actionPerformed(GuiButton button){
		Print.debug(window, button.id);
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
						NBTTagCompound compound = new NBTTagCompound();
						compound.setString("target_listener", "fsmm:atm_gui");
						compound.setString("request", "account_types");
						PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
						break;
					}
					if(button.id == 6 || button.id == 7){
						amount.setVisible(!amount.getVisible());
						amount.y = yhalf + 83; break;
					}
					if(button.id >= 8 && button.id <= 17){
						int i = button.id - 8;
						if(amount.getVisible()){
							amount.setText(amount.getText() + i);
						}
						else{
							input = (input * 10) + i;
							lines[3] = Config.getWorthAsString(input, true, true);
						}
					}
					if(button.id == 19){
						input = 0; if(amount.getVisible()) amount.setText("");
						lines[3] = Config.getWorthAsString(input, true, true);
					}
					if(button.id == 18 && receiver.getText().length() >= 3 && receiver.getText().contains(":") /*rec_cat.length() > 0 && rec_id.length() > 0*/){
						if(INSTANCE.input > 0 || INSTANCE.lastamount.length() > 0){
							this.openPerspective("request_transfer", null);
						}
						else{
							Print.chat(mc.player, "Cannot transfer '0'!");
						}
					}
				}
				else{
					if(button.id >= 21){
						int i = (button.id - 21) + scroll;
						if(rec_cat.equals("")){
							String sel = i >= catlist.tagCount() ? "" : ((NBTTagString)catlist.get(i)).getString();
							if(sel != null && !sel.equals("")){
								rec_cat = sel;
								NBTTagCompound compound = new NBTTagCompound();
								compound.setString("target_listener", "fsmm:atm_gui");
								compound.setString("request", "accounts_of_type");
								compound.setString("type", rec_cat);
								PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
							}
						}
						else if(rec_id.equals("")){
							String sel = i >= idlist.tagCount() ? "" : ((NBTTagString)idlist.get(i)).getString();
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
				if(button.id == 4 || button.id == 4){
					amount.setVisible(!amount.getVisible()); break;
				}
				if(button.id == 6 || button.id == 7){
					this.openPerspective("loading", null);
				}
				if(button.id >= 8 && button.id <= 17){
					int i = button.id - 8;
					if(amount.getVisible()){
						amount.setText(amount.getText() + i);
					}
					else{
						input = (input * 10) + i;
						lines[2] = Config.getWorthAsString(input, true, true);
					}
				}
				if(button.id == 19){
					input = 0; if(amount.getVisible()) amount.setText("");
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
	
	public void openPerspective(String window, NBTTagCompound compound){
		this.window = window; selectbox = false;
		if(amount != null){
			amount.setVisible(false);
			lastamount = amount.getText();
			amount.setText("");
		}
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
				lines[0] = Formatter.PARAGRAPH_SIGN + "8[" + compound.getString("bank_name") + "]";
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
				lines[1] = Config.getWorthAsString(compound.getLong("balance"), true, true);
				lines[2] = "";
				lines[3] = " << Return";
				break;
			}
			case "manage_account":{
				lines[0] = "Transfer";
				lines[1] = "Deposit";
				lines[2] = "Withdraw";
				lines[3] = " - - - ";
				break;
			}
			case "transfer":{
				input = 0; amount.y = yhalf + 61;
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
				if(input > 0 || lastamount.length() > 0){
					sendRequest(window.equals("request_deposit") ? "deposit_result" : "withdraw_result", true, false);
				}
				else{
					openPerspective(window.equals("request_deposit") ? "deposit" : "withdraw", null);
				}
				break;
			}
			case "deposit_result":
			case "withdraw_result":{
				boolean success = compound.getBoolean("success");
				boolean dep = window.equals("deposit_result");
				lines[0] = (dep ? "Deposit" : "Withdraw") + (success ? " Processed." : " Failed!");
				if(success){
					lines[1] = "Amount:";
					lines[2] = Config.getWorthAsString(input, true);
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
				if(input > 0 || lastamount.length() > 0){
					sendRequest("transfer_result", true, true);
				}
				else{
					openPerspective("transfer", null);
				}
				break;
			}
			case "transfer_result":{
				boolean success = compound.getBoolean("success");
				lines[0] = "Transfer " + (success ? " Processed." : "Failed!");
				if(success){
					lines[1] = "Amount:";
					lines[2] = Config.getWorthAsString(input, true);
					lines[3] = compound.getString("receiver");
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
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "fsmm:atm_gui");
		compound.setString("request", str);
		if(sinput){
			compound.setLong("input", INSTANCE.lastamount.length() > 0 ? format() : INSTANCE.input);
		}
		if(srec){
			compound.setString("receiver", INSTANCE.receiver.getText());
		}
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}
	
	private static final DecimalFormat df = new DecimalFormat("#.000");
	static { df.setRoundingMode(RoundingMode.DOWN); }
	
	private static final long format(){
		try{
			String format = df.format(Double.parseDouble(INSTANCE.lastamount));
			return INSTANCE.input = Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			Print.chat(INSTANCE.player, "INVALID INPUT: " + e.getMessage());
			e.printStackTrace(); return 0;
		}
	}
	
}