package bigsir.simplehealthindicator.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingRenderer;
import net.minecraft.core.entity.EntityLiving;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingRenderer.class, remap = false)
public abstract class LivingRendererMixin<T extends EntityLiving> extends EntityRenderer<T> {
	@Inject(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", shift = At.Shift.AFTER))
	public void fixLabel(T entity, String s, double d, double d1, double d2, int maxDistance, boolean depthTest, CallbackInfo ci){
		GL11.glTranslatef(0, entity.getHeadHeight() - 1.5F, 0);
	}
}
