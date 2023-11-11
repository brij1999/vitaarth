package com.brij1999.vitaarth.utils

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
