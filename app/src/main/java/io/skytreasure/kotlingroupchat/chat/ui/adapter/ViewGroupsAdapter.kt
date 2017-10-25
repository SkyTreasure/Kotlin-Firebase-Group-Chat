package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.ui.ChatMessagesActivity
import io.skytreasure.kotlingroupchat.chat.ui.ViewHolders.UserRowViewHolder
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userMap
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 24/10/17.
 */
class ViewGroupsAdapter(val context: Context) : RecyclerView.Adapter<UserRowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserRowViewHolder =
            UserRowViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: UserRowViewHolder, position: Int) {
        val group = DataConstants.myGroups?.get(position)



        holder.tvName.text = group?.name

        if (!group?.lastMessage?.message?.isEmpty()!!) {
            holder.tvEmail.text = userMap?.get(group?.lastMessage?.sender_id!!)?.name?.substring(0, 6) + ": " + group?.lastMessage?.message
        } else {
            holder.tvEmail.text = "No messages in the group"
        }



        loadRoundImage(holder.ivProfile, group?.image_url!!)

        holder.layout.setOnClickListener({
            val intent = Intent(context, ChatMessagesActivity::class.java)
            intent.putExtra(AppConstants.GROUP_ID, group.groupId)
            intent.putExtra(AppConstants.POSITION,position)
            context.startActivity(intent)
        })

    }

    override fun getItemCount(): Int = DataConstants.myGroups?.size!!


}