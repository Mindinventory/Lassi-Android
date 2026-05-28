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

    private var isScaling = false

    private var scalePivotX = 0f
    private var scalePivotY = 0f

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

            val parentView = findCropImageView()

            // 🔥 Always pass to scale detector FIRST
            mScaleDetector.onTouchEvent(event)

            // 🔥 Detect pinch EARLY (IMPORTANT FIX)
            if (event.pointerCount >= 2) {
                if (parentView != null && !parentView.isManualMode) {

                    parentView.isManualMode = true
                    parentView.isMatrixSynced = true

                    // ✅ Sync Crop → Touch only once when entering manual mode
                    setExternalMatrix(parentView.mImageMatrix)
                }
            }

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> {
                    // ✅ ONLY DRAG when single finger
                    if (mode == DRAG && event.pointerCount == 1) {

                        val dx = event.x - last.x
                        val dy = event.y - last.y

                        drawMatrix.postTranslate(dx, dy)
                        fixBounds()

                        imageMatrix = drawMatrix
                        invalidate()

                        last.set(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_POINTER_UP,
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    mode = NONE

                    parentView?.postDelayed({

                        if (parentView.isManualMode) {

                            parentView.lastManualEndTime = System.currentTimeMillis()

                            val matrix = getCurrentMatrix()

                            parentView.mImageMatrix.set(matrix)
                            parentView.imageView.imageMatrix = parentView.mImageMatrix

                            parentView.invalidate()
                        }

                    }, 150)
                }
            }

            // 🔥 Apply matrix
            updateDrawMatrix()
            invalidate()

            true
        }
    }

    private fun findCropImageView(): CropImageView? {
        var viewParent: android.view.ViewParent? = this@TouchImageView.parent

        while (viewParent != null) {
            if (viewParent is CropImageView) {
                return viewParent
            }
            viewParent = viewParent.parent
        }

        return null
    }

    private fun updateDrawMatrix() {
        imageMatrix = drawMatrix
    }

    fun getCurrentMatrix(): Matrix {
        return Matrix(drawMatrix)
    }

    fun setExternalMatrix(externalMatrix: Matrix) {
        drawMatrix.set(externalMatrix)
        imageMatrix = drawMatrix
        invalidate()
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
    fun getScale(): Float {
        val values = FloatArray(9)
        drawMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    fun getTranslation(): Pair<Float, Float> {
        val values = FloatArray(9)
        drawMatrix.getValues(values)
        return Pair(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y])
    }


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
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true

            // 🔥 LOCK pivot ONCE
            scalePivotX = detector.focusX
            scalePivotY = detector.focusY

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false

            fixBounds()
            imageMatrix = drawMatrix
            invalidate()
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val parentView = findCropImageView() ?: return false
            if (!parentView.isManualMode) return false

            val values = FloatArray(9)
            drawMatrix.getValues(values)

            val currentScale = abs(values[Matrix.MSCALE_X])
            var scaleFactor = detector.scaleFactor
            val newScale = currentScale * scaleFactor

            // Clamp scale
            if (newScale > maxScale) {
                scaleFactor = maxScale / currentScale
            } else if (newScale < minScale) {
                scaleFactor = minScale / currentScale
            }

            if (scaleFactor.isNaN() || scaleFactor.isInfinite() || scaleFactor == 1f) {
                return false
            }

            // ✅ FIX:
            // Scale around current finger focus in VIEW coordinates.
            // Do NOT convert focus using drawMatrix.invert().
            drawMatrix.postScale(
                scaleFactor,
                scaleFactor,
                detector.focusX,
                detector.focusY
            )

            fixBounds()

            imageMatrix = drawMatrix
            invalidate()

            return true
        }
    }

    private fun fixBounds() {
        val drawable = drawable ?: return

        val rect = RectF(
            0f,
            0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )

        drawMatrix.mapRect(rect)

        var dx = 0f
        var dy = 0f

        if (rect.width() > width) {
            if (rect.left > 0) dx = -rect.left
            if (rect.right < width) dx = width - rect.right
        } else {
            dx = width / 2f - rect.centerX()
        }

        if (rect.height() > height) {
            if (rect.top > 0) dy = -rect.top
            if (rect.bottom < height) dy = height - rect.bottom
        } else {
            dy = height / 2f - rect.centerY()
        }

        drawMatrix.postTranslate(dx, dy)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable ?: return

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        if (drawableWidth == 0f || drawableHeight == 0f) return

        // 🔥 Calculate base scale (fit center)
        val scale = min(
            viewWidth / drawableWidth,
            viewHeight / drawableHeight
        )

        // 🔥 VERY IMPORTANT (fix your issue)
        minScale = scale          // ❗ prevents zoom-out below bounds
        saveScale = scale

        // 🔥 Reset matrix completely
        drawMatrix.reset()

        // Apply base scale
        drawMatrix.postScale(scale, scale)

        // Center image
        val redundantXSpace = (viewWidth - drawableWidth * scale) / 2f
        val redundantYSpace = (viewHeight - drawableHeight * scale) / 2f
        drawMatrix.postTranslate(redundantXSpace, redundantYSpace)

        // Apply flip
        val flipX = if (flipHorizontally) -1f else 1f
        val flipY = if (flipVertically) -1f else 1f
        drawMatrix.postScale(flipX, flipY, viewWidth / 2f, viewHeight / 2f)

        // Apply EXIF rotation
        drawMatrix.postRotate(exifAngle, viewWidth / 2f, viewHeight / 2f)

        // Apply manual rotation
        drawMatrix.postRotate(rotatedDegrees.toFloat(), viewWidth / 2f, viewHeight / 2f)

        imageMatrix = drawMatrix
        invalidate()
    }

    /**
     * This function generates the new bitmap for the cropped image... the execution comes to here only if the last touch is for the manual zoom.
     */
    fun getTransformedBitmap(): Bitmap? {
        val drawable = drawable ?: return null
        val originalBitmap = (drawable as? BitmapDrawable)?.bitmap ?: return null

        // ✅ Use actual TouchImageView size
        val resultBitmap = createBitmap(width, height)

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
