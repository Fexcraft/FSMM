package net.fexcraft.mod.fsmm;
 
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.blocks.ATM;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.gui.Processor;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.EventHandler;
import net.fexcraft.mod.fsmm.util.Command;
import net.fexcraft.mod.fsmm.util.UpdateHandler;
import net.fexcraft.mod.lib.fcl.JsonObjectPacketHandler;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = FSMM.MODID, name = "Fex's Small Money Mod", version = FSMM.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*")
public class FSMM {

	public static Map<String, Money> CURRENCY = new TreeMap<>();
	public static final String MODID = "fsmm";
	public static final String VERSION = "@VERSION@";

    @Mod.Instance(MODID)
    private static FSMM INSTANCE;
    public static DataManager CACHE;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Config.initialize(event); new ATM();
		CACHE = new DataManager(event.getModConfigurationDirectory());
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM"){
	    @Override public Item getTabIconItem(){ return null; }
	    private ItemStack iconstack = null;
	    @SideOnly(Side.CLIENT)
	    public ItemStack getIconItemStack(){
	        if(iconstack == null){
	        	iconstack = new ItemStack(ATM.INSTANCE, 1, 0);
	        } return this.iconstack;
	    }
	};
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartedEvent event){
		((ServerCommandManager)MinecraftServer.getServer().getCommandManager()).registerCommand(new Command());
	}
	
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		DataManager.saveAll();
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		UpdateHandler.initialize();
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		Config.EventHandler.onRegistry();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	if(event.getSide().isClient()){
    		JsonObjectPacketHandler.addListener(Side.CLIENT, new net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui.Receiver());
    	} else JsonObjectPacketHandler.addListener(Side.CLIENT, new Processor());
    	CACHE.schedule(); PacketHandler.init();
    }
    
    public static FSMM getInstance(){
    	return INSTANCE;
    }
	
	public static List<Money> getSortedMoneyList(){
		return CURRENCY.values().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() < o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}
	
}
