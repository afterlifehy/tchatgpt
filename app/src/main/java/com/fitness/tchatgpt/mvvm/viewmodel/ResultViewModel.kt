package com.fitness.tchatgpt.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.fitness.base.bean.MessageBean
import com.fitness.base.mvvm.base.BaseViewModel
import com.fitness.base.mvvm.base.ErrorMessage
import com.fitness.tchatgpt.mvvm.repository.ChatgptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by huy  on 2023/3/22.
 */
class ResultViewModel : BaseViewModel() {

    val mChatgptRepository by lazy {
        ChatgptRepository()
    }

    val completions = MutableLiveData<String>()
    val chatCompletions = MutableLiveData<String>()

    fun completions(param: Map<String, Any>) {
        launch {
            val response = withContext(Dispatchers.IO) {
                mChatgptRepository.completions(param)
            }
            executeResponse(response, {
                completions.value = response
            }, {
                traverseErrorMsg(ErrorMessage(msg = "error", code = 1000))
            })
        }
    }

    fun chatCompletions(body: MessageBean) {
        launch {
            val response = withContext(Dispatchers.IO) {
                mChatgptRepository.chatCompletions(body)
            }
            executeResponse(response, {
                chatCompletions.value = response
            }, {
                traverseErrorMsg(ErrorMessage(msg = "error", code = 1000))
            })
        }
    }
}