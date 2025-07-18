package bigsir.simplehealthindicator;

import bigsir.simplehealthindicator.options.IOption;
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
import net.minecraft.core.lang.I18n;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.OptionsInitEntrypoint;

import static bigsir.simplehealthindicator.SHealthIndicator.MOD_ID;

public class SHIClient implements ClientStartEntrypoint, OptionsInitEntrypoint {
	public static final String OPTION_STRING = "options." + MOD_ID + ".string.";
	public static OptionsPage optionsPage;
	public static OptionRange maxHearts;
	public static OptionRange heartScale;
	public static OptionRange displayTime;
	public static OptionRange renderOrder;
	public static OptionBoolean healthFullbright;
	public static OptionFloat healthBrightness;
	public static FloatOptionComponent healthBrightnessComponent;
	public static OptionRange fillOrder;

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
				.withComponent(new ToggleableOptionComponent<>(fillOrder))
				.withComponent(healthBrightnessComponent = new FloatOptionComponent(healthBrightness))
		);
		((IOption)healthBrightnessComponent).simple_health_indicator$getSlider().enabled = false;
	}

	public static String translateString(String string) {
		return I18n.getInstance().translateKey(OPTION_STRING + string);
	}

	@Override
	public void initOptions(GameSettings settings) {
		maxHearts = new OptionRange(settings, "simplehealthindicator.maxhearts", 3, 9);
		heartScale = new OptionRange(settings, "simplehealthindicator.heartscale", 50, 150);
		displayTime = new OptionRange(settings, "simplehealthindicator.displaytime", 10, 30);
		renderOrder = new OptionRange(settings, "simplehealthindicator.renderorder", 0, 2);
		healthFullbright = new OptionBoolean(settings, "simplehealthindicator.healthFullbright", false);
		healthBrightness = new OptionFloat(settings, "simplehealthindicator.healthBrightness", 1.0f);
		fillOrder = new OptionRange(settings, "simplehealthindicator.fillorder", 0, 2);
	}
}
