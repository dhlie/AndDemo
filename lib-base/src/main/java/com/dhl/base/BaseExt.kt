package com.dhl.base

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Outline
import android.graphics.Point
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dhl.base.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 *
 * Author: duanhaoliang
 * Create: 2021/10/31 10:54
 * Description:
 *
 */

val screenSize: Point
    get() {
        val wm = ContextHolder.appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.maximumWindowMetrics
            Point(windowMetrics.bounds.width(), windowMetrics.bounds.height())
        } else {
            val point = Point()
            wm.defaultDisplay.getRealSize(point)
            point
        }
    }

val appWindowSize: Point
    get() {
        val wm =
            ContextHolder.appContext.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics

            /**
             * 以下计算方法来自 {@link android.view.WindowMetrics#getBounds()} 注释
             */
            // Gets all excluding insets
            val windowInsets = windowMetrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                //or WindowInsets.Type.statusBars() //状态栏不占应用空间
            )

            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom

            // Legacy size that Display#getSize reports
            val bounds = windowMetrics.bounds
            Point(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
        } else {
            val point = Point()
            wm.defaultDisplay.getSize(point)
            point
        }
    }

val statusBarHeight: Int
    get() {
        val resourceId: Int = Resources.getSystem().getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )
        return if (resourceId > 0) Resources.getSystem().getDimensionPixelSize(resourceId) else 25.dp
    }

/**
 * @see android.util.TypedValue.complexToDimensionPixelSize
 */
val Int.dp: Int
    get() {
        val f = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            ContextHolder.appContext.resources.displayMetrics
        )
        val res = if (f >= 0) (f + 0.5f).toInt() else (f - 0.5f).toInt()
        if (res != 0) return res
        if (this == 0) return 0
        if (this > 0) return 1
        return -1
    }

/**
 * @see android.util.TypedValue.complexToDimensionPixelSize
 */
val Float.dp: Int
    get() {
        val f = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            ContextHolder.appContext.resources.displayMetrics
        )
        val res = if (f >= 0) (f + 0.5f).toInt() else (f - 0.5f).toInt()
        if (res != 0) return res
        if (this == 0f) return 0
        if (this > 0f) return 1
        return -1
    }

val Int.px2dp: Float
    get() = this / ContextHolder.appContext.resources.displayMetrics.density

val Float.px2dp: Float
    get() = this / ContextHolder.appContext.resources.displayMetrics.density

/**
 * @see android.util.TypedValue.complexToDimensionPixelSize
 */
val Int.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        ContextHolder.appContext.resources.displayMetrics
    )

/**
 * @see android.util.TypedValue.complexToDimensionPixelSize
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        ContextHolder.appContext.resources.displayMetrics
    )

val Int.px2sp: Float
    get() = this / ContextHolder.appContext.resources.displayMetrics.scaledDensity

val Float.px2sp: Float
    get() = this / ContextHolder.appContext.resources.displayMetrics.scaledDensity

fun interface SingleClickListener : View.OnClickListener {
    override fun onClick(view: View) {
        val currTime = SystemClock.elapsedRealtime()
        val lastTime = view.getTag(R.id.last_click_time) as? Long ?: 0L
        if (currTime - lastTime > getClickInterval()) {
            view.setTag(R.id.last_click_time, currTime)
            onClicked(view)
        }
    }

    fun onClicked(view: View)

    fun getClickInterval() = 400
}

/**
 * 对 View 做圆角
 * @param radius: 不是百分比时为圆角半径, 是百分比时为[0, 1]之间的小数
 * @param fraction: radius 是否表示百分比
 */
fun View.roundCorner(radius: Int, fraction: Boolean = false) {
    roundCorner(radius.toFloat(), fraction)
}
/**
 * 对 View 做圆角
 * @param radius: 不是百分比时为圆角半径, 是百分比时为[0, 1]之间的小数
 * @param fraction: radius 是否表示百分比
 */
fun View.roundCorner(radius: Float = 0f, fraction: Boolean = false) {
    if (radius == 0f) {
        return
    }

    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val corner = if (fraction) {
                if (measuredWidth > measuredHeight) measuredHeight * radius else measuredWidth * radius
            } else {
                radius
            }
            outline.setRoundRect(
                0,
                0,
                view.measuredWidth,
                view.measuredHeight,
                corner
            )
        }

    }
    clipToOutline = true
}

fun isMainThread() = Looper.getMainLooper() === Looper.myLooper()

/**
 * 展示普通对话框：两个按钮
 * @param title String 标题
 * @param content String 内容
 * @param confirm String 确定
 * @param onConfirmClickListener OnClickListener? 确认回调
 * @param cancel String 取消
 * @param onCancelClickListener OnClickListener?
 */
fun showTipsDialog(
    context: Context,
    title: String = context.getString(R.string.dialog_hint),
    content: CharSequence,
    confirm: String = context.getString(R.string.confirm),
    onConfirmClickListener: View.OnClickListener? = null,
    cancel: String? = context.getString(R.string.cancel),
    onCancelClickListener: View.OnClickListener? = null,
    cancelable: Boolean = false,
    canceledOnTouchOutside: Boolean = false,
): Dialog {
    return CommonDialog.showTipsDialog(
        context = context,
        canceledOnTouchOutside = canceledOnTouchOutside,
        title = title,
        content = content,
        negativeText = cancel,
        negativeAutoDismiss = true,
        negativeClickListener = onCancelClickListener,
        positiveText = confirm,
        positiveAutoDismiss = true,
        positiveClickListener = onConfirmClickListener
    ).apply {
        setCancelable(cancelable)
    }
}

/**
 * 多行字符串,去掉首尾的空行, 和每一行的前后空格
 */
fun String.trimLines(): String {
    val lines = lines()

    val lastIndex = lines.lastIndex
    return lines.mapIndexedNotNull { index, value ->
        if ((index == 0 || index == lastIndex) && value.isBlank())
            null
        else
            value.trim()
    }
        .joinTo(StringBuilder(), "\n")
        .toString()
}

/**
 * 多行字符串,去掉首尾的空行, 再拼接成单行字符串
 */
fun String.joinLines(): String {
    val lines = lines()

    val lastIndex = lastIndex
    return lines.mapIndexedNotNull { index, value ->
        if ((index == 0 || index == lastIndex) && value.isBlank())
            null
        else
            value.trim()
    }
        .joinTo(StringBuilder(), "")
        .toString()
}