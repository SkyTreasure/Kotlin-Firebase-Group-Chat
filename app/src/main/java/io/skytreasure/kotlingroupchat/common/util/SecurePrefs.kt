package io.skytreasure.kotlingroupchat.common.util


/**
 * Created by akash on 23/10/17.
 */

import android.content.Context
import com.google.gson.Gson
import com.securepreferences.SecurePreferences

/**
 * Created by bsreeinf on 24/08/17.
 */
class SecurePrefs(context: Context) {
    @get:Synchronized private val pref = SecurePreferences(context)
    private val gson = Gson()

    companion object {
        const val PREF_FILE = "kotlinGroupChat"
    }

    fun put(key: String, value: String) {
        pref.edit().putString(key, value).apply()
    }

    fun get(key: String): String = pref.getString(key, "")

    fun clear() = pref.destroyKeys()


}