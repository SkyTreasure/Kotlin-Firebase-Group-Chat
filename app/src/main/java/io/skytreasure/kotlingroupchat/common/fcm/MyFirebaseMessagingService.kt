package com.microland.microlandone.firebase.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val TAG: String = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage == null) {
            Log.d(TAG, "remoteMessage null")
        } else {
            if (remoteMessage.data != null && remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "remoteMessage data: " + remoteMessage.data.toString())
                Log.d(TAG, "Message remoteMessage data: " + remoteMessage.data)
                sendNotification(remoteMessage)
            } else if (remoteMessage.notification != null) {
                Log.d(TAG, "remoteMessage notification: " + remoteMessage.notification.toString())
                Log.d(TAG, "Message remoteMessage notification: " + remoteMessage.notification)
                sendNotification(remoteMessage)
            }
        }

//        if (remoteMessage.data.isNotEmpty() && remoteMessage.notification != null) {
//            Log.d(TAG, "Message data payload: " + remoteMessage.data)
//            sendNotification(remoteMessage)
//        } else {
//
//        }

    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val dataBody = remoteMessage.data
      //  val notificationBody = remoteMessage.notification

        val notificationManager = MyNotificationManager.getInstance(this)
        notificationManager.sendNotification(dataBody)
    }

}
