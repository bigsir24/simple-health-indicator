package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobRenderer.class, remap = false)
public abstract class MobRendererMixin<T extends Mob> extends EntityRenderer<T> {

	@Inject(method = "renderSpecials", at = @At("HEAD"), cancellable = true)
	public void cancelLabelRender(Tessellator tessellator, T entity, double d, double d1, double d2, CallbackInfo ci){
		if(entity == RenderUtils.getCachedEntity()) ci.cancel();
	}
}
