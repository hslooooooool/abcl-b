package qsos.base.core.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import qsos.base.core.R
import kotlin.math.sqrt

/**
 * @author : 华清松
 * 角标布局
 */
class CornerLabelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    /**View宽度的一半 */
    private var mHalfWidth: Int = 0
    /**角标画笔 */
    private var mPaint: Paint? = null
    /**文字画笔 */
    private var mTextPaint: TextPaint? = null
    /**角标路径 */
    private var mPath: Path? = null
    /**角标位置，0：右上角、1：右下角、2：左下角、3：左上角 */
    private var position: Int = 0
    /**角标的显示边长 */
    private var sideLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
    /**字体大小 */
    private var textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics).toInt()
    /**字体颜色 */
    private var textColor = Color.WHITE
    private var text: String? = null
    /**角标背景 */
    private var bgColor = Color.RED
    /**文字到斜边的距离 */
    private var marginLeanSide = -1

    init {
        initAttrs(context, attrs)
        init()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CornerLabelView, 0, 0)
        for (i in 0 until ta.indexCount) {
            when (val attr = ta.getIndex(i)) {
                R.styleable.CornerLabelView_position -> position = ta.getInt(attr, 0)
                R.styleable.CornerLabelView_side_length -> sideLength = ta.getDimensionPixelSize(attr, sideLength)
                R.styleable.CornerLabelView_text_size -> textSize = ta.getDimensionPixelSize(attr, textSize)
                R.styleable.CornerLabelView_text_color -> textColor = ta.getColor(attr, textColor)
                R.styleable.CornerLabelView_text -> text = ta.getString(attr)
                R.styleable.CornerLabelView_bg_color -> bgColor = ta.getColor(attr, bgColor)
                R.styleable.CornerLabelView_margin_lean_side -> marginLeanSide = ta.getDimensionPixelSize(attr, marginLeanSide)
            }
        }
        ta.recycle()
    }

    private fun init() {
        mPath = Path()

        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = bgColor

        mTextPaint = TextPaint()
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.color = textColor
        mTextPaint!!.textSize = textSize.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(sideLength * 2, sideLength * 2)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        } else if (widthSpecSize != heightSpecSize) {
            val size = widthSpecSize.coerceAtMost(heightSpecSize)
            setMeasuredDimension(size, size)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHalfWidth = w.coerceAtMost(h) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //将原点移动到画布中心
        canvas.translate(mHalfWidth.toFloat(), mHalfWidth.toFloat())
        //根据角标位置旋转画布
        canvas.rotate((position * 90).toFloat())

        if (sideLength > mHalfWidth * 2) {
            sideLength = mHalfWidth * 2
        }

        //绘制角标背景
        mPath!!.moveTo((-mHalfWidth).toFloat(), (-mHalfWidth).toFloat())
        mPath!!.lineTo((sideLength - mHalfWidth).toFloat(), (-mHalfWidth).toFloat())
        mPath!!.lineTo(mHalfWidth.toFloat(), (mHalfWidth - sideLength).toFloat())
        mPath!!.lineTo(mHalfWidth.toFloat(), mHalfWidth.toFloat())
        mPath!!.close()
        canvas.drawPath(mPath!!, mPaint!!)

        //绘制文字前画布旋转45度
        canvas.rotate(45f)
        //角标实际高度
        val h1 = (sqrt(2.0) / 2.0 * sideLength).toInt()
        val h2 = (-(mTextPaint!!.ascent() + mTextPaint!!.descent())).toInt()
        //文字绘制坐标
        val x = (-mTextPaint!!.measureText(text)).toInt() / 2
        val y: Int
        if (marginLeanSide >= 0) {
            //使用clv:margin_lean_side属性时
            if (position == 1 || position == 2) {
                y = if (h1 - (marginLeanSide - mTextPaint!!.ascent()) < (h1 - h2) / 2) {
                    -(h1 - h2) / 2
                } else {
                    (-(h1 - (marginLeanSide - mTextPaint!!.ascent()))).toInt()
                }
            } else {
                if (marginLeanSide < mTextPaint!!.descent()) {
                    marginLeanSide = mTextPaint!!.descent().toInt()
                }

                if (marginLeanSide > (h1 - h2) / 2) {
                    marginLeanSide = (h1 - h2) / 2
                }
                y = -marginLeanSide
            }
        } else { //默认情况下
            if (sideLength > mHalfWidth) {
                sideLength = mHalfWidth
            }
            y = (-sqrt(2.0) / 2.0 * sideLength + h2).toInt() / 2
        }

        //如果角标在右下、左下则进行画布平移、翻转，已解决绘制的文字显示问题
        if (position == 1 || position == 2) {
            canvas.translate(0f, (-sqrt(2.0) / 2.0 * sideLength).toFloat())
            canvas.scale(-1f, -1f)
        }
        //绘制文字
        canvas.drawText(text!!, x.toFloat(), y.toFloat(), mTextPaint!!)
    }

    /**设置角标背景色*/
    fun setBgColorId(bgColorId: Int): CornerLabelView {
        this.bgColor = resources.getColor(bgColorId)
        mPaint!!.color = bgColor
        invalidate()
        return this
    }

    /**设置角标背景色*/
    fun setBgColor(bgColor: Int): CornerLabelView {
        mPaint!!.color = bgColor
        invalidate()
        return this
    }

    /**设置文字颜色*/
    fun setTextColorId(colorId: Int): CornerLabelView {
        this.textColor = resources.getColor(colorId)
        mTextPaint!!.color = textColor
        invalidate()
        return this
    }

    /**设置文字颜色*/
    fun setTextColor(color: Int): CornerLabelView {
        mTextPaint!!.color = color
        invalidate()
        return this
    }

    /**设置文字*/
    fun setText(textId: Int): CornerLabelView {
        this.text = resources.getString(textId)
        invalidate()
        return this
    }

    /**设置文字*/
    fun setText(text: String): CornerLabelView {
        this.text = text
        invalidate()
        return this
    }
}