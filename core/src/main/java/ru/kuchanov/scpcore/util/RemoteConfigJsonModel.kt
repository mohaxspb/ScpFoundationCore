package ru.kuchanov.scpcore.util

/**
 * Created by kuchanov on 11/03/2018.
 */
data class RemoteConfigJsonModel(val defaultsMap: DefaultsMap)

data class DefaultsMap(val entry: List<Entry>)

data class Entry(val key: String, val value: Any)