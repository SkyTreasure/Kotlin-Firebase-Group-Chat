package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.ui.ViewHolders.UserRowViewHolder
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 24/10/17.
 */
class ParticipantsAdapter(var callback: NotifyMeInterface) : RecyclerView.Adapter<UserRowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserRowViewHolder =
            UserRowViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: UserRowViewHolder, position: Int) {
        val user = DataConstants.selectedUserList?.get(position)

        holder.tvName.text = user?.name
        holder.tvEmail.text = user?.email

        loadRoundImage(holder.ivProfile, user?.image_url!!)
        holder.ivOverflow.visibility = View.VISIBLE

        if (user.admin != null && user.admin!!) {
            holder.labelAdmin.visibility = View.VISIBLE
        } else {
            holder.labelAdmin.visibility = View.GONE
            user.admin = false
        }

        holder.ivOverflow.setOnClickListener({
            holder.llOverflowItems.visibility = View.VISIBLE
        })

        holder.tvMakeAdmin.setOnClickListener({
            holder.llOverflowItems.visibility = View.GONE
            if (holder.tvMakeAdmin.text.equals("Make Admin")) {
                user.admin = true
                holder.tvMakeAdmin.text = "Remove Admin"
                holder.labelAdmin.visibility = View.VISIBLE
            } else {
                user.admin = false
                holder.labelAdmin.visibility = View.GONE
            }
        })

        holder.tvRemoveMember.setOnClickListener({
            holder.llOverflowItems.visibility = View.GONE
            DataConstants.selectedUserList?.remove(user)
            notifyDataSetChanged()
            callback.handleData(true, 1);
        })

    }

    override fun getItemCount(): Int = DataConstants.selectedUserList?.size!!


}