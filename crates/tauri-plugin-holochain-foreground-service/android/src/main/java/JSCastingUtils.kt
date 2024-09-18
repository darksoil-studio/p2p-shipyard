package com.plugin.holochainforegroundservice

import app.tauri.plugin.JSObject
import app.tauri.plugin.JSArray
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KProperty1
import android.util.Log

object JSCastingUtils {
    /// Convert Any object to a JSObject
    /// This is intended to be as generic as possible, but may not work for every object.
    /// If you run into errors, you likely need to override handling of certain property types.
    /// JSObject will accept Any type, but may cast it to a String if it doesn't know how to handle it specifically.
    inline fun <reified T : Any> toJSObject(data: T): JSObject {
        val obj = JSObject()
        val properties = data::class.memberProperties
        for (property in properties) { 
            val prop = property as? KProperty1<T, *>
            val value = prop?.get(data)
            when (value) {
                is String, is Int, is Long, is Double, is Boolean -> obj.put(property.name, value)
                is Enum<*> -> obj.put(property.name, value.name)
                null -> obj.put(property.name, null)
                is ByteArray -> {
                    val byteCollection: MutableCollection<UByte> = value.toUByteArray().toMutableList()
                    val jsValue = try {
                        (byteCollection as? Collection<UByte>)?.toJSArray()
                    } catch (e: Exception) {
                        Log.e("toJSObject", "Error converting property ${property.name} to toJSArray", e)
                        null
                    }
                    obj.put(property.name, jsValue)
                }
                is Collection<*>, is MutableCollection<*> -> {
                    val jsValue = try {
                        (value as? Collection<Any>)?.toJSArray()
                    } catch (e: Exception) {
                        Log.e("toJSObject", "Error converting property ${property.name} to toJSArray", e)
                        null
                    }
                    obj.put(property.name, jsValue)
                }
                else -> {
                    val jsValue = try {
                        (value as? Any)?.toJSObject()
                    } catch (e: Exception) {
                        Log.e("toJSObject", "Error converting property ${property.name} to JSObject", e)
                        null
                    }
                    obj.put(property.name, jsValue)
                }
            }
        }
        return obj
    }

    /// Convert Collection<Any> to a JSArray
    /// This is intended to be as generic as possible, but may not work for every array.
    /// If you run into errors, you likely need to override handling of certain property types.
    /// JSArray will accept Any type, but may cast it to a String if it doesn't know how to handle it specifically.
    inline fun <reified T : Collection<Any>> toJSArray(data: T): JSArray {
        val arr = JSArray()
        for (element in data) {
            when (element) {
                is String, is Int, is Long, is Double, is Float, is Boolean, is Byte -> arr.put(element)
                is UByte -> arr.put(element.toInt())
                is Enum<*> -> arr.put(element.name)
                is Collection<*>, is MutableCollection<*> -> {
                    val jsValue = try {
                        (element as? Collection<Any>)?.toJSArray()
                    } catch (e: Exception) {
                        Log.e("toJSArray", "Error converting element ${element} to toJSArray", e)
                        null
                    }
                    arr.put(jsValue)
                }
                else -> {
                    val jsValue = try {
                        (element as? Any)?.toJSObject()
                    } catch (e: Exception) {
                        Log.e("toJSArray", "Error converting element ${element} to toJSObject", e)
                        null
                    }
                    arr.put(jsValue)
                }
            }
        }
        return arr
    }
}

fun Any.toJSObject(): JSObject = JSCastingUtils.toJSObject(this)
fun Collection<Any>.toJSArray(): JSArray = JSCastingUtils.toJSArray(this)
