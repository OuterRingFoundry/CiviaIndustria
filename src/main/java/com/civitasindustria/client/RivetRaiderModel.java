package com.civitasindustria.client;
import com.civitasindustria.common.threat.IndustrialRaider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
/** Articulated cast-iron body, goggle head, boiler backpack and hammer/tool arms. */
public final class RivetRaiderModel extends HumanoidModel<IndustrialRaider> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.parse("civitas_industria:rivet_raider"),"main");
    public final ModelPart boiler,goggles,lenses;
    private final ModelPart hammer;
    public RivetRaiderModel(ModelPart root){super(root);boiler=body.getChild("boiler");hammer=rightArm.getChild("hammer");goggles=head.getChild("goggles");lenses=head.getChild("lenses");}
    public static LayerDefinition layer(){
        MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);PartDefinition root=mesh.getRoot();
        var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-7,-4,8,7,8),PartPose.ZERO);
        head.addOrReplaceChild("goggles",CubeListBuilder.create().texOffs(0,0).addBox(-4.5f,-5.5f,-5,9,3,2).addBox(-2,-2.5f,-5,4,2,2),PartPose.ZERO);
        head.addOrReplaceChild("lenses",CubeListBuilder.create().texOffs(0,0).addBox(-3.5f,-5,-5.3f,2.5f,2,1).addBox(1,-5,-5.3f,2.5f,2,1),PartPose.ZERO);
        root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
        var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2.5f,8,11,5)
            .texOffs(0,24).addBox(-5,1,-3,10,2,6),PartPose.ZERO);
        body.addOrReplaceChild("boiler",CubeListBuilder.create().texOffs(32,0).addBox(-3,1,2,6,9,4)
            .texOffs(0,24).addBox(1,-4,3,2,6,2),PartPose.ZERO);
        var arm=root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-3,-2,-2,4,10,4),PartPose.offset(-5,2,0));
        arm.addOrReplaceChild("hammer",CubeListBuilder.create().texOffs(0,24).addBox(-2,5,-2,2,7,2)
            .texOffs(32,0).addBox(-4,10,-3,6,3,4),PartPose.ZERO);
        root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(40,16).mirror().addBox(-1,-2,-2,4,11,4),PartPose.offset(5,2,0));
        root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-2,0,-2,4,10,4)
            .texOffs(32,0).addBox(-2.5f,9,-3,5,3,6),PartPose.offset(-2,12,0));
        root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,16).mirror().addBox(-2,0,-2,4,10,4)
            .texOffs(32,0).addBox(-2.5f,9,-3,5,3,6),PartPose.offset(2,12,0));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(IndustrialRaider e,float limb,float amount,float age,float yaw,float pitch){
        super.setupAnim(e,limb,amount,age,yaw,pitch);
        goggles.visible=false;lenses.visible=false;boiler.visible=false;
        body.xScale=e.role()==1?1.25f:e.role()==0?.85f:1;rightArm.xScale=e.role()==1?1.3f:1;leftArm.xScale=e.role()==1?1.3f:1;
        boiler.xScale=e.role()==1?1.3f:1;boiler.yScale=e.role()==0?.7f:1;hammer.visible=e.role()!=0;
        body.zRot=(float)Math.sin(age*.09)*.02f;leftArm.xRot-=.15f;
        if(e.windingUp()){rightArm.xRot=-2.2f+(float)Math.sin(age*.45)*.08f;rightArm.zRot=-.2f;}else rightArm.zRot=0;
    }
}
