package com.fitness.common.dsbridge

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.CookieManager
import android.webkit.WebStorage.QuotaUpdater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ClipboardUtils
import com.lzy.okgo.https.HttpsUtils
import com.fitness.base.util.Constant
import com.fitness.common.util.AppUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.reflect.Method
import java.net.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

class DWebView : WebView {
    private val javaScriptNamespaceInterfaces: MutableMap<String, Any> = HashMap()
    private var APP_CACHE_DIRNAME: String? = null
    private var callID = 0
    private var custWebChromeClient: WebChromeClient? = null

    @Volatile
    private var alertBoxBlock = true
    private var javascriptCloseWindowListener: JavascriptCloseWindowListener? = null
    private var callInfoList: ArrayList<CallInfo>? = null
    private val innerJavascriptInterface: InnerJavascriptInterface = InnerJavascriptInterface()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var webViewUrlChangeListener: WebViewUrlChangeListener? = null
    private var sslParams: HttpsUtils.SSLParams? = null
    var toLoadJs = "WebViewJavascriptBridge.js"
    private var httpClient: OkHttpClient? = null

    //是否允许抓包
    private var isProxy = false

    private inner class InnerJavascriptInterface() {
        private fun PrintDebugInfo(error: String) {
            Log.d(LOG_TAG, error)
            if (isDebug) {
                evaluateJavascript(
                    String.format(
                        "alert('%s')",
                        "DEBUG ERR MSG:\\n" + error.replace("\\'".toRegex(), "\\\\'")
                    )
                )
            }
        }

        @Keep
        @JavascriptInterface
        fun call(methodName: String, argStr: String?): String {
            var methodName = methodName
            var error = "Js bridge  called, but can't find a corresponded " +
                    "JavascriptInterface object , please check your code!"
            val nameStr = parseNamespace(methodName.trim { it <= ' ' })
            methodName = nameStr[1]
            val jsb = javaScriptNamespaceInterfaces[nameStr[0]]
            val ret = JSONObject()
            try {
                ret.put("code", -1)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (jsb == null) {
                PrintDebugInfo(error)
                return ret.toString()
            }
            var arg: Any? = null
            var method: Method? = null
            var callback: String? = null
            try {
                val args = JSONObject(argStr)
                if (args.has("_dscbstub")) {
                    callback = args.getString("_dscbstub")
                }
                if (args.has("data")) {
                    arg = args["data"]
                }
            } catch (e: JSONException) {
                error = String.format(
                    "The argument of \"%s\" must be a JSON object string!",
                    methodName
                )
                PrintDebugInfo(error)
                e.printStackTrace()
                return ret.toString()
            }
            val cls: Class<*> = jsb.javaClass
            var asyn = false
            try {
                method = cls.getMethod(
                    methodName,
                    *arrayOf(Any::class.java, CompletionHandler::class.java)
                )
                asyn = true
            } catch (e: Exception) {
                try {
                    method = cls.getMethod(methodName, *arrayOf<Class<*>>(Any::class.java))
                } catch (ex: Exception) {
                }
            }
            if (method == null) {
                error =
                    "Not find method \"$methodName\" implementation! please check if the  signature or namespace of the method is right "
                PrintDebugInfo(error)
                return ret.toString()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val annotation = method.getAnnotation(
                    JavascriptInterface::class.java
                )
                if (annotation == null) {
                    error = ("Method " + methodName + " is not invoked, since  " +
                            "it is not declared with JavascriptInterface annotation! ")
                    PrintDebugInfo(error)
                    return ret.toString()
                }
            }
            val retData: Any
            method.isAccessible = true
            try {
                if (asyn) {
                    val cb = callback
                    method.invoke(jsb, arg, object : CompletionHandler<Any?> {
                        override fun complete(retValue: Any?) {
                            complete(retValue, true)
                        }

                        override fun complete() {
                            complete(null, true)
                        }

                        override fun setProgressData(value: Any?) {
                            complete(value, false)
                        }

                        private fun complete(retValue: Any?, complete: Boolean) {
                            try {
                                val ret = JSONObject()
                                ret.put("code", 0)
                                ret.put("data", retValue)
                                //retValue = URLEncoder.encode(ret.toString(), "UTF-8").replaceAll("\\+", "%20");
                                if (cb != null) {
                                    //String script = String.format("%s(JSON.parse(decodeURIComponent(\"%s\")).data);", cb, retValue);
                                    var script = String.format("%s(%s.data);", cb, ret.toString())
                                    if (complete) {
                                        script += "delete window.$cb"
                                    }
                                    //Log.d(LOG_TAG, "complete " + script);
                                    evaluateJavascript(script)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                } else {
                    retData = method.invoke(jsb, arg)
                    ret.put("code", 0)
                    ret.put("data", retData)
                    return ret.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error = String.format(
                    "Call failed：The parameter of \"%s\" in Java is invalid.",
                    methodName
                )
                PrintDebugInfo(error)
                return ret.toString()
            }
            return ret.toString()
        }
    }

    var handlerMap: MutableMap<Int, OnReturnValue<*>> = HashMap()

    interface JavascriptCloseWindowListener {
        /**
         * @return If true, close the current activity, otherwise, do nothing.
         */
        fun onClose(): Boolean
    }

    @Deprecated("")
    interface FileChooser {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun openFileChooser(valueCallback: ValueCallback<*>?, acceptType: String?)

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun openFileChooser(
            valueCallback: ValueCallback<Uri?>?,
            acceptType: String?, capture: String?
        )
    }

    constructor(context: Context?, attrs: AttributeSet?) : super((context)!!, attrs) {
        init()
    }

    constructor(context: Context?) : super((context)!!) {
        init()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun init() {
//        isProxy = BaseApplication.instance().getOnAppBaseProxyLinsener()!!.onIsProxy()
        val byteArrayInputStream = ByteArrayInputStream(Constant.h5CrtStr.toByteArray())
        val sslParams = HttpsUtils.getSslSocketFactory(byteArrayInputStream)
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder().connectTimeout(Constant.timeOut, TimeUnit.SECONDS)
            .readTimeout(Constant.timeOut, TimeUnit.SECONDS)
            .writeTimeout(Constant.timeOut, TimeUnit.SECONDS).proxy(Proxy.NO_PROXY)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //证书;
        httpClient = mBuilder.build()
        APP_CACHE_DIRNAME = context.filesDir.absolutePath + "/webcache"
        val settings = settings
        settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.setGeolocationEnabled(true)
        settings.allowFileAccess = false
        settings.allowContentAccess = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccessFromFileURLs = true
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.setAppCachePath(APP_CACHE_DIRNAME)
        settings.useWideViewPort = true
        settings.databaseEnabled = true
        super.setWebChromeClient(mWebChromeClient)
        webViewClient = generateDWebViewClient()
        addInternalJavascriptObject()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            super.addJavascriptInterface(innerJavascriptInterface, BRIDGE_NAME)
        } else {
            // add dsbridge tag in lower android version
            settings.setUserAgentString(settings.userAgentString + " _dsbridge")
        }
    }

    private fun generateDWebViewClient(): DWebViewClient {
        return DWebViewClient()
    }

    private fun parseNamespace(method: String): Array<String> {
        var method = method
        val pos = method.lastIndexOf('.')
        var namespace = ""
        if (pos != -1) {
            namespace = method.substring(0, pos)
            method = method.substring(pos + 1)
        }
        return arrayOf(namespace, method)
    }

    @Keep
    private fun addInternalJavascriptObject() {
        addJavascriptObject(object : Any() {
            @Keep
            @JavascriptInterface
            @Throws(JSONException::class)
            fun hasNativeMethod(args: Any): Boolean {
                val jsonObject = args as JSONObject
                val methodName = jsonObject.getString("name").trim { it <= ' ' }
                val type = jsonObject.getString("type").trim { it <= ' ' }
                val nameStr = parseNamespace(methodName)
                val jsb = javaScriptNamespaceInterfaces[nameStr[0]]
                if (jsb != null) {
                    val cls: Class<*> = jsb.javaClass
                    var asyn = false
                    var method: Method? = null
                    try {
                        method = cls.getMethod(
                            nameStr[1],
                            *arrayOf(Any::class.java, CompletionHandler::class.java)
                        )
                        asyn = true
                    } catch (e: Exception) {
                        try {
                            method = cls.getMethod(
                                nameStr[1], *arrayOf<Class<*>>(
                                    Any::class.java
                                )
                            )
                        } catch (ex: Exception) {
                        }
                    }
                    if (method != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            val annotation = method.getAnnotation(
                                JavascriptInterface::class.java
                            ) ?: return false
                        }
                        if (("all" == type) || (asyn && ("asyn" == type) || (!asyn && ("syn" == type)))) {
                            return true
                        }
                    }
                }
                return false
            }

            @Keep
            @JavascriptInterface
            @Throws(JSONException::class)
            fun closePage(`object`: Any?): String? {
                runOnMainThread(Runnable {
                    if ((javascriptCloseWindowListener == null
                                || javascriptCloseWindowListener!!.onClose())
                    ) {
                        val context = context
                        if (context is Activity) {
                            context.onBackPressed()
                        }
                    }
                })
                return null
            }

            @Keep
            @JavascriptInterface
            @Throws(JSONException::class)
            fun disableJavascriptDialogBlock(`object`: Any) {
                val jsonObject = `object` as JSONObject
                alertBoxBlock = !jsonObject.getBoolean("disable")
            }

            @Keep
            @JavascriptInterface
            fun dsinit(jsonObject: Any?) {
                dispatchStartupQueue()
            }

            @Keep
            @JavascriptInterface
            fun returnValue(obj: Any) {
                runOnMainThread(object : Runnable {
                    override fun run() {
                        val jsonObject = obj as JSONObject
                        var data: Any? = null
                        try {
                            val id = jsonObject.getInt("id")
                            val isCompleted = jsonObject.getBoolean("complete")
                            val handler = handlerMap[id]
                            if (jsonObject.has("data")) {
                                data = jsonObject["data"]
                            }
                            if (handler != null) {
                                handler.onValue(data as Nothing?)
                                if (isCompleted) {
                                    handlerMap.remove(id)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }, "_dsb")
    }

    private fun _evaluateJavascript(script: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super@DWebView.evaluateJavascript(script, null)
        } else {
            super.loadUrl("javascript:$script")
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param script
     */
    fun evaluateJavascript(script: String) {
        runOnMainThread(object : Runnable {
            override fun run() {
                _evaluateJavascript(script)
            }
        })
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param url
     */
    override fun loadUrl(url: String) {
        runOnMainThread(object : Runnable {
            override fun run() {
                if (url != null && url.startsWith("javascript:")) {
                    super@DWebView.loadUrl(url)
                } else {
                    callInfoList = ArrayList()
                    super@DWebView.loadUrl(url)
                }
            }
        })
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param url
     * @param additionalHttpHeaders
     */
    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        runOnMainThread(object : Runnable {
            override fun run() {
                if (url != null && url.startsWith("javascript:")) {
                    super@DWebView.loadUrl(url, additionalHttpHeaders)
                } else {
                    callInfoList = ArrayList()
                    super@DWebView.loadUrl(url, additionalHttpHeaders)
                }
            }
        })
    }

    override fun reload() {
        runOnMainThread(object : Runnable {
            override fun run() {
                callInfoList = ArrayList()
                super@DWebView.reload()
            }
        })
    }

    /**
     * set a listener for javascript closing the current activity.
     */
    fun setJavascriptCloseWindowListener(listener: JavascriptCloseWindowListener?) {
        javascriptCloseWindowListener = listener
    }

    private class CallInfo internal constructor(handlerName: String, id: Int, args: Array<Any?>?) {
        private val data: String
        val callbackId: Int
        private val method: String
        override fun toString(): String {
            val jo = JSONObject()
            try {
                jo.put("method", method)
                jo.put("callbackId", callbackId)
                jo.put("data", data)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return jo.toString()
        }

        init {
            var args = args
            if (args == null) args = arrayOfNulls(0)
            data = JSONArray(Arrays.asList(*args)).toString()
            callbackId = id
            method = handlerName
        }
    }

    @Synchronized
    private fun dispatchStartupQueue() {
        if (callInfoList != null) {
            for (info: CallInfo in callInfoList!!) {
                dispatchJavascriptCall(info)
            }
            callInfoList = null
        }
    }

    private fun dispatchJavascriptCall(info: CallInfo) {
        evaluateJavascript(String.format("window._handleMessageFromNative(%s)", info.toString()))
    }

    @Synchronized
    fun <T> callHandler(method: String, args: Array<Any?>?, handler: OnReturnValue<T>?) {
        val callInfo = CallInfo(method, ++callID, args)
        if (handler != null) {
            handlerMap[callInfo.callbackId] = handler
        }
        if (callInfoList != null) {
            callInfoList!!.add(callInfo)
        } else {
            dispatchJavascriptCall(callInfo)
        }
    }

    fun callHandler(method: String, args: Array<Any?>?) {
        callHandler<Any>(method, args, null)
    }

    fun <T> callHandler(method: String, handler: OnReturnValue<T>?) {
        callHandler(method, null, handler)
    }

    /**
     * Test whether the handler exist in javascript
     *
     * @param handlerName
     * @param existCallback
     */
    fun hasJavascriptMethod(handlerName: String?, existCallback: OnReturnValue<Boolean>?) {
        callHandler("_hasJavascriptMethod", arrayOf(handlerName), existCallback)
    }

    /**
     * Add a java object which implemented the javascript interfaces to dsBridge with namespace.
     * Remove the object using [removeJavascriptObject(String)][.removeJavascriptObject]
     *
     * @param object
     * @param namespace if empty, the object have no namespace.
     */
    fun addJavascriptObject(`object`: Any?, namespace: String?) {
        var namespace = namespace
        if (namespace == null) {
            namespace = ""
        }
        if (`object` != null) {
            javaScriptNamespaceInterfaces[namespace] = `object`
        }
    }

    /**
     * remove the javascript object with supplied namespace.
     *
     * @param namespace
     */
    fun removeJavascriptObject(namespace: String?) {
        var namespace = namespace
        if (namespace == null) {
            namespace = ""
        }
        javaScriptNamespaceInterfaces.remove(namespace)
    }

    fun disableJavascriptDialogBlock(disable: Boolean) {
        alertBoxBlock = !disable
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        custWebChromeClient = client
    }

    private val mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (webViewUrlChangeListener != null) {
                webViewUrlChangeListener!!.loadProgress(newProgress)
            }
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onProgressChanged(view, newProgress)
            } else {
                super.onProgressChanged(view, newProgress)
            }
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onReceivedTitle(view, title)
            } else {
                super.onReceivedTitle(view, title)
            }
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onReceivedIcon(view, icon)
            } else {
                super.onReceivedIcon(view, icon)
            }
        }

        override fun onReceivedTouchIconUrl(view: WebView, url: String, precomposed: Boolean) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onReceivedTouchIconUrl(view, url, precomposed)
            } else {
                super.onReceivedTouchIconUrl(view, url, precomposed)
            }
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onShowCustomView(view, callback)
            } else {
                super.onShowCustomView(view, callback)
            }
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        override fun onShowCustomView(
            view: View, requestedOrientation: Int,
            callback: CustomViewCallback
        ) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onShowCustomView(view, requestedOrientation, callback)
            } else {
                super.onShowCustomView(view, requestedOrientation, callback)
            }
        }

        override fun onHideCustomView() {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onHideCustomView()
            } else {
                super.onHideCustomView()
            }
        }

        override fun onCreateWindow(
            view: WebView, isDialog: Boolean,
            isUserGesture: Boolean, resultMsg: Message
        ): Boolean {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.onCreateWindow(
                    view, isDialog,
                    isUserGesture, resultMsg
                )
            } else super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onRequestFocus(view: WebView) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onRequestFocus(view)
            } else {
                super.onRequestFocus(view)
            }
        }

        override fun onCloseWindow(window: WebView) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onCloseWindow(window)
            } else {
                super.onCloseWindow(window)
            }
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            if (!alertBoxBlock) {
                result.confirm()
            }
            if (custWebChromeClient != null) {
                if (custWebChromeClient!!.onJsAlert(view, url, message, result)) {
                    return true
                }
            }
            val alertDialog: Dialog = AlertDialog.Builder(
                context
            ).setMessage(message).setCancelable(false)
                .setPositiveButton("Ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                        if (alertBoxBlock) {
                            result.confirm()
                        }
                    }
                })
                .create()
            alertDialog.show()
            return true
        }

        override fun onJsConfirm(
            view: WebView, url: String, message: String,
            result: JsResult
        ): Boolean {
            if (!alertBoxBlock) {
                result.confirm()
            }
            if (custWebChromeClient != null && custWebChromeClient!!.onJsConfirm(
                    view,
                    url,
                    message,
                    result
                )
            ) {
                return true
            } else {
                val listener: DialogInterface.OnClickListener =
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            if (alertBoxBlock) {
                                if (which == Dialog.BUTTON_POSITIVE) {
                                    result.confirm()
                                } else {
                                    result.cancel()
                                }
                            }
                        }
                    }
                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", listener)
                    .setNegativeButton("Cancel", listener).show()
                return true
            }
        }

        override fun onJsPrompt(
            view: WebView, url: String, message: String,
            defaultValue: String, result: JsPromptResult
        ): Boolean {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                val prefix = "_dsbridge="
                if (message.startsWith(prefix)) {
                    result.confirm(
                        innerJavascriptInterface.call(
                            message.substring(prefix.length),
                            defaultValue
                        )
                    )
                    return true
                }
            }
            if (!alertBoxBlock) {
                result.confirm()
            }
            if (custWebChromeClient != null && custWebChromeClient!!.onJsPrompt(
                    view,
                    url,
                    message,
                    defaultValue,
                    result
                )
            ) {
                return true
            } else {
                val editText = EditText(context)
                editText.setText(defaultValue)
                if (defaultValue != null) {
                    editText.setSelection(defaultValue.length)
                }
                val dpi = context.resources.displayMetrics.density
                val listener: DialogInterface.OnClickListener =
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            if (alertBoxBlock) {
                                if (which == Dialog.BUTTON_POSITIVE) {
                                    result.confirm(editText.text.toString())
                                } else {
                                    result.cancel()
                                }
                            }
                        }
                    }
                AlertDialog.Builder(context)
                    .setTitle(message)
                    .setView(editText)
                    .setCancelable(false)
                    .setPositiveButton("Ok", listener)
                    .setNegativeButton("Cancel", listener)
                    .show()
                val layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val t = (dpi * 16).toInt()
                layoutParams.setMargins(t, 0, t, 0)
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                editText.layoutParams = layoutParams
                val padding = (15 * dpi).toInt()
                editText.setPadding(padding - (5 * dpi).toInt(), padding, padding, padding)
                return true
            }
        }

        override fun onJsBeforeUnload(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.onJsBeforeUnload(view, url, message, result)
            } else super.onJsBeforeUnload(view, url, message, result)
        }

        override fun onExceededDatabaseQuota(
            url: String, databaseIdentifier: String, quota: Long,
            estimatedDatabaseSize: Long,
            totalQuota: Long,
            quotaUpdater: QuotaUpdater
        ) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onExceededDatabaseQuota(
                    url, databaseIdentifier, quota,
                    estimatedDatabaseSize, totalQuota, quotaUpdater
                )
            } else {
                super.onExceededDatabaseQuota(
                    url, databaseIdentifier, quota,
                    estimatedDatabaseSize, totalQuota, quotaUpdater
                )
            }
        }

        override fun onReachedMaxAppCacheSize(
            requiredStorage: Long,
            quota: Long,
            quotaUpdater: QuotaUpdater
        ) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
            }
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback
        ) {
            callback.invoke(origin, true, true)
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onGeolocationPermissionsShowPrompt(origin, callback)
            } else {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
            }
        }

        override fun onGeolocationPermissionsHidePrompt() {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onGeolocationPermissionsHidePrompt()
            } else {
                super.onGeolocationPermissionsHidePrompt()
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: PermissionRequest) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onPermissionRequest(request)
            } else {
                super.onPermissionRequest(request)
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequestCanceled(request: PermissionRequest) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onPermissionRequestCanceled(request)
            } else {
                super.onPermissionRequestCanceled(request)
            }
        }

        override fun onJsTimeout(): Boolean {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.onJsTimeout()
            } else super.onJsTimeout()
        }

        override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.onConsoleMessage(message, lineNumber, sourceID)
            } else {
                super.onConsoleMessage(message, lineNumber, sourceID)
            }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.onConsoleMessage(consoleMessage)
            } else super.onConsoleMessage(consoleMessage)
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.defaultVideoPoster
            } else super.getDefaultVideoPoster()
        }

        override fun getVideoLoadingProgressView(): View? {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.videoLoadingProgressView
            } else super.getVideoLoadingProgressView()
        }

        override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
            if (custWebChromeClient != null) {
                custWebChromeClient!!.getVisitedHistory(callback)
            } else {
                super.getVisitedHistory(callback)
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            return if (custWebChromeClient != null) {
                custWebChromeClient!!.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            } else super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

        @Keep
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun openFileChooser(valueCallback: ValueCallback<*>?, acceptType: String?) {
            if (custWebChromeClient is FileChooser) {
                (custWebChromeClient as FileChooser).openFileChooser(valueCallback, acceptType)
            }
        }

        @Keep
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun openFileChooser(
            valueCallback: ValueCallback<Uri?>?,
            acceptType: String?, capture: String?
        ) {
            if (custWebChromeClient is FileChooser) {
                (custWebChromeClient as FileChooser).openFileChooser(valueCallback, acceptType, capture)
            }
        }
    }

    fun setWebViewUrlChangeListener(webViewUrlChangeListener: WebViewUrlChangeListener?) {
        this.webViewUrlChangeListener = webViewUrlChangeListener
    }

    override fun clearCache(includeDiskFiles: Boolean) {
        super.clearCache(includeDiskFiles)
        CookieManager.getInstance().removeAllCookie()
        val context = context
        try {
            context.deleteDatabase("webview.db")
            context.deleteDatabase("webviewCache.db")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val appCacheDir = File(APP_CACHE_DIRNAME)
        val webviewCacheDir = File(
            context.cacheDir
                .absolutePath + "/webviewCache"
        )
        if (webviewCacheDir.exists()) {
            deleteFile(webviewCacheDir)
        }
        if (appCacheDir.exists()) {
            deleteFile(appCacheDir)
        }
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                val files = file.listFiles()
                for (i in files.indices) {
                    deleteFile(files[i])
                }
            }
            file.delete()
        } else {
            Log.e("Webview", "delete file no exists " + file.absolutePath)
        }
    }

    private fun runOnMainThread(runnable: Runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run()
            return
        }
        mainHandler.post(runnable)
    }

    internal inner class DWebViewClient() : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            var url = url
            val tempUrl = url
            try {
                url = URLDecoder.decode(url, "UTF-8")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (url.endsWith("apk")) {
                ClipboardUtils.copyText(url)
                AppUtil.goBrowser(url)
                return true
            } else {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (webViewUrlChangeListener != null) {
                webViewUrlChangeListener!!.onPageFinsh()
            }
        }

        override fun onLoadResource(view: WebView, url: String) {
            if (webViewUrlChangeListener != null) {
                webViewUrlChangeListener!!.changedUrl(view.url)
            }
            super.onLoadResource(view, url)
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            //6.0以下执行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return
            }
            if (webViewUrlChangeListener != null) {
                webViewUrlChangeListener!!.onPageError()
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            if (request.isForMainFrame) {
                if (webViewUrlChangeListener != null) {
                    webViewUrlChangeListener!!.onPageError()
                }
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            // 这个方法在 android 6.0才出现
            val statusCode = errorResponse.statusCode
            if (404 == statusCode || 500 == statusCode) {
                if (webViewUrlChangeListener != null) {
//                    if (!view.getUrl().startsWith(PPT)) {
//                        webViewUrlChangeListener.onPageError();
//                    }
                }
            }
        }

        //        @Override
        //        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        //            handler.proceed();
        //        }
        //        @Override
        //        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        //            super.onReceivedSslError(view, handler, error);
        //            Log.i("keey", "err:" + error.getCertificate());
        //            if (error.getPrimaryError() == SslError.SSL_INVALID) {
        //                if (SSLCertUtil.isSSLCertOk(error.getCertificate(), Constant.h5CrtStr)) {
        //                    handler.proceed();
        //                } else {
        //                    ToastUtils.INSTANCE.showErrorToast("发现风险请检查网络");
        //                }
        //            } else {
        //                handler.cancel();
        //            }
        //        }
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            if (isProxy) { //如果允许抓包
                return super.shouldInterceptRequest(view, request)
            }
            val url = request.url.toString()
            //            return getNewResponse(url, request);
            return checkSsl(view, request)
        }
    }

    private fun checkSsl(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url
        val urlPath = uri.toString()
        if (urlPath.startsWith("http://") || !urlPath.startsWith("https://app.jinilife.com/") || request.url.toString()
                .contains("/activity/attend")
        ) {
            return null
        }
        var urlConnection: URLConnection? = null
        try {
            var url: URL? = null
            try {
                url = URL(urlPath)
                urlConnection = url.openConnection()
                if (urlConnection is HttpsURLConnection) {
                    val headers = request.requestHeaders
                    val keySet: Set<String> = headers.keys
                    for (key: String in keySet) {
                        urlConnection.setRequestProperty(key, headers[key])
                    }
                    val httpsURLConnection = urlConnection
                    httpsURLConnection.instanceFollowRedirects = false
                    httpsURLConnection.sslSocketFactory = sslParams!!.sSLSocketFactory
                    httpsURLConnection.hostnameVerifier = object : HostnameVerifier {
                        override fun verify(hostname: String, session: SSLSession): Boolean {
                            return true
                        }
                    }
                    val respCode = httpsURLConnection.responseCode
                    if (respCode == 301 || respCode == 302) {
                        httpsURLConnection.disconnect()
                        return null
                    }
                    if (respCode == 400) {
                        httpsURLConnection.disconnect()
                        return WebResourceResponse(null, null, null)
                    }
                    if (respCode != 200) {
                        httpsURLConnection.disconnect()
                        return null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val `is` = urlConnection!!.getInputStream()
            val contentType = urlConnection.contentType
            val encoding = urlConnection.contentEncoding
            if (contentType != null) {
                var mimeType = contentType
                if (contentType.contains(";")) {
                    mimeType =
                        Arrays.asList(*contentType.split(";").toTypedArray())[0].trim { it <= ' ' }
                }
                return WebResourceResponse(mimeType, encoding, `is`)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (urlConnection != null) {
            if (urlConnection is HttpsURLConnection) {
                urlConnection.disconnect()
            } else if (urlConnection is HttpURLConnection) {
                urlConnection.disconnect()
            }
        }
        return WebResourceResponse(null, null, null)
    }

    interface WebViewUrlChangeListener {
        fun changedUrl(url: String?)
        fun onPageFinsh()
        fun loadProgress(progress: Int)
        fun onPageError()
    }

    public override fun computeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }

    private fun getNewResponse(
        url: String,
        mWebResourceRequest: WebResourceRequest
    ): WebResourceResponse? {
        if (url.startsWith("http://") || !url.startsWith("https://app.jinilife.com/") || mWebResourceRequest.url.toString()
                .contains("/activity/attend")
        ) {
            return null
        }
        try {
            val builder: Request.Builder = Request.Builder()
                .url(url.trim { it <= ' ' })
            val headers = mWebResourceRequest.requestHeaders
            val keySet: Set<String> = headers.keys
            for (key: String in keySet) {
                builder.addHeader(key, headers[key].toString())
            }
            val request: Request = builder.method(mWebResourceRequest.method, null).build()
            val response = httpClient!!.newCall(request).execute()
            Log.i("DWebView", "code:" + response.code)
            if (response.code == 200) {
                var conentType =
                    response.header("Content-Type", response.body!!.contentType()!!.type)
                val temp = conentType!!.lowercase(Locale.getDefault())
                if (temp.contains("charset=utf-8")) {
                    conentType =
                        conentType.replace("(?i)" + "charset=utf-8".toRegex(), "") //不区分大小写的替换
                }
                if (conentType.contains(";")) {
                    conentType = conentType.replace(";".toRegex(), "")
                    conentType = conentType.trim { it <= ' ' }
                }
                return WebResourceResponse(
                    conentType,
                    response.header("Content-Encoding", "utf-8"),
                    response.body!!.byteStream()
                )
            } else {
                return WebResourceResponse(null, null, null)
            }
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        private val BRIDGE_NAME = "_dsbridge"
        private val LOG_TAG = "dsBridge"
        private var isDebug = false

        /**
         * Set debug mode. if in debug mode, some errors will be prompted by a dialog
         * and the exception caused by the native handlers will not be captured.
         *
         * @param enabled
         */
        fun setWebContentsDebuggingEnabled(enabled: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(enabled)
            }
            isDebug = enabled
        }
    }
}