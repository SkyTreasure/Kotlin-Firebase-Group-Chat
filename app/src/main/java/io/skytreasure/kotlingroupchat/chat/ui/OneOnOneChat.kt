package io.skytreasure.kotlingroupchat.chat.ui

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.OneOnOneListingAdapter
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserListAdapter
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserSelectionAdapter
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import kotlinx.android.synthetic.main.activity_create_group.*

class OneOnOneChat : AppCompatActivity() {

    var adapter: OneOnOneListingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_on_one_chat)




        MyChatManager.setmContext(this@OneOnOneChat)

        if (DataConstants.userList?.size == 0) {
            MyChatManager.getAllUsersFromFirebase(object : NotifyMeInterface {
                override fun handleData(`asv`: Any, requestCode: Int?) {
                    DataConstants.userList = `asv` as ArrayList<UserModel>
                    DataConstants.selectedUserList?.clear();
                    rv_user_list.layoutManager = LinearLayoutManager(this@OneOnOneChat) as RecyclerView.LayoutManager?
                    adapter = OneOnOneListingAdapter(this@OneOnOneChat)
                    rv_user_list.adapter = adapter
                }

            }, NetworkConstants.GET_ALL_USERS_REQUEST)
        } else {
            DataConstants.selectedUserList?.clear();
            rv_user_list.layoutManager = LinearLayoutManager(this@OneOnOneChat) as RecyclerView.LayoutManager?
            adapter = OneOnOneListingAdapter(this@OneOnOneChat)
            rv_user_list.adapter = adapter
        }

    }
}
