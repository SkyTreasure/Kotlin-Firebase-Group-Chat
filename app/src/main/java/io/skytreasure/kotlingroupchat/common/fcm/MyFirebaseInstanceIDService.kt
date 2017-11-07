package com.microland.microlandone.firebase.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.common.constants.AppConstants

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    companion object {
        private val TAG = "MyFirebaseIIDService"
    }

    override fun onTokenRefresh() {
        sendRegistrationToServer(FirebaseInstanceId.getInstance().token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val i = Intent()
        i.action = AppConstants.NOTIFICATION_ID_REFRESHED
        i.putExtra(AppConstants.NEW_TOKEN, token)
        sendBroadcast(i)

        MyChatManager.updateFCMTokenAndDeviceId(this, token!!)

        /*if (token != null && token.isNotBlank()) {
            FirebaseUtils.saveFCMToSharedPref(this, token, "", "")
            FirebaseUtils.saveFCMonRTDB(context = this)
        }

        val sharedPrefs = getSharedPreferences("FCM", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("token", token).apply()
        val deviceID = sharedPrefs.getString("device_id", "")
        val myUID = sharedPrefs.getString("emp_uid", "")

        Log.e(TAG, String.format("\nNEW_TOKEN\nuid: %s,\ndevice_id: %s,\nfcm_ids: %s\n", myUID, deviceID, token))

        if (myUID!!.trim { it <= ' ' } != "" && myUID.matches("/^uid-[1-9][0-9]*$/".toRegex())) {
            sharedPrefs.edit().putString("token", token).apply()
        } else {
            FirebaseDatabase.getInstance().reference.child("devices").child(deviceID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value !== myUID) {
                        val existingUID = dataSnapshot.value!!.toString()
                        dataSnapshot.ref.child("fcm_ids").child(existingUID).child(deviceID).removeValue()
                    }
                    FirebaseDatabase.getInstance().reference.child("devices").child(deviceID).setValue(myUID)
                    FirebaseDatabase.getInstance().reference.child("fcm_ids").child(myUID).child(deviceID).setValue(token)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Push Error")
                }
            })
        }*/
    }
}
