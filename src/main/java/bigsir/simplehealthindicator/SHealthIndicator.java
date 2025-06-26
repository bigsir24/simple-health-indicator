package bigsir.simplehealthindicator;

import bigsir.simplehealthindicator.net.MessageHurt;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.network.NetworkHandler;

public class SHealthIndicator implements ModInitializer {
	public static final String MOD_ID = "simplehealthindicator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		LOGGER.info("Simple Health Indicator initialized.");

		NetworkHandler.registerNetworkMessage(MessageHurt::new);
	}
}
