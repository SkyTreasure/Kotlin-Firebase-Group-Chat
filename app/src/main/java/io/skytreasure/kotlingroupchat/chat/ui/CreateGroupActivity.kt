package io.skytreasure.kotlingroupchat.chat.ui

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_create_group.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserListAdapter
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserSelectionAdapter
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.mapList
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.selectedUserList
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userList
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {


    var progressDialog: ProgressDialog? = null
    var adapter: UserListAdapter? = null
    var secondaryAdapter: UserSelectionAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        MyChatManager.setmContext(this@CreateGroupActivity)
        progressDialog = ProgressDialog(this)
        progressDialog!!.show()
        MyChatManager.getAllUsersFromFirebase(object : NotifyMeInterface {
            override fun handleData(`asv`: Any, requestCode: Int?) {
                userList = `asv` as ArrayList<UserModel>
                rv_user_list.layoutManager = LinearLayoutManager(this@CreateGroupActivity) as RecyclerView.LayoutManager?
                adapter = UserListAdapter(this@CreateGroupActivity, userSelectionInterface)
                rv_user_list.adapter = adapter

                rv_selected_user.layoutManager = LinearLayoutManager(this@CreateGroupActivity, LinearLayoutManager.HORIZONTAL, false);
                secondaryAdapter = UserSelectionAdapter(this@CreateGroupActivity, userRemovedFromSelection)
                rv_selected_user.adapter = secondaryAdapter

                progressDialog!!.hide()
            }

        }, NetworkConstants.GET_ALL_USERS_REQUEST)


    }


    private var userSelectionInterface = object : NotifyMeInterface {
        override fun handleData(`object`: Any, requestCode: Int?) {
            when (requestCode) {
                NetworkConstants.USER_REMOVED -> {
                    selectedUserList?.remove(`object` as UserModel)
                    secondaryAdapter?.notifyDataSetChanged()
                }
                NetworkConstants.USER_ADDED -> {
                    selectedUserList?.add(`object` as UserModel)
                    secondaryAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private var userRemovedFromSelection = object : NotifyMeInterface {
        override fun handleData(`object`: Any, requestCode: Int?) {
            when (requestCode) {
                NetworkConstants.USER_REMOVED -> {
                    adapter?.resetView(`object` as String)
                }
            }
        }

    }


    override fun onClick(v: View?) {

    }
}
