package io.skytreasure.kotlingroupchat.common.constants

import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import java.security.acl.Group

/**
 * Created by akash on 23/10/17.
 */
class DataConstants {

    companion object {
        var userList: ArrayList<UserModel>? = ArrayList()
        var selectedUserList: ArrayList<UserModel>? = ArrayList()
        var mapList: MutableMap<String, UserModel> = mutableMapOf()
        var myGroups: ArrayList<GroupModel>? = ArrayList()
        var sCurrentUser: UserModel? = UserModel()

        /**
         * Chat
         */
        var groupMessageMap: MutableMap<String, ArrayList<MessageModel>>? = mutableMapOf()
        var groupMap: MutableMap<String, GroupModel>? = mutableMapOf()
        var userMap: MutableMap<String, UserModel>? = mutableMapOf()
        var groupMembersMap: MutableMap<String, ArrayList<UserModel>>? = mutableMapOf()
    }
}