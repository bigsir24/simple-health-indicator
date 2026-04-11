package bigsir.simplehealthindicator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SHealthIndicator implements ModInitializer {
	public static final String MOD_ID = "simplehealthindicator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String HURT_MSG = "SHI_MSG:HURT";
	public static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());

	@Override
	public void onInitialize() {
		LOGGER.info("Simple Health Indicator initialized.");
	}
}
