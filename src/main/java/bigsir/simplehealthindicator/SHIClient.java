package bigsir.simplehealthindicator;

import bigsir.simplehealthindicator.options.IOption;
import bigsir.simplehealthindicator.render.RenderUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.*;
import net.minecraft.client.render.texture.TextureBuffered;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.util.helper.Textures;
import net.minecraft.core.item.Items;
import net.minecraft.core.lang.I18n;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static bigsir.simplehealthindicator.SHealthIndicator.MOD_ID;

public final class SHIClient implements ClientModInitializer {
	public static final String OPTION_STRING = "options." + MOD_ID + ".string.";
	public static OptionsPage optionsPage;
	public static OptionRange trackedMobCount;
	public static OptionRange maxHearts;
	public static OptionRange heartScale;
	public static OptionRange displayTime;
	public static OptionRange renderOrder;
	public static OptionBoolean healthFullbright;
	public static OptionFloat healthBrightness;
	public static FloatOptionComponent healthBrightnessComponent;
	public static OptionRange fillOrder;

	public static TextureBuffered modIcon;

	@Override
	public void onInitializeClient() {
		TextureRegistry.excludedNamespaces.add(MOD_ID);
	}

	public static void afterClientStart() {
		optionsPage = new OptionsPage("simplehealthindicator.title", Items.FOOD_APPLE.getDefaultStack());
		OptionsPages.register(optionsPage);

		optionsPage.withComponent(
			new OptionsCategory("simplehealthindicator.category")
				.withComponent(new ToggleableOptionComponent<>(trackedMobCount))
				.withComponent(new ToggleableOptionComponent<>(maxHearts))
				.withComponent(new ToggleableOptionComponent<>(heartScale))
				.withComponent(new ToggleableOptionComponent<>(displayTime))
				.withComponent(new ToggleableOptionComponent<>(renderOrder))
				.withComponent(new BooleanOptionComponent(healthFullbright))
				.withComponent(new ToggleableOptionComponent<>(fillOrder))
				.withComponent(healthBrightnessComponent = new FloatOptionComponent(healthBrightness))
		);
		((IOption)healthBrightnessComponent).simple_health_indicator$getSlider().enabled = SHIClient.healthFullbright.value;

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

	public static String translateString(String string) {
		return I18n.getInstance().translateKey(OPTION_STRING + string);
	}

	public static void initOptions() {
		trackedMobCount = new OptionRange("simplehealthindicator.trackedMobCount", 1, 17)
			.withDisplayStringProvider(SHIClient::legacyStringProvider)
			.addOnChangeCallback(SHIClient::changeMobCount);
		maxHearts = new OptionRange("simplehealthindicator.maxhearts", 3, 9).withDisplayStringProvider(SHIClient::legacyStringProvider);
		heartScale = new OptionRange("simplehealthindicator.heartscale", 50, 150).withDisplayStringProvider(SHIClient::legacyStringProvider);
		displayTime = new OptionRange("simplehealthindicator.displaytime", 10, 30).withDisplayStringProvider(SHIClient::legacyStringProvider);
		renderOrder = new OptionRange("simplehealthindicator.renderorder", 0, 2).withDisplayStringProvider(SHIClient::legacyStringProvider);
		healthFullbright = new OptionBoolean("simplehealthindicator.healthFullbright", false)
			.withDisplayStringProvider(SHIClient::legacyStringProvider)
			.addOnChangeCallback(SHIClient::changeSliderState);
		healthBrightness = new OptionFloat("simplehealthindicator.healthBrightness", 1.0f).withDisplayStringProvider(SHIClient::legacyStringProvider);
		fillOrder = new OptionRange("simplehealthindicator.fillorder", 0, 2).withDisplayStringProvider(SHIClient::legacyStringProvider);

		GameSettings.register(trackedMobCount);
		GameSettings.register(maxHearts);
		GameSettings.register(heartScale);
		GameSettings.register(displayTime);
		GameSettings.register(renderOrder);
		GameSettings.register(healthFullbright);
		GameSettings.register(healthBrightness);
		GameSettings.register(fillOrder);

		// Callback isn't called after saved values are loaded
		RenderUtils.setTrackedCount(trackedMobCount.value);
	}

	private static void changeMobCount(final Minecraft mc, final Option<Integer> option) {
		RenderUtils.setTrackedCount(option.value);
	}

	private static void changeSliderState(final Minecraft mc, final Option<?> option) {
		((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$getSlider().enabled = SHIClient.healthFullbright.value;
		((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$refreshString();
	}

	@SuppressWarnings("DataFlowIssue")
	private static String legacyStringProvider(final Minecraft mc, final I18n i18n, final Option<?> option) {
		if(option == SHIClient.maxHearts){
			return String.valueOf(SHIClient.maxHearts.getValueIndex() + 2);
		}else if(option == SHIClient.heartScale){
			return (SHIClient.heartScale.getValueIndex() + 50) / 100.0 + "x";
		}else if(option == SHIClient.displayTime){
			return SHIClient.displayTime.getValueIndex() / 10.0 + "s";
		}else if(option == SHIClient.renderOrder){
			return SHIClient.renderOrder.getValueIndex() == 0 ? SHIClient.translateString("default") : SHIClient.translateString("guidebook");
		}else if(option == SHIClient.healthBrightness && !SHIClient.healthFullbright.value){
			return SHIClient.translateString("disabled");
		}else if(option == SHIClient.fillOrder){
			return SHIClient.fillOrder.getValueIndex() == 0 ? SHIClient.translateString("down") : SHIClient.translateString("up");
		}

		return option.toOptionsString();
	}
}
