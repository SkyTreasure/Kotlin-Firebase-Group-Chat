package io.skytreasure.kotlingroupchat.chat.ui.ViewHolders

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import io.skytreasure.kotlingroupchat.R

/**
 * Created by akash on 25/10/17.
 */
class UserRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var ivProfile = itemView.findViewById(R.id.iv_profile) as AppCompatImageView
    var tvName = itemView.findViewById(R.id.tv_name) as TextView
    var tvEmail = itemView.findViewById(R.id.tv_email) as TextView
    var layout = itemView.findViewById(R.id.rl_parent) as RelativeLayout
    var ivSelected = itemView.findViewById(R.id.iv_selected) as AppCompatImageView
    var viewOnlineStatus = itemView.findViewById(R.id.view_online_status) as TextView
    var tvUnreadCount = itemView.findViewById(R.id.tv_unreadcount) as TextView
    var ivOverflow = itemView.findViewById(R.id.iv_overflow) as AppCompatImageView
    var llOverflowItems = itemView.findViewById(R.id.ll_otheritems) as LinearLayout
    var tvMakeAdmin = itemView.findViewById(R.id.tv_make_admin) as TextView
    var tvRemoveMember = itemView.findViewById(R.id.tv_remove_member) as TextView
    var labelAdmin = itemView.findViewById(R.id.label_admin) as TextView
}