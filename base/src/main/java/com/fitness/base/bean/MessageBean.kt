package com.fitness.base.bean

/**
 * Created by huy  on 2023/3/21.
 */
data class MessageBean(
    val model: String,
    val messages: List<MessageBeanItem>,
    val stream: Boolean,
    val max_tokens: Int,
    val temperature: Double
)

data class MessageBeanItem(
    val role: String,
    val content: String
)