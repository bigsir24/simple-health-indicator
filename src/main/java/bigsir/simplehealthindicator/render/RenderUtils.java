package bigsir.simplehealthindicator.render;

import bigsir.simplehealthindicator.SHIClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorShader;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public final class RenderUtils {
	private static final IconCoordinate CONTAINER = TextureRegistry.getTexture("minecraft:gui/hud/heart/container");
	private static final IconCoordinate CONTAINER_BLINKING = TextureRegistry.getTexture("minecraft:gui/hud/heart/container_blinking");
	private static final IconCoordinate FULL = TextureRegistry.getTexture("minecraft:gui/hud/heart/full");
	private static final IconCoordinate HALF = TextureRegistry.getTexture("minecraft:gui/hud/heart/half");

	private static final Vector3d VEC3D = new Vector3d();
	private static final Vector3f VEC3F = new Vector3f();
	private static final Vector3f VEC3F_IM = new Vector3f();

	private static @Nullable TrackedMob[] trackedMobs = new TrackedMob[1];

	public static void renderInfo(final Minecraft mc, final float partialTick, final long systemNano){
		final Entity mouseOverEntity = getMouseOverEntity(mc);
		if (mouseOverEntity instanceof Mob mob) trySetTarget(mob);

		final long renderTimeLength = SHIClient.displayTime.value;

		for (int i = 0, trackedMobsLength = trackedMobs.length; i < trackedMobsLength; i++) {
			final TrackedMob tracked = trackedMobs[i];
			if (tracked == null) continue;

			if (tracked.mob.isRemoved() || (systemNano - tracked.lastTimestamp) / 1_000_000L > renderTimeLength * 100) {
				trackedMobs[i] = null;
				continue;
			}

			renderHearts(mc, partialTick, tracked.mob);
		}
	}

	public static void renderHearts(final Minecraft mc, final float partialTick, @Nullable final Mob mob) {
		if (mob == null) return;

		final int renderOrder = SHIClient.renderOrder.value == 0 ? 1 : -1;
		final int heartsInRow = SHIClient.maxHearts.value + 2;

		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.INTERFACE);

		//Previously headHeight + 1
		final float heightOffset = mob.getHeadHeight() + (mob.nickname.isEmpty() ? 0.6F : 0.85F);

		float scale = 0.3F * ((SHIClient.heartScale.value + 50)/100.0F);
		final int hearts = MathHelper.ceilInt(mob.getMaxHealth(), 2);
		final int length = Math.min(hearts, heartsInRow);

		GLRenderer.modelM4f().translate(getCamPosInverse(mc, partialTick));
		GLRenderer.modelM4f().translate(getEntityPos(mob, partialTick).add(0, heightOffset, 0));
		final float yRad = MathHelper.toRadians(180 - (float) mc.activeCamera.getYRot(partialTick));
		final float xRad = MathHelper.toRadians((float) -mc.activeCamera.getXRot(partialTick));
		GLRenderer.modelM4f().rotateY(yRad);
		GLRenderer.modelM4f().rotateX(xRad);
		GLRenderer.modelM4f().translate(-(length - (length - 1) / 9.0F) / 2.0F * scale, 0, 0);
		GLRenderer.modelM4f().translate(0, 0, 0.001F);

		final int rows = MathHelper.ceilInt(hearts, heartsInRow);
		final float brightness = getBrightness(mob, partialTick);

		final boolean heartsFlash = mob.heartsFlashTime >= 10 && mob.heartsFlashTime / 3 % 2 == 1;
		final boolean fillOrder = SHIClient.fillOrder.value == 0;

		final TessellatorShader tess = GLRenderer.getTessellator();
		tess.startDrawingQuads();

		// Draw heart containers
		int yOff = fillOrder ? (rows - 1) * 4 : 0;
		double zOff = (rows - 1) * 0.001;
		for (int i = 0; i < rows; i++) {
			int xOff = 0;
			for (int j = 0; j < Math.min(hearts - i * heartsInRow, heartsInRow); j++) {
				drawHeart(tess, heartsFlash ? CONTAINER_BLINKING : CONTAINER, xOff, yOff, zOff, scale, brightness);
				xOff += 8;
				zOff -= 0.001 * (fillOrder ? renderOrder : -renderOrder);
			}
			yOff -= fillOrder ? 4 : -4;
		}

		final int health = mob.getHealth();
		final int healthFull = MathHelper.ceilInt(health, 2);
		final boolean drawHalf = (health & 1) == 1;
		final int healthRow = MathHelper.ceilInt(healthFull, heartsInRow);

		// Draw hearts
		yOff = fillOrder ? (rows - 1) * 4 : 0;
		int xOff;
		zOff = (rows - 1) * 0.001 + 0.001;
		for (int i = 0; i < healthRow; i++) {
			xOff = 0;
			int heartsRemaining = Math.min(healthFull - i * heartsInRow, heartsInRow);
			for (int j = 0; j < heartsRemaining; j++) {
				drawHeart(tess, drawHalf && i == healthRow - 1 && j == heartsRemaining - 1 ? HALF : FULL, xOff, yOff, zOff, scale, brightness);
				xOff += 8;
				zOff -= 0.001 * (fillOrder ? renderOrder : -renderOrder);
			}
			yOff -= fillOrder ? 4 : -4;
		}

		tess.draw();

		GLRenderer.popFrame();
	}

	private static void drawHeart(TessellatorShader tess, IconCoordinate icon, int xOffset, int yOffset, double zOffset, double scale, float brightness){
		final double posX = xOffset / 9d;
		final double posY = yOffset / 9d;

		icon.parentAtlas.bind();

		tess.setColorOpaque3f(brightness, brightness, brightness);

		tess.addVertexWithUV(posX * scale,posY * scale, zOffset, icon.getIconUMin()+0.0001, icon.getIconVMax()-0.0001);
		tess.addVertexWithUV((posX+1) * scale,posY * scale, zOffset, icon.getIconUMax()-0.0001,icon.getIconVMax()-0.0001);
		tess.addVertexWithUV((posX+1) * scale,(posY+1) * scale, zOffset, icon.getIconUMax()-0.0001, icon.getIconVMin()+0.0001);
		tess.addVertexWithUV(posX * scale,(posY+1) * scale, zOffset, icon.getIconUMin()+0.0001,icon.getIconVMin()+0.0001);
	}

	public static void setTrackedCount(final int count) {
		trackedMobs = Arrays.copyOf(trackedMobs, count);
	}

	private static @NonNull Vector3f getCamPosInverse(final Minecraft mc, final float partialTick) {
		return mc.activeCamera.getPosition(partialTick, VEC3D).get(VEC3F).negate();
	}

	private static @NonNull Vector3f getEntityPos(@NonNull final Entity entity, final float partialTick) {
		VEC3F_IM.set(entity.x, entity.y, entity.z);
		return VEC3F.set(entity.xo, entity.yo, entity.zo).lerp(VEC3F_IM, partialTick);
	}

	private static @Nullable Entity getMouseOverEntity(final Minecraft mc){
		return mc.objectMouseOver instanceof HitResult.Entity hit ? hit.entity : null;
	}

	private static float getBrightness(@NonNull final Entity entity, final float partialTick){
		final float gamma = GameSettings.BRIGHTNESS.value;
		return applyGamma(SHIClient.healthFullbright.value ? SHIClient.healthBrightness.value : entity.getBrightness(partialTick), gamma);
	}

	public static void trySetTarget(@Nullable final Entity mobCandidate){
		if (mobCandidate instanceof Mob mob) {
			int finalIndex = -1;
			int minIndex = -1;
			long minTimestamp = Long.MAX_VALUE;
			for (int i = 0, trackedMobsLength = trackedMobs.length; i < trackedMobsLength; i++) {
				final TrackedMob tracked = trackedMobs[i];

				if (tracked == null) {
					finalIndex = i;
				}else {
					final long timestamp = tracked.lastTimestamp;
					if (finalIndex < 0 && timestamp < minTimestamp) {
						minTimestamp = timestamp;
						minIndex = i;
					}

					if (tracked.mob == mob) {
						tracked.lastTimestamp = System.nanoTime();
						return;
					}
				}
			}

			if (finalIndex < 0) finalIndex = minIndex;

			if (finalIndex < 0) return;
			final TrackedMob tracked = new TrackedMob(mob, System.nanoTime());
			trackedMobs[finalIndex] = tracked;
		}
	}

	private static float applyGamma(final float color, final float gamma) {
		float color2 = 1.0F - color;
		color2 = 1.0F - color2 * color2 * color2 * color2;
		return color * (1.0F - gamma) + color2 * gamma;
	}

	public static final class TrackedMob {
		final @NonNull Mob mob;
		long lastTimestamp;

		public TrackedMob(@NonNull final Mob mob, final long lastTimestamp) {
			this.mob = mob;
			this.lastTimestamp = lastTimestamp;
		}
	}
}
