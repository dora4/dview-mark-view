package dora.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
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

    private var markDrawable: Drawable? = null
    private var markView: View? = null
    private val textMarks = mutableListOf<TextMark>()
    private var markGravity: Int = Gravity.TOP or Gravity.END
    private var markMargin: Int = 0
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

        markDrawable = ta.getDrawable(R.styleable.DoraMarkView_dview_mv_drawable)

        val layoutId = ta.getResourceId(R.styleable.DoraMarkView_dview_mv_layout, 0)
        if (layoutId != 0) {
            markView = LayoutInflater.from(context).inflate(layoutId, this, false)
        }

        markGravity = ta.getInt(R.styleable.DoraMarkView_dview_mv_gravity, Gravity.TOP or Gravity.END)
        markMargin = ta.getDimensionPixelSize(R.styleable.DoraMarkView_dview_mv_margin, 0)
        markVisible = ta.getBoolean(R.styleable.DoraMarkView_dview_mv_visible, true)

        ta.recycle()
    }

    fun setMarkDrawable(drawable: Drawable?) {
        markDrawable = drawable
        invalidate()
    }

    fun setMarkView(view: View?) {
        markView = view
        requestLayout()
    }

    fun setMarkLayout(@LayoutRes layoutId: Int) {
        val view = LayoutInflater.from(context).inflate(layoutId, this, false)
        setMarkView(view)
    }

    fun setMarkGravity(gravity: Int) {
        markGravity = gravity
        invalidate()
    }

    fun setMarkMargin(margin: Int) {
        markMargin = margin
        invalidate()
    }

    fun setMarkVisible(visible: Boolean) {
        markVisible = visible
        invalidate()
    }

    fun addTextMark(
        text: String,
        textColor: Int = Color.WHITE,
        bgColor: Int = Color.RED,
        textSizeSp: Float = 12f,
        cornerRadius: Float = 12f,
        paddingH: Int = 12,
        paddingV: Int = 6,
        gravity: Int = Gravity.TOP or Gravity.END,
        margin: Int = 8
    ) {
        textMarks.add(
            TextMark(text, textColor, bgColor, textSizeSp, cornerRadius, paddingH, paddingV, gravity, margin)
        )
        invalidate()
    }

    fun clearTextMarks() {
        textMarks.clear()
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!markVisible) return

        // Drawable
        markDrawable?.let { drawable ->
            val rect = Rect()
            Gravity.apply(
                markGravity,
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Rect(
                    paddingLeft + markMargin,
                    paddingTop + markMargin,
                    width - paddingRight - markMargin,
                    height - paddingBottom - markMargin
                ),
                rect
            )
            drawable.bounds = rect
            drawable.draw(canvas)
        }

        // View
        markView?.let { view ->
            val contentWidth = width - paddingLeft - paddingRight - markMargin * 2
            val contentHeight = height - paddingTop - paddingBottom - markMargin * 2

            measureChild(
                view,
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.AT_MOST)
            )

            val markRect = Rect()
            Gravity.apply(
                markGravity,
                view.measuredWidth,
                view.measuredHeight,
                Rect(
                    paddingLeft + markMargin,
                    paddingTop + markMargin,
                    width - paddingRight - markMargin,
                    height - paddingBottom - markMargin
                ),
                markRect
            )

            canvas.save()
            canvas.translate(markRect.left.toFloat(), markRect.top.toFloat())
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.draw(canvas)
            canvas.restore()
        }

        // Text marks
        textMarks.forEach { mark ->
            textPaint.color = mark.textColor
            textPaint.textSize = mark.textSizeSp * resources.displayMetrics.scaledDensity
            textPaint.typeface = Typeface.DEFAULT_BOLD
            bgPaint.color = mark.bgColor

            val textWidth = textPaint.measureText(mark.text)
            val textHeight = textPaint.fontMetrics.run { descent - ascent }

            val bgWidth = (textWidth + mark.paddingH * 2).roundToInt()
            val bgHeight = (textHeight + mark.paddingV * 2).roundToInt()

            tmpRect.set(0, 0, bgWidth, bgHeight)
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
