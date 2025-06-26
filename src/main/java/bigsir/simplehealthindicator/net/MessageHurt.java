package bigsir.simplehealthindicator.net;

import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.world.WorldClientMP;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.EnvironmentHelper;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class MessageHurt implements NetworkMessage {
	public int id;

	public MessageHurt() {}

	public MessageHurt(int id) {
		this.id = id;
	}

	@Override
	public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
		packet.writeInt(id);
	}

	@Override
	public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
		this.id = packet.readInt();
	}

	@Override
	public void handle(NetworkContext context) {
		if (EnvironmentHelper.isClientWorld() && context.player.world != null) {
			RenderUtils.setTarget(((WorldClientMP)context.player.world).getEntityFromId(id));
		}
	}
}
