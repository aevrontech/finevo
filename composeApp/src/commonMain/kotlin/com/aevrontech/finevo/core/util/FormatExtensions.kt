package com.aevrontech.finevo.core.util

import kotlin.math.pow

/**
 * Formats a Double to a string with a fixed number of decimal places. This is a KMP-safe
 * replacement for String.format("%.2f", value).
 */
fun Double.formatDecimal(decimals: Int = 2, useGrouping: Boolean = false): String {
    if (decimals < 0) return this.toString()

    val multiplier = 10.0.pow(decimals)
    val rounded = (kotlin.math.round(this * multiplier)) / multiplier

    // Separate integer and decimal parts manually to avoid scientific notation issues with large
    // numbers
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')

    val integerPartStr: String
    val decimalPartStr: String

    if (dotIndex >= 0) {
        integerPartStr = str.substring(0, dotIndex)
        val decimalsStr = str.substring(dotIndex + 1)
        decimalPartStr = if (decimalsStr.length >= decimals) {
            decimalsStr.substring(0, decimals)
        } else {
            decimalsStr + "0".repeat(decimals - decimalsStr.length)
        }
    } else {
        integerPartStr = str
        decimalPartStr = "0".repeat(decimals)
    }

    val formattedInteger = if (useGrouping) {
        val reversed = integerPartStr.reversed()
        val chunked = reversed.chunked(3)
        chunked.joinToString(",").reversed()
    } else {
        integerPartStr
    }

    return if (decimals > 0) {
        "$formattedInteger.$decimalPartStr"
    } else {
        formattedInteger
    }
}

/**
 * Pads an integer with leading zeros to reach the specified length. This is a KMP-safe replacement
 * for String.format("%02d", value).
 */
fun Int.padZero(length: Int = 2): String {
    val str = this.toString()
    return if (str.length < length) {
        "0".repeat(length - str.length) + str
    } else {
        str
    }
}
