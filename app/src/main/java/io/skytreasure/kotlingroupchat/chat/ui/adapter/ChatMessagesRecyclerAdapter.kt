package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMessageMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userMap
import io.skytreasure.kotlingroupchat.common.util.MyTextUtil
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import io.skytreasure.kotlingroupchat.databinding.ItemChatRowBinding

/**
 * Created by akash on 24/10/17.
 */
class ChatMessagesRecyclerAdapter(var groupId: String, context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var currentUser: UserModel = SharedPrefManager.getInstance(context).savedUserModel!!

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ViewHolder
        val chatMessage = groupMessageMap?.get(groupId)?.get(position) as MessageModel

        if (chatMessage.sender_id.toString() == currentUser.uid) {
            viewHolder.binding.llParent.gravity = Gravity.END
            viewHolder.binding.llChild.background =
                    ContextCompat.getDrawable(viewHolder.binding.root.context, R.drawable.chat_bubble_grey_sender)
            viewHolder.binding.name.text = "You"
        } else {
            viewHolder.binding.llParent.gravity = Gravity.START
            viewHolder.binding.name.text = userMap?.get(chatMessage.sender_id!!)?.name
            viewHolder.binding.llChild.background = ContextCompat.getDrawable(viewHolder.binding.llParent.context, R.drawable.chat_bubble_grey)
        }
        viewHolder.binding.message.text = chatMessage.message
        viewHolder.binding.timestamp.text = MyTextUtil().getTimestamp(chatMessage.timestamp?.toLong()!!)

        viewHolder.binding.rlName.layoutParams.width = viewHolder.binding.message.layoutParams.width

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent?.context),
                    R.layout.item_chat_row, parent, false)
            )

    override fun getItemCount(): Int {
        /*  if (MyChatManager.getChatMessages(mKey) == null) return 0
          else return MyChatManager.getChatMessages(mKey)!!.size*/
        return groupMessageMap?.get(groupId)?.size!!
    }

    class ViewHolder(var binding: ItemChatRowBinding) : RecyclerView.ViewHolder(binding.root)
}