package bigsir.simplehealthindicator.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.option.OptionRange;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnusedReturnValue")
public class SHIOptionRange extends OptionRange {
	private int divisor = 1;
	private int valueOffset;
	private @NonNull String suffix = "";
	private String @Nullable [] keys;

	public SHIOptionRange(@NotNull String name, int defaultValue, int values) {
		super(name, defaultValue, values);
	}

	public SHIOptionRange setDivisor(final int divisor) {
		this.divisor = divisor;
		return this;
	}

	public SHIOptionRange setStartValue(final int startValue) {
		this.valueOffset = startValue;
		return this;
	}

	public SHIOptionRange setValueSuffix(@NonNull final String suffix) {
		this.suffix = suffix;
		return this;
	}

	public SHIOptionRange setKeys(@NonNull final String... keys) {
		this.keys = keys;
		return this;
	}

	public float getMappedFloat() {
		return (this.getValueIndex() + this.valueOffset) / (float) this.divisor;
	}

	public int getMappedValue() {
		return this.getValueIndex() + this.valueOffset;
	}

	@Override
	public @NotNull String getDisplayString() {
		if (keys != null) return getDisplayEnumString();
		return this.displayStringProvider != null ? this.displayStringProvider.getDisplayString(Minecraft.getMinecraft(), I18n.getInstance(), this) : this.customValueString();
	}

	public String getDisplayEnumString() {
		if (keys == null || value < 0 || value >= keys.length) return "[ERROR]";
		return I18n.getInstance().translateKey(this.keys[value]);
	}

	public @NonNull String customValueString() {
		if (this.divisor == 1) return (this.value + this.valueOffset) + this.suffix;
		return ((this.value + this.valueOffset) / (float) this.divisor) + this.suffix;
	}
}
