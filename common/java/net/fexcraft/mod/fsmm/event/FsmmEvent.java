package net.fexcraft.mod.fsmm.event;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmEvent {

	private static HashMap<Class<? extends FsmmEvent>, ArrayList<Consumer<FsmmEvent>>> listeners = new HashMap<>();

	public static <FE extends FsmmEvent> void addListener(Class<FE> clazz, Consumer<FE> cons){
		if(!listeners.containsKey(clazz)) listeners.put(clazz, new ArrayList<>());
		listeners.get(clazz).add((Consumer<FsmmEvent>)cons);
	}

	public static void run(FsmmEvent event){
		ArrayList<Consumer<FsmmEvent>> list = listeners.get(event.getClass());
		if(list == null) return;
		for(Consumer<FsmmEvent> cons : list) cons.accept(event);
	}

}
