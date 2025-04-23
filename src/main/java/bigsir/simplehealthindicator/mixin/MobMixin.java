package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import turniplabs.halplibe.helper.EnvironmentHelper;

@Mixin(value = Mob.class, remap = false)
public abstract class MobMixin extends Entity{
	public MobMixin(@Nullable World world) {
		super(world);
	}

	@Inject(method = "hurt", at = @At("HEAD"))
	public void markRender(Entity attacker, int baseDamage, DamageType type, CallbackInfoReturnable<Boolean> cir){
		if(EnvironmentHelper.isSinglePlayer() && attacker == Minecraft.getMinecraft().thePlayer){ //Doesn't work in multiplayer
			RenderUtils.setTarget(this);
		}
	}
}
