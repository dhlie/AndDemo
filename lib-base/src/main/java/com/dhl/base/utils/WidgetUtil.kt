package com.dhl.base.utils

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup

/**
 *
 * Author: Hello
 * Create: 2024/5/11 19:13
 * Description:
 *
 */
class WidgetUtil private constructor() {
    companion object {

        interface OnDoubleClickListener {
            fun onDoubleClick(view: View)
        }

        /**
         * 扩展view点击区域
         * @param view
         * @param offset 扩展的距离 正值向外扩大, 负值向内缩小
         *
         */
        fun expandViewClickArea(view: View?, offsetHor: Int, offsetVer: Int) {
            view ?: return
            if (view.width == 0 || view.height == 0) {
                view.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View?,
                        left: Int,
                        top: Int,
                        right: Int,
                        bottom: Int,
                        oldLeft: Int,
                        oldTop: Int,
                        oldRight: Int,
                        oldBottom: Int,
                    ) {
                        view.removeOnLayoutChangeListener(this)
                        view.post { expandViewClickArea(view, offsetHor, offsetVer) }
                    }
                })
                return
            }

            val rect = Rect()
            view.getHitRect(rect)
            rect.inset(-offsetHor, -offsetVer)
            val delegate = TouchDelegate(rect, view)
            (view.parent as? ViewGroup)?.touchDelegate = delegate
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setDoubleClickListener(
            view: View?,
            listener: GestureDetector.SimpleOnGestureListener?,
        ) {
            view ?: return
            if (listener == null) {
                view.setOnTouchListener(null)
                return
            }
            val gestureDetector =
                GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        return listener.onSingleTapConfirmed(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        return listener.onDoubleTap(e)
                    }
                })
            view.isClickable = true
            view.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
            }
        }

    }
}