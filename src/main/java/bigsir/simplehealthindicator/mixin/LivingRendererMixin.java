package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingRenderer;
import net.minecraft.core.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingRenderer.class, remap = false)
public abstract class LivingRendererMixin<T extends EntityLiving> extends EntityRenderer<T> {

	@Inject(method = "passSpecialRender", at = @At("HEAD"), cancellable = true)
	public void cancelLabelRender(T entity, double d, double d1, double d2, CallbackInfo ci){
		if(entity == RenderUtils.getCachedEntity()) ci.cancel();
	}
}
