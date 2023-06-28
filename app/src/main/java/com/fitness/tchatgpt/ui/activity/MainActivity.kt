package com.fitness.tchatgpt.ui.activity

import android.content.Intent
import android.view.View
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.fitness.base.arouter.ARouterMap
import com.fitness.base.viewbase.VbBaseActivity
import com.fitness.tchatgpt.R
import com.fitness.tchatgpt.databinding.ActivityMainBinding
import com.fitness.tchatgpt.mvvm.viewmodel.MainViewModel

/**
 * Created by huy  on 2023/3/21.
 */
class MainActivity : VbBaseActivity<MainViewModel, ActivityMainBinding>(), View.OnClickListener {
    override fun initView() {

    }

    override fun initListener() {
        binding.rtvStart.setOnClickListener(this)
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rtv_start -> {
                var prompt = "我是${if (binding.rbMale.isChecked) "男" else "女"}生,"
                if (binding.etHight.text.toString().isNotEmpty()) {
                    prompt += "身高${binding.etHight.text}cm,"
                }
                if (binding.etWeight.text.toString().isNotEmpty()) {
                    prompt += "体重${binding.etWeight.text}kg,"
                }
                if (binding.etAge.text.toString().isNotEmpty()) {
                    prompt += "年龄${binding.etAge.text}岁,"
                }
                if (binding.etBodyFatRadio.text.toString().isNotEmpty()) {
                    prompt += "体脂率${binding.etBodyFatRadio.text}%,"
                }
                var time = "20~30分钟"
                if (binding.rgExerciseTime.checkedRadioButtonId == binding.rbTime2.id) {
                    time = "30~40分钟"
                } else if (binding.rgExerciseTime.checkedRadioButtonId == binding.rbTime3.id) {
                    time = "40分钟以上"
                }
                prompt += "我希望每天运动${time},"
                var day = "3天"
                if (binding.rgExerciseday.checkedRadioButtonId == binding.rbDay2.id) {
                    day = "4天"
                } else if (binding.rgExerciseday.checkedRadioButtonId == binding.rbDay3.id) {
                    day = "5天"
                } else if (binding.rgExerciseday.checkedRadioButtonId == binding.rbDay4.id) {
                    day = "6天"
                }
                prompt += "我希望每周运动${day},"
                if (binding.rbYes.isChecked) {
                    if (binding.rbDumbbell.isChecked) {
                        prompt += "我希望使用哑铃,"
                    } else {
                        prompt += "我希望使用弹力带,"
                    }
                } else {
                    prompt += "我不希望使用器械健身,"
                }
                prompt += "我健身的目的是${if (binding.cbReduceFat.isChecked) "减脂" else ""} ${if (binding.cbBuildMuscles.isChecked) "增肌" else ""} ${if (binding.cbImpovePhysicalFitness.isChecked) "提升身体素质" else ""},"
                prompt += "请根据以上信息，给我制定一份详细的健身计划，字体样式请包含html标签，同时通过html标签设置字体大小，最小为50px"

                ARouter.getInstance().build(ARouterMap.RESULT).withString(ARouterMap.RESULT_PROMPT, prompt)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).navigation()
            }
        }
    }

    override fun startObserve() {
        super.startObserve()
    }

    override fun providerVMClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun marginStatusBarView(): View? {
        return binding.llHello
    }

    override fun onReloadData() {
    }

    override val isFullScreen: Boolean
        get() = true

    override fun getVbBindingView(): ViewBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

}