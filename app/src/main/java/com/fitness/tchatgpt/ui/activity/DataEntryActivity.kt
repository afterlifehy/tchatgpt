package com.fitness.tchatgpt.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ShadowUtils
import com.blankj.utilcode.util.SizeUtils
import com.fitness.base.arouter.ARouterMap
import com.fitness.base.ext.gone
import com.fitness.base.ext.i18N
import com.fitness.base.ext.show
import com.fitness.base.viewbase.VbBaseActivity
import com.fitness.common.util.AppUtil
import com.fitness.common.util.GlideUtils
import com.fitness.tchatgpt.R
import com.fitness.tchatgpt.adapter.PlanAdapter
import com.fitness.tchatgpt.databinding.ActivityDataEntryBinding
import com.fitness.tchatgpt.mvvm.viewmodel.DataEntryViewModel
import com.tbruyelle.rxpermissions3.RxPermissions


/**
 * Created by huy  on 2023/3/28.
 */
class DataEntryActivity : VbBaseActivity<DataEntryViewModel, ActivityDataEntryBinding>(), OnClickListener {
    var dataEntryViewList: MutableList<View> = ArrayList()
    var currentViewPostion = 2
    var age = 20
    var height = 170f
    var weight = 70f
    var exercisePlanAdapter: PlanAdapter? = null
    var frequencyPlanAdapter: PlanAdapter? = null
    var equipmentPlanAdapter: PlanAdapter? = null
    var exceptPlanAdapter: PlanAdapter? = null
    var exercisePlanList: MutableList<String> = ArrayList()
    var frequencyPlanList: MutableList<String> = ArrayList()
    var equipmentPlanList: MutableList<String> = ArrayList()
    var exceptPlanList: MutableList<String> = ArrayList()

    override fun initView() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, com.fitness.base.R.color.transparent))
        binding.ivBack.gone()

        dataEntryViewList.add(binding.layoutChooseGender.llChooseGender)
        dataEntryViewList.add(binding.layoutChooseAge.llChooseAge)
        dataEntryViewList.add(binding.layoutChooseBMI.llChooseBMI)
        dataEntryViewList.add(binding.layoutChoosePlan.llChoosePlan)

        binding.gtvTitle.typeface = heavyTypeFace
        binding.layoutChooseGender.tvGender.typeface = heavyTypeFace
        binding.layoutChooseGender.tvKeepSecret.typeface = mediumTypeFace
        binding.layoutChooseAge.gtvAgeTitle.typeface = heavyTypeFace
        binding.layoutChooseAge.rtvAge.typeface = heavyTypeFace
        binding.layoutChooseAge.tvOk.typeface = heavyTypeFace
        binding.layoutChooseAge.rtvAgeReset.typeface = heavyTypeFace
        binding.layoutChooseAge.tvKeepAgeSecret.typeface = mediumTypeFace
        binding.layoutChooseBMI.gtvBmiTitle.typeface = heavyTypeFace
        binding.layoutChooseBMI.tvHeightTitle.typeface = heavyTypeFace
        binding.layoutChooseBMI.tvCmTitle.typeface = mediumTypeFace
        binding.layoutChooseBMI.tvWeightTitle.typeface = heavyTypeFace
        binding.layoutChooseBMI.tvKgTitle.typeface = mediumTypeFace
        binding.layoutChooseBMI.tvBmiOk.typeface = heavyTypeFace
        binding.layoutChooseBMI.rtvBmiReset.typeface = heavyTypeFace
        binding.layoutChoosePlan.tvExerciseTime.typeface = heavyTypeFace
        binding.layoutChoosePlan.tvFrequency.typeface = heavyTypeFace
        binding.layoutChoosePlan.tvEquipment.typeface = heavyTypeFace
        binding.layoutChoosePlan.tvExcept.typeface = heavyTypeFace
        binding.layoutChoosePlan.tvCreatePlan.typeface = heavyTypeFace
        binding.layoutChoosePlan.rtvPlanReset.typeface = heavyTypeFace

        ShadowUtils.apply(binding.layoutChooseAge.rtvAge, ShadowUtils.Config().setShadowSize(SizeUtils.dp2px(12f)))
        ShadowUtils.apply(binding.layoutChooseAge.rtvMinus, ShadowUtils.Config().setShadowSize(SizeUtils.dp2px(5f)))
        ShadowUtils.apply(binding.layoutChooseAge.rtvAdd, ShadowUtils.Config().setShadowSize(SizeUtils.dp2px(5f)))

        binding.gtvTitle.setGradientColor(
            intArrayOf(
                Color.parseColor("#3cb6ba"),
                Color.parseColor("#749fdf"),
                Color.parseColor("#46b0ab"),
                Color.parseColor("#6cb3d9")
            )
        )
        binding.layoutChooseAge.gtvAgeTitle.setGradientColor(
            intArrayOf(
                Color.parseColor("#3cb6ba"),
                Color.parseColor("#749fdf"),
                Color.parseColor("#46b0ab"),
                Color.parseColor("#6cb3d9")
            )
        )
        binding.layoutChooseBMI.gtvBmiTitle.text = i18N(com.fitness.base.R.string.your_bmi_is) + 0
        binding.layoutChooseBMI.gtvBmiTitle.setGradientColor(
            intArrayOf(
                Color.parseColor("#3cb6ba"),
                Color.parseColor("#749fdf"),
                Color.parseColor("#46b0ab"),
                Color.parseColor("#6cb3d9")
            )
        )

        exercisePlanList.add("15min")
        exercisePlanList.add("30min")
        exercisePlanList.add("60min")

        frequencyPlanList.add("3day")
        frequencyPlanList.add("4day")
        frequencyPlanList.add("5day")
        frequencyPlanList.add("6day")

        equipmentPlanList.add("dumbbell")
        equipmentPlanList.add("elastic band")
        equipmentPlanList.add("no")

        exceptPlanList.add("Fat loss")
        exceptPlanList.add("muscle gain")
        exceptPlanList.add("increase resistance")

        binding.layoutChoosePlan.rvExerciseTime.setHasFixedSize(true)
        binding.layoutChoosePlan.rvExerciseTime.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        exercisePlanAdapter = PlanAdapter(exercisePlanList, 3, "15min")
        binding.layoutChoosePlan.rvExerciseTime.adapter = exercisePlanAdapter

        binding.layoutChoosePlan.rvFrequency.setHasFixedSize(true)
        binding.layoutChoosePlan.rvFrequency.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frequencyPlanAdapter = PlanAdapter(frequencyPlanList, 4, "3day")
        binding.layoutChoosePlan.rvFrequency.adapter = frequencyPlanAdapter

        binding.layoutChoosePlan.rvEquipment.setHasFixedSize(true)
        binding.layoutChoosePlan.rvEquipment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        equipmentPlanAdapter = PlanAdapter(equipmentPlanList, 3, "dumbbell")
        binding.layoutChoosePlan.rvEquipment.adapter = equipmentPlanAdapter

        binding.layoutChoosePlan.rvExcept.setHasFixedSize(true)
        binding.layoutChoosePlan.rvExcept.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        exceptPlanAdapter = PlanAdapter(exceptPlanList, 3, "Fat loss")
        binding.layoutChoosePlan.rvExcept.adapter = exceptPlanAdapter
        calculateBMI()
    }

    override fun initListener() {
        binding.layoutChooseGender.rgGender.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_male -> {
                    GlideUtils.instance?.loadImage(
                        binding.layoutChooseGender.ivGender,
                        com.fitness.common.R.mipmap.ic_male
                    )
                }
                R.id.rb_female -> {
                    GlideUtils.instance?.loadImage(
                        binding.layoutChooseGender.ivGender,
                        com.fitness.common.R.mipmap.ic_female
                    )
                }
            }
            nextStep()
            binding.ivBack.show()
            binding.gtvTitle.gone()
        }
        binding.layoutChooseBMI.rvHeight.setOnValueChangedListener {
            height = it
            calculateBMI()
        }
        binding.layoutChooseBMI.rvWeight.setOnValueChangedListener {
            weight = it
            calculateBMI()
        }
        binding.ivBack.setOnClickListener(this)
        binding.layoutChooseGender.tvKeepSecret.setOnClickListener(this)
        binding.layoutChooseAge.rtvAdd.setOnClickListener(this)
        binding.layoutChooseAge.rtvMinus.setOnClickListener(this)
        binding.layoutChooseAge.tvOk.setOnClickListener(this)
        binding.layoutChooseAge.rtvAgeReset.setOnClickListener(this)
        binding.layoutChooseAge.tvKeepAgeSecret.setOnClickListener(this)
        binding.layoutChooseBMI.tvBmiOk.setOnClickListener(this)
        binding.layoutChooseBMI.rtvBmiReset.setOnClickListener(this)
        binding.layoutChoosePlan.tvCreatePlan.setOnClickListener(this)
        binding.layoutChoosePlan.rtvPlanReset.setOnClickListener(this)
    }

    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                dataEntryViewList[currentViewPostion].gone()
                currentViewPostion--
                dataEntryViewList[currentViewPostion].show()
                if (currentViewPostion == 0) {
                    binding.ivBack.gone()
                    binding.gtvTitle.show()
                }
            }
            R.id.tv_keepSecret -> {
                nextStep()
                binding.ivBack.show()
                binding.gtvTitle.gone()
            }
            R.id.rtv_add -> {
                if (age == 80) {
                    return
                }
                age++
                binding.layoutChooseAge.rtvAge.text = age.toString()
            }
            R.id.rtv_minus -> {
                if (age == 10) {
                    return
                }
                age--
                binding.layoutChooseAge.rtvAge.text = age.toString()
            }
            R.id.tv_ok -> {
                nextStep()
            }
            R.id.rtv_ageReset -> {
                age = 20
                binding.layoutChooseAge.rtvAge.text = age.toString()
            }
            R.id.tv_keepAgeSecret -> {
                nextStep()
            }
            R.id.tv_bmiOk -> {
                nextStep()
            }
            R.id.rtv_bmiReset -> {
                binding.layoutChooseBMI.rvHeight.currentValue = 170f
                binding.layoutChooseBMI.rvWeight.currentValue = 70f

            }
            R.id.tv_createPlan -> {
                ARouter.getInstance().build(ARouterMap.PLAN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).navigation()
            }
            R.id.rtv_planReset -> {
                exercisePlanAdapter?.planStr = "15min"
                frequencyPlanAdapter?.planStr = "3day"
                equipmentPlanAdapter?.planStr = "dumbbell"
                exceptPlanAdapter?.planStr = "Fat loss"
                exercisePlanAdapter?.notifyDataSetChanged()
                frequencyPlanAdapter?.notifyDataSetChanged()
                equipmentPlanAdapter?.notifyDataSetChanged()
                exceptPlanAdapter?.notifyDataSetChanged()

                var rxPermissions = RxPermissions(this@DataEntryActivity)
                rxPermissions.request(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                    .subscribe {
                        if (it) {
                            // 获取日历实例
                            val beginTime: Calendar = Calendar.getInstance()
                            beginTime.set(2023, 3, 13, 15, 38) // 设置事件开始时间为 2022 年 1 月 1 日上午 8 点

                            val endTime: Calendar = Calendar.getInstance()
                            endTime.set(2023, 3, 13, 16, 0) // 设置事件结束时间为 2022 年 1 月 1 日上午 9 点

                            val title = "新年快乐！" // 设置事件标题

                            val cr = contentResolver
                            val values = ContentValues()
                            values.put(CalendarContract.Events.DTSTART, beginTime.timeInMillis)
                            values.put(CalendarContract.Events.DTEND, endTime.timeInMillis)
                            values.put(CalendarContract.Events.TITLE, title)
                            values.put(CalendarContract.Events.CALENDAR_ID, 1)
                            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                            val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)

                            // 获取事件 ID
                            val eventID: Long = uri?.lastPathSegment!!.toLong()

                            // 添加提醒
                            val reminderValues = ContentValues()
                            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID)
                            reminderValues.put(
                                CalendarContract.Reminders.METHOD,
                                CalendarContract.Reminders.METHOD_ALERT
                            )
                            reminderValues.put(CalendarContract.Reminders.MINUTES, 10)
                            val reminderUri: Uri? = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                        }
                    }
            }
        }
    }

    override fun initData() {
    }

    fun nextStep() {
        dataEntryViewList[currentViewPostion].gone()
        currentViewPostion++
        dataEntryViewList[currentViewPostion].show()
    }

    fun calculateBMI() {
        var bmi = AppUtil.keepNDecimal((weight / (height / 100 * height / 100)).toString(), 2)
        binding.layoutChooseBMI.gtvBmiTitle.text = i18N(com.fitness.base.R.string.your_bmi_is) + bmi
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityDataEntryBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override fun marginStatusBarView(): View {
        return binding.ablToolbar
    }

    override val isFullScreen: Boolean
        get() = true

    override fun providerVMClass(): Class<DataEntryViewModel>? {
        return DataEntryViewModel::class.java
    }

}