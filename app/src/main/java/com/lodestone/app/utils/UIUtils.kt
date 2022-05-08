package com.lodestone.app.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.longToast(message: String) {
    toast(message, Toast.LENGTH_LONG)
}

fun Context.longToast(@StringRes messageId: Int) {
    toast(messageId, Toast.LENGTH_LONG)
}

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.toast(@StringRes messageId: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageId, length).show()
}
