package com.fitness.base.viewbase

import android.content.Context
import android.view.View
import com.fitness.base.R

class BaseViewAddFactoryImpl : BaseViewAddFactory {


    override fun getRootView(context: Context): View {//写在这里好统一布局
        val mRootView = View.inflate(context, R.layout.new_base_not_title_layout, null)
        return mRootView
    }
}