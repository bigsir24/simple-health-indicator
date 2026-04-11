package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHealthIndicator;
import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketHandlerClient.class)
public abstract class PacketHandlerClientMixin {

	@Inject(method = "handleCustomPayload", at = @At("HEAD"))
	public void handleCustom(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
		if (isInvalidPacket(packetCustomPayload)) return;

		SHealthIndicator.BUFFER.clear().put(packetCustomPayload.data, 0, 4);
		final int id = SHealthIndicator.BUFFER.flip().getInt();

		if (Minecraft.getMinecraft().thePlayer.world instanceof WorldClientMP world) {
			RenderUtils.trySetTarget(world.getEntityFromId(id));
		}
	}

	@Unique
	private boolean isInvalidPacket(PacketCustomPayload payload) {
		return !SHealthIndicator.HURT_MSG.equals(payload.channel) || payload.data == null || payload.data.length < 4;
	}
}
