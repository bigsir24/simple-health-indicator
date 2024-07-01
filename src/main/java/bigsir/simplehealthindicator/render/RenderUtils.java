package bigsir.simplehealthindicator.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.core.HitResult;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.util.helper.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
	private static long lastNano = 0;
	private static Entity entityCache = null;

	public static void renderInfo(Minecraft mc, float partialTick, long systemNano){
		Entity entity = null;
		if(mc.objectMouseOver != null && mc.objectMouseOver.entity instanceof EntityLiving){
			lastNano = systemNano;
			entity = entityCache = mc.objectMouseOver.entity;
		}else if(mc.objectMouseOver == null || mc.objectMouseOver.hitType != HitResult.HitType.ENTITY && entityCache != null){
			if(entityCache != null && entityCache.removed) entityCache = null;
			entity = entityCache;
			if((systemNano - lastNano) / 1000000L > 1000 ) entityCache = null;
		}
		double scale = 0.3;
		int heartsInRow = 5;
		if(entity != null) {
			int hearts = MathHelper.ceilInt(((EntityLiving) entity).getMaxHealth(), 2);
			int length = Math.min(hearts, heartsInRow);
			int rows = MathHelper.ceilInt(hearts, heartsInRow);

			GL11.glPushMatrix();
			GL11.glTranslated(-mc.activeCamera.getX(partialTick), -mc.activeCamera.getY(partialTick), -mc.activeCamera.getZ(partialTick));
			GL11.glTranslated(entity.xo + (entity.x - entity.xo) * partialTick, entity.yo + (entity.y - entity.yo) * partialTick + entity.bbHeight + 0.7, entity.zo + (entity.z - entity.zo) * partialTick);
			GL11.glRotatef(180 - (float) mc.activeCamera.getYRot(partialTick), 0, 1, 0);
			GL11.glRotatef((float) -mc.activeCamera.getXRot(partialTick), 1, 0, 0);

			GL11.glTranslated(-(length - (length - 1) / 9d) / 2d * scale, -0.5 * scale, 0);

			Tessellator tessellator = Tessellator.instance;

			GL11.glEnable(GL11.GL_TEXTURE_2D);

			GL11.glBindTexture(3553, mc.renderEngine.getTexture("/gui/icons.png"));
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
					drawHeart(tessellator, heartsFlash ? 1 : 0, xOff, yOff, zOff, scale);
					xOff += 8;
					zOff -= 0.001;
				}
				yOff -= 4;
			}


			int health = ((EntityLiving) entity).getHealth();
			int healthFull = MathHelper.ceilInt(health, 2);
			boolean drawHalf = health % 2 != 0;
			int healthRow = MathHelper.ceilInt(healthFull, heartsInRow);

			yOff = rows * 4;
			int xOff = 0;
			zOff = (rows - 1) * 0.001 + 0.001;
			for (int i = 0; i < healthRow; i++) {
				xOff = 0;
				yOff -= 4;
				int heartsRemaining = Math.min(healthFull - i * heartsInRow, heartsInRow);
				for (int j = 0; j < heartsRemaining; j++) {
					drawHeart(tessellator, drawHalf && i == healthRow - 1 && j == heartsRemaining - 1 ? 5 : 4, xOff, yOff, zOff, scale);
					xOff += 8;
					zOff -= 0.001;
				}
			}
			GL11.glPopMatrix();
		}

	}

	private static void drawHeart(Tessellator tessellator, int heartIndex, int xOffset, int yOffset, double zOffset, double scale){
		int offset = heartIndex * 9;
		double posX = xOffset / 9d;
		double posY = yOffset / 9d;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tessellator.startDrawing(GL11.GL_QUADS);

		tessellator.addVertexWithUV(posX * scale,posY*scale,zOffset, (16 + offset)/256d, 9/256d);
		tessellator.addVertexWithUV((posX+1) * scale,posY *scale, zOffset, (25 + offset)/256d,9/256d);

		tessellator.addVertexWithUV((posX+1) * scale,(posY+1) * scale,zOffset, (25 + offset)/256d, 0);
		tessellator.addVertexWithUV(posX * scale,(posY+1) * scale,zOffset, (16 + offset)/256d,0);

		tessellator.draw();
	}
}
