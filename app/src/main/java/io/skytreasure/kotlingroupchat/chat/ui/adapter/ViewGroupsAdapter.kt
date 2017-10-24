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
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 24/10/17.
 */
class ViewGroupsAdapter : RecyclerView.Adapter<ViewGroupsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewGroupsAdapter.ViewHolder =
            ViewGroupsAdapter.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: ViewGroupsAdapter.ViewHolder, position: Int) {
        val group = DataConstants.myGroups?.get(position)

        holder.tvName.text = group?.name
        holder.tvEmail.text = group?.image_url

        loadRoundImage(holder.ivProfile, group?.image_url!!)

    }

    override fun getItemCount(): Int = DataConstants.myGroups?.size!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProfile = itemView.findViewById(R.id.iv_profile) as AppCompatImageView
        var tvName = itemView.findViewById(R.id.tv_name) as TextView
        var tvEmail = itemView.findViewById(R.id.tv_email) as TextView
        var layout = itemView.findViewById(R.id.rl_parent) as RelativeLayout
        var ivSelected = itemView.findViewById(R.id.iv_selected) as AppCompatImageView
    }

}