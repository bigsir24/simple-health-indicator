package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHIClient;
import bigsir.simplehealthindicator.options.IOption;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameSettings.class, remap = false)
public abstract class GameSettingsMixin {

	@Inject(method = "getDisplayString", at = @At("HEAD"), cancellable = true)
	public void offsetDisplayValue(Option<?> option, CallbackInfoReturnable<String> cir){
		if(option == SHIClient.maxHearts){
			cir.setReturnValue(String.valueOf(SHIClient.maxHearts.getValueIndex() + 2));
		}else if(option == SHIClient.heartScale){
			cir.setReturnValue((SHIClient.heartScale.getValueIndex() + 50) / 100.0 + "x");
		}else if(option == SHIClient.displayTime){
			cir.setReturnValue(SHIClient.displayTime.getValueIndex() / 10.0 + "s");
		}else if(option == SHIClient.renderOrder){
			cir.setReturnValue(SHIClient.renderOrder.getValueIndex() == 0 ? SHIClient.translateString("default") : SHIClient.translateString("guidebook") );
		}else if(option == SHIClient.healthBrightness && !SHIClient.healthFullbright.value){
			cir.setReturnValue(SHIClient.translateString("disabled"));
		}else if(option == SHIClient.fillOrder){
			cir.setReturnValue(SHIClient.fillOrder.getValueIndex() == 0 ? SHIClient.translateString("down") : SHIClient.translateString("up") );
		}
	}

	@Inject(method = "optionChanged", at = @At("HEAD"))
	public void changeText(Option<?> option, CallbackInfo ci){
		if(option == SHIClient.healthFullbright){
			((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$refreshString();
			((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$getSlider().enabled = SHIClient.healthFullbright.value;
		}
	}
}
