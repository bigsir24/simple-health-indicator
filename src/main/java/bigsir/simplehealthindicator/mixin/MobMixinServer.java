package bigsir.simplehealthindicator.mixin;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static bigsir.simplehealthindicator.SHealthIndicator.BUFFER;
import static bigsir.simplehealthindicator.SHealthIndicator.HURT_MSG;

@Mixin(Mob.class)
public abstract class MobMixinServer extends Entity {

	public MobMixinServer(@NotNull World world) {
		super(world);
	}

	@Inject(method = "hurt", at = @At("HEAD"))
	public void sendHurtMessage(Entity attacker, int damage, DamageType type, CallbackInfoReturnable<Boolean> cir) {
		if (this.world instanceof WorldServer && attacker instanceof PlayerServer player) {
			player.playerNetServerHandler.sendPacket(new PacketCustomPayload(HURT_MSG, BUFFER.clear().putInt(this.id).flip()));
		}
	}
}
