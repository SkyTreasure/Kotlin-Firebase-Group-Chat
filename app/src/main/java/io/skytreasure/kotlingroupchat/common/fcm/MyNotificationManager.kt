package com.microland.microlandone.firebase.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.ui.ChatMessagesActivity
import io.skytreasure.kotlingroupchat.chat.ui.ViewGroupsActivity
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import java.util.*

/**
 * This class handles all the push based notifications.
 *
 * Created by akash on 31/08/17.
 */

class MyNotificationManager private constructor(private val mContext: Context) {

    private var prefManager: SharedPrefManager? = null
    private val gson = Gson()

    private fun setPrefManager(prefManager: SharedPrefManager) {
        this.prefManager = prefManager
    }

    companion object {
        private val TAG = "MyNotificationManager"
        private var sNotificationManager: MyNotificationManager? = null

        fun getInstance(context: Context): MyNotificationManager {
            if (sNotificationManager == null) {
                sNotificationManager = MyNotificationManager(context)
                //Setting prefmanager
                sNotificationManager!!.setPrefManager(SharedPrefManager.getInstance(context))
            }
            return sNotificationManager as MyNotificationManager
        }
    }


    /**
     * Public method exposed to send notification.
     * @param dataBody
     */
    fun sendNotification(dataBody: Map<String, String>/*, notification: RemoteMessage.Notification*/) {

        when (dataBody[FirebaseConstants.TYPE]) {
        //TODO: Handle chat notif here. Show only when notif is not for active chat room

            FirebaseConstants.FOLLOW_GROUP -> {

                sendGroupCreateNotification(dataBody)
            }

            FirebaseConstants.GROUP_CHAT -> {
                sendGroupChatNotification(dataBody)
            }

            FirebaseConstants.ONE_ON_ONE_CHAT -> {
                sendOneOnOneChatNotification(dataBody)
            }
        }
    }

    private fun sendOneOnOneChatNotification(dataBody: Map<String, String>) {
        var title = dataBody[FirebaseConstants.TITLE];
        var body = dataBody[FirebaseConstants.BODY];
        var image = dataBody[FirebaseConstants.IMAGE];
        var group_id = dataBody[FirebaseConstants.GROUP_ID];
        var sender_id = dataBody[FirebaseConstants.SENDER];
        var sender_name = dataBody[FirebaseConstants.SENDER_NAME];
        var sender_image = dataBody[FirebaseConstants.SENDER_IMAGE];

        val resultIntent = Intent(mContext, ViewGroupsActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivities(
                mContext, 0,
                arrayOf(resultIntent), 0)


        val notif_id = (System.currentTimeMillis() and 0xFFL).toInt()

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!SharedPrefManager.getInstance(context = mContext).savedUserModel?.uid.equals(sender_id)) {
            val notificationBuilder = NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_cancel_white_24dp)
                    .setContentTitle(sender_name + "sent you a message")
                    .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent)
            notificationManager.notify(notif_id, notificationBuilder.build())
        }
    }

    private fun sendGroupChatNotification(dataBody: Map<String, String>) {
        var title = dataBody[FirebaseConstants.TITLE];
        var body = dataBody[FirebaseConstants.BODY];
        var image = dataBody[FirebaseConstants.IMAGE];
        var group_id = dataBody[FirebaseConstants.GROUP_ID];
        var sender_id = dataBody[FirebaseConstants.SENDER];
        var sender_name = dataBody[FirebaseConstants.SENDER_NAME];
        var sender_image = dataBody[FirebaseConstants.SENDER_IMAGE];

        val resultIntent = Intent(mContext, ViewGroupsActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivities(
                mContext, 0,
                arrayOf(resultIntent), 0)


        val notif_id = (System.currentTimeMillis() and 0xFFL).toInt()

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!SharedPrefManager.getInstance(context = mContext).savedUserModel?.uid.equals(sender_id)) {
            val notificationBuilder = NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_cancel_white_24dp)
                    .setContentTitle(title)
                    .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                    .setContentText(sender_name + ": " + body)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent)
            notificationManager.notify(notif_id, notificationBuilder.build())
        }
    }

    private fun sendGroupCreateNotification(dataBody: Map<String, String>) {
        FirebaseMessaging.getInstance().subscribeToTopic(dataBody[FirebaseConstants.GROUP_ID]);
        var title = dataBody[FirebaseConstants.TITLE];
        var body = dataBody[FirebaseConstants.BODY];

        val resultIntent = Intent(mContext, ViewGroupsActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivities(
                mContext, 0,
                arrayOf(resultIntent), 0)


        val notif_id = (System.currentTimeMillis() and 0xFFL).toInt()

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_cancel_white_24dp)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
        notificationManager.notify(notif_id, notificationBuilder.build())
    }


}
