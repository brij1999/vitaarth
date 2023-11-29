package com.brij1999.vitaarth.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.escapeSpecialCharacters(): String {
    return buildString {
        for (char in this@escapeSpecialCharacters) {
            when (char) {
                // List all special characters you want to escape here
                '\'', '\\', '\n', '\t', '"', '$', '.', '+', '*', '?', '[', ']', '(', ')', '{', '}', '^', '|', '/'
                -> append('\\').append(char)
                else -> append(char)
            }
        }
    }
}

fun Date.toFormattedString(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

fun Long.toFirebaseTimestamp(): Timestamp {
    return Timestamp(this / 1000, ((this % 1000) * 1000).toInt())
}
