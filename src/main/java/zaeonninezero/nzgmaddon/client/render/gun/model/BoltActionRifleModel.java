package zaeonninezero.nzgmaddon.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.client.GunModel;
import zaeonninezero.nzgmaddon.client.SpecialModels;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 * Modified by zaeonNineZero for Nine Zero's Gun Expansion
 * Attachment detection logic based off of code from Mo' Guns by Bomb787 and AlanorMiga (MigaMi)
 */
public class BoltActionRifleModel implements IOverrideModel
{
    @Override
    // This class renders a multi-part model that supports animations, removeable parts.
 	// We're also including support for model variations based on the CustomModelData NBT tag.
	
	// We start by declaring our render function that will handle rendering the core baked model (which is a non-moving part).
    public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
    	// Render the item's BakedModel, which will serve as the core of our custom model.
    	// We select which model variant to use by fetching the value of the CustomModelData tag.
        BakedModel bakedModel = SpecialModels.BOLT_ACTION_RIFLE_BASE.getModel();
        if (getVariant(stack) == 1)
        bakedModel = SpecialModels.BOLT_ACTION_RIFLE_BASE_1.getModel();

        // Render the BakedModel we selected.
        Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

		// Render the iron sights element, which is only present when a scope is not attached.
		// We have to grab the gun's scope attachment slot and check whether it is empty or not.
		// If the isEmpty function returns true, then we render the iron sights.
		ItemStack attachmentStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        if(attachmentStack.isEmpty())
		{
            RenderUtil.renderModel(SpecialModels.BOLT_ACTION_RIFLE_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		}
		else
		// Render the top rail element that appears when a scope is attached.
		{
            RenderUtil.renderModel(SpecialModels.BOLT_ACTION_RIFLE_RAIL.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		}
        
        // Next, we do the animated parts.
		
		// Get the item's cooldown from the user entity, then process it into a usable animation.
        boolean isPlayer = (entity != null && entity.equals(Minecraft.getInstance().player) ? true : false);
        float boltMovement = 0F;
        float boltPivot = 0F;
        if(isPlayer)
        {
            float cooldownDivider = 3.0F;
            float cooldownOffset1 = 1.0F;
            float intensity = 1.9F +1;
            float boltLeadTime = 0.4F;
            
        	ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown *= cooldownDivider;
            float cooldown_a = cooldown-cooldownOffset1;

            float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
            float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
            float cooldown_d = Math.min(cooldown_b,cooldown_c);

            float cooldown_e = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
            float cooldown_f = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
            float cooldown_g = Math.min(cooldown_e,cooldown_f);
            
            boltMovement = cooldown_d;
            boltPivot = cooldown_g;
        }

		// Sniper Rifle bolt and chamber. This animated part cycles backward then forward after firing.
        // This element consists of two parts.
        
        // Part 1: Rotating bolt handle
		// Push pose so we can make do transformations without affecting the models above.
        poseStack.pushPose();
		// Now we apply our transformations.
        if(isPlayer)
        {
        	// Translate the Z-axis for back and forth movement, and the Y-axis to set our pivot point.
        	poseStack.translate(0, -4.15 * 0.0625, (boltMovement * 2.5) * 0.0625);
        	// Rotate the model to represent the bolt being rotated.
        	poseStack.mulPose(Vector3f.ZN.rotationDegrees(-67.5F * Math.min(boltPivot*2F,1)));
			// Translate the Y-axis back to its original position, without touching the Z-axis.
        	poseStack.translate(0, 4.15 * 0.0625, 0);
        }
		// Our transformations are done - now we can render the model.
        RenderUtil.renderModel(SpecialModels.BOLT_ACTION_RIFLE_BOLT.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		// Pop pose to compile everything in the render matrix.
        poseStack.popPose();
        
        // Part 2: Non-rotating bolt/chamber
		// Push pose so we can make do transformations without affecting the models above.
        poseStack.pushPose();
		// Now we apply our transformations.
        if(isPlayer)
        poseStack.translate(0, 0, (boltMovement * 2.5) * 0.0625);
		// Our transformations are done - now we can render the model.
        RenderUtil.renderModel(SpecialModels.BOLT_ACTION_RIFLE_CHAMBER.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		// Pop pose to compile everything in the render matrix.
        poseStack.popPose();
    }
    
    //NBT fetch code for skin variants - ported from the "hasAmmo" function under common/Gun.java
    public static int getVariant(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        return tag.getInt("CustomModelData");
    }
}