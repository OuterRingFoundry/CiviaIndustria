package com.civitasindustria.common.cargo;
import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.common.config.ServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import java.util.*;

public final class Encumbrance {
    public static final TagKey<Item> BULK=tag("bulk_cargo"),HEAVY=tag("heavy_cargo"),OVERSIZED=tag("oversized_cargo");
    private static TagKey<Item> tag(String name){return TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(CivitasIndustria.MOD_ID,name));}
    private record Entry(ItemStack stack,int depth){}
    private Encumbrance(){}
    public static long mass(ItemStack root){
        ArrayDeque<Entry> pending=new ArrayDeque<>();pending.add(new Entry(root,0));long mass=0;int visited=0;
        while(!pending.isEmpty()){
            Entry e=pending.remove();if(e.stack().isEmpty())continue;
            if(++visited>256||e.depth()>4)return Long.MAX_VALUE/4;
            ItemStack stack=e.stack();long unit=stack.is(OVERSIZED)?512:stack.is(HEAVY)?32:stack.is(BULK)?8:0;
            mass=Math.min(Long.MAX_VALUE/4,mass+unit*stack.getCount());
            var container=stack.get(DataComponents.CONTAINER);
            if(container!=null){
                if(stack.getCount()>1)return Long.MAX_VALUE/4;
                var iterator=container.stream().iterator();
                while(iterator.hasNext()){if(pending.size()+visited>=256)return Long.MAX_VALUE/4;pending.add(new Entry(iterator.next(),e.depth()+1));}
            }
            var bundle=stack.get(DataComponents.BUNDLE_CONTENTS);
            if(bundle!=null)for(ItemStack nested:bundle.items()){
                if(pending.size()+visited>=256||stack.getCount()>1)return Long.MAX_VALUE/4;
                pending.add(new Entry(nested,e.depth()+1));
            }
            // Unknown BE-backed portable inventories are not assumed weightless.
            if(stack.has(DataComponents.BLOCK_ENTITY_DATA)&&container==null)return Long.MAX_VALUE/4;
        }
        return mass;
    }
    public static long carried(Player player){
        long sum=mass(player.containerMenu.getCarried());
        for(int i=0;i<player.getInventory().getContainerSize();i++)sum=Math.min(Long.MAX_VALUE/4,sum+mass(player.getInventory().getItem(i)));
        return sum;
    }
    public static int state(long mass){long limit=ServerConfig.CARGO_NORMAL.get();return mass<=limit?0:mass<=2*limit?1:mass<=4*limit?2:3;}
}
