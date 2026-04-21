package bigsir.simplehealthindicator.mixin.temp;

import bigsir.simplehealthindicator.SHIClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "startGame", at = @At("TAIL"))
	public void startSHI(CallbackInfo ci) {
		SHIClient.afterClientStart();
	}
}
