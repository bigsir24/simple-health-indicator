package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.net.MessageHurt;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import turniplabs.halplibe.helper.EnvironmentHelper;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Mixin(value = Mob.class, remap = false)
public abstract class MobMixinServer extends Entity {
	public MobMixinServer(@Nullable World world) {
		super(world);
	}

	@Inject(method = "hurt", at = @At("HEAD"))
	public void sendHurtMessage(Entity attacker, int damage, DamageType type, CallbackInfoReturnable<Boolean> cir) {
		if (EnvironmentHelper.isServerEnvironment() && attacker instanceof Player) {
			NetworkHandler.sendToPlayer((Player) attacker, new MessageHurt(this.id));
		}
	}
}
