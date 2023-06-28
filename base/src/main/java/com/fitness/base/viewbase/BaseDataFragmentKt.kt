package com.fitness.base.viewbase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.fitness.base.mvvm.base.BaseViewModel
import java.util.*

/**
 * Created by zj on 2019/12/23.
 */
abstract class BaseDataFragmentKt<VM : BaseViewModel> : BaseFragment<VM>(), View.OnTouchListener{
    private var mViewAddManager: BaseViewAddManagers? = null

    init {
        mViewAddManager = BaseViewAddManagersImpl()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRoot != null && mRoot?.parent != null) {
            val parent = mRoot?.parent as ViewGroup
            parent.removeView(mRoot)
        } else {
            //加一层布局
            val mBinView = getBindingView()
            if (mBinView == null) {
                mRoot = mViewAddManager?.getRootViewId(requireContext(), getLayoutResId())!!
            } else {
                mRoot = mViewAddManager?.getRootView(requireContext(), mBinView)!!

            }
            savedInstanceState?.let { }
            mInflater = inflater
            // Get savedInstanceState
            savedInstanceState?.let { onRestartInstance(it) }
        }
        return mRoot
    }

    open fun getBindingView(): View? {
        return null
    }

    protected abstract fun onReloadData()

    /**
     * 是否需要添加暂无数据
     *
     * @return
     */
    open fun isLoadNotData(): Boolean{
        return false
    }

    /**
     * 请求数据
     */
    open fun getData() {

    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * 点击了布局
     */
    fun onMyTouch() { //点击其他地方的时候，EditText失去焦点
        mRoot?.isFocusable = true
        mRoot?.isFocusableInTouchMode = true
        mRoot?.requestFocus()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        onMyTouch()
        if (null != activity?.currentFocus) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            val mInputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(
                activity?.currentFocus!!.windowToken,
                0
            )
        }
        return false
    }

    protected open fun getFragment(tag: String?): Fragment? {
        val fragmentManager = Objects.requireNonNull(requireActivity()).supportFragmentManager
            ?: return null
        val fragment = fragmentManager.findFragmentByTag(tag)
        return if (fragment is Fragment) fragment else null
    }

    /**
     * 是否需要显示title
     */
    open fun isShowTitle(): Boolean{
        return false
    }
}