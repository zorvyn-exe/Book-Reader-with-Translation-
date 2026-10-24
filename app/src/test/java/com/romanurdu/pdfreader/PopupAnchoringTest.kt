package com.romanurdu.pdfreader

import org.junit.Assert.assertTrue
import org.junit.Test

class PopupAnchoringTest {

    data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float)
    data class PopupPos(val x: Float, val y: Float, val isAbove: Boolean)

    private fun calculatePopupPosition(
        wordRect: Rect,
        screenWidth: Float,
        screenHeight: Float,
        popupWidth: Float,
        popupHeight: Float,
        topMarginThreshold: Float = 140f
    ): PopupPos {
        val wordCenterX = (wordRect.left + wordRect.right) / 2f
        val halfWidth = popupWidth / 2f

        // Horizontal clamping within screen bounds
        val clampedX = wordCenterX.coerceIn(halfWidth + 16f, screenWidth - halfWidth - 16f) - halfWidth

        // Vertical anchoring: ABOVE word unless near the top
        val isNearTop = wordRect.top < topMarginThreshold
        val clampedY = if (isNearTop) {
            wordRect.bottom + 12f
        } else {
            (wordRect.top - popupHeight - 12f).coerceAtLeast(40f)
        }

        return PopupPos(clampedX, clampedY, !isNearTop)
    }

    @Test
    fun testPopupAnchorsAboveWordWhenSpacePermits() {
        val wordRect = Rect(left = 100f, top = 300f, right = 180f, bottom = 320f)
        val pos = calculatePopupPosition(
            wordRect = wordRect,
            screenWidth = 360f,
            screenHeight = 640f,
            popupWidth = 280f,
            popupHeight = 120f
        )

        assertTrue("Popup must be above the word when space permits", pos.isAbove)
        assertTrue("Popup Y coordinate (${pos.y}) must be above word top (${wordRect.top})", pos.y < wordRect.top)
    }

    @Test
    fun testPopupAnchorsBelowWordNearTopEdge() {
        val wordRect = Rect(left = 100f, top = 50f, right = 180f, bottom = 70f)
        val pos = calculatePopupPosition(
            wordRect = wordRect,
            screenWidth = 360f,
            screenHeight = 640f,
            popupWidth = 280f,
            popupHeight = 120f
        )

        assertTrue("Popup should anchor below word when near top edge", !pos.isAbove)
        assertTrue("Popup Y coordinate (${pos.y}) must be below word bottom (${wordRect.bottom})", pos.y > wordRect.bottom)
    }

    @Test
    fun testPopupClampedWithinRightScreenBoundary() {
        // Tapped word at extreme right margin
        val wordRect = Rect(left = 320f, top = 250f, right = 355f, bottom = 270f)
        val screenWidth = 360f
        val popupWidth = 280f

        val pos = calculatePopupPosition(
            wordRect = wordRect,
            screenWidth = screenWidth,
            screenHeight = 640f,
            popupWidth = popupWidth,
            popupHeight = 120f
        )

        assertTrue("Popup left edge must not be negative", pos.x >= 0)
        assertTrue("Popup right edge (${pos.x + popupWidth}) must not exceed screen width ($screenWidth)", (pos.x + popupWidth) <= screenWidth)
    }

    @Test
    fun testPopupClampedWithinLeftScreenBoundary() {
        // Tapped word at extreme left margin
        val wordRect = Rect(left = 5f, top = 250f, right = 35f, bottom = 270f)
        val screenWidth = 360f
        val popupWidth = 280f

        val pos = calculatePopupPosition(
            wordRect = wordRect,
            screenWidth = screenWidth,
            screenHeight = 640f,
            popupWidth = popupWidth,
            popupHeight = 120f
        )

        assertTrue("Popup left edge (${pos.x}) must be inside screen boundary", pos.x >= 0)
        assertTrue("Popup right edge must not exceed screen width", (pos.x + popupWidth) <= screenWidth)
    }
}
