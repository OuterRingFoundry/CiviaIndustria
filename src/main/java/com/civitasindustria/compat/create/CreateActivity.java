package com.civitasindustria.compat.create;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.lang.reflect.Method;
/** Optional bridge uses Create's public getSpeed method; no optional classes in common linkage. */
public final class CreateActivity {
    private static final ClassValue<Method> SPEED=new ClassValue<>(){
        @Override protected Method computeValue(Class<?> type){try{return type.getMethod("getSpeed");}catch(NoSuchMethodException e){return null;}}
    };
    public static boolean active(BlockEntity entity){
        if(entity==null)return false;
        Method speed=SPEED.get(entity.getClass());
        if(speed==null)return false;
        try{return Math.abs(((Number)speed.invoke(entity)).doubleValue())>.001;}
        catch(ReflectiveOperationException|ClassCastException e){return false;}
    }
}
