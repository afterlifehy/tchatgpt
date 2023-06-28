package com.fitness.base.viewbase

import android.graphics.Typeface
import android.view.View
import androidx.viewbinding.ViewBinding
import com.fitness.base.R
import com.fitness.base.mvvm.base.BaseViewModel

abstract class VbBaseActivity<VM : BaseViewModel, vb : ViewBinding> : BaseDataActivityKt<VM>() {
    lateinit var binding: vb
    val heavyTypeFace by lazy {
        Typeface.createFromAsset(assets, "fonts/Alibaba-PuHuiTi-Heavy.otf")
    }
    val mediumTypeFace by lazy {
        Typeface.createFromAsset(assets, "fonts/Alibaba-PuHuiTi-Medium.otf")
    }

    override fun getBindingView(): View? {
        val mBindind = getVbBindingView()
        binding = mBindind as vb
        return mBindind.root
    }


    abstract fun getVbBindingView(): ViewBinding

    override fun getLayoutResId(): Int {
        return R.layout.activity_vb_default_layout
    }
}