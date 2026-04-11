package bigsir.simplehealthindicator.mixin.temp;

import bigsir.simplehealthindicator.SHealthIndicator;
import bigsir.simplehealthindicator.utils.FileUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.stream.Stream;

@Mixin(I18n.class)
public abstract class I18nMixin {

	@Shadow
	private Language currentLanguage;

	@Unique
	int langLoadCounter;

	@Unique
	private void tryLoadLang(final Path path) {
		final String ext = FileUtils.getExtension(path);
		if (!"lang".equals(ext)) return;

		if (matchesLang(path, currentLanguage)) {
			loadLang(path, currentLanguage);
		}else if (matchesLang(path, Language.Default.INSTANCE)) {
			loadLang(path, Language.Default.INSTANCE);
		}
	}

	@Unique
	private void loadLang(final Path path, final Language lang) {
		try {
			final Properties langProps = ((LanguageAccessor)lang).getEntries();
			langProps.load(Files.newInputStream(path));
			SHealthIndicator.LOGGER.debug("Loaded {}", path.getFileName().toString());
		} catch (IOException e) {
			SHealthIndicator.LOGGER.error("Failed to load", e);
		}
	}

	@Unique
	private boolean matchesLang(final Path path, final Language lang) {
		final String langId = lang.getId();
		final String name = FileUtils.getName(path);
		if (langId.equals(name)) return true;

		// Walk back until we find a matching language code
		Path p = path;
		while ((p = p.getParent()) != null) {
			final String parentName = FileUtils.getName(p);
			// Make sure we don't walk to root in non-jar context
			if ("resources".equals(parentName)) return false;

			if (langId.equals(parentName)) return true;
		}

		return false;
	}

	@Unique
	private boolean isLangDir(final Path path, final BasicFileAttributes attributes) {
		return Files.isDirectory(path) && "lang".equals(FileUtils.getName(path));
	}

	@Unique
	private void exploreModResources(final ModContainer mod) {
		if ("minecraft".equals(mod.getMetadata().getId())) return;

		final Path modJsonPath = mod.findPath("fabric.mod.json").orElse(null);
		if (modJsonPath == null || !Files.exists(modJsonPath)) return;

		final Path langPath = FileUtils.findFirst(modJsonPath.getParent(), this::isLangDir);
		if (langPath == null) return;

		++this.langLoadCounter;

		try (final Stream<Path> pathStream = Files.list(langPath)) {
			pathStream.forEach(path -> FileUtils.walkEach(path, this::tryLoadLang));
		} catch (IOException e) {
			SHealthIndicator.LOGGER.error("Failed to load .lang files for '{}'", mod.getMetadata().getId());
		}
	}

	@Inject(method = "reload", at = @At("TAIL"))
	public void reloadInject(String languageCode, CallbackInfo ci) {
		final ModContainer thisMod = FabricLoader.getInstance().getModContainer(SHealthIndicator.MOD_ID).orElse(null);
		if (thisMod == null) {
			SHealthIndicator.LOGGER.error("Failed to load lang files");
			return;
		}

		this.exploreModResources(thisMod);

		SHealthIndicator.LOGGER.info("Found lang folders of {} mods", this.langLoadCounter);
		this.langLoadCounter = 0;
	}
}
