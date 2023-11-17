package net.fexcraft.mod.fsmm.events;

import net.fexcraft.mod.fsmm.data.FSMMCapabilities;
import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.fexcraft.mod.fsmm.data.cap.PlayerCapSerializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CapabilityEvents {
    
    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<net.minecraft.entity.Entity> event){
    	if(FSMMCapabilities.PLAYER != null && event.getObject() instanceof EntityPlayer){
    		event.addCapability(PlayerCapability.REGISTRY_NAME, new PlayerCapSerializer((EntityPlayer)event.getObject()));
    	}
    }

}
