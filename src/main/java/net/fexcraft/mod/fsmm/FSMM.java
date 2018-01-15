package net.fexcraft.mod.fsmm;
 
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.fexcraft.mod.fsmm.account.AccountManager;
import net.fexcraft.mod.fsmm.account.AccountManager.DefaultBank;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.gui.GuiATM;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.gui.Processor;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.EventHandler;
import net.fexcraft.mod.fsmm.util.FSMMCommand;
import net.fexcraft.mod.fsmm.util.UpdateHandler;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.PacketHandler.PacketHandlerType;
import net.fexcraft.mod.lib.util.registry.RegistryUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
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
	public static final String VERSION = "@VERSION@";

    @Mod.Instance(MODID)
    private static FSMM instance;
	
	private static AccountManager account_manager;
    
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		CURRENCY = new RegistryBuilder<Money>().setName(new ResourceLocation("fsmm:money")).setType(Money.class).create();
		//
		RegistryUtil.newAutoRegistry("fsmm");
		Config.initialize(event);
		//
		account_manager = new AccountManager();
		account_manager.initialize(event.getModConfigurationDirectory());
		account_manager.registerBank(new DefaultBank());
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM") {
	    @Override
	    public ItemStack getTabIconItem() {
	    	return new ItemStack(RegistryUtil.getBlock("fsmm:atm"));
	    }
	};
	
	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event){
		event.registerServerCommand(new FSMMCommand());
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		UpdateHandler.initialize();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.JSON, Side.CLIENT, new GuiATM.Receiver());
    	}
    	PacketHandler.registerListener(PacketHandlerType.JSON, Side.SERVER, new Processor());
    }
    
    public static FSMM getInstance(){
    	return instance;
    }

	public AccountManager getAccountManager() {
		return account_manager;
	}
	
	public static List<Money> getSortedList(){
		return CURRENCY.getValues().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() > o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}
	
}