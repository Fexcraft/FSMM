package net.fexcraft.mod.fsmm;
 
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.gui.Processor;
import net.fexcraft.mod.fsmm.impl.cap.MoneyCapabilityUtil;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.EventHandler;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Command;
import net.fexcraft.mod.fsmm.util.UpdateHandler;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.PacketHandler.PacketHandlerType;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.perms.PermissionNode;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.registry.RegistryUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod(modid = FSMM.MODID, name = "Fex's Small Money Mod", version = FSMM.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*",
		updateJSON = "http://fexcraft.net/minecraft/fcl/request?mode=getForgeUpdateJson&modid=fsmm", dependencies = "required-after:fcl", guiFactory = "net.fexcraft.mod.fsmm.util.GuiFactory")
public class FSMM {

	public static IForgeRegistry<Money> CURRENCY;
	public static final String MODID = "fsmm";
	public static final String VERSION = "2.1.0";

    @Mod.Instance(MODID)
    private static FSMM INSTANCE;
    
    public static final Logger LOGGER = Print.getCustomLogger("fsmm", "transfers", "FSMM", null);
    
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		CURRENCY = new RegistryBuilder<Money>().setName(new ResourceLocation("fsmm:money")).setType(Money.class).create();
		//
		AccountManager accman = new AccountManager();
		accman.initialize(event.getModConfigurationDirectory());
		RegistryUtil.newAutoRegistry("fsmm");
		Config.initialize(event);
		//
		PermManager.add("fsmm.admin", PermissionNode.Type.BOOLEAN, false, true);
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM") {
	    @Override
	    public ItemStack getTabIconItem() {
	    	return new ItemStack(RegistryUtil.getBlock("fsmm:atm"));
	    }
	};
	
	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event){
		event.registerServerCommand(new Command());
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		UpdateHandler.initialize();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.JSON, Side.CLIENT, new net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui.Receiver());
    	}
    	PacketHandler.registerListener(PacketHandlerType.JSON, Side.SERVER, new Processor());
    	CapabilityManager.INSTANCE.register(MoneyCapability.class, new MoneyCapabilityUtil.Storage(), new MoneyCapabilityUtil.Callable());
    }
    
    public static FSMM getInstance(){
    	return INSTANCE;
    }
	
	public static List<Money> getSortedMoneyList(){
		return CURRENCY.getValues().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() < o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}
	
}