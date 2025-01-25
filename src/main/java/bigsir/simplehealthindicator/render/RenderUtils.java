package bigsir.simplehealthindicator.render;

import bigsir.simplehealthindicator.SHealthIndicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.HitResult;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
	private static long lastNano = 0;
	private static Entity entityCache = null;

	public static void renderInfo(Minecraft mc, float partialTick, long systemNano){
		int renderOrder = SHealthIndicator.renderOrder.value == 0 ? 1 : -1;
		long renderTimeLength = SHealthIndicator.displayTime.value;

		Entity entity = null;
		if(mc.objectMouseOver != null && mc.objectMouseOver.entity instanceof EntityLiving){
			lastNano = systemNano;
			entity = entityCache = mc.objectMouseOver.entity;
		}else if(mc.objectMouseOver == null || mc.objectMouseOver.hitType != HitResult.HitType.ENTITY && entityCache != null){
			if(entityCache != null && entityCache.removed) entityCache = null;
			entity = entityCache;
			if((systemNano - lastNano) / 1000000L > renderTimeLength * 100 ) entityCache = null;
		}
		double scale = 0.3 * ((SHealthIndicator.heartScale.value + 50)/100.0);
		int heartsInRow = SHealthIndicator.maxHearts.value + 2;
		if(entity != null) {
			float brightness = SHealthIndicator.healthFullbright.value ? SHealthIndicator.healthBrightness.value : entity.getBrightness(partialTick);

			int hearts = MathHelper.ceilInt(((EntityLiving) entity).getMaxHealth(), 2);
			int length = Math.min(hearts, heartsInRow);
			int rows = MathHelper.ceilInt(hearts, heartsInRow);

			GL11.glPushMatrix();
			GL11.glTranslated(-mc.activeCamera.getX(partialTick), -mc.activeCamera.getY(partialTick), -mc.activeCamera.getZ(partialTick));

			float playerHeight = (entity instanceof EntityPlayer ? (((EntityPlayer) entity).isDwarf() ? 0.4F : 1.4F) : 0);
			//Previously headHeight + 1
			float heightOffset = entity.getHeadHeight() + (((EntityLiving) entity).nickname.isEmpty() ? 0.6F : 0.85F) + playerHeight;

			GL11.glTranslated(entity.xo + (entity.x - entity.xo) * partialTick, entity.yo + (entity.y - entity.yo) * partialTick + heightOffset, entity.zo + (entity.z - entity.zo) * partialTick);
			GL11.glRotatef(180 - (float) mc.activeCamera.getYRot(partialTick), 0, 1, 0);
			GL11.glRotatef((float) -mc.activeCamera.getXRot(partialTick), 1, 0, 0);

			GL11.glTranslated(-(length - (length - 1) / 9d) / 2d * scale, 0, 0);

			Tessellator tessellator = Tessellator.instance;

			GL11.glEnable(GL11.GL_TEXTURE_2D);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/assets/minecraft/textures/gui/icons.png"));
			GL11.glTranslated(0, 0, 0.001);

			boolean heartsFlash = entity.heartsFlashTime / 3 % 2 == 1;
			if (entity.heartsFlashTime < 10) {
				heartsFlash = false;
			}

			int yOff = (rows - 1) * 4;
			double zOff = (rows - 1) * 0.001;
			for (int i = 0; i < rows; i++) {
				int xOff = 0;
				for (int j = 0; j < Math.min(hearts - i * heartsInRow, heartsInRow); j++) {
					drawHeart(tessellator, heartsFlash ? 1 : 0, xOff, yOff, zOff, scale, brightness);
					xOff += 8;
					zOff -= 0.001 * renderOrder;
				}
				yOff -= 4;
			}


			int health = ((EntityLiving) entity).getHealth();
			int healthFull = MathHelper.ceilInt(health, 2);
			boolean drawHalf = health % 2 != 0;
			int healthRow = MathHelper.ceilInt(healthFull, heartsInRow);

			yOff = rows * 4;
			int xOff;
			zOff = (rows - 1) * 0.001 + 0.001;
			for (int i = 0; i < healthRow; i++) {
				xOff = 0;
				yOff -= 4;
				int heartsRemaining = Math.min(healthFull - i * heartsInRow, heartsInRow);
				for (int j = 0; j < heartsRemaining; j++) {
					drawHeart(tessellator, drawHalf && i == healthRow - 1 && j == heartsRemaining - 1 ? 5 : 4, xOff, yOff, zOff, scale, brightness);
					xOff += 8;
					zOff -= 0.001 * renderOrder;
				}
			}
			GL11.glPopMatrix();
		}

	}

	private static void drawHeart(Tessellator tessellator, int heartIndex, int xOffset, int yOffset, double zOffset, double scale, float brightness){
		int offset = heartIndex * 9;
		double posX = xOffset / 9d;
		double posY = yOffset / 9d;

		tessellator.startDrawing(GL11.GL_QUADS);
		tessellator.setColorOpaque_F(brightness, brightness, brightness);

		tessellator.addVertexWithUV(posX * scale,posY*scale,zOffset, (16 + offset)/256d + 0.0001, 9/256d - 0.0001);
		tessellator.addVertexWithUV((posX+1) * scale,posY *scale, zOffset, (25 + offset)/256d - 0.0001,9/256d - 0.0001);

		tessellator.addVertexWithUV((posX+1) * scale,(posY+1) * scale,zOffset, (25 + offset)/256d - 0.0001, 0 + 0.0001);
		tessellator.addVertexWithUV(posX * scale,(posY+1) * scale,zOffset, (16 + offset)/256d + 0.0001,0 + 0.0001);

		tessellator.draw();
	}

	public static Entity getCachedEntity(){
		return entityCache;
	}

	public static void setCachedEntity(Entity entity){
		entityCache = entity;
		lastNano = System.nanoTime();
	}
}
