package com.fitness.tchatgpt

import android.content.Context
import android.net.http.HttpResponseCache
import com.alibaba.android.arouter.launcher.ARouter
import com.fitness.base.BaseApplication
import com.fitness.base.http.interceptor.*
import io.realm.Realm
import okhttp3.Interceptor
import java.io.File

/**
 * Created by huy  on 2022/12/2.
 */
class AppApplication : BaseApplication() {
    companion object {
        var _context: BaseApplication? = null
        fun instance(): BaseApplication {
            return _context!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        _context = this
        //realm
        Realm.init(this)
        val cacheDir = File(cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)

        if (BuildConfig.is_debug) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this@AppApplication)
        //初始化数据库
        //支付宝沙箱环境
//        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        //初始化数据库
//        val mAppDatabase = Room.databaseBuilder(
//            applicationContext,
//            AppRoomDatabase::class.java, "android_room_xdj.db"
//        )
//            .allowMainThreadQueries()
//            .addMigrations(MIGRATION_2_3)
//            .build()
    }

    override fun onAddOkHttpInterceptor(): List<Interceptor> {
        val list = ArrayList<Interceptor>()
        list.add(HeaderInterceptor())
        list.add(LoginExpiredInterceptor())
        list.add(HostInterceptor())
        list.add(TokenInterceptor())
        if (BuildConfig.is_debug) {
            list.add(LogInterceptor(BuildConfig.is_debug))
            val mHttpLoggingInterceptor = HttpLoggingInterceptor("chatgpt_http")
            mHttpLoggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
            list.add(mHttpLoggingInterceptor)
        }
        return list
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}