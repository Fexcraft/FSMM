package net.fexcraft.mod.fsmm;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.PacketHandler.PacketHandlerType;
import net.fexcraft.lib.mc.registry.FCLRegistry;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.gui.Processor;
import net.fexcraft.mod.fsmm.data.MoneyItem;
import net.fexcraft.mod.fsmm.data.cap.PlayerCapCallable;
import net.fexcraft.mod.fsmm.data.cap.PlayerCapStorage;
import net.fexcraft.mod.fsmm.util.Command;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

@Mod(modid = FSMM.MODID, name = "Fex's Small Money Mod", version = FSMM.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*",
		updateJSON = "http://fexcraft.net/minecraft/fcl/request?mode=getForgeUpdateJson&modid=fsmm", dependencies = "required-after:fcl;before:votifier", guiFactory = "net.fexcraft.mod.fsmm.util.GuiFactory")
public class FSMM {

	public static IForgeRegistry<Money> CURRENCY;
	public static final String MODID = "fsmm";
	public static final String VERSION = "#VERSION#";

    @Mod.Instance(MODID)
    private static FSMM INSTANCE;
    public static DataManager CACHE;
    public static final Logger LOGGER = Print.getCustomLogger("fsmm", "transfers", "FSMM", null);
    
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
    	CapabilityManager.INSTANCE.register(PlayerCapability.class, new PlayerCapStorage(), new PlayerCapCallable());
		CURRENCY = new RegistryBuilder<Money>().setName(new ResourceLocation("fsmm:money")).setType(Money.class).create();
		//
		FCLRegistry.newAutoRegistry("fsmm");
		Config.initialize(event);
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM") {
	    @Override
	    public ItemStack createIcon() {
	    	return new ItemStack(FCLRegistry.getBlock("fsmm:atm"));
	    }
	    @SideOnly(Side.CLIENT)
	    public void displayAllRelevantItems(NonNullList<ItemStack> items){
	        for(MoneyItem item : MoneyItem.sorted){
	        	items.add(new ItemStack(item));
	        }
	        items.add(new ItemStack(FCLRegistry.getBlock("fsmm:atm")));
	    }
	};
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new Command());
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		PermissionAPI.registerNode("fsmm.admin", DefaultPermissionLevel.OP, "FSMM Admin Permission");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.NBT, Side.CLIENT, new net.fexcraft.mod.fsmm.gui.Receiver());
    	}
    	PacketHandler.registerListener(PacketHandlerType.NBT, Side.SERVER, new Processor());
    }
    
    public static FSMM getInstance(){
    	return INSTANCE;
    }
	
	public static List<Money> getSortedMoneyList(){
		return CURRENCY.getValuesCollection().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() < o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}

	public static void loadDataManager(){
		if(isDataManagerLoaded()){
			Print.debug("SKIPPING LOADING FSMM DATAMANAGER");
			return;
		}
		Print.debug("LOADING FSMM DATAMANAGER");
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll(); FSMM.CACHE.clearAll();
		}
		FSMM.CACHE = new DataManager(Static.getServer().getEntityWorld().getSaveHandler().getWorldDirectory());
		FSMM.CACHE.schedule();
	}

	public static void unloadDataManager(){
		Print.debug("UN-LOADING FSMM DATAMANAGER");
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll();
			FSMM.CACHE.clearAll();
			FSMM.CACHE = null;
		}
	}
	
	public static boolean isDataManagerLoaded(){
		return CACHE != null;
	}
	
}