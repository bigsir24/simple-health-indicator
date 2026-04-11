package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.WorldClient;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends Entity{

	public MobMixin(@NotNull World world) {
		super(world);
	}

	@Inject(method = "hurt", at = @At("HEAD"))
	public void markRender(Entity attacker, int damage, DamageType type, CallbackInfoReturnable<Boolean> cir){
		if(this.world instanceof WorldClient && !(this.world instanceof WorldClientMP) && attacker == Minecraft.getMinecraft().thePlayer) {
			RenderUtils.trySetTarget(this);
		}
	}
}
