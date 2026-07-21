package bigsir.simplehealthindicator.mixin.temp;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.IntegerSliderElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes a BTA issue that makes the slider snap incorrectly
 */
@Mixin(value = IntegerSliderElement.class, remap = false)

public abstract class IntegerSliderElementMixin extends ButtonElement {
	public IntegerSliderElementMixin(int id, int xPosition, int yPosition, String text) {
		super(id, xPosition, yPosition, text);
	}

	@ModifyExpressionValue(method = "mouseDragged", at =  @At(value = "CONSTANT", args = "intValue=2"))
	private int wrapIncorrectWidth(int original) {
		return this.width;
	}
}
