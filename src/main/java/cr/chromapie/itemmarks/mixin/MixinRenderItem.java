package cr.chromapie.itemmarks.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cr.chromapie.itemmarks.core.MarkConfig;
import cr.chromapie.itemmarks.core.MarkRegistry;

/**
 * Mixin to render item marks at the same time as vanilla item overlays.
 * This ensures marks render below tooltips and follow dragged items.
 */
@Mixin(RenderItem.class)
public class MixinRenderItem {

    /**
     * Inject at the end of renderItemOverlayIntoGUI to render our mark
     * after vanilla renders the stack count but at the same rendering time.
     */
    @Inject(
        method = "renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At("TAIL"))
    private void itemmarks$renderMark(FontRenderer font, TextureManager textureManager, ItemStack stack, int x, int y,
        String altText, CallbackInfo ci) {
        if (stack == null) return;
        if (!MarkConfig.isEnabled()) return;

        String mark = MarkRegistry.getMark(stack);
        if (mark == null || mark.isEmpty()) return;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        float scale = MarkConfig.getActualScale();
        float textWidth = font.getStringWidth(mark) * scale;
        float textHeight = 8 * scale;

        float offsetX = 0;
        float offsetY = 0;

        switch (MarkConfig.getMarkPosition()) {
            case TOP_LEFT:
                offsetX = 0;
                offsetY = 0;
                break;
            case TOP_RIGHT:
                offsetX = 16 - textWidth;
                offsetY = 0;
                break;
            case BOTTOM_LEFT:
                offsetX = 0;
                offsetY = 16 - textHeight;
                break;
            case MIDDLE:
                offsetX = 8 - textWidth / 2;
                offsetY = 8 - textHeight / 2;
                break;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(x + offsetX, y + offsetY, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);
        font.drawStringWithShadow(mark, 0, 0, 0xFFFFFF);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
