package bigsir.simplehealthindicator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.RangeOption;
import net.minecraft.core.item.Item;
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
	public static RangeOption maxHearts;
	public static RangeOption heartScale;

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
		optionsPage = new OptionsPage("simplehealthindicator.options", Item.foodApple.getDefaultStack());
		OptionsPages.register(optionsPage);

		optionsPage.withComponent(
			new OptionsCategory("simplehealthindicator.category")
				.withComponent(new ToggleableOptionComponent<>(maxHearts))
				.withComponent(new ToggleableOptionComponent<>(heartScale))
		);
	}

	public static void optionsInit(GameSettings settings){
		maxHearts = new RangeOption(settings, "simplehealthindicator.maxhearts", 3, 9);
		heartScale = new RangeOption(settings, "simplehealthindicator.heartscale", 50 ,150);
	}
}
