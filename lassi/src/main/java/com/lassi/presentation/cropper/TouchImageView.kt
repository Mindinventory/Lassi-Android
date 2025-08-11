package com.lassi.presentation.cropper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import androidx.exifinterface.media.ExifInterface
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.createBitmap
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class TouchImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private val baseMatrix = Matrix() // this is the initial matrix
    private val gestureMatrix = Matrix() // this is for tracking the movement
    private val drawMatrix = Matrix() // this is for the drawing of the image
    private var mode = NONE

    private val last = PointF()
    private val start = PointF()
    private var minScale = 1f
    private var maxScale = 4f

    private var viewWidth = 0
    private var viewHeight = 0
    private var saveScale = 1f
    private var origWidth = 0f
    private var origHeight = 0f

    private val mScaleDetector: ScaleGestureDetector

    private var originalBitmap: Bitmap? = null
    private var originalUri: Uri? = null
    private var exifAngle: Float = 0f
    private var rotatedDegrees = 0

    var flipHorizontally = false
    var flipVertically = false

    fun flipImageHorizontally() {
        flipHorizontally = !flipHorizontally
        fitImageToView()
    }

    fun flipImageVertically() {
        flipVertically = !flipVertically
        fitImageToView()
    }

    fun setOriginalBitmap(bitmap: Bitmap?) {
        originalBitmap = bitmap
    }

    fun setOriginalUri(uri: Uri?) {
        originalUri = uri
        uri?.let {
            exifAngle = extractExifRotation(it)
        }
    }

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
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
                    gestureMatrix.postTranslate(dx, dy)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            updateDrawMatrix()
            invalidate()
            true
        }
    }

    private fun updateDrawMatrix() {
        drawMatrix.set(baseMatrix)
        drawMatrix.postConcat(gestureMatrix)
        imageMatrix = drawMatrix
    }

    private fun extractExifRotation(uri: Uri): Float {
        val exif = ExifInterface(context.contentResolver.openInputStream(uri)!!)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }
    fun getScale(): Float = saveScale


    private fun fixTrans() {
        val drawable = drawable ?: return

        val matrix = Matrix()
        matrix.set(baseMatrix)
        matrix.postConcat(gestureMatrix)

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        val points = floatArrayOf(
            0f, 0f,
            drawableWidth, 0f,
            drawableWidth, drawableHeight,
            0f, drawableHeight
        )
        matrix.mapPoints(points)

        val xs = listOf(points[0], points[2], points[4], points[6])
        val ys = listOf(points[1], points[3], points[5], points[7])

        val minX = xs.minOrNull() ?: 0f
        val maxX = xs.maxOrNull() ?: 0f
        val minY = ys.minOrNull() ?: 0f
        val maxY = ys.maxOrNull() ?: 0f

        val imageWidth = maxX - minX
        val imageHeight = maxY - minY

        var deltaX = 0f
        var deltaY = 0f

        // Horizontal bounds
        if (imageWidth <= viewWidth) {
            deltaX = (viewWidth - imageWidth) / 2 - minX
        } else {
            if (minX > 0) {
                deltaX = -minX
            } else if (maxX < viewWidth) {
                deltaX = viewWidth - maxX
            }
        }

        // Vertical bounds
        if (imageHeight <= viewHeight) {
            deltaY = (viewHeight - imageHeight) / 2 - minY
        } else {
            if (minY > 0) {
                deltaY = -minY
            } else if (maxY < viewHeight) {
                deltaY = viewHeight - maxY
            }
        }

        gestureMatrix.postTranslate(deltaX, deltaY)
    }


    private fun getImageDimensionsAfterBaseMatrix(): Pair<Float, Float> {
        val drawable = drawable ?: return Pair(0f, 0f)

        val points = floatArrayOf(
            0f, 0f,  // top-left
            drawable.intrinsicWidth.toFloat(), 0f, // top-right
            drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat(), // bottom-right
            0f, drawable.intrinsicHeight.toFloat()  // bottom-left
        )

        baseMatrix.mapPoints(points)

        val xs = listOf(points[0], points[2], points[4], points[6])
        val ys = listOf(points[1], points[3], points[5], points[7])

        val width = (xs.maxOrNull() ?: 0f) - (xs.minOrNull() ?: 0f)
        val height = (ys.maxOrNull() ?: 0f) - (ys.minOrNull() ?: 0f)

        return Pair(width, height)
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

            gestureMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
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
        Log.d("Debugging", "fitImageToView: manual zoom matrix: $baseMatrix")
        val drawable = drawable ?: return

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        val scale: Float = min(
            viewWidth.toFloat() / drawableWidth,
            viewHeight.toFloat() / drawableHeight
        )

        baseMatrix.setScale(scale, scale)
//        gestureMatrix.setScale(scale*2,scale*2)


        // Apply flipping
        val flipScaleX = if (flipHorizontally) -1f else 1f
        val flipScaleY = if (flipVertically) -1f else 1f
        baseMatrix.postScale(flipScaleX, flipScaleY, viewWidth / 2f, viewHeight / 2f)

        // Centering
        val redundantYSpace = (viewHeight.toFloat() - scale * drawableHeight) / 2 // this is for centering the image
        val redundantXSpace = (viewWidth.toFloat() - scale * drawableWidth) / 2   // this is for centering the image

        baseMatrix.postTranslate(redundantXSpace, redundantYSpace) // this is for centering the image

        // Apply EXIF rotation
        baseMatrix.postRotate(exifAngle, viewWidth / 2f, viewHeight / 2f)

        // Apply user-triggered rotation
        baseMatrix.postRotate(rotatedDegrees.toFloat(), viewWidth / 2f, viewHeight / 2f)

//        origWidth = viewWidth - 2 * redundantXSpace
//        origHeight = viewHeight - 2 * redundantYSpace

        val (transformedWidth, transformedHeight) = getImageDimensionsAfterBaseMatrix()
        origWidth = transformedWidth
        origHeight = transformedHeight

        updateDrawMatrix()
    }

    /**
     * This function generates the new bitmap for the cropped image... the execution comes to here only if the last touch is for the manual zoom.
     */
    fun getTransformedBitmap(): Bitmap? {
        val drawable = drawable ?: return null
        val originalBitmap = (drawable as? BitmapDrawable)?.bitmap ?: return null

        val resultBitmap = createBitmap(viewWidth, viewHeight)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(originalBitmap, drawMatrix, null)
        return resultBitmap
    }

    /**
     * This function is essential for handling the manually done rotation in the image cropping.
     */
    fun setImageManuallyRotatedDegrees(degrees: Int) {
        rotatedDegrees = (rotatedDegrees + degrees) % 360
        if (rotatedDegrees < 0) rotatedDegrees += 360
        fitImageToView()
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
    }
}
