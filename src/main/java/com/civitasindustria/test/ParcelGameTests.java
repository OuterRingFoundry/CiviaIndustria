package com.civitasindustria.test;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class ParcelGameTests {
    @GameTest(template="empty")
    public static void ownershipAndVerticalBounds(GameTestHelper h){
        var level=h.getLevel();BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        UUID owner=UUID.randomUUID();var visitor=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"CIVisitor"));
        var resident=FakePlayerFactory.get(level,new GameProfile(owner,"CIOwner"));
        var p=new Parcel(UUID.randomUUID(),owner,pos.getX(),pos.getY(),pos.getZ(),pos.getX()+2,pos.getY()+1,pos.getZ()+2,"test",Set.of(),Set.of());
        var index=WorldRuntime.get(level).state().parcels;index.add(p);
        try{
            if(!ParcelProtection.allows(level,pos,resident,Parcel.Flag.BREAK)||ParcelProtection.allows(level,pos,visitor,Parcel.Flag.BREAK))throw new AssertionError("Owner policy");
            if(!ParcelProtection.allows(level,pos.above(2),visitor,Parcel.Flag.BREAK))throw new AssertionError("Vertical adjacency");
            var event=new BlockEvent.BreakEvent(level,pos,Blocks.STONE.defaultBlockState(),visitor);NeoForge.EVENT_BUS.post(event);
            if(!event.isCanceled())throw new AssertionError("Real break event bypassed parcel");
            if(!DataMigrationManager.decode(DataMigrationManager.encode(WorldRuntime.get(level).state())).parcels.at(pos.getX(),pos.getY(),pos.getZ()).orElseThrow().owner().equals(owner))throw new AssertionError("Parcel persistence");
        }finally{index.remove(p.id());}
        h.succeed();
    }
}
