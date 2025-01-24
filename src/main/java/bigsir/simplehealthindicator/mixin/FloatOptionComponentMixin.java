package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.options.IOption;
import net.minecraft.client.gui.SliderElement;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsComponent;
import net.minecraft.client.option.OptionFloat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FloatOptionComponent.class, remap = false)
public abstract class FloatOptionComponentMixin implements IOption, OptionsComponent {
	@Shadow
	@Final
	protected SliderElement slider;

	@Shadow
	@Final
	protected OptionFloat option;

	@Override
	public void simple_health_indicator$refreshString() {
		this.slider.displayString = this.option.getDisplayString();
	}

	@Override
	public SliderElement simple_health_indicator$getSlider() {
		return slider;
	}
}
