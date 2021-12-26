package dev.erickvieira.ppcc.service.wallet.extension

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * It runs over a nullable instance of `T` and converts it to a not null one
 * @param unnullify an extension function that can run from `null` and returns a not null instance of T
 * @return T a not null instance of the given extension type
 */
inline fun <reified T> load(unnullify: T?.() -> T) = null.unnullify()

inline fun <reified T : Any> T.toUntypedMap(): Map<String, Any?> = Gson().let { gson ->
    gson.toJson(this).let { json ->
        gson.fromJson(json, object : TypeToken<Map<String, Any?>>() {}.type)
    }
}

inline fun <reified T : Any> T.toPairArray() = this.toUntypedMap().toList().toTypedArray()