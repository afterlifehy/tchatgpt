package com.fitness.base.bean

/**
 * Created by huy  on 2023/3/22.
 */
data class OpenAIResponse(val choices: List<Choice>)

data class Choice(
    val finish_reason: String,
    val index: Int,
    val logprobs: Any,
    val text: String
)

