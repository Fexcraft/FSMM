package net.fexcraft.mod.fsmm.events;

import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.api.WorldCapability;
import net.fexcraft.mod.fsmm.impl.cap.PlayerCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.WorldCapabilityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CapabilityEvents {
    
    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<net.minecraft.world.World> event){
    	if(FSMMCapabilities.WORLD != null && event.getObject() != null){
    		event.addCapability(WorldCapability.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
    	}
    }
    
    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<net.minecraft.entity.Entity> event){
    	if(FSMMCapabilities.PLAYER != null && event.getObject() instanceof EntityPlayer){
    		event.addCapability(PlayerCapability.REGISTRY_NAME, new PlayerCapabilityUtil((EntityPlayer)event.getObject()));
    	}
    }

}
