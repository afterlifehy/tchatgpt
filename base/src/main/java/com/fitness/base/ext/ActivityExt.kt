package com.fitness.base.ext

import android.app.Activity
import android.text.TextUtils


fun Activity.i18N(id: Int): String {
    return this.resources.getString(id)
}

fun Activity.isEmpty(value: String): Boolean {
    return TextUtils.isEmpty(value)
}

