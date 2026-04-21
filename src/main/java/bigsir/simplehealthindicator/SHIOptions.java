package bigsir.simplehealthindicator;

import bigsir.simplehealthindicator.options.IOption;
import bigsir.simplehealthindicator.options.SHIOptionRange;
import bigsir.simplehealthindicator.render.ContainerStyle;
import bigsir.simplehealthindicator.render.RenderUtils;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.core.item.Items;
import org.jspecify.annotations.NonNull;

import static bigsir.simplehealthindicator.SHealthIndicator.MOD_ID;

@SuppressWarnings("SameParameterValue")
public final class SHIOptions {
	private static final String OPTION_STRING = "options." + MOD_ID + ".string.";

	public static final SHIOptionRange TRACKED_MOB_COUNT = range("tracked_mob_count", 1, 17);
	public static final SHIOptionRange MAX_HEARTS = range("max_hearts", 3, 9);
	public static final SHIOptionRange HEART_SCALE  = range("heart_scale", 50, 150);
	public static final SHIOptionRange DISPLAY_TIME = range("display_time", 10, 30);
	public static final SHIOptionRange RENDER_ORDER = range("render_order", 0, 2);
	public static final OptionBoolean HEALTH_FULL_BRIGHT = boolOpt("health_fullbright", false);
	public static final OptionFloat HEALTH_BRIGHTNESS = floatOpt("health_brightness", 1.0f);
	public static final SHIOptionRange FILL_ORDER = range("fill_order", 0, 2);
	public static final OptionBoolean DAMAGE_FLASH = boolOpt("damage_flash", true);
	public static final SHIOptionRange CONTAINER_STYLE = range("container_style", 0, 3);
	public static final SHIOptionRange FADE_OUT = range("fade_out", 0, 11);
	public static final OptionBoolean STYLIZED_HEARTS = boolOpt("stylized_hearts", true);

	public static OptionsPage optionsPage;
	public static FloatOptionComponent healthBrightnessComponent;

	private SHIOptions() {}

	public static void init() {
		initOptions();
		initPages();
	}

	private static void initOptions() {
		MAX_HEARTS.setStartValue(2);
		HEART_SCALE.setStartValue(50).setDivisor(100).setValueSuffix("x");
		DISPLAY_TIME.setDivisor(10).setValueSuffix("s");
		FADE_OUT.setDivisor(10).setValueSuffix("s");

		RENDER_ORDER.setKeys(rawString("default"), rawString("guidebook"));
		FILL_ORDER.setKeys(rawString("down"), rawString("up"));
		CONTAINER_STYLE.setKeys(rawString("opaque"), rawString("transparent"), rawString("minimal"));

		TRACKED_MOB_COUNT.addOnChangeCallback((mc, opt) -> RenderUtils.setTrackedCount(opt.value));
		HEALTH_FULL_BRIGHT.addOnChangeCallback((mc, opt) -> {
			final IOption duck = (IOption) healthBrightnessComponent;
			duck.simple_health_indicator$getSlider().enabled = opt.value;
			duck.simple_health_indicator$refreshString();
		});
		CONTAINER_STYLE.addOnChangeCallback((mc, opt) -> {
			final ContainerStyle style = getEnumOrDefault(ContainerStyle.values(), opt.value, ContainerStyle.OPAQUE);
			RenderUtils.setContainerStyle(style);
		});

		HEALTH_BRIGHTNESS.withDisplayStringProvider((mc, i18n, opt) -> {
			if (healthBrightnessComponent == null) return opt.toOptionsString();
			final boolean enabled = ((IOption) healthBrightnessComponent).simple_health_indicator$getSlider().enabled;
			return enabled ? (int) (opt.value * 100) + "%" : i18n.translateKey(rawString("disabled"));
		});

		FADE_OUT.withDisplayStringProvider((mc, i18n, opt) -> {
			final SHIOptionRange option = (SHIOptionRange) opt;
			return opt.value == 0 ? i18n.translateKey("options.off") : option.customValueString();
		});

		// Callback isn't called on init
		RenderUtils.setTrackedCount(TRACKED_MOB_COUNT.value);
		final ContainerStyle style = getEnumOrDefault(ContainerStyle.values(), CONTAINER_STYLE.value, ContainerStyle.OPAQUE);
		RenderUtils.setContainerStyle(style);
	}

	private static void initPages() {
		optionsPage = new OptionsPage("simplehealthindicator.title", Items.FOOD_APPLE.getDefaultStack());
		OptionsPages.register(optionsPage);

		optionsPage.withComponent(
			new OptionsCategory("simplehealthindicator.category")
				.withComponent(new ToggleableOptionComponent<>(TRACKED_MOB_COUNT))
				.withComponent(new ToggleableOptionComponent<>(MAX_HEARTS))
				.withComponent(new BooleanOptionComponent(DAMAGE_FLASH))
				.withComponent(new ToggleableOptionComponent<>(HEART_SCALE))
				.withComponent(new ToggleableOptionComponent<>(DISPLAY_TIME))
				.withComponent(new BooleanOptionComponent(STYLIZED_HEARTS))
				.withComponent(new ToggleableOptionComponent<>(CONTAINER_STYLE))
				.withComponent(new ToggleableOptionComponent<>(RENDER_ORDER))
				.withComponent(new ToggleableOptionComponent<>(FILL_ORDER))
				.withComponent(new ToggleableOptionComponent<>(FADE_OUT))
				.withComponent(new BooleanOptionComponent(HEALTH_FULL_BRIGHT))
				.withComponent(healthBrightnessComponent = new FloatOptionComponent(HEALTH_BRIGHTNESS))
		);
		((IOption)healthBrightnessComponent).simple_health_indicator$getSlider().enabled = HEALTH_FULL_BRIGHT.value;
	}

	private static @NonNull <T extends Enum<T>> T getEnumOrDefault(@NonNull final T[] values, final int value, @NonNull final T defaultEnum) {
		return value < 0 || value >= values.length ? defaultEnum : values[value];
	}

	private static @NonNull String rawString(final String string) {
		return OPTION_STRING + string;
	}

	private static SHIOptionRange range(final String key, final int defaultValue, final int values) {
		return GameSettings.register(new SHIOptionRange(SHealthIndicator.MOD_ID + "." + key, defaultValue, values));
	}

	private static OptionFloat floatOpt(final String key, final float value) {
		return GameSettings.register(new OptionFloat(SHealthIndicator.MOD_ID + "." + key, value));
	}

	private static OptionBoolean boolOpt(final String key, final boolean value) {
		return GameSettings.register(new OptionBoolean(SHealthIndicator.MOD_ID + "." + key, value));
	}
}
