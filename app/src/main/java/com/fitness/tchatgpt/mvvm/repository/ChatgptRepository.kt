package com.fitness.tchatgpt.mvvm.repository

import com.fitness.base.bean.*
import com.fitness.base.mvvm.base.BaseRepository

/**
 * Created by huy  on 2023/3/21.
 */
class ChatgptRepository: BaseRepository() {
    suspend fun completions(param: Map<String, Any>): String {
        return mServer.completions(param)
    }

    suspend fun chatCompletions(body: MessageBean): String {
        return mServer.chatCompletions(body)
    }
}