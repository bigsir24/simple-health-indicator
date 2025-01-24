package bigsir.simplehealthindicator.options;

import net.minecraft.client.gui.SliderElement;

public interface IOption {
	void simple_health_indicator$refreshString();

	SliderElement simple_health_indicator$getSlider();
}
