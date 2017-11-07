package io.skytreasure.kotlingroupchat.chat.model

import java.util.*

/**
 * Created by akash on 23/10/17.
 */
data class UserModel(var uid: String? = null,
                     var name: String? = null,
                     var image_url: String? = null,
                     var email: String? = null,
                     var group: HashMap<String, Boolean> = hashMapOf(),
                     var deviceIds: HashMap<String, String> = hashMapOf(),
                     var online: Boolean? = null,
                     var unread_group_count: Int? = null,
                     var last_seen_online: String? = null,
                     var last_seen_message_timestamp: String? = null,
                     var admin: Boolean? = null,
                     var delete_till: String? = null,
                     var active: Boolean? = null)


data class GroupModel(var name: String? = null,
                      var image_url: String? = null,
                      var groupId: String? = null,
                      var group_deleted: Boolean? = null,
                      var group: Boolean? = null,
                      var lastMessage: MessageModel? = MessageModel(),
                      var members: HashMap<String, UserModel> = hashMapOf())


data class FileModel(var type: String? = "",
                     var url_file: String? = "",
                     var name_file: String? = "",
                     var size_file: String? = "")

data class LocationModel(var latitude: String? = "",
                         var longitude: String? = "")

data class MessageModel(var message: String? = "",
                        var sender_id: String? = "",
                        var timestamp: String? = "",
                        var message_id: String? = "",
                        var read_status: HashMap<String, Boolean> = hashMapOf(),
                        var file: FileModel? = null,
                        var location: LocationModel? = null)