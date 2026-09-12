package com.civitasindustria.compat.immersiveengineering;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.workshop.WorkshopEntity;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.api.wires.*;
public final class WorkshopWireChecks {
    public static void connect(GameTestHelper h,BlockPos dynamo,BlockPos receiver){
        h.setBlock(receiver,CivitasRegistries.CONTENT.get("precision_workbench").get());
        BlockPos a=dynamo.east(),b=receiver.west();
        var block=IEBlocks.Connectors.getEnergyConnector("LV",false).get();
        var facing=block.defaultBlockState().getProperties().stream().filter(p->p instanceof net.minecraft.world.level.block.state.properties.DirectionProperty).findFirst().orElseThrow();
        @SuppressWarnings("unchecked") var direction=(net.minecraft.world.level.block.state.properties.Property<Direction>)facing;
        h.setBlock(a,block.defaultBlockState().setValue(direction,Direction.WEST));h.setBlock(b,block.defaultBlockState().setValue(direction,Direction.EAST));
        var net=GlobalWireNetwork.getNetwork(h.getLevel());
        var source=(blusunrize.immersiveengineering.api.wires.IImmersiveConnectable)h.getBlockEntity(a);var sink=(blusunrize.immersiveengineering.api.wires.IImmersiveConnectable)h.getBlockEntity(b);
        net.onConnectorLoad(source,h.getLevel());net.onConnectorLoad(sink,h.getLevel());
        net.addConnection(new Connection(WireType.COPPER,new ConnectionPoint(h.absolutePos(a),0),new ConnectionPoint(h.absolutePos(b),0),net));
    }
    public static void verify(GameTestHelper h,BlockPos receiver,int paid){int got=((WorkshopEntity)h.getBlockEntity(receiver)).power.getEnergyStored();if(got<=0||got>paid/2+256)throw new AssertionError("IE native copper wire transfer/loss: received="+got+" paid="+paid);}
}
