package com.fitness.common.widget

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Created by huy  on 2023/3/28.
 */
class GradientTextview(context: Context?, attrs: AttributeSet?) : AppCompatTextView(
    context!!, attrs
) {
    private var mLinearGradient: LinearGradient? = null
    private var mPaint: Paint? = null
    private var mViewWidth = 0
    private val mTextBound = Rect()
    var colors = intArrayOf(-0x000000, -0x000000)

    override fun onDraw(canvas: Canvas) {
        mViewWidth = measuredWidth
        mPaint = paint
        val mTipText = text.toString()
        (mPaint as TextPaint?)?.getTextBounds(mTipText, 0, mTipText.length, mTextBound)
        mLinearGradient = LinearGradient(
            0f, 0f, mViewWidth.toFloat(), 0f, colors,
            null, Shader.TileMode.CLAMP
        )
        (mPaint as TextPaint?)?.shader = mLinearGradient
        canvas.drawText(
            mTipText,
            (measuredWidth / 2 - mTextBound.width() / 2).toFloat(),
            (measuredHeight / 2 + mTextBound.height() / 2).toFloat(),
            (mPaint as TextPaint?)!!
        )
    }

    fun setGradientColor(colors: IntArray) {
        this.colors = colors
        invalidate()
    }
}