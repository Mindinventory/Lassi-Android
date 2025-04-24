package com.lassi.presentation.cropper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class TouchImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private val matrix = Matrix()
    private var mode = NONE

    private val last = PointF()
    private val start = PointF()
    private var minScale = 1f
    private var maxScale = 4f
    private var m: FloatArray = FloatArray(9)

    private var viewWidth = 0
    private var viewHeight = 0
    private var saveScale = 1f
    private var origWidth = 0f
    private var origHeight = 0f

    private val mScaleDetector: ScaleGestureDetector

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        imageMatrix = matrix
        scaleType = ScaleType.MATRIX

        setOnTouchListener { _, event ->
            mScaleDetector.onTouchEvent(event)

            val curr = PointF(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val dx = curr.x - last.x
                    val dy = curr.y - last.y
                    matrix.postTranslate(dx, dy)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }

            imageMatrix = matrix
            invalidate()
            true
        }
    }

    private fun fixTrans() {
        matrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            matrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        return when {
            trans < minTrans -> -trans + minTrans
            trans > maxTrans -> -trans + maxTrans
            else -> 0f
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= scaleFactor

            saveScale = max(minScale, min(saveScale, maxScale))
            scaleFactor = saveScale / origScale

            val focusX = detector.focusX
            val focusY = detector.focusY

            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
            fixTrans()
            return true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable ?: return

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        val scale: Float = min(
            viewWidth.toFloat() / drawableWidth,
            viewHeight.toFloat() / drawableHeight
        )

        matrix.setScale(scale, scale)

        val redundantYSpace = (viewHeight.toFloat() - scale * drawableHeight) / 2
        val redundantXSpace = (viewWidth.toFloat() - scale * drawableWidth) / 2

        matrix.postTranslate(redundantXSpace, redundantYSpace)

        origWidth = viewWidth - 2 * redundantXSpace
        origHeight = viewHeight - 2 * redundantYSpace

        imageMatrix = matrix
    }

    fun getTransformedBitmap(): Bitmap? {
        val drawable = drawable ?: return null
        val originalBitmap = (drawable as? BitmapDrawable)?.bitmap ?: return null

        val resultBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(originalBitmap, matrix, null)
        return resultBitmap
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
    }
}
