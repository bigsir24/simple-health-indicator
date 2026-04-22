package bigsir.simplehealthindicator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.render.texture.TextureBuffered;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.util.helper.Textures;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static bigsir.simplehealthindicator.SHealthIndicator.MOD_ID;

public final class SHIClient implements ClientModInitializer {
	public static TextureBuffered modIcon;

	@Override
	public void onInitializeClient() {
		TextureRegistry.excludedNamespaces.add(MOD_ID);
		try {
			TextureRegistry.initializeAllFiles(MOD_ID, TextureRegistry.guiSpriteAtlas, true);
		} catch (URISyntaxException | IOException ignored) {}
		SHIShaders.init();
	}

	public static void afterClientStart() {
		SHIOptions.init();
		modIcon = new TextureBuffered(loadModIcon(), false, false, false);
	}

	public static @NonNull BufferedImage loadModIcon() {
		final ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);
		if (mod == null) return Textures.missingTexture;

		final Path path = mod.findPath("icon.png").orElse(null);
		if (path == null) return Textures.missingTexture;

		try (final InputStream is = Files.newInputStream(path)) {
			return ImageIO.read(is);
		} catch (IOException ignored) {}

		return Textures.missingTexture;
	}
}
