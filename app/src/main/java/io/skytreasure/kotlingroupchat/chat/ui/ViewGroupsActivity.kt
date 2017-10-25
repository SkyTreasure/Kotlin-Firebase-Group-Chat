package io.skytreasure.kotlingroupchat.chat.ui

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_view_groups.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ViewGroupsAdapter
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface

class ViewGroupsActivity : AppCompatActivity() {

    var adapter: ViewGroupsAdapter? = null
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_groups)
        MyChatManager.setmContext(this@ViewGroupsActivity)

        progressDialog?.setTitle("Loading")
        progressDialog?.show()

        MyChatManager.fetchCurrentUser(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                val isValid: Boolean = `object` as Boolean
                if (isValid) {
                    fetchMyGroups()
                } else {
                    Toast.makeText(this@ViewGroupsActivity, "", Toast.LENGTH_SHORT).show()
                    progressDialog?.hide()
                }

            }

        }, sCurrentUser?.uid, NetworkConstants.FETCH_CURRENT_USER)


    }

    private fun fetchMyGroups() {
        MyChatManager.fetchMyGroups(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                rv_main.layoutManager = LinearLayoutManager(this@ViewGroupsActivity) as RecyclerView.LayoutManager?
                adapter = ViewGroupsAdapter(this@ViewGroupsActivity)
                rv_main.adapter = adapter
                progressDialog?.hide()
            }

        }, NetworkConstants.FETCH_GROUPS, sCurrentUser)
    }
}
