package net.fexcraft.mod.fsmm.data.cap;

import net.fexcraft.mod.fsmm.data.PlayerCapability;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerCapStorage implements Capability.IStorage<PlayerCapability> {

    @Override
    public NBTBase writeNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side) {
        return new NBTTagCompound();
    }

    @Override
    public void readNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side, NBTBase nbt) {
        //
    }

}
