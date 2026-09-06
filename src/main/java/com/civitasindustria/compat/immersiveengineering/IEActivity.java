package com.civitasindustria.compat.immersiveengineering;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.lang.reflect.Method;

/** Optional bridge to IE 12.4.2's public master/helper/state APIs; dummy blocks never contribute. */
public final class IEActivity {
    private record Access(Method helper){}
    private static final ClassValue<Access> ACCESS=new ClassValue<>(){
        @Override protected Access computeValue(Class<?> type){
            boolean master=false;
            for(Class<?> c=type;c!=null;c=c.getSuperclass())if(c.getName().equals("blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster"))master=true;
            if(!master)return new Access(null);
            try{return new Access(type.getMethod("getHelper"));}catch(NoSuchMethodException e){return new Access(null);}
        }
    };
    private record StateAccess(Method active,boolean coke){}
    private static final ClassValue<Access> HELPERS=new ClassValue<>(){
        @Override protected Access computeValue(Class<?> type){try{return new Access(type.getMethod("getState"));}catch(NoSuchMethodException e){return new Access(null);}}
    };
    private static final ClassValue<StateAccess> STATES=new ClassValue<>(){
        @Override protected StateAccess computeValue(Class<?> type){
            String name=type.getName();String method=name.contains("ArcFurnaceLogic$")?"isClientActive":name.contains("CrusherLogic$")?"shouldRenderActive":name.contains("DieselGeneratorLogic$")?"isActive":null;
            try{return new StateAccess(method==null?null:type.getMethod(method),name.contains("CokeOvenLogic$"));}catch(NoSuchMethodException e){return new StateAccess(null,false);}
        }
    };
    public static boolean master(BlockEntity entity){return entity!=null&&ACCESS.get(entity.getClass()).helper()!=null;}
    public static boolean active(BlockEntity entity,BlockState block){
        if(!master(entity))return false;
        try{
            Object helper=ACCESS.get(entity.getClass()).helper().invoke(entity);
            Method getter=HELPERS.get(helper.getClass()).helper();if(getter==null)return false;
            Object state=getter.invoke(helper);var access=STATES.get(state.getClass());
            if(access.active()!=null)return Boolean.TRUE.equals(access.active().invoke(state));
            if(access.coke())for(var property:block.getProperties())if(property.getName().equals("active"))return Boolean.TRUE.equals(block.getValue(property));
        }catch(ReflectiveOperationException e){return false;}
        return false;
    }
}
