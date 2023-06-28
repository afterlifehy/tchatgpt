package com.fitness.base.start

import android.app.Application
import com.fitness.base.BaseApplication

class BaseStartUpManager private constructor() : AppInitManager() {

    companion object {
        var mStartUpManager: BaseStartUpManager? = null

        fun instance(): BaseStartUpManager {
            if (mStartUpManager == null) {
                mStartUpManager = BaseStartUpManager()
            }
            return mStartUpManager!!
        }

    }


    override fun applicationInit(application: Application) {
    }

    override fun delayInit(application: Application) {
        //只有测试才开启
        BaseApplication.instance().getOnAppBaseProxyLinsener()?.let {
            if (it.onIsDebug()) {

            }
        }
//        val appDir = File(Environment.getExternalStorageDirectory(), Constant.tchatgpt_FILE_PATH)
//        FileUtils.createOrExistsDir(appDir)
    }

}