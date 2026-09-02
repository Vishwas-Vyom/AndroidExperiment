package me.vishwas.androidexperimental.ui.blur

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogWindowProvider

/** Light enough to leave the blur visible, heavy enough to stand in for it when blur is off. */
private const val DefaultBlurDimAmount = 0.2f

/**
 * Blurs everything *behind this window* — the activity under a dialog, or whatever is under a
 * translucent activity.
 *
 * This is the one blur an app cannot draw itself. The pixels belong to other windows, so only the
 * system compositor can touch them; [backdropBlur] can reach a background inside your own
 * hierarchy but never past the window edge.
 *
 * Requirements, all of which fail silently rather than throwing:
 *  - API 31+.
 *  - The window must be translucent, or there is nothing to see through. For a Compose `Dialog`
 *    that is already true; for an activity it means a translucent theme.
 *  - [BlurSupport.isCrossWindowBlurEnabled] must be true — battery saver and low-end device
 *    profiles switch window blurs off wholesale.
 *
 * Call it from inside the `Dialog` content so `LocalView` resolves to the dialog's window.
 *
 * @param dimAmount how far the window dims what is behind it, 0f to 1f. Blur and dim are set
 *   together on purpose: a dialog's default dim is heavy enough to bury the blur, and dim is also
 *   the fallback that still gives foreground contrast on devices where blur is switched off.
 */
@Composable
fun BlurBehindWindow(
    radius: Dp,
    enabled: Boolean = true,
    dimAmount: Float = DefaultBlurDimAmount,
) {
    val view = LocalView.current
    val radiusPx = with(LocalDensity.current) { radius.roundToPx() }

    DisposableEffect(view, radiusPx, enabled, dimAmount) {
        val window = view.findWindow()
        if (window == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return@DisposableEffect onDispose { }
        }
        val hadFlag = window.attributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND != 0
        val previousRadius = window.attributes.blurBehindRadius
        val previousDim = window.attributes.dimAmount

        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = radiusPx }
            window.setDimAmount(dimAmount)
        }
        onDispose {
            window.attributes = window.attributes.apply { blurBehindRadius = previousRadius }
            window.setDimAmount(previousDim)
            if (!hadFlag) window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }
}

/**
 * Blurs whatever shows through this window's own background, within its bounds.
 *
 * The sibling of [BlurBehindWindow]: that one blurs the windows underneath, this one blurs what
 * the window's translucent background lets through. Same API 31 and
 * [BlurSupport.isCrossWindowBlurEnabled] requirements, and the window background must have alpha —
 * an opaque background leaves nothing to blur.
 */
@Composable
fun WindowBackgroundBlur(radius: Dp, enabled: Boolean = true) {
    val view = LocalView.current
    val radiusPx = with(LocalDensity.current) { radius.roundToPx() }

    DisposableEffect(view, radiusPx, enabled) {
        val window = view.findWindow()
        if (window == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return@DisposableEffect onDispose { }
        }
        window.setBackgroundBlur(if (enabled) radiusPx else 0)
        onDispose { window.setBackgroundBlur(0) }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun Window.setBackgroundBlur(radiusPx: Int) {
    setBackgroundBlurRadius(radiusPx)
}

/** The dialog's window when inside one, otherwise the host activity's. */
private fun View.findWindow(): Window? =
    (parent as? DialogWindowProvider)?.window ?: context.findActivity()?.window

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
