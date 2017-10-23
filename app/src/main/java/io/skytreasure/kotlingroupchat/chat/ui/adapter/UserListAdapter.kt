package io.skytreasure.kotlingroupchat.chat.ui.adapter

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.mapList
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userList
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage

/**
 * Created by akash on 23/10/17.
 */
class UserListAdapter(context: Context,
                      var callback: NotifyMeInterface)
    : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    var holderMap: MutableMap<String, ViewHolder> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList?.get(position)

        holder.tvName.text = user?.name
        holder.tvEmail.text = user?.email

        loadRoundImage(holder.ivProfile, user?.imageUrl!!)

        holder.layout.setOnClickListener {

            if (mapList.containsKey(userList?.get(position)?.uid)) {
                //Already Selected remove from the list
                mapList.remove(userList?.get(position)?.uid!!)
                holder.ivSelected.visibility = View.INVISIBLE
                holderMap.remove(userList?.get(position)?.uid!!)
                callback.handleData(userList?.get(position)!!, NetworkConstants.USER_REMOVED)
            } else {
                //User haven't selected the member so add him to list
                mapList.put(userList?.get(position)?.uid!!, userList?.get(position)!!)
                holder.ivSelected.visibility = View.VISIBLE
                holderMap.put(userList?.get(position)?.uid!!, holder)
                callback.handleData(userList?.get(position)!!, NetworkConstants.USER_ADDED)
            }
        }
    }

    fun resetView(uid: String) {
        holderMap.get(uid)?.ivSelected?.visibility = View.GONE
    }

    override fun getItemCount(): Int = userList?.size!!


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProfile = itemView.findViewById(R.id.iv_profile) as AppCompatImageView
        var tvName = itemView.findViewById(R.id.tv_name) as TextView
        var tvEmail = itemView.findViewById(R.id.tv_email) as TextView
        var layout = itemView.findViewById(R.id.rl_parent) as RelativeLayout
        var ivSelected = itemView.findViewById(R.id.iv_selected) as AppCompatImageView
    }
}