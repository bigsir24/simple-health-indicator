package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHIClient;
import bigsir.simplehealthindicator.options.IOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(value = GameSettings.class, remap = false)
public abstract class GameSettingsMixin {
	@Unique
	public OptionRange maxHearts;
	@Unique
	public OptionRange heartScale;
	@Unique
	public OptionRange displayTime;
	@Unique
	public OptionRange renderOrder;
	@Unique
	public OptionBoolean healthFullbright;
	@Unique
	public OptionFloat healthBrightness;

	@Inject(method = "getDisplayString", at = @At("HEAD"), cancellable = true)
	public void offsetDisplayValue(Option<?> option, CallbackInfoReturnable<String> cir){
		if(option == SHIClient.maxHearts){
			cir.setReturnValue(String.valueOf(maxHearts.getValueIndex() + 2));
		}else if(option == SHIClient.heartScale){
			cir.setReturnValue( (heartScale.getValueIndex() + 50) / 100.0 + "x");
		}else if(option == SHIClient.displayTime){
			cir.setReturnValue( displayTime.getValueIndex() / 10.0 + "s");
		}else if(option == SHIClient.renderOrder){
			cir.setReturnValue( renderOrder.getValueIndex() == 0 ? "Default" : "Guidebook" );
		}else if(option == SHIClient.healthBrightness && !SHIClient.healthFullbright.value){
			cir.setReturnValue("Disabled");
		}
	}

	@Inject(method = "<init>", at = @At(value = "NEW", target = "(Ljava/io/File;Ljava/lang/String;)Ljava/io/File;"))
	public void addOptions(Minecraft minecraft, File file, CallbackInfo ci){
		SHIClient.optionsInit((GameSettings) (Object)this);

		this.maxHearts = SHIClient.maxHearts;
		this.heartScale = SHIClient.heartScale;
		this.displayTime = SHIClient.displayTime;
		this.renderOrder = SHIClient.renderOrder;
		this.healthFullbright = SHIClient.healthFullbright;
		this.healthBrightness = SHIClient.healthBrightness;
	}

	@Inject(method = "optionChanged", at = @At("HEAD"))
	public void changeText(Option<?> option, CallbackInfo ci){
		if(option == SHIClient.healthFullbright){
			((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$refreshString();
			((IOption) SHIClient.healthBrightnessComponent).simple_health_indicator$getSlider().enabled = SHIClient.healthFullbright.value;
		}
	}
}
