package io.skytreasure.kotlingroupchat.common.util

import android.text.InputFilter
import android.text.TextUtils
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by akash on 24/10/17.
 */
class MyTextUtil {

    fun getTimestamp(milliseconds: Long): String =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(milliseconds)) + " " +
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(milliseconds)).replace(".", "").toLowerCase()

    fun getEmojiFIlter(): Array<InputFilter> {
        return arrayOf(InputFilter { src, _, _, _, _, _ ->
            if (src == "") { // for backspace
                return@InputFilter src
            }
            if (src.toString().matches("[\\x00-\\x7F]+".toRegex())) {
                src
            } else ""
        })
    }

    fun isValidPhoneNumber(phoneNumber: CharSequence): Boolean {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return Patterns.PHONE.matcher(phoneNumber).matches()
        }
        return false
    }

    fun getHash(a: String, b: String): String {
        var result: Long = 17
        result = 37 * result + a.hashCode().toLong() + b.hashCode().toLong()
        return result.toString()
    }

}