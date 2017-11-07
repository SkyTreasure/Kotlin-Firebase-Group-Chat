package com.microland.microlandone.firebase.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.ui.ChatMessagesActivity
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import java.util.*

/**
 * This class handles all the push based notifications.
 *
 * Created by neeraja on 31/08/17.
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
    fun sendNotification(dataBody: Map<String, String>, notification: RemoteMessage.Notification) {

        when (dataBody[FirebaseConstants.NOTIFICATION_TYPE]) {
        //TODO: Handle chat notif here. Show only when notif is not for active chat room
            FirebaseConstants.CHAT -> sendChatNotification(dataBody, notification)
        }
    }


    /**
     * Method to send chat notification
     * @param remoteMessage
     */
    private fun sendChatNotification(remoteMessage: Map<String, String>, notification: RemoteMessage.Notification) {
        /*  if (remoteMessage.containsKey(AppConstants.NOTIF_EMP_ID) &&
                  remoteMessage.containsKey(AppConstants.NOTIF_CHAT_ROOM_ID) &&
                  remoteMessage.containsKey(AppConstants.NOTIF_CHAT_ROOM_TITLE)) {*/

        val resultIntent = Intent(mContext, ChatMessagesActivity::class.java)
//            val resultIntent = Intent(mContext, ChatMessagesActivity::class.java)
//                    .putExtra(AppConstants.FROM, AppConstants.APP)
//                    .putExtra(AppConstants.NOTIF_EMP_ID, remoteMessage[AppConstants.NOTIF_EMP_ID])
//                    .putExtra(AppConstants.NOTIF_CHAT_ROOM_ID, remoteMessage[AppConstants.NOTIF_CHAT_ROOM_ID])
//                    .putExtra(AppConstants.NOTIF_CHAT_ROOM_TITLE, remoteMessage[AppConstants.NOTIF_CHAT_ROOM_TITLE])

//            val backIntentChat = Intent(mContext, ChatRoomsActivity::class.java)
//            val backIntentMain = Intent(mContext, MainActivity::class.java)

        val resultPendingIntent = PendingIntent.getActivities(
                mContext, 0,
                arrayOf(resultIntent), 0)
        buildAndSendNotification(remoteMessage, resultPendingIntent, notification)
        /*} else {
            Log.e("NotifManager", "Insuff keys in remoteMessage. notif stub is " + notification.toString())
        }*/
    }

    /**
     * Build and send notification, handle sound mute and other things in this method
     *
     * @param remoteMessage       Remote message received from the FCM
     * @param resultPendingIntent Prepare pending intent and send it to this function
     */
    private fun buildAndSendNotification(remoteMessage: Map<String, String>, resultPendingIntent: PendingIntent, notification: RemoteMessage.Notification) {
        val notif_id = (System.currentTimeMillis() and 0xFFL).toInt()
//        if (remoteMessage.containsKey("fromEmpId")) {
//            remoteMessage["fromEmpId"]!!.toInt()
//        } else {
//            (System.currentTimeMillis() and 0xFFL).toInt()
//        }
        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_cancel_white_24dp)
                .setContentTitle(notification.title)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
        if (remoteMessage.containsKey("notification_key"))
            notificationManager.notify(Integer.parseInt(remoteMessage["notification_key"]), notificationBuilder.build())
        else {
            //            if (ChatManager.activeEmpIDForNotif != null &&
//                    ChatManager.activeEmpIDForNotif is Int &&
//                    ChatManager.activeEmpIDForNotif!! > 0 &&
//                    ChatManager.activeEmpIDForNotif == notif_id) {
            //  if (!MyChatManager.chatRoomOpen && !MyChatManager.chatMessagesOpen) {
//                Log.e("ChatNotif", "Currently in the same chat screen as notif target")
//            } else {
            notificationManager.notify(notif_id, notificationBuilder.build())
            // }
        }
    }
}
