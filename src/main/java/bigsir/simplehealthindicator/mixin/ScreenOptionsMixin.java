package bigsir.simplehealthindicator.mixin;

import bigsir.simplehealthindicator.SHIClient;
import bigsir.simplehealthindicator.SHIOptions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorShader;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenOptions.class)
public abstract class ScreenOptionsMixin {

	@WrapOperation(method = "drawPagesListItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ItemElement;render(Lnet/minecraft/core/item/ItemStack;II)V"))
	public void wrap(ItemElement instance, ItemStack itemStack, int x, int y, Operation<Void> original, @Local(name = "page") OptionsPage page) {
		if (page != SHIOptions.optionsPage) {
			original.call(instance, itemStack, x, y);
			return;
		}

		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.INTERFACE);

		final TessellatorShader tess = GLRenderer.getTessellator();
		if (SHIClient.modIcon != null) SHIClient.modIcon.bind();

		final int vy = y + 1;

		tess.startDrawingQuads();

		tess.setColorOpaque3f(1, 1, 1);
		tess.addVertexWithUV(x, vy, 0, 0, 0);
		tess.addVertexWithUV(x, vy + 16, 0, 0, 1);
		tess.addVertexWithUV(x + 16, vy + 16, 0, 1, 1);
		tess.addVertexWithUV(x + 16, vy, 0, 1, 0);

		tess.draw();

		GLRenderer.popFrame();
	}

}
