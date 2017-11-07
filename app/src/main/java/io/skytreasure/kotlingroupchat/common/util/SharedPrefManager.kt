package io.skytreasure.kotlingroupchat.common.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.PrefConstants
import java.util.*

/**
 * Created by akash on 24/10/17.
 */
class SharedPrefManager private constructor(context: Context) {
    @get:Synchronized private val preferences: SharedPreferences
    private val gson = Gson()

    init {
        preferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Use this method to save key value pair, can be encrypted

     * @param key           Key to save
     * *
     * @param value         Value to save
     * *
     * @param shouldEncrypt If true, it will encrypt, else it will be saved as usual.
     */
    fun savePreferences(key: String, value: String, shouldEncrypt: Boolean) {
        if (shouldEncrypt) {
            val saveText = Base64.encodeToString(value.toByteArray(), Base64.DEFAULT)
            val editor = preferences.edit()
            editor.putString(key, saveText)
            editor.apply()
        } else {
            val editor = preferences.edit()
            editor.putString(key, value)
            editor.apply()
        }

    }

    /**
     * Call this method to get the shared preference instance

     * @param context
     * *
     * @return
     */
    fun getInstance(context: Context): SharedPrefManager {
        if (instance == null) {
            instance = SharedPrefManager(context)
        }
        return instance as SharedPrefManager
    }

    @Synchronized private fun getPreferences(): SharedPreferences {
        return preferences
    }

    /**
     * Use this method to save preferences

     * @param key
     * *
     * @param value
     */
    fun savePreferences(key: String, value: String) {

        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()

    }

    /**
     * Returns default value which is passed when called, else returns the actual value

     * @param key
     * *
     * @param defaultValue
     * *
     * @return
     */
    fun ifEmptyReturnDefault(key: String, defaultValue: String): String {
        if (isEmpty(key)) {
            return defaultValue
        } else {
            return getSavedPref(key)
        }
    }


    /**
     * Get the saved preferences value from shared preference

     * @param key         Key to fetch, returns "" if not found
     * *
     * @param isEncrypted if true tries to decrypt the fetched data
     * *
     * @return Value
     */
    fun getSavedPref(key: String, isEncrypted: Boolean): String {
        if (isEncrypted) {
            val text = preferences.getString(key, null) ?: return ""
            val converted = String(Base64.decode(text, Base64.DEFAULT))
            return converted
        } else {
            val text = preferences.getString(key, null) ?: return ""

            return text
        }
    }

    fun getSavedPref(key: String): String {
        val text = preferences.getString(key, null) ?: return ""

        return text
    }

    /**
     * Checks if the value for that key in sharedpref is empty or not

     * @param key
     * *
     * @return
     */
    fun isEmpty(key: String): Boolean {
        val result = getSavedPref(key, false)
        return result.equals("", ignoreCase = true)
    }

    /**
     * Pass key and value, compares the value with the value in the sharedpreference

     * @param key
     * *
     * @param value
     * *
     * @return
     */
    fun isEqualTo(key: String, value: String): Boolean {
        val result = getSavedPref(key, false)
        return result.equals(value, ignoreCase = true)
    }


    /**
     * Clears all the shared preference data
     */
    fun cleanSharedPreferences() {
        preferences.edit().clear().apply()

    }



    /**
     * @return
     */
    val savedUserModel: UserModel?
        get() {
            if (isEmpty(PrefConstants.USER_DATA)) {
                return null
            } else {
                return gson.fromJson(getSavedPref(PrefConstants.USER_DATA, false), UserModel::class.java)
            }
        }

    companion object {
        private val SHARED_PREFERENCE_NAME = "microland"
        private var instance: SharedPrefManager? = null

        /**
         * Call this method to get the shared preference instance

         * @param context
         * *
         * @return
         */
        fun getInstance(context: Context): SharedPrefManager {
            if (instance == null) {
                instance = SharedPrefManager(context)
            }
            return instance as SharedPrefManager
        }


    }





}
