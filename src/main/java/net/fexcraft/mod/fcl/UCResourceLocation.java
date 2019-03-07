package net.fexcraft.mod.fcl;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.util.Locale;

public class UCResourceLocation extends ResourceLocation {

    public UCResourceLocation(String... resourceName){
        super(org.apache.commons.lang3.StringUtils.isEmpty(s(resourceName)[0]) ? "minecraft" : s(resourceName)[0].toLowerCase(Locale.ROOT),
                s(resourceName)[1].toLowerCase(Locale.ROOT));
        Validate.notNull(this.getResourcePath());
        Field domain = ResourceLocation.class.getDeclaredFields()[0];
        domain.setAccessible(true);
        Field path = ResourceLocation.class.getDeclaredFields()[1];
        path.setAccessible(true);
        try {
            domain.set(this, resourceName[0].replace("�", ""));
            path.set(this, resourceName[1].indexOf(":") == 0 ? resourceName[1].substring(1) : resourceName[1]);
        }
        catch(IllegalArgumentException | IllegalAccessException e){
            e.printStackTrace();
        }
        Validate.notNull(this.getResourcePath());
    }

    public UCResourceLocation(ResourceLocation rs){
        this(rs.getResourceDomain(), rs.getResourcePath());
    }

    private static String[] s(String... resourceName){
        return resourceName.length < 2 ? new String[]{"�", resourceName[0]} : resourceName;
    }
}
