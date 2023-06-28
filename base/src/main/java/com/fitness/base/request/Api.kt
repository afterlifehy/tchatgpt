package com.fitness.base.request

import com.fitness.base.bean.MessageBean
import retrofit2.http.*


interface Api {
    /**
     * chatgpt 发消息
     */
    @POST("completions")
    suspend fun completions(@Body param: @JvmSuppressWildcards Map<String, Any?>): String

    /**
     * chatgpt 发消息
     */
    @POST("chat/completions")
    @Streaming
    suspend fun chatCompletions(@Body body: MessageBean): String
}