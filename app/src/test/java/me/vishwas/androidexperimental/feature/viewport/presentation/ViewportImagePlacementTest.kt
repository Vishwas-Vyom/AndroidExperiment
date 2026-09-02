package me.vishwas.androidexperimental.feature.viewport.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val VIEWPORT_WIDTH = 800f
private const val VIEWPORT_HEIGHT = 1000f
private const val TOLERANCE = 0.01f

/** Aspect ratios a picked photo realistically arrives with. */
private const val LANDSCAPE_16_9 = 16f / 9f
private const val PORTRAIT_3_4 = 3f / 4f
private const val PORTRAIT_9_16 = 9f / 16f
private const val SQUARE = 1f

class ViewportImagePlacementTest {
    private fun placementFor(aspectRatio: Float) =
        viewportImagePlacement(
            viewportWidthPx = VIEWPORT_WIDTH,
            viewportHeightPx = VIEWPORT_HEIGHT,
            imageAspectRatio = aspectRatio,
        )

    @Test
    fun `image is never narrower than the viewport`() {
        listOf(LANDSCAPE_16_9, PORTRAIT_3_4, PORTRAIT_9_16, SQUARE).forEach { ratio ->
            assertTrue(
                "ratio $ratio produced an image narrower than the viewport",
                placementFor(ratio).widthPx >= VIEWPORT_WIDTH,
            )
        }
    }

    @Test
    fun `image always covers the viewport height`() {
        listOf(LANDSCAPE_16_9, PORTRAIT_3_4, PORTRAIT_9_16, SQUARE).forEach { ratio ->
            assertTrue(
                "ratio $ratio produced an image shorter than the viewport",
                placementFor(ratio).heightPx >= VIEWPORT_HEIGHT - TOLERANCE,
            )
        }
    }

    @Test
    fun `scrolling right stops with the left edges flush`() {
        val placement = placementFor(LANDSCAPE_16_9)

        // Scrolling right pushes offsetX up; 0f is the ceiling.
        val clamped = placement.clampOffsetX(placement.centredOffsetX() + 100_000f)

        assertEquals(0f, clamped, TOLERANCE)
    }

    @Test
    fun `scrolling left stops with the right edges flush`() {
        val placement = placementFor(LANDSCAPE_16_9)

        val clamped = placement.clampOffsetX(placement.centredOffsetX() - 100_000f)

        // Image's right edge = offsetX + widthPx, measured from the viewport's left edge.
        assertEquals(VIEWPORT_WIDTH, clamped + placement.widthPx, TOLERANCE)
    }

    @Test
    fun `no reachable offset ever pulls an edge inside the viewport`() {
        listOf(LANDSCAPE_16_9, PORTRAIT_3_4, PORTRAIT_9_16, SQUARE).forEach { ratio ->
            val placement = placementFor(ratio)
            val steps = (-20..20).map { placement.clampOffsetX(it * VIEWPORT_WIDTH / 4f) }

            steps.forEach { offsetX ->
                assertTrue(
                    "ratio $ratio left a gap on the left at offset $offsetX",
                    offsetX <= TOLERANCE,
                )
                assertTrue(
                    "ratio $ratio left a gap on the right at offset $offsetX",
                    offsetX + placement.widthPx >= VIEWPORT_WIDTH - TOLERANCE,
                )
            }
        }
    }

    @Test
    fun `an image that only just covers the viewport cannot pan`() {
        // Height-fill would leave this one narrower than the viewport, so it is widened to exactly
        // the viewport's width — flush on both sides, with nowhere left to go.
        val placement = placementFor(PORTRAIT_9_16)

        assertFalse(placement.canPan)
        assertEquals(VIEWPORT_WIDTH, placement.widthPx, TOLERANCE)
        assertEquals(0f, placement.minOffsetXPx, TOLERANCE)
    }

    @Test
    fun `a wide image starts centred on the viewport`() {
        val placement = placementFor(LANDSCAPE_16_9)

        val leftBleed = -placement.centredOffsetX()
        val rightBleed = placement.centredOffsetX() + placement.widthPx - VIEWPORT_WIDTH

        assertTrue(placement.canPan)
        assertEquals(leftBleed, rightBleed, TOLERANCE)
    }

    @Test
    fun `excess height is cropped evenly above and below`() {
        val placement = placementFor(PORTRAIT_9_16)

        val topCrop = -placement.offsetYPx
        val bottomCrop = placement.offsetYPx + placement.heightPx - VIEWPORT_HEIGHT

        assertEquals(topCrop, bottomCrop, TOLERANCE)
    }

    @Test
    fun `bias maps the pan limits onto the constraint solver's 0 to 1 range`() {
        val placement = placementFor(LANDSCAPE_16_9)

        assertEquals(0f, placement.biasFor(0f), TOLERANCE)
        assertEquals(1f, placement.biasFor(placement.minOffsetXPx), TOLERANCE)
        assertEquals(0.5f, placement.biasFor(placement.centredOffsetX()), TOLERANCE)
    }

    @Test
    fun `bias never escapes 0 to 1, whatever the drag`() {
        listOf(LANDSCAPE_16_9, PORTRAIT_3_4, PORTRAIT_9_16, SQUARE).forEach { ratio ->
            val placement = placementFor(ratio)

            (-20..20).forEach { step ->
                val bias = placement.biasFor(step * VIEWPORT_WIDTH)
                assertTrue("ratio $ratio produced bias $bias", bias in 0f..1f)
            }
        }
    }

    @Test
    fun `an unmeasured image is inert rather than mis-sized`() {
        val placement = placementFor(0f)

        assertFalse(placement.canPan)
        assertEquals(VIEWPORT_WIDTH, placement.widthPx, TOLERANCE)
        assertEquals(0f, placement.clampOffsetX(500f), TOLERANCE)
    }
}
