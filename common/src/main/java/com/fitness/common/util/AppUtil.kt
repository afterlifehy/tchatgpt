package com.fitness.common.util

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.TimeUtils
import com.fitness.base.help.ActivityCacheManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by huy  on 2022/8/5.
 */
object AppUtil {

    /**
     * 跳转到手机浏览器
     */
    fun goBrowser(url: String?) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        ActivityCacheManager.instance().getCurrentActivity()?.startActivity(intent)
    }

    /**
     * 截图
     *
     * @param v
     * @return
     */
    fun getViewBp(v: View?): Bitmap? {
        if (null == v) {
            return null
        }
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache()
        v.measure(
            View.MeasureSpec.makeMeasureSpec(v.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(v.height, View.MeasureSpec.EXACTLY)
        )
        v.layout(v.x.toInt(), v.y.toInt(), v.x.toInt() + v.measuredWidth, v.y.toInt() + v.measuredHeight)
        val b = Bitmap.createBitmap(v.drawingCache, 0, 0, v.measuredWidth, v.measuredHeight)
        v.isDrawingCacheEnabled = false
        v.destroyDrawingCache()
        return b
    }

    fun getGroupSeparator(): Char {
        return DecimalFormatSymbols.getInstance().groupingSeparator
    }

    fun getDecimalSeparator(): Char {
        return DecimalFormatSymbols.getInstance().decimalSeparator
    }

    fun replaceComma(value: String): String {
        return value.replace(getGroupSeparator().toString(), "").replace(getDecimalSeparator().toString(), ".")
    }

    fun groupSeparatorFormat(value: String): String {
        var format = ",###.##"
        val df = DecimalFormat(format)
        return df.format(value.toDouble())
    }

    /**
     * 保留N位小数
     */
    fun keepNDecimal(value: Double, N: Int): String {
        var format = "##0."
        for (i in 1..N) {
            format += "0"
        }
        val df = DecimalFormat(format)
        return df.format(value)
    }

    /**
     * 保留N位小数
     */
    fun keepNDecimal(value: String, N: Int): String {
        if (N == 0) {
            val format = "##"
            val df = DecimalFormat(format)
            return df.format(value.toDouble())
        } else {
            if (value.length - 1 - value.indexOf(".") > N) {
                var format = "##0."
                if (!TextUtils.isEmpty(value)) {
                    for (i in 1..N) {
                        format += "0"
                    }
                } else {
                    return ""
                }
                val df = DecimalFormat(format)
                return df.format(value.toDouble())
            } else {
                return value
            }
        }
    }

    /**
     * 保留N位小数，最多maxLength位数
     */
    val maxLength = 7
    fun subStringNDecimal(value: String, N: Int): String {
        if (value.contains(".")) {
            val pointIndex = value.indexOf(".")
            if (N == 0) {
                return value.substring(0, pointIndex)
            } else if (N < 0) {
                return value
            } else {
                if (pointIndex + 1 + N > value.length) {
                    return value
                } else {
                    val tempValue = value.substring(0, pointIndex + 1 + N)
                    if (tempValue.length < maxLength) {
                        if (value.length <= maxLength) {
                            return value
                        } else {
                            return value.substring(0, maxLength)
                        }
                    }
                    return value.substring(0, pointIndex + 1 + N)
                }
            }
        } else {
            return value
        }
    }

    /**
     * 将固定格式转化成时间戳（默认 yyyy-MM-dd HH:mm:ss）
     */
    @SuppressLint("SimpleDateFormat")
    fun getStringToLong(format: String?, dateString: String): Long {
        var format = format
        if (TextUtils.isEmpty(format)) {
            format = "yyyy-MM-dd HH:mm:ss"
        }
        val sdf = SimpleDateFormat(format)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return try {
            val date = sdf.parse(dateString)
            date.time
        } catch (e: ParseException) {
            e.printStackTrace()
            0
        }
    }

//    //long转String
//    fun getLongToString(date: Long, type: String?): String? {
//        return SimpleDateFormat(type, Locale.ENGLISH)
//            .format(Date(date))
//    }


    //long转String
    fun getLongToString(date: Long, type: String?): String? {
        return SimpleDateFormat(type, Locale.getDefault())
            .format(Date(date))
    }

    //防止快速点击
    private var lastClickTime1: Long = 0

    fun isFastClick(interval: Long): Boolean {
        val currentClickTime = System.currentTimeMillis()
        Log.v("lastClickTime3", (currentClickTime - lastClickTime1).toString())
        return if (currentClickTime - lastClickTime1 >= interval) {
            lastClickTime1 = currentClickTime
            false
        } else {
            lastClickTime1 = currentClickTime
            true
        }
    }

    /**
     * 补0
     */
    fun fillZero(value: String): String {
        if (value.length == 1) {
            return "0" + value
        } else {
            return value
        }
    }

    /**
     * date转calendar，获取weekOfYear
     */
    fun getWeekOfYear(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.WEEK_OF_YEAR).toString()
    }

    /**
     * 获取一周的开始时间和结束时间
     */
    fun getWeekStartEnd(localDate: String): String {
        val date = TimeUtils.string2Date(localDate, "yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        val startDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        calendar.add(Calendar.DATE, 6)
        val endDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        return startDate + "~" + endDate
    }

    /**
     * 获取一周的开始时间
     */
    fun getWeekStartDay(localDate: String): String {
        val date = TimeUtils.string2Date(localDate, "yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        val startDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        calendar.add(Calendar.DATE, 6)
        val endDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        return startDate
    }

    /**
     * 获取一周的结束时间
     */
    fun getWeekEndDay(localDate: String): String {
        val date = TimeUtils.string2Date(localDate, "yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        val startDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        calendar.add(Calendar.DATE, 6)
        val endDate = TimeUtils.date2String(calendar.time, "yyyy-MM-dd")
        return endDate
    }

//    //textview不同字体大小，颜色
//    fun getSpan(strings: Array<String>, sizes: IntArray, colors: IntArray): Spannable? {
//        val builder: Spans.Builder = Spans.builder()
//        for (i in strings.indices) {
//            builder.text(strings[i], sizes[i], BaseApplication.instance().resources.getColor(colors[i]))
//        }
//        return builder.build()
//    }
//
//    //textview不同字体大小，颜色
//    fun getSpan(strings: Array<String>, sizes: IntArray, colors: IntArray, textStyles: Array<TextStyle>): Spannable? {
//        val builder: Spans.Builder = Spans.builder()
//        for (i in strings.indices) {
//            if (!TextUtils.isEmpty(strings[i])) {
//                builder.text(strings[i], sizes[i], BaseApplication.instance().resources.getColor(colors[i]))
//                    .style(
//                        textStyles[i]
//                    )
//            }
//        }
//        return builder.build()
//    }

    /**
     * 根据百分比改变颜色透明度
     */
    fun changeAlpha(color: Int, fraction: Float): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val alpha = (Color.alpha(color) * fraction).toInt()
        return Color.argb(alpha, red, green, blue)
    }
}