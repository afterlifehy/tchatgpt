package com.fitness.tchatgpt.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.fitness.base.BaseApplication
import com.fitness.base.adapter.BaseBindingAdapter
import com.fitness.base.adapter.VBViewHolder
import com.fitness.tchatgpt.databinding.ItemExercisePlanBinding

/**
 * Created by huy  on 2023/3/29.
 */
class PlanAdapter(data: MutableList<String>? = null, val columnCount: Int, var planStr: String) :
    BaseBindingAdapter<String, ItemExercisePlanBinding>(data) {

    override fun convert(holder: VBViewHolder<ItemExercisePlanBinding>, item: String) {
        val lp = (holder.vb.rtvPlan.layoutParams) as MarginLayoutParams
        lp.width =
            (ScreenUtils.getAppScreenWidth() - SizeUtils.dp2px(72f) - SizeUtils.dp2px(12f) * (columnCount - 1)) / columnCount
        if (data.indexOf(item) == 0) {
            lp.marginStart = 0
        } else {
            lp.marginStart = 6
        }
        holder.vb.rtvPlan.layoutParams = lp
        holder.vb.rtvPlan.text = item
        holder.vb.rtvPlan.typeface =
            Typeface.createFromAsset(BaseApplication.instance().assets, "fonts/Alibaba-PuHuiTi-Heavy.otf")
        if (planStr == item) {
            holder.vb.rtvPlan.delegate.setBackgroundColor(
                ContextCompat.getColor(
                    BaseApplication.instance(),
                    com.fitness.base.R.color.color_ff6a93ce
                )
            )
            holder.vb.rtvPlan.delegate.setTextColor(
                ContextCompat.getColor(
                    BaseApplication.instance(),
                    com.fitness.base.R.color.white
                )
            )
        } else {
            holder.vb.rtvPlan.delegate.setBackgroundColor(
                ContextCompat.getColor(
                    BaseApplication.instance(),
                    com.fitness.base.R.color.white
                )
            )
            holder.vb.rtvPlan.delegate.setTextColor(
                ContextCompat.getColor(
                    BaseApplication.instance(),
                    com.fitness.base.R.color.color_ff51798e
                )
            )
        }
        holder.vb.rtvPlan.delegate.init()

        holder.vb.rtvPlan.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                planStr = item
                notifyDataSetChanged()
            }
        })
    }

    override fun createViewBinding(inflater: LayoutInflater, parent: ViewGroup): ItemExercisePlanBinding {
        return ItemExercisePlanBinding.inflate(inflater)
    }

}