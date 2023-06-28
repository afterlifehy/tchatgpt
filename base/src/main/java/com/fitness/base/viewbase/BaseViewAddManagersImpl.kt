package com.fitness.base.viewbase

import android.app.Activity
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.fitness.base.R

class BaseViewAddManagersImpl : BaseViewAddManagers {
    //加载动画
    private var mLadAnimation: AnimationDrawable? = null
    private var loading_iv //动画加载img
            : ImageView? = null
    private var mRootView: View? = null
    protected var mViewAddManager: BaseViewAddFactory? = null
    private var mContext: Context? = null

    //title
    private var tv_base_title: TextView? = null

    init {
        mViewAddManager = BaseViewAddFactory.getInstant()
    }

    override fun getRootView(context: Context, mView: View): View {
        mContext = context
        mRootView = mViewAddManager?.getRootView(context)!!
        val fl_content = mRootView!!.findViewById<FrameLayout>(R.id.fl_content)
        fl_content.addView(mView)
        return mRootView!!
    }

    override fun getRootViewId(context: Context, contextId: Int): View {
        mContext = context
        mRootView = mViewAddManager?.getRootView(context)!!
        addContentView(mRootView, contextId)
        return mRootView!!
    }

    /**
     * 把子布局添加进来
     */
    private fun addContentView(view: View?, contentId: Int) {
        val fl_content = view!!.findViewById<FrameLayout>(R.id.fl_content)
        val mContetxView = View.inflate(view.context, contentId, null)
        fl_content.addView(mContetxView)
    }

    private fun initTitle() {
        mRootView?.apply {
            findViewById<Toolbar>(R.id.toolbar_navigation)?.setNavigationOnClickListener {
                if (context is Activity) {
                    (context as Activity).onBackPressed()
                } else if (context is Fragment) {
                    (context as Fragment).activity?.onBackPressed()
                }
            }
        }
    }

    /**
     * 停止动画
     */
    private fun stiopAnimation() {
        mLadAnimation?.apply {
            if (isRunning) {
                stop()
            }
        }
    }

}