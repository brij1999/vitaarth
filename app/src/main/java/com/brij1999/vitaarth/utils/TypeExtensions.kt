package com.brij1999.vitaarth.utils

import android.net.Uri
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

fun Uri.upsertParameter(key: String, newValue: String): Uri {
    val params = queryParameterNames
    val newUri = buildUpon().clearQuery()
    var isSameParamPresent = false
    for (param in params) {
        // if same param is present override it, otherwise add the old param back
        newUri.appendQueryParameter(param, if (param == key) newValue else getQueryParameter(param))
        if (param == key) {
            // make sure we do not add new param again if already overridden
            isSameParamPresent = true
        }
    }
    if (!isSameParamPresent) {
        // never overrode same param so add new passed value now
        newUri.appendQueryParameter(key, newValue)
    }
    return newUri.build()
}
