package com.fitness.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.fitness.base.help.ActivityCacheManager
import com.fitness.base.http.OnAddOkhttpInterceptor
import com.fitness.base.proxy.OnAppBaseProxyLinsener
import kotlin.properties.Delegates


abstract class BaseApplication : Application(), Application.ActivityLifecycleCallbacks, OnAddOkhttpInterceptor {
    private var mOnAppBaseProxyLinsener: OnAppBaseProxyLinsener? = null

    companion object {
        var baseApplication: BaseApplication by Delegates.notNull()

        @JvmStatic
        fun instance() = baseApplication
    }

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
        registerActivityLifecycleCallbacks(this)
    }

    /**
     * 用来获取子类和父类直接的交互
     */
    fun setOnAppBaseProxyLinsener(mOnAppBaseProxyLinsener: OnAppBaseProxyLinsener?) {
        this.mOnAppBaseProxyLinsener = mOnAppBaseProxyLinsener
    }

    fun getOnAppBaseProxyLinsener(): OnAppBaseProxyLinsener? {
        return mOnAppBaseProxyLinsener
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        ActivityCacheManager.instance().removeActivity(activity)
        activity.finish()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivityCacheManager.instance().addActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

}