package io.skytreasure.kotlingroupchat.chat.model

/**
 * Created by akash on 23/10/17.
 */
data class UserModel(var uid: String="", var name: String="", var imageUrl: String="",
                     var email:String="",
                     var arrCRKeys: HashMap<String, Boolean> = hashMapOf(), var isOnline: Boolean = false,
                     var unReadCount: Int = 0, var lastSeenOnline: String = "")