package dora.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * DoraMarkView
 * 支持在内容上添加 Drawable 或 文字 标签
 */
class DoraMarkView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    data class DrawableMark(
        val drawable: Drawable,
        val gravity: Int = Gravity.TOP or Gravity.END,
        val padding: Int = 0,
        val size: Int = -1
    )

    data class TextMark(
        val text: String,
        @ColorInt val textColor: Int = Color.WHITE,
        @ColorInt val backgroundColor: Int = Color.RED,
        val gravity: Int = Gravity.TOP or Gravity.END,
        val paddingH: Int = 12,
        val paddingV: Int = 4,
        val textSizeSp: Float = 12f,
        val cornerRadius: Float = 12f,
        val margin: Int = 8
    )

    private val drawableMarks = mutableListOf<DrawableMark>()
    private val textMarks = mutableListOf<TextMark>()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setWillNotDraw(false)
    }

    /** 添加图片标签 */
    fun addDrawableMark(drawable: Drawable, gravity: Int = Gravity.TOP or Gravity.END, padding: Int = 0, size: Int = -1) {
        drawableMarks.add(DrawableMark(drawable, gravity, padding, size))
        invalidate()
    }

    /** 添加文字标签（如“热门”、“限时”等） */
    fun addTextMark(text: String, @ColorInt textColor: Int = Color.WHITE, @ColorInt bgColor: Int = Color.RED,
                    gravity: Int = Gravity.TOP or Gravity.END, textSizeSp: Float = 12f,
                    cornerRadius: Float = 12f, margin: Int = 8) {
        textMarks.add(TextMark(text, textColor, bgColor, gravity, margin, margin, textSizeSp, cornerRadius, margin))
        invalidate()
    }

    fun clearMarks() {
        drawableMarks.clear()
        textMarks.clear()
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawDrawableMarks(canvas)
        drawTextMarks(canvas)
    }

    private fun drawDrawableMarks(canvas: Canvas) {
        for (mark in drawableMarks) {
            val d = mark.drawable
            val size = if (mark.size > 0) mark.size else d.intrinsicWidth
            val left: Int
            val top: Int
            when (mark.gravity) {
                Gravity.TOP or Gravity.END -> {
                    left = width - paddingRight - size - mark.padding
                    top = paddingTop + mark.padding
                }
                Gravity.BOTTOM or Gravity.END -> {
                    left = width - paddingRight - size - mark.padding
                    top = height - paddingBottom - size - mark.padding
                }
                Gravity.CENTER -> {
                    left = (width - size) / 2
                    top = (height - size) / 2
                }
                else -> {
                    left = paddingLeft + mark.padding
                    top = paddingTop + mark.padding
                }
            }
            d.setBounds(left, top, left + size, top + size)
            d.draw(canvas)
        }
    }

    private fun drawTextMarks(canvas: Canvas) {
        for (mark in textMarks) {
            textPaint.color = mark.textColor
            textPaint.textSize = mark.textSizeSp * resources.displayMetrics.scaledDensity
            textPaint.typeface = Typeface.DEFAULT_BOLD

            bgPaint.color = mark.backgroundColor

            val textWidth = textPaint.measureText(mark.text)
            val textHeight = textPaint.fontMetrics.run { descent - ascent }

            val bgWidth = textWidth + mark.paddingH * 2
            val bgHeight = textHeight + mark.paddingV * 2

            val left: Float
            val top: Float

            when (mark.gravity) {
                Gravity.TOP or Gravity.END -> {
                    left = width - bgWidth - mark.margin
                    top = mark.margin.toFloat()
                }
                Gravity.TOP or Gravity.START -> {
                    left = mark.margin.toFloat()
                    top = mark.margin.toFloat()
                }
                Gravity.BOTTOM or Gravity.END -> {
                    left = width - bgWidth - mark.margin
                    top = height - bgHeight - mark.margin
                }
                else -> {
                    left = mark.margin.toFloat()
                    top = mark.margin.toFloat()
                }
            }

            val rectF = RectF(left, top, left + bgWidth, top + bgHeight)
            canvas.drawRoundRect(rectF, mark.cornerRadius, mark.cornerRadius, bgPaint)

            val textX = left + mark.paddingH
            val textY = top + mark.paddingV - textPaint.fontMetrics.ascent
            canvas.drawText(mark.text, textX, textY, textPaint)
        }
    }
}
