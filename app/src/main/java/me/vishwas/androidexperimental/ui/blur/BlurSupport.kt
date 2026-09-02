package me.vishwas.androidexperimental.ui.blur

import android.content.Context
import android.os.Build
import android.view.WindowManager

/**
 * Runtime capability checks for the blur techniques in this package.
 *
 * Blur on Android is not one feature but three, each gated on a different API level, which is why
 * every technique here has to say which one it needs:
 *
 *  - **RenderEffect** (API 31) — the GPU blur behind `Modifier.blur`. Below 31 there is no
 *    hardware blur at all and `Modifier.blur` is silently a no-op.
 *  - **RuntimeShader / AGSL** (API 33) — lets you write the blur kernel yourself, which is the
 *    only way to get effects `BlurEffect` cannot express (variable radius, directional, radial).
 *  - **Cross-window blur** (API 31) — the compositor blurring *other windows* behind yours. The
 *    system turns it off under battery saver and on low-end devices, so it needs a runtime check
 *    rather than an SDK check.
 */
object BlurSupport {

    /** `RenderEffect`, and therefore `Modifier.blur`, landed in Android 12. */
    val isRenderEffectSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** AGSL `RuntimeShader` landed in Android 13. */
    val isRuntimeShaderSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * Whether the compositor will currently honour window blur. Unlike the two flags above this
     * can flip at runtime — battery saver, low-end device config, or the developer-options toggle
     * all turn it off — so re-read it rather than caching it.
     */
    fun isCrossWindowBlurEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return windowManager.isCrossWindowBlurEnabled
    }
}
