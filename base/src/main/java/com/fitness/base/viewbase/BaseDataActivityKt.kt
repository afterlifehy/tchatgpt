package com.fitness.base.viewbase

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.blankj.utilcode.util.BarUtils
import com.fitness.base.R
import com.fitness.base.mvvm.base.BaseViewModel


abstract class BaseDataActivityKt<VM : BaseViewModel> : BaseActivity<VM>(), View.OnTouchListener{
    protected var mRoot: View? = null
    protected var mViewAddManager: BaseViewAddManagers? = null

    init {
        mViewAddManager = BaseViewAddManagersImpl()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val mBinView = getBindingView()
        if (mBinView == null) {
            mRoot = mViewAddManager?.getRootViewId(this, getLayoutResId())!!
        } else {
            mRoot = mViewAddManager?.getRootView(this, mBinView)!!

        }
        if (isFullScreen) {
            BarUtils.transparentStatusBar(this)
            if (marginStatusBarView() != null) {
                BarUtils.addMarginTopEqualStatusBarHeight(marginStatusBarView()!!)
                BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.transparent))
                marginStatusBarView()?.setBackgroundColor(
                    ContextCompat.getColor(
                        this@BaseDataActivityKt,
                        R.color.transparent
                    )
                )
            }
        }
        BarUtils.setNavBarColor(this, ContextCompat.getColor(this, navbarColor()))

        savedInstanceState?.let { }
        setIsLoadContentView(false)
        setContentView(mRoot)
        super.onCreate(savedInstanceState)
        BarUtils.setStatusBarLightMode(this, true)
    }

    open fun getBindingView(): View? {
        return null
    }

    open fun navbarColor(): Int {
        return R.color.white
    }

    open fun marginStatusBarView(): View? {
        return null
    }

    protected abstract fun onReloadData()

    /**
     * 是否需要添加暂无数据
     *
     * @return
     */
    open fun isLoadNotData(): Boolean {
        return false
    }

    /**
     * 是否需要显示title
     */
    open fun isShowTitle(): Boolean {
        return false
    }

    abstract val isFullScreen: Boolean

    /**
     * 请求数据
     */
    open fun getData() {

    }


    override fun onPause() {
        super.onPause()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return checkTouch()

    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return checkTouch()
    }

    fun checkTouch(): Boolean {
        onMyTouch()
        if (null != currentFocus) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            val mInputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return false
    }

    /**
     * 点击了布局
     */
    fun onMyTouch() { //点击其他地方的时候，EditText失去焦点
        mRoot?.isFocusable = true
        mRoot?.isFocusableInTouchMode = true
        mRoot?.requestFocus()
    }

    protected fun getFragment(tag: String?): Fragment? {
        val fragmentManager = supportFragmentManager ?: return null
        val fragment = fragmentManager.findFragmentByTag(tag)
        return if (fragment is Fragment) fragment else null
    }

    fun showFragment(
        fragmentManager: FragmentManager,
        fragmentTransaction: FragmentTransaction?,
        willShowFragment: Fragment?,
        id: Int,
        tag: String
    ) {
        var fragmentTransaction = fragmentTransaction
        fragmentTransaction = fragmentManager.beginTransaction()
        if (willShowFragment == null) {
            return
        }
        hideFragments(fragmentManager, fragmentTransaction)
        if (!willShowFragment.isAdded && null == fragmentManager.findFragmentByTag(tag)) {
            fragmentTransaction.add(id, willShowFragment, tag)
        } else {
            fragmentTransaction.show(willShowFragment)
        }
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun hideFragments(
        fragmentManager: FragmentManager,
        fragmentTransaction: FragmentTransaction
    ) {
        val fragments = fragmentManager.fragments
        for (i in fragments.indices) {
            if (fragments[i] != null && fragments[i].isAdded) {
                fragmentTransaction.hide(fragments[i])
            }
        }
    }
}