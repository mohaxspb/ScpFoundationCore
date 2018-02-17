package ru.kuchanov.scpcore.util

/**
 * Created by mohax on 27.01.2018.
 *
 * for ScpCore
 */

fun <T> List<T>.toStringWithLineBreaks(): String {
    val sb = StringBuilder()
    with(sb) { this@toStringWithLineBreaks.forEach { append(it).append("\n") } }
    return sb.toString()
}