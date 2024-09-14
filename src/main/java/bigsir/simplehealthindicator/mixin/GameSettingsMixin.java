package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHealthIndicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.Option;
import net.minecraft.client.option.RangeOption;
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
	public RangeOption maxHearts;
	@Unique
	public RangeOption heartScale;

	@Inject(method = "getDisplayString", at = @At("TAIL"), cancellable = true)
	public void offsetDisplayValue(Option<?> option, CallbackInfoReturnable<String> cir){
		if(option == SHealthIndicator.maxHearts){
			cir.setReturnValue(String.valueOf(((RangeOption)option).getValueIndex() + 2));
		}else if(option == SHealthIndicator.heartScale){
			cir.setReturnValue( (((RangeOption)option).getValueIndex() + 50) / 100.0 + "x");
		}
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/b100/utils/ReflectUtils;getAllObjects(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Object;)[Ljava/lang/Object;"))
	public void addOptions(Minecraft minecraft, File file, CallbackInfo ci){
		SHealthIndicator.optionsInit((GameSettings) (Object)this);

		this.maxHearts = SHealthIndicator.maxHearts;
		this.heartScale = SHealthIndicator.heartScale;
	}
}
