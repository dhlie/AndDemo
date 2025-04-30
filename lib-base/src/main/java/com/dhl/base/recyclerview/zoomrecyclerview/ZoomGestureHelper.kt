package com.dhl.base.recyclerview.zoomrecyclerview

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.dhl.base.utils.SystemUtil

/**
 *
 * Author: duanhl
 * Create: 2021/4/11 3:48 PM
 * Description:
 *
 */
class ZoomGestureHelper {

    companion object {

        fun initScaleGesture(recyclerView: RecyclerView, zoomLayoutManager: ZoomLayoutManager) {
            val scaleGestureDetector = ScaleGestureDetector(
                recyclerView.context,
                object : ScaleGestureDetector.OnScaleGestureListener {

                    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                        zoomLayoutManager.setScalePivot(detector.focusX, detector.focusY)
                        return true
                    }

                    override fun onScaleEnd(detector: ScaleGestureDetector) {
                        zoomLayoutManager.adjustScale()
                    }

                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        return zoomLayoutManager.scaleBy(detector.scaleFactor)
                    }

                })

            val itemTouchListener = object : OnItemTouchListener {

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                    scaleGestureDetector.onTouchEvent(e)
                }

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    return zoomLayoutManager.zoomable && e.pointerCount > 1
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                }
            }

            //添加缩放手势监听
            recyclerView.removeOnItemTouchListener(itemTouchListener)
            recyclerView.addOnItemTouchListener(itemTouchListener)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun initRecyclerViewDoubleTapGesture(
            view: View,
            simpleOnGestureListener: GestureDetector.SimpleOnGestureListener? = null
        ) {
            val clickGestureDetector =
                GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        return simpleOnGestureListener?.onSingleTapConfirmed(e) ?: false
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        val recycleView: RecyclerView
                        if (view !is RecyclerView) {
                            var parent: ViewParent? = view.parent
                            while (parent != null && parent !is RecyclerView) {
                                parent = parent.parent
                            }
                            recycleView = parent as? RecyclerView ?: return false
                        } else {
                            recycleView = view
                        }

                        val zoomLayoutManager =
                            recycleView.layoutManager as? ZoomLayoutManager ?: return false
                        if (!zoomLayoutManager.zoomable) {
                            return false
                        }

                        var pivotX = e.x
                        var pivotY = e.y
                        if (view !is RecyclerView) {
                            val rect = Rect()
                            SystemUtil.getPositionInParentView(view, recycleView, rect)

                            pivotX += rect.left
                            pivotY += rect.top
                        }

                        zoomLayoutManager.setScalePivot(pivotX, pivotY)
                        zoomLayoutManager.doubleTap()
                        return true
                    }
                })

            //添加双击手势监听
            view.setOnTouchListener { _, event ->
                clickGestureDetector.onTouchEvent(event)
            }
        }

    }

}