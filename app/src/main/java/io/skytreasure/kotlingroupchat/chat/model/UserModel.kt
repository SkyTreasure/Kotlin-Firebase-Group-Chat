package io.skytreasure.kotlingroupchat.chat.model

/**
 * Created by akash on 23/10/17.
 */
data class UserModel(var uid: String? = null,
                     var name: String? = null,
                     var image_url: String? = null,
                     var email: String? = null,
                     var groups: HashMap<String, Boolean> = hashMapOf(),
                     var online: Boolean? = null,
                     var unread_count: Int? = null,
                     var last_seen_online: String? = null,
                     var admin: Boolean? = null,
                     var delete_till: String? = null,
                     var active: Boolean? = null
)


data class GroupModel(var name: String? = null,
                      var image_url: String? = null,
                      var groupId: String? = null,
                      var group_deleted: Boolean? = null,
                      var group: Boolean? = null,
                      var members: HashMap<String, UserModel> = hashMapOf()
)