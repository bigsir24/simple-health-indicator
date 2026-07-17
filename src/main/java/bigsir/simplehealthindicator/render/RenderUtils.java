package bigsir.simplehealthindicator.render;

import bigsir.simplehealthindicator.SHIShaders;
import bigsir.simplehealthindicator.SHealthIndicator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorShader;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.animal.MobPig;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.player.gamemode.Gamemodes;
import net.minecraft.core.util.helper.LightIndexHelper;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.IVehicle;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static bigsir.simplehealthindicator.SHIOptions.*;

@Environment(EnvType.CLIENT)
public final class RenderUtils {
	private static final IconCoordinate[] CONTAINER = new IconCoordinate[2];
	private static final IconCoordinate[] CONTAINER_BLINKING = new IconCoordinate[2];
	private static final IconCoordinate[] SURVIVAL = getHeartTextures("minecraft:gui/hud/heart/survival/");
	private static final IconCoordinate[] HARDCORE = getHeartTextures("minecraft:gui/hud/heart/hardcore/");
	private static final IconCoordinate[] CREATIVE = getHeartTextures(SHealthIndicator.MOD_ID + ":gui/hud/heart/creative/");
	private static final IconCoordinate[] VEHICLE = getHeartTextures("minecraft:gui/hud/heart/vehicle/pig/");

	private static final Vector3d VEC3D = new Vector3d();
	private static final Vector3d VEC3F = new Vector3d();
	private static final Vector3d VEC3F_IM = new Vector3d();
	private static final Vector3f VEC3_FINAL = new Vector3f();

	private static @Nullable TrackedMob[] trackedMobs = new TrackedMob[1];

	public static void renderInfo(final Minecraft mc, final float partialTick, final long systemNano) {
		final Entity mouseOverEntity = getMouseOverEntity(mc);
		if (mouseOverEntity instanceof Mob mob) trySetTarget(mob);

		final long renderTimeLength = DISPLAY_TIME.value;

		for (int i = 0, trackedMobsLength = trackedMobs.length; i < trackedMobsLength; i++) {
			final TrackedMob tracked = trackedMobs[i];
			if (tracked == null) continue;

			final long elapsedNanos = (systemNano - tracked.lastTimestamp) / 1_000_000L;
			final boolean removedOrCancelled = !shouldDisplayHearts(tracked.mob) || tracked.mob.isRemoved();
			if (removedOrCancelled || (systemNano - tracked.lastTimestamp) / 1_000_000L > renderTimeLength * 100) {
				trackedMobs[i] = null;
				continue;
			}

			renderHearts(mc, partialTick, tracked.mob, renderTimeLength * 100 - elapsedNanos);
		}
	}

	public static void renderHearts(final Minecraft mc, final float partialTick, @Nullable final Mob mob, final long remainingMs) {
		if (mob == null || GameSettings.IMMERSIVE_MODE.value > 0) return;

		GLRenderer.pushFrame();
		GLRenderer.setShader(SHIShaders.WORLD_INTERFACE);
		final boolean doFadeOut = FADE_OUT.value > 0;
		if (doFadeOut) {
			GLRenderer.enableState(State.BLEND);
		}else {
			GLRenderer.disableState(State.BLEND);
		}

		//Previously headHeight + 1
		final float heightOffset = mob.getHeadHeight() + (mob.nickname.isEmpty() ? 0.6F : 0.85F);

		getEntityPos(mc, mob, partialTick).add(0, heightOffset, 0).get(VEC3_FINAL);
		GLRenderer.modelM4f().translate(VEC3_FINAL);
		final float yRad = MathHelper.toRadians(180 - (float) mc.activeCamera.getYRot(partialTick));
		final float xRad = MathHelper.toRadians((float) -mc.activeCamera.getXRot(partialTick));
		GLRenderer.modelM4f().rotateY(yRad);
		GLRenderer.modelM4f().rotateX(xRad);

		final boolean heartsFlash = DAMAGE_FLASH.value && mob.heartsFlashTime >= 10 && mob.heartsFlashTime / 3 % 2 == 1;

		final TessellatorShader tess = GLRenderer.getTessellator();
		tess.startDrawingQuads();
		final float alpha = doFadeOut ? Math.min(remainingMs / (FADE_OUT.getMappedFloat() * 1000.0F), 1) : 1.0F;
		tess.setColor4f(1, 1, 1, alpha);
		TextureRegistry.guiSpriteAtlas.bind();

		final byte lightmapIndex = getLightmapIndex(mob, partialTick);
		// Make sure that vertex attrib 3 (lightmap)
		// is enabled in the VAO
		tess.setLightmapCoord1i(lightmapIndex);
		LightmapHelper.instance.enableLightmapRendering();
		GLRenderer.setLightMapStrength(1.0F); // Typically called via Lighting.enableLight()

		final int health = mob.getHealth();
		final int maxHealth = mob.getMaxHealth();

		final int finalHealth;
		final int finalMaxHealth;

		final int healthPerPage = PAGE_HEALTH_LIMIT.value;
		if (healthPerPage <= 0) {
			finalHealth = health;
			finalMaxHealth = maxHealth;
		}else {
			final int remainder = health % healthPerPage;
			final int div = floorDivEq(health, healthPerPage);
			final int pageHealth = health <= 0 ? 0 : remainder == 0 ? healthPerPage : remainder;
			final int pageMaxHealth = Math.min(Math.min(maxHealth, healthPerPage), maxHealth - div * healthPerPage);
			finalHealth = pageHealth;
			finalMaxHealth = pageMaxHealth;

			final boolean alwaysVisible = PAGE_NUMBER_STYLE.value >= 4;
			// Set the lightmap value for the font renderer
			GLRenderer.setLightmapCoord1i(lightmapIndex);
			if (alwaysVisible || (div > 0 && PAGE_NUMBER_STYLE.value != 0)) drawPageNumber(mc, mob, div, finalHealth, finalMaxHealth, alpha);
			// NOTE: vertex attrib 5 is still enabled at this
			// point, which can cause issues (especially if we
			// mistakenly use it instead :p)
		}

		final int styledMaxHealth = CONTAINER_STYLE.value == 2 ? finalHealth : finalMaxHealth;

		final int containerHealth = CONTAINER_STYLE.value == 2 ? Math.max(finalHealth, 1) : styledMaxHealth;
		final int containerMaxHealth = Math.max(styledMaxHealth, 1);

		drawHearts(tess, containerHealth, containerMaxHealth, heartsFlash ? CONTAINER_BLINKING : CONTAINER);
		drawHearts(tess, finalHealth, styledMaxHealth, getMobHeartTex(mob));

		tess.draw();

		GLRenderer.popFrame();
	}

	private static void drawHearts(final TessellatorShader tess, final int health, final int containers, final IconCoordinate[] icons) {
		final boolean fillOrderDown = FILL_ORDER.value == 0;
		final int renderOrderZ = RENDER_ORDER.value == 0 ? 1 : -1;
		final int heartsPerRow = MAX_HEARTS.getMappedValue();
		final float scale = 0.3F * HEART_SCALE.getMappedFloat();

		final int numHearts = ceilDiv(health, 2);
		final int rows = ceilDiv(numHearts, heartsPerRow);
		final int containerRows = ceilDiv(containers, heartsPerRow * 2);
		final int containerWidthHalf = Math.min(ceilDiv(containers, 2), heartsPerRow) * 4;

		final int orderOffset = fillOrderDown ? (containerRows - 1) * 4 : 0;

		for (int i = 0; i < rows; ++i) {
			final int yOffset = i * (fillOrderDown ? -4 : 4) + orderOffset;
			final int heartsToDraw = Math.min(numHearts - (i * heartsPerRow), heartsPerRow);
			for (int j = 0; j < heartsToDraw; ++j) {
				final int xOffset = j * 8 - containerWidthHalf;
				final double zOffset = (i + j) * 0.001 * (fillOrderDown ? -renderOrderZ : renderOrderZ);
				final boolean isHalfNibble = i == rows - 1 && j == heartsToDraw - 1 && (health & 1) == 1;
				drawHeart(tess, icons[isHalfNibble ? 1 : 0], xOffset, yOffset, zOffset, scale);
			}
		}
	}

	private static void drawPageNumber(final Minecraft mc, final Mob mob, final int div, final int health, final int maxHealth, float alpha) {
		final int styledMaxHealth = CONTAINER_STYLE.value == 2 ? health : maxHealth;
		final int containerMaxHealth = Math.max(styledMaxHealth, 1);
		final int heartsPerRow = MAX_HEARTS.getMappedValue() * 2;
		final int maxWidth = Math.min(containerMaxHealth, heartsPerRow);
		final int healthPerPage = PAGE_HEALTH_LIMIT.value;

		final String pageNumber = getPageNumber(mob, div, healthPerPage);

		final int al = Math.max(1, (int) (alpha * 255));

		GLRenderer.pushFrame();
		GLRenderer.enableState(State.BLEND);
		final float scale = 0.3F * HEART_SCALE.getMappedFloat() * 1 / 9.0f;
		GLRenderer.modelM4f().translate(0, 0, 0.01f);
		GLRenderer.modelM4f().scale(1 * scale, -1 * scale, -1 * scale);
		// final int numberOffset = ((maxWidth & 1) == 1) ? 2 : 2;
		mc.font.render(pageNumber, maxWidth * 2 - 2, -7).setColor(0x00FFFFFF | (al << 24)).call();
		GLRenderer.popFrame();
		TextureRegistry.guiSpriteAtlas.bind();
	}

	private static String getPageNumber(final Mob mob, final int div, final int healthPerPage) {
		final int style = PAGE_NUMBER_STYLE.value;
		if (style == 1) {
			// Page
			return "+" + div;
		}else if (style == 2) {
			// Hearts
			final int health = div * healthPerPage;
			return "+" + health / 2 + ((health & 1) == 1 ? ".5" : "");
		} else if(style == 3) {
			// Health
			final int health = div * healthPerPage;
			return "+" + health;
		}else if(style == 4) {
			// Total Hearts
			final int health = Math.max(0, mob.getHealth());
			return health / 2 + ((health & 1) == 1 ? ".5" : "");
		}else if(style == 5) {
			// Total Health
			final int health = Math.max(0, mob.getHealth());
			return String.valueOf(health);
		}

		return "?";
	}

	private static int ceilDiv(final int a, final int b) {
		final int c = a / b;
		return c * b < a ? c + 1 : c;
	}

	private static int floorDivEq(final int a, final int b) {
		final int c = a / b;
		return c * b >= a ? c - 1 : c;
	}

	private static void drawHeart(TessellatorShader tess, IconCoordinate icon, float xOffset, int yOffset, double zOffset, double scale) {
		final double posX = xOffset / 9d;
		final double posY = yOffset / 9d;

		tess.addVertexWithUV(posX * scale,posY * scale, zOffset, icon.getIconUMin()+0.0001, icon.getIconVMax()-0.0001);
		tess.addVertexWithUV((posX+1) * scale,posY * scale, zOffset, icon.getIconUMax()-0.0001,icon.getIconVMax()-0.0001);
		tess.addVertexWithUV((posX+1) * scale,(posY+1) * scale, zOffset, icon.getIconUMax()-0.0001, icon.getIconVMin()+0.0001);
		tess.addVertexWithUV(posX * scale,(posY+1) * scale, zOffset, icon.getIconUMin()+0.0001,icon.getIconVMin()+0.0001);
	}

	private static IconCoordinate @NonNull [] getHeartTextures(@NonNull final String subKey) {
		final String full = subKey + "full";
		final String half = subKey + "half";
		return new IconCoordinate[]{TextureRegistry.getTexture(full), TextureRegistry.getTexture(half)};
	}

	private static IconCoordinate @NonNull [] getMobHeartTex(@NonNull final Mob mob) {
		if (!STYLIZED_HEARTS.value) return SURVIVAL;

		if (mob instanceof Player player) {
			final Gamemode gm = player.getGamemode();
			if (gm == Gamemodes.SURVIVAL) {
				return SURVIVAL;
			} else if (gm == Gamemodes.HARDCORE) {
				return HARDCORE;
			} else if (gm == Gamemodes.CREATIVE) {
				return CREATIVE;
			}
		}else if (mob instanceof MobPig pig) {
			return pig.getSaddled() ? VEHICLE : SURVIVAL;
		}

		return SURVIVAL;
	}

	public static void setContainerStyle(@NonNull final ContainerStyle style) {
		if (style == ContainerStyle.TRANSPARENT) {
			CONTAINER[0] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_tp");
			CONTAINER[1] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_half_tp");
			CONTAINER_BLINKING[0] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_blinking_tp");
			CONTAINER_BLINKING[1] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_half_blinking_tp");
		} else {
			CONTAINER[0] = TextureRegistry.getTexture("minecraft:gui/hud/heart/container");
			CONTAINER[1] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_half");
			CONTAINER_BLINKING[0] = TextureRegistry.getTexture("minecraft:gui/hud/heart/container_blinking");
			CONTAINER_BLINKING[1] = TextureRegistry.getTexture("simplehealthindicator:gui/hud/heart/container/container_half_blinking");
		}
	}

	public static void setTrackedCount(final int count) {
		trackedMobs = Arrays.copyOf(trackedMobs, count);
	}

	private static @NonNull Vector3d getEntityPos(final Minecraft mc, @NonNull final Entity entity, final float partialTick) {
		mc.activeCamera.getPosition(partialTick, VEC3D);
		VEC3F_IM.set(entity.x, entity.y, entity.z);
		return VEC3F.set(entity.xo, entity.yo, entity.zo).lerp(VEC3F_IM, partialTick).sub(VEC3D);
	}

	private static @Nullable Entity getMouseOverEntity(final Minecraft mc){
		return mc.objectMouseOver instanceof HitResult.Entity hit ? hit.entity : null;
	}

	private static byte getLightmapIndex(@NonNull final Entity entity, final float partialTick) {
		if (HEALTH_FULL_BRIGHT.value) {
			final float gamma = GameSettings.BRIGHTNESS.value;
			float brightness = applyGamma(HEALTH_BRIGHTNESS.value, gamma);
			return LightIndexHelper.lightIndex2f(brightness, brightness);
		}else {
			return entity.getLightIndex(partialTick);
		}
	}

	public static void trySetTarget(@Nullable final Entity mobCandidate) {
		if (mobCandidate instanceof Mob mob && shouldDisplayHearts(mob)) {
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

	private static boolean shouldDisplayHearts(@NonNull final Mob mob) {
		// Skip if mob is current vehicle
		final Player player = Minecraft.getMinecraft().thePlayer;
		final IVehicle vehicle = player == null ? null : player.vehicle;
		return vehicle != mob;
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
