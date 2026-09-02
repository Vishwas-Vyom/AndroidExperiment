package me.vishwas.androidexperimental.feature.viewport.presentation

import kotlin.math.max

/**
 * Where the room image sits inside the viewport, in pixels.
 *
 * [offsetXPx] is measured from the viewport's left edge to the image's left edge, so the pan range
 * is always `[minOffsetXPx, 0]`:
 *
 *  - `0f` — the two left edges are flush; the scroll-right limit.
 *  - [minOffsetXPx] — the two right edges are flush; the scroll-left limit.
 */
internal data class ViewportImagePlacement(
    val widthPx: Float,
    val heightPx: Float,
    val minOffsetXPx: Float,
    val offsetYPx: Float,
) {
    /** True only when the image is wider than the viewport and therefore has somewhere to pan. */
    val canPan: Boolean get() = minOffsetXPx < 0f

    fun clampOffsetX(offsetX: Float): Float = offsetX.coerceIn(minOffsetXPx, 0f)

    /** Centres the image on the viewport — the starting position for a freshly picked image. */
    fun centredOffsetX(): Float = minOffsetXPx / 2f

    /**
     * Maps a pan offset onto the horizontal bias ConstraintLayout positions the image by: `0f` puts
     * the image's left edge on the left viewport edge, `1f` puts its right edge on the right one.
     * Because bias cannot leave `[0f, 1f]`, the constraints enforce the same limits [clampOffsetX]
     * does. An image with nowhere to pan is centred.
     */
    fun biasFor(offsetX: Float): Float =
        if (canPan) clampOffsetX(offsetX) / minOffsetXPx else 0.5f
}

/**
 * Sizes an image of [imageAspectRatio] (width / height) so it always *covers* the viewport.
 *
 * Filling the viewport's height alone is not enough: a portrait photo scaled that way comes out
 * narrower than the viewport, leaving its left and right edges stranded inside it with gaps that no
 * amount of clamping can close. So the image is widened to the viewport's width whenever height-fill
 * would fall short, and the extra height that costs is cropped evenly top and bottom.
 */
internal fun viewportImagePlacement(
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    imageAspectRatio: Float,
): ViewportImagePlacement {
    if (imageAspectRatio <= 0f || viewportWidthPx <= 0f || viewportHeightPx <= 0f) {
        return ViewportImagePlacement(
            widthPx = viewportWidthPx,
            heightPx = viewportHeightPx,
            minOffsetXPx = 0f,
            offsetYPx = 0f,
        )
    }

    val widthPx = max(viewportHeightPx * imageAspectRatio, viewportWidthPx)
    val heightPx = widthPx / imageAspectRatio
    return ViewportImagePlacement(
        widthPx = widthPx,
        heightPx = heightPx,
        minOffsetXPx = viewportWidthPx - widthPx,
        offsetYPx = (viewportHeightPx - heightPx) / 2f,
    )
}
