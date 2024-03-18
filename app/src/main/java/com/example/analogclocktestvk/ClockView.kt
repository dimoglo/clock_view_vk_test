package com.example.analogclocktestvk

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var hourHandColor: Int = 0,
    private var hourStrokeWidth: Float = 0f,
    private var minuteHandColor: Int = 0,
    private var minuteStrokeWidth: Float = 0f,
    private var secondHandColor: Int = 0,
    private var secondStrokeWidth: Float = 0f,
    private var digitSize: Float = 0f,
    private var digitColor: Int = 0,
    private var radiusPercent: Float = 0f,
    private var circleColor: Int = 0,
    private var circleStyle: CircleStyle = CircleStyle.STROKE
) : View(context, attrs, defStyleAttr) {

    companion object{
        const val DEFAULT_CLOCK_BORDER_WIDTH = 25f
        const val DEFAULT_STROKE_WIDTH = 5f
        const val DEFAULT_HOUR_HANDLE_WIDTH = 20f
        const val DEFAULT_MINUTE_HANDLE_WIDTH = 10f
        const val DEFAULT_SECOND_HANDLE_WIDTH = 8f
        const val DEFAULT_RADIUS_PERCENT = 1f
        const val DEFAULT_HOUR_HANDLE_HEIGHT = 45
        const val DEFAULT_MINUTE_HANDLE_HEIGHT = 55
        const val DEFAULT_SECOND_HANDLE_HEIGHT = 66
    }

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var viewHalfWidth: Float = 0f
    private var viewHalfHeight: Float = 0f

    private var currentState: Int = 0
    private var radius: Float = 0f

    init {
        setBackgroundColor(Color.WHITE)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        if (hourHandColor == 0) hourHandColor =
            attributes.getColor(R.styleable.ClockView_hourHandColor, Color.BLACK)
        if (hourStrokeWidth == 0f) hourStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_hourStrokeWidth, DEFAULT_HOUR_HANDLE_WIDTH)
        if (minuteHandColor == 0) minuteHandColor =
            attributes.getColor(R.styleable.ClockView_minuteHandColor, Color.BLACK)
        if (minuteStrokeWidth == 0f) minuteStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_minuteStrokeWidth, DEFAULT_MINUTE_HANDLE_WIDTH)
        if (secondHandColor == 0) secondHandColor =
            attributes.getColor(R.styleable.ClockView_secondHandColor, Color.RED)
        if (secondStrokeWidth == 0f) secondStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_secondStrokeWidth, DEFAULT_SECOND_HANDLE_WIDTH)
        if (digitSize == 0f) {
            val attrValue = attributes.getDimension(R.styleable.ClockView_digitSize, 0f)
            digitSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, attrValue, resources.displayMetrics
            )
        }
        if (digitColor == 0) digitColor =
            attributes.getColor(R.styleable.ClockView_digitColor, Color.BLACK)
        if (radiusPercent == 0f) radiusPercent =
            attributes.getFloat(R.styleable.ClockView_radiusPercent, DEFAULT_RADIUS_PERCENT)

        if (circleColor == 0) circleColor =
            attributes.getColor(R.styleable.ClockView_circleColor, Color.BLACK)

        val circleStyleOrdinal = attributes.getInt(
            R.styleable.ClockView_circleStyle,
            CircleStyle.STROKE.ordinal
        )
        circleStyle = CircleStyle.entries.toTypedArray()[circleStyleOrdinal]

        attributes.recycle()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        if(digitSize == 0f){
            digitSize = viewWidth * 0.05f
        }
        viewHalfWidth = viewWidth / 2f
        viewHalfHeight = viewHeight / 2f
        
        radius = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            (viewWidth / 2f) * radiusPercent
        } else {
            (viewHeight/ 2f) * radiusPercent
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(viewHalfWidth, viewHalfHeight, radius, strokePaint(circleStyle))
        drawNumber(canvas)
        drawClockHandles(canvas)
        canvas.drawCircle(viewHalfWidth, viewHalfHeight, hourStrokeWidth * 0.6f, fillPaint())
        postInvalidateDelayed(1000)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = CustomViewSavedState(superState)
        savedState.currentState = currentState + 1
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as CustomViewSavedState
        super.onRestoreInstanceState(savedState.superState)
        currentState = savedState.currentState
        invalidate()
    }

    private fun strokePaint(styleAttr: CircleStyle) = Paint().apply {
        isAntiAlias = true
        color = circleColor
        strokeWidth = DEFAULT_CLOCK_BORDER_WIDTH
        style = when (styleAttr) {
            CircleStyle.FILL -> Paint.Style.FILL
            CircleStyle.FILL_AND_STROKE -> Paint.Style.FILL_AND_STROKE
            CircleStyle.STROKE -> Paint.Style.STROKE
        }
    }

    private fun fillPaint() = Paint().apply {
        isAntiAlias = true
        color = digitColor
        style = Paint.Style.FILL
        strokeWidth = DEFAULT_STROKE_WIDTH
        textSize = digitSize
    }

    private fun handlePaint(unit: Int) = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        when (unit) {
            Calendar.HOUR -> {
                color = hourHandColor
                strokeWidth = hourStrokeWidth
            }
            Calendar.MINUTE -> {
                color = minuteHandColor
                strokeWidth = minuteStrokeWidth
            }
            Calendar.SECOND -> {
                color = secondHandColor
                strokeWidth = secondStrokeWidth
            }
        }

    }

    private fun drawNumber(canvas: Canvas?) {
        val centerX = viewHalfWidth
        val centerY = viewHalfHeight
        for (number in 1..12) {

            val numberPositionOffset = if(number > 9) centerX * 0.05f else centerX * 0.03f

            val angle = Math.toRadians((30 * (number - 3)).toDouble())
            val radiusToNumerics = calculateValueByPercent(80, radius)
            val x = (centerX - numberPositionOffset + cos(angle) * radiusToNumerics).toFloat()
            val y = (centerY + numberPositionOffset + sin(angle) * radiusToNumerics).toFloat()
            canvas?.drawText(number.toString(), x, y, fillPaint())

            val startRadius = radius * 0.88f
            val endRadius = radius * 0.96f
            val sinAngle = sin(angle).toFloat()
            val cosAngle = cos(angle).toFloat()

            val startX = centerX + startRadius * sinAngle
            val startY = centerY - startRadius * cosAngle
            val endX = centerX + endRadius * sinAngle
            val endY = centerY - endRadius * cosAngle
            canvas?.drawLine(startX, startY, endX, endY, fillPaint())

            for (j in 1..4) {
                val minuteAngle = PI / 30 * (number * 5 + j)

                val minuteStartRadius = radius * 0.9f
                val minuteEndRadius = radius * 0.95f
                val sinMinuteAngle = sin(minuteAngle).toFloat()
                val cosMinuteAngle = cos(minuteAngle).toFloat()

                val minuteStartX = centerX + minuteStartRadius * sinMinuteAngle
                val minuteStartY = centerY - minuteStartRadius * cosMinuteAngle
                val minuteEndX = centerX + minuteEndRadius * sinMinuteAngle
                val minuteEndY = centerY - minuteEndRadius * cosMinuteAngle
                canvas?.drawLine(minuteStartX, minuteStartY, minuteEndX, minuteEndY, fillPaint())
            }
        }
    }

    private fun calculateValueByPercent(percent: Int, originalValue: Float): Float {
        return (originalValue / 100) * percent
    }

    private fun drawClockHandles(canvas: Canvas?) {
        val calInstance = Calendar.getInstance()

        drawHandle(canvas, calInstance, Calendar.HOUR)
        drawHandle(canvas, calInstance, Calendar.MINUTE)
        drawHandle(canvas, calInstance, Calendar.SECOND)
    }

    private fun drawHandle(canvas: Canvas?, cal: Calendar, unit: Int) {
        val timeByUnit = cal.get(unit)
        val onePercent = 360 / (if (unit == Calendar.HOUR) 12f else 60f)
        val degree = (onePercent * timeByUnit).roundToInt()
        val coordinatesFromDegree = getCoordinatesFromDegree(
            degree - 90,
            when(unit){
                Calendar.HOUR -> DEFAULT_HOUR_HANDLE_HEIGHT
                Calendar.MINUTE -> DEFAULT_MINUTE_HANDLE_HEIGHT
                else -> DEFAULT_SECOND_HANDLE_HEIGHT
            }
        )

        canvas?.drawLine(
            viewHalfWidth,
            viewHalfHeight,
            coordinatesFromDegree.first,
            coordinatesFromDegree.second,
            handlePaint(unit)
        )

    }

    private fun getCoordinatesFromDegree(degree: Int, length: Int): Pair<Float, Float> {
        val angle = Math.toRadians(degree.toDouble())
        val radiusToHandle = calculateValueByPercent(length, radius)
        val x = (viewHalfWidth + cos(angle) * radiusToHandle).toFloat()
        val y = (viewHalfHeight + sin(angle) * radiusToHandle).toFloat()
        return Pair(x, y)
    }

    enum class CircleStyle {
        FILL,
        STROKE,
        FILL_AND_STROKE
    }

    private class CustomViewSavedState : BaseSavedState {
        var currentState: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            currentState = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(currentState)
        }

        companion object CREATOR : Parcelable.Creator<CustomViewSavedState> {
            override fun createFromParcel(parcel: Parcel): CustomViewSavedState {
                return CustomViewSavedState(parcel)
            }

            override fun newArray(size: Int): Array<CustomViewSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}