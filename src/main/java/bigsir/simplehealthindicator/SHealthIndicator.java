package bigsir.simplehealthindicator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionRange;
import net.minecraft.core.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

public class SHealthIndicator implements ModInitializer, GameStartEntrypoint, RecipeEntrypoint, ClientStartEntrypoint {
    public static final String MOD_ID = "simplehealthindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Simple Health Indicator initialized.");
    }
	public static OptionsPage optionsPage;
	public static OptionRange maxHearts;
	public static OptionRange heartScale;
	public static OptionRange displayTime;
	public static OptionRange renderOrder;
	public static OptionBoolean healthFullbright;
	public static OptionFloat healthBrightness;

	@Override
	public void beforeGameStart() {

	}

	@Override
	public void afterGameStart() {

	}

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}

	@Override
	public void beforeClientStart() {

	}

	@Override
	public void afterClientStart() {
		optionsPage = new OptionsPage("simplehealthindicator.title", Items.FOOD_APPLE.getDefaultStack());
		OptionsPages.register(optionsPage);

		optionsPage.withComponent(
			new OptionsCategory("simplehealthindicator.category")
				.withComponent(new ToggleableOptionComponent<>(maxHearts))
				.withComponent(new ToggleableOptionComponent<>(heartScale))
				.withComponent(new ToggleableOptionComponent<>(displayTime))
				.withComponent(new ToggleableOptionComponent<>(renderOrder))
				.withComponent(new BooleanOptionComponent(healthFullbright))
				.withComponent(new FloatOptionComponent(healthBrightness))
		);
	}

	public static void optionsInit(GameSettings settings){
		maxHearts = new OptionRange(settings, "simplehealthindicator.maxhearts", 3, 9);
		heartScale = new OptionRange(settings, "simplehealthindicator.heartscale", 50, 150);
		displayTime = new OptionRange(settings, "simplehealthindicator.displaytime", 10, 30);
		renderOrder = new OptionRange(settings, "simplehealthindicator.renderorder", 0, 2);
		healthFullbright = new OptionBoolean(settings, "simplehealthindicator.healthFullbright", false);
		healthBrightness = new OptionFloat(settings, "simplehealthindicator.healthBrightness", 1.0f);
	}
}
