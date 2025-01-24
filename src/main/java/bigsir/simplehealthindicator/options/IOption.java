package bigsir.simplehealthindicator.options;

import net.minecraft.client.gui.GuiSlider;

public interface IOption {
	void simple_health_indicator$refreshString();

	GuiSlider simple_health_indicator$getSlider();
}
