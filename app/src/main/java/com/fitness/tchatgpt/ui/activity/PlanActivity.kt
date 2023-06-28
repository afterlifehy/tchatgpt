package com.fitness.tchatgpt.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatMessageBuilder
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.BarUtils
import com.fitness.base.BaseApplication
import com.fitness.base.arouter.ARouterMap
import com.fitness.base.viewbase.VbBaseActivity
import com.fitness.tchatgpt.databinding.ActivityPlanBinding
import com.fitness.tchatgpt.mvvm.viewmodel.PlanViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by huy  on 2023/3/29.
 */
@OptIn(BetaOpenAI::class)
@Route(path = ARouterMap.PLAN)
class PlanActivity : VbBaseActivity<PlanViewModel, ActivityPlanBinding>() {
    var speechRecognizer: SpeechRecognizer? = null
    var openAI: OpenAI? = null
    var contextMessage: MutableList<ChatMessage> = ArrayList()
    private lateinit var tts: TextToSpeech
    lateinit var recognizerIntent: Intent

    // 当前任务的文本内容
    private var currentText: String = ""
    var taskList: MutableList<String> = ArrayList()

    @SuppressLint("CheckResult")
    override fun initView() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, com.fitness.base.R.color.transparent))
        binding.tvVatago.typeface = heavyTypeFace

        openAI = OpenAI("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")
        initTTS()

        var rxPermissions = RxPermissions(this@PlanActivity)
        rxPermissions.request(Manifest.permission.RECORD_AUDIO)
            .subscribe {
                if (it) {
                    createSpeech()
                }
            }
    }

    fun createSpeech() {
        // 查找当前系统的内置使用的语音识别服务
        val serviceComponent: String = Settings.Secure.getString(
            contentResolver,
            "voice_recognition_service"
        )
        if (TextUtils.isEmpty(serviceComponent)) {
            return
        }
        val component = ComponentName.unflattenFromString(serviceComponent) ?: return
        var isRecognizerServiceValid = false
        var currentRecognitionCmp: ComponentName? = null
        val list: List<ResolveInfo> =
            packageManager.queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), MATCH_ALL)
        if (list.isNotEmpty()) {
            for (i in list) {
                if (i.serviceInfo.packageName.equals(component.packageName)) {
                    isRecognizerServiceValid = true
                    break
                } else {
                    currentRecognitionCmp = ComponentName(i.serviceInfo.packageName, i.serviceInfo.name)
                }
            }
        } else {
            return
        }
        // 创建 SpeechRecognizer 对象
        if (isRecognizerServiceValid) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        } else {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, currentRecognitionCmp)
        }
        // 创建 Intent，指定语音识别的模式
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        //        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SECURE, true)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
//                Log.v("SpeechRecognizer", "onReadyForSpeech1")
            }

            override fun onBeginningOfSpeech() {
//                Log.v("SpeechRecognizer", "onReadyForSpeech2")
            }

            override fun onRmsChanged(rmsdB: Float) {
//                Log.v("SpeechRecognizer", "onReadyForSpeech3")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
//                Log.v("SpeechRecognizer", "onReadyForSpeech4")
            }

            override fun onEndOfSpeech() {
                Log.v("SpeechRecognizer", "onReadyForSpeech5")
                speechRecognizer?.stopListening()
            }

            override fun onError(error: Int) {
                Log.v("SpeechRecognizer", recogError(error))
            }

            override fun onResults(results: Bundle?) {
                var message = ""
                val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                for (i in matches!!) {
                    message += i
                }
                binding.tvMessage.text = binding.tvMessage.text.toString() + message + "\n"
                binding.nsvMessage.smoothScrollTo(0, binding.tvMessage.bottom)
                initChatRq(message)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.v("SpeechRecognizer", "onReadyForSpeech6")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

        })
        // 开始语音识别，传入 Intent 和 RecognitionListener 对象
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun recogError(errorCode: Int): String {
        val message: String
        message = when (errorCode) {
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "连接超时"
            SpeechRecognizer.ERROR_NETWORK -> "网络问题"
            SpeechRecognizer.ERROR_AUDIO -> "音频问题"
            SpeechRecognizer.ERROR_SERVER -> "服务端错误"
            SpeechRecognizer.ERROR_CLIENT -> "其它客户端错误"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有语音输入"
            SpeechRecognizer.ERROR_NO_MATCH -> {
                speechRecognizer?.startListening(recognizerIntent)
                return "没有匹配的识别结果"
            }
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "引擎忙"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
            else -> "未知错误:$errorCode"
        }
        return message
    }

    fun isRecognitionAvailable(): Boolean {
        val list: List<ResolveInfo> = BaseApplication.instance().packageManager.queryIntentServices(
            Intent(RecognitionService.SERVICE_INTERFACE), 0
        )
        return list != null && list.size != 0
    }

    fun initChatRq(prompt: String) {
        contextMessage.add(
            ChatMessage(
                role = ChatRole.User,
                content = prompt
            )
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = contextMessage
        )
        GlobalScope.launch {
            try {
                chatCompletions(openAI!!, chatCompletionRequest, this)
            } catch (e: Exception) {
                chatCompletions(openAI!!, chatCompletionRequest, this)
            }
        }
    }

    suspend fun chatCompletions(
        openAI: OpenAI,
        chatCompletionRequest: ChatCompletionRequest,
        coroutineScope: CoroutineScope
    ) {
        var result = ""
        var chatMessageBuilder = ChatMessageBuilder()
        chatMessageBuilder.role = ChatRole.Assistant
        val job = openAI.chatCompletions(chatCompletionRequest)
            .onEach {
                if (it.choices[0].delta!!.content != null) {
                    result += it.choices[0].delta!!.content
                    Log.v("1234", it.choices[0].delta!!.content.toString())
                    runOnUiThread {
//                        binding.dwWeb.loadData(result, "text/html", "UTF-8")
                        binding.tvMessage.append(it.choices[0].delta!!.content)
                        binding.nsvMessage.smoothScrollTo(0, binding.tvMessage.bottom)

                        addTextToTask(it.choices[0].delta!!.content.toString())
                        chatMessageBuilder.content = result
                    }
                }
            }
            .launchIn(coroutineScope)
        job.invokeOnCompletion {
            runOnUiThread {
                if (chatMessageBuilder.content!!.isNotEmpty()) {
                    contextMessage.add(chatMessageBuilder.build())
                }
                binding.tvMessage.append("\n\n")
                binding.nsvMessage.smoothScrollTo(0, binding.tvMessage.bottom)
            }
        }
    }

    fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported")
                }
                // 设置语速和音调
                tts.setSpeechRate(1f)
                tts.setPitch(1.0f)
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }
    }

    var PAUSE_MARKS = ",.?!:;，。？！：；…—-~·`!@#$%^&*()_+={}[]\\|\"':;<>/?！@#￥%……&*（）——+={}【】‘；：”“’。，、 "
    private fun addTextToTask(text: String) {
        currentText += text
        if (PAUSE_MARKS.contains(text)) {
            val params = Bundle()
            taskList.add(currentText.hashCode().toString())
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text.hashCode().toString())
            tts.speak(currentText, TextToSpeech.QUEUE_ADD, params, currentText.hashCode().toString())
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    if (utteranceId == taskList[taskList.size - 1]) {
                        runOnUiThread {
                            speechRecognizer?.startListening(intent)
                            taskList.clear()
                        }
                    }
                }

                override fun onError(utteranceId: String?) {}

                override fun onStart(utteranceId: String?) {}
            })
            currentText = ""
        }
    }

    override fun initListener() {
    }

    override fun initData() {
    }

    override fun marginStatusBarView(): View {
        return binding.tvVatago
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityPlanBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override val isFullScreen: Boolean
        get() = true

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        openAI = null
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}