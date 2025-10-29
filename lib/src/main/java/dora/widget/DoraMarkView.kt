package dora.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import dora.widget.markview.R
import kotlin.math.roundToInt

class DoraMarkView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    data class TextMark(
        val text: String,
        val textColor: Int = Color.WHITE,
        val bgColor: Int = Color.RED,
        val textSizeSp: Float = 12f,
        val cornerRadius: Float = 12f,
        val paddingH: Int = 12,
        val paddingV: Int = 6,
        val gravity: Int = Gravity.TOP or Gravity.END,
        val margin: Int = 8
    )

    data class DrawableMark(
        val drawable: Drawable,
        val gravity: Int = Gravity.TOP or Gravity.END,
        val margin: Int = 8
    )

    private val textMarks = mutableListOf<TextMark>()
    private val drawableMarks = mutableListOf<DrawableMark>()
    private var markView: View? = null
    private var markVisible: Boolean = true

    private val tmpRect = Rect()
    private val outRect = Rect()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setWillNotDraw(false)
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        attrs ?: return
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DoraMarkView)

        // 原来的 markDrawable
        ta.getDrawable(R.styleable.DoraMarkView_dview_mv_drawable)?.let {
            addDrawableMark(it)
        }

        val layoutId = ta.getResourceId(R.styleable.DoraMarkView_dview_mv_layout, 0)
        if (layoutId != 0) {
            markView = LayoutInflater.from(context).inflate(layoutId, this, false)
        }

        markVisible = ta.getBoolean(R.styleable.DoraMarkView_dview_mv_visible, true)
        ta.recycle()
    }

    /** Drawable 标记相关 */
    fun addDrawableMark(drawable: Drawable, gravity: Int = Gravity.TOP or Gravity.END, margin: Int = 8) {
        drawableMarks.add(DrawableMark(drawable, gravity, margin))
        invalidate()
    }

    fun clearDrawableMarks() {
        drawableMarks.clear()
        invalidate()
    }

    /** View 标记 */
    fun setMarkView(view: View?) {
        markView = view
        requestLayout()
    }

    fun setMarkVisible(visible: Boolean) {
        markVisible = visible
        invalidate()
    }

    /** Text 标记 */
    fun addTextMark(mark: TextMark) {
        textMarks.add(mark)
        invalidate()
    }

    fun clearTextMarks() {
        textMarks.clear()
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!markVisible) return

        // 绘制 Drawable 标记
        drawableMarks.forEach { mark ->
            val rect = Rect()
            Gravity.apply(
                mark.gravity,
                mark.drawable.intrinsicWidth,
                mark.drawable.intrinsicHeight,
                Rect(
                    paddingLeft + mark.margin,
                    paddingTop + mark.margin,
                    width - paddingRight - mark.margin,
                    height - paddingBottom - mark.margin
                ),
                rect
            )
            mark.drawable.bounds = rect
            mark.drawable.draw(canvas)
        }

        // 绘制 View
        markView?.let { view ->
            val contentWidth = width - paddingLeft - paddingRight
            val contentHeight = height - paddingTop - paddingBottom

            measureChild(
                view,
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.AT_MOST)
            )

            val markRect = Rect()
            Gravity.apply(
                Gravity.TOP or Gravity.END,
                view.measuredWidth,
                view.measuredHeight,
                Rect(
                    paddingLeft,
                    paddingTop,
                    width - paddingRight,
                    height - paddingBottom
                ),
                markRect
            )

            canvas.save()
            canvas.translate(markRect.left.toFloat(), markRect.top.toFloat())
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.draw(canvas)
            canvas.restore()
        }

        // 绘制 Text 标记
        textMarks.forEach { mark ->
            textPaint.color = mark.textColor
            textPaint.textSize = mark.textSizeSp * resources.displayMetrics.scaledDensity
            textPaint.typeface = Typeface.DEFAULT_BOLD
            bgPaint.color = mark.bgColor

            val textWidth = textPaint.measureText(mark.text)
            val textHeight = textPaint.fontMetrics.run { descent - ascent }

            val bgWidth = (textWidth + mark.paddingH * 2).roundToInt()
            val bgHeight = (textHeight + mark.paddingV * 2).roundToInt()

            val parentRect = Rect(
                paddingLeft + mark.margin,
                paddingTop + mark.margin,
                width - paddingRight - mark.margin,
                height - paddingBottom - mark.margin
            )

            Gravity.apply(mark.gravity, bgWidth, bgHeight, parentRect, outRect)

            val rectF = RectF(outRect)
            canvas.drawRoundRect(rectF, mark.cornerRadius, mark.cornerRadius, bgPaint)

            val textX = rectF.left + mark.paddingH
            val textY = rectF.top + mark.paddingV - textPaint.fontMetrics.ascent
            canvas.drawText(mark.text, textX, textY, textPaint)
        }
    }
}
