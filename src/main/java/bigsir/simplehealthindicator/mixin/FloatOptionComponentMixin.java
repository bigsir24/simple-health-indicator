package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.options.IOption;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsComponent;
import net.minecraft.client.option.FloatOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FloatOptionComponent.class, remap = false)
public abstract class FloatOptionComponentMixin implements IOption, OptionsComponent {
	@Shadow
	@Final
	private GuiSlider slider;

	@Shadow
	@Final
	private FloatOption option;

	@Override
	public void simple_health_indicator$refreshString() {
		this.slider.displayString = this.option.getDisplayString();
	}

	@Override
	public GuiSlider simple_health_indicator$getSlider() {
		return slider;
	}
}
