package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.ui.ChatMessagesActivity
import io.skytreasure.kotlingroupchat.chat.ui.ViewHolders.UserRowViewHolder
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 31/10/17.
 */
class OneOnOneListingAdapter(var context: Context) : RecyclerView.Adapter<UserRowViewHolder>() {
    var holderMap: MutableMap<String, UserRowViewHolder> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserRowViewHolder =
            UserRowViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: UserRowViewHolder, position: Int) {
        val user = DataConstants.userList?.get(position)

        holder.tvName.text = user?.name
        holder.tvEmail.text = user?.email

        loadRoundImage(holder.ivProfile, user?.image_url!!)


        if (user.online != null && user.online!!) {
            holder.viewOnlineStatus.visibility = View.VISIBLE
        } else {
            // holder.viewOnlineStatus.setBackgroundColor(R.color.greyish)
            holder.viewOnlineStatus.visibility = View.GONE
        }

        holder.layout.setOnClickListener {
            val intent = Intent(context, ChatMessagesActivity::class.java)
            intent.putExtra(AppConstants.USER_ID, user.uid)
            intent.putExtra(AppConstants.CHAT_TYPE, AppConstants.ONE_ON_ONE_CHAT)
            intent.putExtra(AppConstants.POSITION, position)
            context.startActivity(intent)
        }
    }

    fun resetView(uid: String) {
        holderMap.get(uid)?.ivSelected?.visibility = View.GONE
    }

    override fun getItemCount(): Int = DataConstants.userList?.size!!
}