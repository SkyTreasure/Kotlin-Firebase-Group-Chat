package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.R.id.chat_messages_recycler
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMessageMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userMap
import io.skytreasure.kotlingroupchat.common.util.MyTextUtil
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import io.skytreasure.kotlingroupchat.databinding.ItemChatRowBinding

/**
 * Created by akash on 24/10/17.
 */
class ChatMessagesRecyclerAdapter(var groupId: String, var context: Context, var ref: Query) :
        FirebaseRecyclerAdapter<MessageModel, ChatMessagesRecyclerAdapter.ViewHolder>(
                MessageModel::class.java, R.layout.item_chat_row,
                ChatMessagesRecyclerAdapter.ViewHolder::class.java, ref) {

    var firstMessage: MessageModel = MessageModel()
    var totalCount: Int = 0;


    override fun populateViewHolder(holder: ViewHolder?, model: MessageModel?, position: Int) {


        val viewHolder = holder as ViewHolder
        val chatMessage = model!!
        totalCount = position
        if (position == 0) {
            firstMessage = chatMessage
        }

        if (chatMessage.sender_id.toString() == currentUser.uid) {
            viewHolder.llParent.gravity = Gravity.END
            viewHolder.llChild.background =
                    ContextCompat.getDrawable(context, R.drawable.chat_bubble_grey_sender)
            viewHolder.name.text = "You"
        } else {
            viewHolder.llParent.gravity = Gravity.START
            viewHolder.name.text = userMap?.get(chatMessage.sender_id!!)?.name
            viewHolder.llChild.background = ContextCompat.getDrawable(viewHolder.llParent.context, R.drawable.chat_bubble_grey)
        }
        viewHolder.message.text = chatMessage.message
        try {
            viewHolder.timestamp.text = MyTextUtil().getTimestamp(chatMessage.timestamp?.toLong()!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        viewHolder.rlName.layoutParams.width = viewHolder.message.layoutParams.width

    }

    var currentUser: UserModel = SharedPrefManager.getInstance(context).savedUserModel!!


    /* override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ChatMessagesRecyclerAdapter.ViewHolder =
              ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent?.context),
                      R.layout.item_chat_row, parent, false)
              )*/

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_chat_row, parent, false))


    override fun getItem(position: Int): MessageModel {
        return super.getItem(position)
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    /*  override fun getItemCount(): Int {
          *//**//*  if (MyChatManager.getChatMessages(mKey) == null) return 0
          else return MyChatManager.getChatMessages(mKey)!!.size*//**//*

        return messageList.size
    } */

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var llParent = itemView.findViewById(R.id.ll_parent) as LinearLayout
        var llChild = itemView.findViewById(R.id.ll_child) as LinearLayout
        var name = itemView.findViewById(R.id.name) as TextView
        var timestamp = itemView.findViewById(R.id.timestamp) as TextView
        var rlName = itemView.findViewById(R.id.rl_name) as RelativeLayout
        var message = itemView.findViewById(R.id.message) as TextView
    }
}