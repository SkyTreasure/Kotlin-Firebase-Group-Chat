package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 24/10/17.
 */
class ParticipantsAdapter : RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ParticipantsAdapter.ViewHolder =
            ParticipantsAdapter.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: ParticipantsAdapter.ViewHolder, position: Int) {
        val user = DataConstants.selectedUserList?.get(position)

        holder.tvName.text = user?.name
        holder.tvEmail.text = user?.email

        loadRoundImage(holder.ivProfile, user?.image_url!!)

    }

    override fun getItemCount(): Int = DataConstants.selectedUserList?.size!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProfile = itemView.findViewById(R.id.iv_profile) as AppCompatImageView
        var tvName = itemView.findViewById(R.id.tv_name) as TextView
        var tvEmail = itemView.findViewById(R.id.tv_email) as TextView
        var layout = itemView.findViewById(R.id.rl_parent) as RelativeLayout
        var ivSelected = itemView.findViewById(R.id.iv_selected) as AppCompatImageView
    }
}