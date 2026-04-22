package bigsir.simplehealthindicator;

import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.shader.Shader;

public final class SHIShaders {
	public static final Shader WORLD_INTERFACE = Shaders.register("shi_world_interface", new Shader());

	private SHIShaders() {}

	public static void init() {
		SHealthIndicator.LOGGER.info("Initializing shaders.");
	}
}
