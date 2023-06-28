package com.fitness.base.viewbase

import android.view.View
import androidx.viewbinding.ViewBinding
import com.fitness.base.R
import com.fitness.base.mvvm.base.BaseViewModel

/**
 * Created by zj on 2021/3/12.
 */
abstract class VbBaseFragment<VM : BaseViewModel, vb : ViewBinding> : BaseDataFragmentKt<VM>() {
    lateinit var binding: vb

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