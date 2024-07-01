package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHealthIndicator;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.Option;
import net.minecraft.client.option.RangeOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameSettings.class, remap = false)
public abstract class GameSettingsMixin {

	@Inject(method = "getDisplayString", at = @At("TAIL"), cancellable = true)
	public void offsetDisplayValue(Option<?> option, CallbackInfoReturnable<String> cir){
		if(option == SHealthIndicator.maxHearts){
			cir.setReturnValue(String.valueOf(((RangeOption)option).getValueIndex() + 2));
		}else if(option == SHealthIndicator.heartScale){
			cir.setReturnValue( (((RangeOption)option).getValueIndex() + 50) / 100.0 + "x");
		}
	}
}
