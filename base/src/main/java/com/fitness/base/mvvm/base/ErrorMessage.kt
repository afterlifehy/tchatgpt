package com.fitness.base.mvvm.base

data class ErrorMessage(
    var msg: String,
    var type: String = "",
    var code: Int = 0,
    var data: Any? = null
)