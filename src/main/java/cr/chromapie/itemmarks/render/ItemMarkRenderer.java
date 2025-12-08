package cr.chromapie.itemmarks.render;

/**
 * Item mark rendering is now handled by MixinRenderItem which injects into
 * RenderItem.renderItemOverlayIntoGUI. This ensures marks:
 * 1. Render at the same time as vanilla item counts (below tooltips)
 * 2. Follow dragged items
 * 3. Appear on hotbar items
 *
 * This class is kept for potential future non-mixin fallback.
 */
public class ItemMarkRenderer {
    // Rendering handled by MixinRenderItem
}
