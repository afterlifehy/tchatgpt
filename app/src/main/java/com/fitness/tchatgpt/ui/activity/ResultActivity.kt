package com.fitness.tchatgpt.ui.activity

import android.util.Log
import android.view.View
import androidx.viewbinding.ViewBinding
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.alibaba.android.arouter.facade.annotation.Route
import com.fitness.base.arouter.ARouterMap
import com.fitness.base.viewbase.VbBaseActivity
import com.fitness.tchatgpt.databinding.ActivityResultBinding
import com.fitness.tchatgpt.mvvm.viewmodel.ResultViewModel
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by huy  on 2023/3/22.
 */
@OptIn(BetaOpenAI::class)
@Route(path = ARouterMap.RESULT)
class ResultActivity : VbBaseActivity<ResultViewModel, ActivityResultBinding>() {
    var prompt = ""
    override fun initView() {
//        binding.rtvReply.movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun initListener() {
    }

    @OptIn(BetaOpenAI::class, BetaOpenAI::class)
    override fun initData() {
        prompt = intent.getStringExtra(ARouterMap.RESULT_PROMPT).toString()
        val openAI = OpenAI("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")
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )
        GlobalScope.launch {
            try {
                chatCompletions(openAI, chatCompletionRequest, this)
            }catch (e:Exception){
                chatCompletions(openAI, chatCompletionRequest, this)
            }
        }
//        var ret: String? = null
//        val client = HttpClient(Android) {
//            engine {
//                connectTimeout = 100_000
//                socketTimeout = 100_000
//            }
//        }
//        try {
//            ret = client.get<String>(url) { }
//        } catch (e: Exception) {
//        } finally {
//            client.close()
//        }
//        return ret
    }

    suspend fun chatCompletions(
        openAI: OpenAI,
        chatCompletionRequest: ChatCompletionRequest,
        coroutineScope: CoroutineScope
    ) {
        var result = ""
        openAI.chatCompletions(chatCompletionRequest)
            .onEach {
                if (it.choices[0].delta!!.content != null) {
                    result+=it.choices[0].delta!!.content
                    Log.v("1234", it.choices[0].delta!!.content.toString())
                    runOnUiThread {
                        binding.dwWeb.loadData(result, "text/html", "UTF-8")
//                        binding.rtvReply.append(it.choices[0].delta!!.content)
                    }
                }
            }
            .launchIn(coroutineScope)
            .join()
    }

    override fun startObserve() {
        super.startObserve()
//        mViewModel.apply {
//            completions.observe(this@ResultActivity) {
//                val openAIResponse = GsonUtils.fromJson<OpenAIResponse>(it, OpenAIResponse::class.java)
//                Log.v("chatgpt", openAIResponse.choices[0].text)
//            }
//            chatCompletions.observe(this@ResultActivity) {
//                ToastUtil.showToast(it)
//            }
//        }
    }


    override fun onReloadData() {
    }

    override fun providerVMClass(): Class<ResultViewModel> {
        return ResultViewModel::class.java
    }

    override fun marginStatusBarView(): View {
        return binding.dwWeb
    }

    override val isFullScreen: Boolean
        get() = true

    override fun getVbBindingView(): ViewBinding {
        return ActivityResultBinding.inflate(layoutInflater)
    }
}