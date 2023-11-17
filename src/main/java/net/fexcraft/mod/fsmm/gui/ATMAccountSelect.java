package net.fexcraft.mod.fsmm.gui;

import static net.fexcraft.mod.fsmm.gui.Processor.LISTENERID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class ATMAccountSelect extends GenericGui<ATMContainer> {
	
	private static final ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/account_select.png");
	private ArrayList<String> tooltip = new ArrayList<>();
	private List<AccountPermission> accounts;
	private BasicButton up, dw, type, uid, search;
	private BasicText acc0, acc1;
	private BasicText[] accs = new BasicText[8];
	private BasicButton[] acc = new BasicButton[4];
	private int scroll, mode;

	public ATMAccountSelect(EntityPlayer player, int mode){
		super(texture, new ATMContainer(player), player);
		this.mode = mode;
		this.xSize = 256;
		this.ySize = 152;
	}

	@Override
	protected void init(){
		this.texts.put("acc0", acc0 = new BasicText(guiLeft + 6, guiTop + 6, 244, null, "Synchronizing...."));
		this.texts.put("acc1", acc1 = new BasicText(guiLeft + 6, guiTop + 16, 244, null, "Please wait.").autoscale());
		for(int i = 0; i < 4; i++){
			int j = i * 2;
			this.texts.put("accs" + j, accs[j] = new BasicText(guiLeft + 6, guiTop + 58 + (i * 22), 244, null, "").autoscale());
			this.texts.put("accs" + (j + 1), accs[j + 1] = new BasicText(guiLeft + 6, guiTop + 68 + (i * 22), 244, null, "").autoscale());
			this.buttons.put("acc" + j, acc[i] = new BasicButton("acc" + i, guiLeft + 5, guiTop + 57 + (i * 22), 5, 57, 246, 20, true));
		}
		this.buttons.put("type", type = new BasicButton("type", guiLeft + 242, guiTop + 32, 242, 32, 8, 8, false));
		this.buttons.put("uid", uid = new BasicButton("uid", guiLeft + 242, guiTop + 42, 242, 42, 8, 8, false));
		this.buttons.put("search", search = new BasicButton("search", guiLeft + 233, guiTop + 42, 233, 42, 8, 8, true));
		this.buttons.put("up", up = new BasicButton("up", guiLeft + 228, guiTop + 144, 228, 144, 7, 7, true));
		this.buttons.put("dw", dw = new BasicButton("dw", guiLeft + 237, guiTop + 144, 237, 144, 7, 7, true));
		fields.put("type", new TextField(0, fontRenderer, guiLeft + 6, guiTop + 32, 235, 8).setEnableBackground(false));
		fields.put("id", new TextField(1, fontRenderer, guiLeft + 6, guiTop + 42, 226, 8).setEnableBackground(false));
		this.container.sync("account", mode == 0 ? "account_list" : "");
		if(mode == 1){
			accs[2].string = "Please enter a type and";
			accs[3].string = "name/id then press search!";
		}
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		if(container.account != null){
			acc0.string = container.account.getName();
			acc1.string = container.account.getType() + ":" + container.account.getId();
		}
		if(container.accounts != null){
			if(accounts == null){
				accounts = new ArrayList<>();
				accounts.addAll(container.accounts);
			}
			Account account;
			for(int i = 0; i < 4; i++){
				int j = i * 2, k = i + scroll;
				if(k >= accounts.size()){
					accs[j].string = accs[j + 1].string = "";
				}
				else{
					account = accounts.get(k).getAccount();
					accs[j].string = account.getName();
					accs[j + 1].string = account.getType() + ":" + account.getId();
				}
			}
		}
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		tooltip.clear();
		if(container.accounts != null){
			for(int i = 0; i < 4; i++){
				if(!acc[i].hovered || i + scroll >= accounts.size()) continue;
				tooltip.add(Formatter.format("&7Click to select this account."));
				if(mode == 0){
					AccountPermission perm = accounts.get(i + scroll);
					tooltip.add(Formatter.format("&9Permissions:"));
					tooltip.add(Formatter.format("&7Withdraw: " + (perm.withdraw ? "&atrue" : "&cfalse")));
					tooltip.add(Formatter.format("&7Deposit: " + (perm.deposit ? "&atrue" : "&cfalse")));
					tooltip.add(Formatter.format("&7Transfer: " + (perm.transfer ? "&atrue" : "&cfalse")));
					tooltip.add(Formatter.format("&7Manage: " + (perm.manage ? "&atrue" : "&cfalse")));
				}
			}
		}
		if(type.hovered){
			tooltip.add(Formatter.format("&7Type of &9Account &7to be &6searched&7."));
			tooltip.add(Formatter.format("&7(enter the full type name)."));
			tooltip.add(Formatter.format("&9&oFor Player accounts type in \"player\"."));
			if(mode == 1) tooltip.add(Formatter.format("&c&o*required"));
		}
		if(uid.hovered){
			tooltip.add(Formatter.format("&7ID/Name of &9Account &7to be &6searched&7."));
			tooltip.add(Formatter.format(Config.PARTIAL_ACCOUNT_NAME_SEARCH ? "&7(you can just write bits of the name/id)" : "&7(enter the full account name/id)"));
			if(mode == 1) tooltip.add(Formatter.format("&c&o*required"));
		}
		if(search.hovered) tooltip.add(Formatter.format("&7Search/Filter"));
		if(up.hovered) tooltip.add(Formatter.format("&7Scroll Up"));
		if(dw.hovered) tooltip.add(Formatter.format("&7Scroll Down"));
	    if(tooltip.size() > 0) this.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		if(button.name.startsWith("acc")){
			int i = Integer.parseInt(button.name.substring(3));
			if(i < 0 || i >= 4 || accounts == null || i + scroll >= accounts.size()) return false;
			AccountPermission perm = accounts.get(i + scroll);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("cargo", "account_select");
			compound.setString("type", perm.getAccount().getType());
			compound.setString("id", perm.getAccount().getId());
			compound.setInteger("mode", mode);
			container.send(Side.SERVER, compound);
			return true;
		}
		switch(button.name){
			case "up":{
				scroll--;
				if(scroll < 0) scroll = 0;
				return true;
			}
			case "dw":{
				scroll++;
				return true;
			}
			case "search":{
				search();
				return true;
			}
		}
		return false;
	}

	private void search(){
		if(accounts != null) accounts.clear();
		String type = fields.get("type").getText(), id = fields.get("id").getText();
		boolean notype = type.trim().length() == 0, noid = id.trim().length() == 0;
		if(mode == 1 && notype){
			Print.chat(player, "&cYou need to enter the searched account type.");
			return;
		}
		if(mode == 1 && (noid || id.length() < Config.MIN_SEARCH_CHARS)){
			Print.chat(player, "&cYou need to enter at least " + Config.MIN_SEARCH_CHARS + " characters of the searched id.");
			return;
		}
		if(notype && noid){
			if(mode == 0) accounts.addAll(container.accounts);
			return;
		}
		if(mode == 0){
			accounts = container.accounts.stream().filter(acc -> {
				return (notype || acc.getAccount().getType().equals(type)) && (noid || acc.getAccount().getId().contains(id) || acc.getAccount().getName().toLowerCase().contains(id));
			}).collect(Collectors.toList());
		}
		if(mode == 1){
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("cargo", "account_search");
			compound.setString("type", type);
			compound.setString("id", id);
			container.send(Side.SERVER, compound);
			container.accounts = null;
			accounts = null;
		}
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		scroll += am > 0 ? 1 : -1;
		if(scroll < 0) scroll = 0;
	}

	@Override
    public void keyTyped(char typedChar, int keyCode) throws IOException{
        if(keyCode == 1){
    		if(mode == 0) openGui(GuiHandler.ATM_MAIN, new int[]{ 0, 0, 0 }, LISTENERID);
    		if(mode == 1) openGui(GuiHandler.ACCOUNT_TRANSFER, new int[]{ 0, 0, 0 }, LISTENERID);
            return;
        }
        else super.keyTyped(typedChar, keyCode);
    }

}
