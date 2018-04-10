package com.ekeitho.simplepomodoro.pomodoro

import android.content.SharedPreferences


operator fun SharedPreferences.set(key: String, any: Any?) {
    when (any) {
        is Long -> this.edit().putLong(key, any).apply()
        is String -> this.edit().putString(key, any).apply()
        is Boolean -> this.edit().putBoolean(key, any).apply()
    }
}

operator inline fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T) : T {
    return when (defaultValue::class) {
        Long::class -> this.getLong(key, defaultValue as Long) as T
        String::class -> this.getString(key, defaultValue as String) as T
        Boolean::class -> this.getBoolean(key, defaultValue as Boolean) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}