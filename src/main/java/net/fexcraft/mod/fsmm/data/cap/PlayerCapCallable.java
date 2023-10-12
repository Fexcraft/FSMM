package net.fexcraft.mod.fsmm.data.cap;

import net.fexcraft.mod.fsmm.data.PlayerCapability;

public class PlayerCapCallable implements java.util.concurrent.Callable<PlayerCapability> {

    @Override
    public PlayerCapability call() throws Exception {
        return new PlayerCapImpl();
    }

}
